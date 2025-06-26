
#define DEBUG 1 // enable DEBUG 1, disable DEBUG 0

#include <Arduino.h>
#include <Adafruit_Sensor.h>
#include <Adafruit_HX711.h>
#include <DHT.h>

#if DEBUG
#define debug_print(x)   Serial.print(x)
#define debug_println(x) Serial.println(x)
#else // else DEBUG
#define debug_print(x)
#define debug_println(x)
#endif // DEBUG

DHT dht22_inside(A0, DHT22);
DHT dht22_outside(A1, DHT22);

float dht22_read_temperature(DHT & dht_sensor);
float dht22_read_humidity(DHT & dht_sensor);
float dht22_compute_heat_index(DHT & dht_sensor, float t, float h);
void dht22_print_data(const String & prefix, float t, float h, float hi);

constexpr uint8_t LOADCELL_DATA_PIN = A5;
constexpr uint8_t LOADCELL_CLOCK_PIN = A6;
Adafruit_HX711 load_cell(LOADCELL_DATA_PIN, LOADCELL_CLOCK_PIN);

void hx711_calibrate();

void setup() {
#if DEBUG
  Serial.begin(9600UL);
  while (!Serial) {
    delay(10UL);
  }
#endif // DEBUG
  dht22_inside.begin();
  dht22_outside.begin();
  debug_println("Starting HX711 sensor calibration...");
  load_cell.begin();
  delay(100UL);
  // TODO: maybe load calibration factor from EEPROM
  hx711_calibrate();
}

/// Reading temperature or humidity takes about 250 milliseconds.
void loop() {
  // wait a few seconds between measurements
  delay(2000UL);
  float inside_t = dht22_read_temperature(dht22_inside);
  float inside_h = dht22_read_humidity(dht22_inside);
  float inside_heat_index = dht22_compute_heat_index(dht22_inside, inside_t, inside_h);
  // check if any reads failed and exit early
  if (isnan(inside_t) or isnan(inside_h) or isnan(inside_heat_index)) {
    debug_println(F("Failed to read from inside DHT sensor!"));
    return;
  }
  float outside_t = dht22_outside.readTemperature();
  float outside_h = dht22_outside.readHumidity();
  float outside_heat_index = dht22_compute_heat_index(dht22_outside, outside_t, outside_h);
  // check if any reads failed and exit early
  if (isnan(inside_t) or isnan(inside_h) or isnan(inside_heat_index)) {
    debug_println(F("Failed to read from outside DHT sensor!"));
    return;
  }
  // print the collected data from the inside DHT sensor
#if DEBUG
  const String inside_data_prefix = "INSIDE\t-- ";
  dht22_print_data(inside_data_prefix, inside_t, inside_h, inside_heat_index);
  // print the collected data from the outside DHT sensor
  const String outside_data_prefix = "OUTSIDE\t-- ";
  dht22_print_data(outside_data_prefix, outside_t, outside_h, outside_heat_index);
#endif // DEBUG
}

float dht22_read_temperature(DHT & dht_sensor)
{
  const float t = dht_sensor.readTemperature();
  // check if read failed
  if (isnan(t)) {
    debug_println(F("Failed to read temperature from DHT sensor!"));
  }
  return t;
}

float dht22_read_humidity(DHT & dht_sensor)
{
  const float h = dht_sensor.readHumidity();
  // check if read failed
  if (isnan(h)) {
    debug_println(F("Failed to read humidity from DHT sensor!"));
  }
  return h;
}

float dht22_compute_heat_index(DHT & dht_sensor, float t, float h)
{
  // check if temperature or humidity is NAN
  float hi = NAN;
  if (isnan(t) or isnan(h)) {
    debug_println(F("Failed to compute heat index from DHT sensor!"));
  }
  else {
    hi = dht_sensor.computeHeatIndex(t, h);
  }
  return hi;
}

void dht22_print_data(const String & prefix, float t, float h, float hi)
{
#if DEBUG
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
#endif // DEBUG
}

void hx711_calibrate()
{
  
}

