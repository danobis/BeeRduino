
#if 0
#define LORAWAN
#endif

#include <cassert>

#include <Arduino.h>
#include <Adafruit_Sensor.h>
#include <FlashStorage_SAMD.h>
#include <DHT.h>
#include <HX711_ADC.h>

#if defined ( LORAWAN )
#include <MKRWAN.h>
#include "arduino_secrets.h"
#endif // defined ( LORAWAN )

constexpr int EEPROM_START_ADDRESS = 0x0000;

void flash_put_calibration_factor(float value);
float flash_get_calibration_factor();

DHT dht22_inside(A0, DHT22);
DHT dht22_outside(A1, DHT22);

float dht22_read_temperature(DHT & dht_sensor);
float dht22_read_humidity(DHT & dht_sensor);
float dht22_compute_heat_index(DHT & dht_sensor, float t, float h);
void dht22_print_data(const String & prefix, float t, float h, float hi);

constexpr static uint8_t LOADCELL_DATA_PIN = A5;
constexpr static uint8_t LOADCELL_CLOCK_PIN = A6;
static float calibration_factor = 0.0f;
HX711_ADC hx711_adc(LOADCELL_DATA_PIN, LOADCELL_CLOCK_PIN);

void hx711_calibrate();
float hx711_read_weight();
void hx711_print_data(const String & prefix, float w);

#if defined ( LORAWAN )

struct LoRaMessage {
  uint64_t timestamp;
  float inside_temperature;
  float inside_humidity;
  float outside_temperature;
  float outside_humidity;
  float weight;
};

LoRaModem lora_modem;
static String MKRWAN_APP_EUI = SECRET_APP_EUI; // replace with active app EUI
static String MKRWAN_APP_KEY = SECRET_APP_KEY; // replace with active app Key
static uint64_t last_lora_write = 0ul;
constexpr static uint64_t LORA_WRITE_INTERVAL = 5000ul;

void lora_setup();
void lora_write_message(const LoRaMessage & message);

#endif // defined ( LORAWAN )

static uint64_t last_sensor_read = 0ul;
constexpr static uint64_t SENSOR_READ_INTERVAL = 500ul;

void setup()
{
  Serial.begin(9600ul);
  while (!Serial) {
    delay(10ul);
  }
  dht22_inside.begin();
  dht22_outside.begin();
  Serial.println("Starting HX711 sensor calibration...");
  hx711_adc.begin();
  delay(100ul);
  hx711_calibrate();
#if defined ( LORAWAN )
  lora_setup();
#endif // defined ( LORAWAN )
}

/// Reading temperature or humidity takes about 250 milliseconds.
void loop()
{
  uint64_t current_time = millis();
  // wait a few seconds between measurements
  if (current_time - last_sensor_read >= SENSOR_READ_INTERVAL) {
    last_sensor_read = current_time;
    float inside_t = dht22_read_temperature(dht22_inside);
    float inside_h = dht22_read_humidity(dht22_inside);
    float inside_heat_index = dht22_compute_heat_index(dht22_inside, inside_t, inside_h);
    // check if any reads failed and exit early
    if (isnan(inside_t) or isnan(inside_h) or isnan(inside_heat_index)) {
      Serial.println(F("Failed to read from inside DHT sensor!"));
      return;
    }
    float outside_t = dht22_outside.readTemperature();
    float outside_h = dht22_outside.readHumidity();
    float outside_heat_index = dht22_compute_heat_index(dht22_outside, outside_t, outside_h);
    // check if any reads failed and exit early
    if (isnan(inside_t) or isnan(inside_h) or isnan(inside_heat_index)) {
      Serial.println(F("Failed to read from outside DHT sensor!"));
      return;
    }
    float w = hx711_read_weight();
    if (isnan(w)) {
      Serial.println(F("Failed to read from HX711 sensor!"));
      return;
    }
    // print the collected data from the inside DHT sensor
    const String inside_data_prefix = "INSIDE\t-- ";
    dht22_print_data(inside_data_prefix, inside_t, inside_h, inside_heat_index);
    // print the collected data from the outside DHT sensor
    const String outside_data_prefix = "OUTSIDE\t-- ";
    dht22_print_data(outside_data_prefix, outside_t, outside_h, outside_heat_index);
    // print the collectd data form the HX711 sensor
    const String weight_data_prefix = "WEIGHT\t-- ";
    hx711_print_data(weight_data_prefix, w);
#if defined ( LORAWAN )
    if (current_time - last_lora_write >= LORA_WRITE_INTERVAL) {
      last_lora_write = current_time;
      LoRaMessage message{}; // create lora message
      message.timestamp = current_time;
      message.inside_temperature = inside_t;
      message.inside_humidity = inside_h;
      message.outside_temperature = outside_t;
      message.outside_humidity = outside_h;
      message.weight = w;
      lora_write_message(message);
    }
#endif // defined ( LORAWAN )
  }
  // receive command from serial terminal
  if (Serial.available() > 0) {
    const int in_byte = Serial.read();
    if (in_byte == 't') {
      Serial.println("HX711 tareing...");
      hx711_adc.tareNoDelay();
    }
    if (in_byte == 'r') {
      hx711_calibrate();
    }
  }
  if (hx711_adc.getTareStatus()) {
    Serial.println(F("Tareing completed."));
  }
}

void flash_put_calibration_factor(float value)
{
  EEPROM.put(EEPROM_START_ADDRESS, value);
  // commit changes if not, set to auto-commit
  if (not EEPROM.getCommitASAP()) {
    Serial.println(F("Commiting to flash storage..."));
    EEPROM.commit();
  }
  float out_value;
  EEPROM.get(EEPROM_START_ADDRESS, out_value);
  if (abs(out_value - value) < 0.000001) {
    Serial.println(F("Flash storage write, verified successfully."));
  }
  else {
    Serial.println(F("WARNING: Flash storage write, verification failed!"));
  }
}

float flash_get_calibration_factor()
{
  float value;
  EEPROM.get(EEPROM_START_ADDRESS, value);
  // check if valid data exists
  if (isnan(value) or value == 0.0f) {
    Serial.println(F("Failed to load valid 'calibration_factor' from flash storage!"));
  }
  else {
    Serial.print(F("Calibration factor: "));
    Serial.println(value, 6);
  }
  return value;
}


float dht22_read_temperature(DHT & dht_sensor)
{
  const float t = dht_sensor.readTemperature();
  // check if read failed
  if (isnan(t)) {
    Serial.println(F("Failed to read temperature from DHT sensor!"));
  }
  return t;
}

float dht22_read_humidity(DHT & dht_sensor)
{
  const float h = dht_sensor.readHumidity();
  // check if read failed
  if (isnan(h)) {
    Serial.println(F("Failed to read humidity from DHT sensor!"));
  }
  return h;
}

float dht22_compute_heat_index(DHT & dht_sensor, float t, float h)
{
  // check if temperature or humidity is NAN
  float hi = NAN;
  if (isnan(t) or isnan(h)) {
    Serial.println(F("Failed to compute heat index from DHT sensor!"));
  }
  else {
    hi = dht_sensor.computeHeatIndex(t, h);
  }
  return hi;
}

void dht22_print_data(const String & prefix, float t, float h, float hi)
{
  if (not prefix.isEmpty()) {
    Serial.print(prefix);
  }
  Serial.print(F("Temperature: "));
  Serial.print(t);
  Serial.print(F("°C"));
  Serial.print(F("  Humidity: "));
  Serial.print(h);
  Serial.print(F("%"));
  Serial.print(F("  Heat index: "));
  Serial.print(hi);
  Serial.println(F("°C"));
}

void hx711_calibrate()
{
  Serial.println(F("Follow these steps to calibrate the HX711 sensor:"));
  Serial.println(F("1. Remove all weight from the scale."));
  Serial.println(F("2. Send 't' from serial monitor to start tareing."));
  bool resume = false;
  while (not resume) {
    hx711_adc.update();
    if (Serial.available() > 0) {
      const int in_byte = Serial.read();
      if (in_byte == 't') {
        Serial.println("HX711 tareing...");
        hx711_adc.tareNoDelay();
      }
      if (hx711_adc.getTareStatus()) {
        Serial.println(F("Tareing completed."));
        resume = true;
      }
    }
  }
  Serial.println(F("3. Place known weight on scale."));
  Serial.println(F("4. Enter the known weight in grams (e.g. 100) from serial minitor."));
  float known_weight = 0.0f;
  resume = false;
  while (not resume) {
    if (Serial.available() > 0) {
      known_weight = Serial.parseFloat();
      if (known_weight != 0) {
        Serial.print(F("Known weight: "));
        Serial.println(known_weight);
        resume = true;
      }
    }
  }
  hx711_adc.refreshDataSet(); //refresh the dataset to be sure that the known mass is measured correct
  calibration_factor = hx711_adc.getNewCalibration(known_weight);
  Serial.println(F("Finished calibrating HX711 sensor"));
  Serial.print(F("Calibration factor: "));
  Serial.println(calibration_factor, 6);
  Serial.println("Save this value to Flash Storage? y/n");
  resume = false;
  while (not resume) {
    if (Serial.available() > 0) {
      const int in_byte = Serial.read();
      if (in_byte == 'y') {
        flash_put_calibration_factor(calibration_factor);
        resume = true;
      }
      if (in_byte == 'n') {
        Serial.println(F("Value not saved to flash storage."));
        resume = true;
      }
    }
  }
  Serial.println(F("Finished calibrating HX711 sensor."));
  Serial.println("To re-calibrate, send 'r' from serial monitor.");
  delay(1000ul);
}

float hx711_read_weight()
{
  float w = NAN;
  if (hx711_adc.update()) {
    w = hx711_adc.getData();
  }
  return w;
}

void hx711_print_data(const String & prefix, float w)
{
  if (not prefix.isEmpty()) {
    Serial.print(prefix);
  }
  if (w >= 1000.0f) {
    Serial.print(F("Weight: "));
    Serial.print(w / 1000.0f);
    Serial.println(F("kg"));
  }
  else {
    Serial.print(F("Weight: "));
    Serial.print(w);
    Serial.println(F("g"));
  }
}

#if defined ( LORAWAN )

void lora_setup()
{
  Serial.println(F("Initializing LoRaWAN modem..."));
  if (not lora_modem.begin(EU868)) {
    Serial.println(F("Failed to start LoRaWAN modem!"));
    [[noreturn]] while (true) {} // wait infinitely if LoRaModem cannot start
  }
  Serial.println(F("LoRaWAN modem initialized."));
  Serial.print(F("LoRaWAN device EUI:"));
  Serial.println(lora_modem.deviceEUI());
  Serial.print(F("LoRaWAN version:"));
  Serial.println(lora_modem.version());
  delay(1000ul);
  Serial.println(F("Attempting to join LoRaWAN network..."));
  int connected = lora_modem.joinOTAA(MKRWAN_APP_EUI, MKRWAN_APP_KEY);
  if (not connected) {
    Serial.println(F("Failed to join LoRaWAN network!"));
    [[noreturn]] while (true) {} // wait infinitely if LoRaModem cannot connect
  }
  Serial.println(F("Successfully joined LoRaWAN network."));
  lora_modem.dataRate(5u);
}

void write_bytes(uint8_t * payload, const uint8_t * bytes, const size_t length)
{
  memcpy(payload, bytes, length);
}

void lora_write_message(const LoRaMessage & message)
{
  // build payload: timestamp(8 bytes) + 5 * float(5 * 4 bytes) = 28 bytes
  constexpr size_t uint64_size = sizeof(uint64_t);
  constexpr size_t float_size = sizeof(float);
  constexpr size_t payload_size = uint64_size + 5ul * float_size;
  uint8_t payload[payload_size];
  size_t offset = 0ul;
  // pack timestamp, little-endian
  uint8_t timestamp_bytes[uint64_size];
  for (size_t i = 0ul; i < uint64_size; i++) {
    timestamp_bytes[i] = static_cast<uint8_t>(message.timestamp >> (8 * i)) & 0xff;
  }
  write_bytes(&payload[offset], timestamp_bytes, uint64_size);
  offset += uint64_size;
  // pack floats in IEEE-754 LE
  union FloatIEEE_754LE {
    float value;
    uint8_t bytes[float_size];
  } converter{};
  converter.value = message.inside_temperature;
  write_bytes(&payload[offset], converter.bytes, float_size);
  offset += float_size;
  converter.value = message.inside_humidity;
  write_bytes(&payload[offset], converter.bytes, float_size);
  offset += float_size;
  converter.value = message.outside_temperature;
  write_bytes(&payload[offset], converter.bytes, float_size);
  offset += float_size;
  converter.value = message.outside_humidity;
  write_bytes(&payload[offset], converter.bytes, float_size);
  offset += float_size;
  converter.value = message.weight;
  write_bytes(&payload[offset], converter.bytes, float_size);
  offset += float_size;
  assert(payload_size == offset);
  // send message via LoRaWAN
  Serial.println(F("Sending LoRaMessage..."));
  lora_modem.beginPacket();
  lora_modem.write(payload, payload_size);
  int status = lora_modem.endPacket();
  if (status > 0) {
    Serial.println(F("Successfully sent LoRaMessage."));
  }
  else {
    Serial.print(F("Failed to send LoRaMessage, ERROR_CODE: "));
    Serial.println(status);
  }
}

#endif // defined ( LORAWAN )
