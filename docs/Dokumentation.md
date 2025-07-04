# BeeRduino Projektdokumentation

<div align="center">
  <img src="https://github.com/user-attachments/assets/f67fd0d1-dd92-4d45-b6be-9ea535b3ac1a" alt="BeeRduino" width="300"/>
</div>

## Autoren
- Daniel Hametner
- Christopher Nobis

## 1. Problembeschreibung

Die traditionelle Imkerei steht vor großen Herausforderungen bei der Überwachung der Bienenvölker. Wichtige Umweltparameter wie Temperatur, Luftfeuchtigkeit und Gewicht der Bienenstöcke werden häufig manuell, unregelmäßig oder gar nicht erfasst. Dies erschwert die rechtzeitige Erkennung von Problemen wie Krankheiten, Schädlingen oder Umwelteinflüssen, die die Gesundheit der Bienen negativ beeinflussen können. Die Folge sind nicht nur wirtschaftliche Verluste für Imkerinnen und Imker, sondern auch eine Bedrohung des Ökosystems.

Daher besteht ein Bedarf an einer automatisierten und verlässlichen Lösung zur kontinuierlichen Überwachung von Bienenstöcken in Echtzeit.

---

## 2. Lösungsansatz

### 2.1 Systemüberblick

BeeRduino ist ein intelligentes Monitoring-System für Bienenstöcke, das auf moderner Sensorik und einer Cloud-nativen Microservice-Architektur basiert. Unser Ziel war es, eine ganzheitliche Lösung zu schaffen, die von der Datenerfassung direkt am Bienenstock bis zur intuitiven Visualisierung der Messwerte reicht.

Das System besteht aus drei Kernkomponenten:

![BeeRduino](https://github.com/user-attachments/assets/8db04b61-08db-4bc5-b58f-310a386d31c3)

- **Arduino-Firmware**: Erfasst regelmäßig Sensordaten (Temperatur, Luftfeuchtigkeit, Gewicht) über Sensoren wie den DHT22 und eine Wägesensorplattform (HX711). Die Daten werden drahtlos an das Backend gesendet.

- **Backend Microservices**: Implementiert mit Quarkus und Spring Boot, bestehen aus mehreren Microservices, die jeweils unterschiedliche Aufgaben übernehmen:
  - **Core-Service** verwaltet Stammdaten der Bienenstöcke und koordiniert die Registrierung neuer Völker.
  - **Data-Collector-Service** empfängt Messdaten vom Arduino via Gateway und verteilt sie asynchron per RabbitMQ.
  - **Data-Analysis-Service** speichert und verarbeitet die Messwerte, stellt historische Daten und Live-Updates via GraphQL bereit.
  - **Gateway-Service** fungiert als API-Gateway mit Service-Discovery (Consul) und Lastverteilung.

- **Frontend Webanwendung**: Realisiert mit React und Apollo Client, visualisiert Messdaten in Echtzeit und historisch über interaktive Zeitreihendiagramme (Chart.js). Sie bietet eine intuitive Oberfläche für Imker, um jederzeit den Zustand ihrer Bienenvölker im Blick zu behalten.

Die Sensordaten werden direkt auf dem Arduino ausgelesen und als JSON-Nachrichten an unser Backend gesendet. Dort fängt ein Service die Daten auf, verteilt sie über RabbitMQ an verschiedene Services und speichert sie schließlich in MariaDB-Datenbanken. Mithilfe der GraphQL-Schnittstelle können sowohl historische Daten als auch Echtzeit-Updates per Subscriptions vom Frontend abgefragt werden.

---

## 3. Implementierung

### 3.1 Hardware und Sensorik

Für die Datenerfassung setzen wir auf bewährte Komponenten, die zuverlässig und stromsparend sind. Zentraler Mikrocontroller ist ein **Arduino MKR WAN 1310**, basierend auf dem SAMD21 Cortex-M0+ 32-bit ARM-Prozessor. Dieser verfügt über ein integriertes LoRa-Funkmodul, um in späteren Ausbaustufen auch lange Funkreichweiten zu ermöglichen.

Für die Umweltmessung nutzen wir zwei **DHT22** Sensor-Boards (jeweils innen und außen am Bienenstock), die Temperatur und Luftfeuchtigkeit erfassen. Die Sensoren sind als kleine Breakout-Boards montiert und bieten einen digitalen Datenausgang, der direkt mit dem Arduino verbunden ist.

Der Gewichtssensor besteht aus vier einzelnen Load Cells (Dehnungsmessstreifen), die mechanisch an den Ecken einer stabilen Plattform montiert sind. Elektrisch sind sie zu einer Wheatstone-Brücke verschaltet. Diese wird an einen **HX711**-Messverstärker angeschlossen, der das analoge Signal verstärkt, digitalisiert und an den Arduino überträgt.

![Waage mit angebrachtem Sensor](./assembly-photos/IMG_4644.png)

Auf diesem Bild sieht man die Waageplatte mit den vier an den Ecken befestigten Load Cells. Die dünnen Drähte führen zum HX711-Messverstärker.

![Gewichtssensor (Load Cell)](./assembly-photos/IMG_4645.png)

Hier ist das HX711-Modul zu sehen, ein präziser 24-Bit-Messverstärker für digitale Wägetechnik. An den linken Anschlussleisten (E+, E−, A+, A−) werden die Load Cells in Brückenschaltung angeschlossen. Auf der rechten Seite befinden sich die vier Pins zur Verbindung mit dem Arduino: VCC (Stromversorgung), GND (Masse), DT (Datenleitung) und SCK (Takt).

![Arduino-Schaltkreis mit angeschlossener Load Cell](./assembly-photos/arduino-circuit.png)

Das Schaltbild zeigt, wie die vier Load Cells zu einer Wheatstone-Brücke verschaltet und an den HX711 angeschlossen sind. Der HX711 verstärkt das resultierende Differenzsignal und wandelt es in digitale Werte um, die der Arduino auslesen kann.

![Lastzellen-Verkabelung mit HX711](./assembly-photos/HX711_4x50kg_load_cell_diagram.png)

Diese Grafik zeigt die Verdrahtung von vier Load Cells, die gemeinsam eine Wheatstone-Brücke bilden. Die kombinierten Signale werden an den HX711-Messverstärker weitergeleitet. Der HX711 überträgt die Messdaten über zwei digitale Leitungen (DT für Daten, SCK für Takt) an den Arduino. Zusätzlich erfolgt die Spannungsversorgung über VCC und GND.

### 3.2 Lötarbeiten und Montage

Eine wichtige Phase der Implementierung war das Löten der feinen Drähte an die Lastsensoren und den HX711-Verstärker. Aufgrund der geringen Größe der Bauteile und der Drahtstärke erfordert dies sorgfältiges Arbeiten und gutes Equipment.

![Lötarbeiten am Gewichtssensor](./assembly-photos/IMG_4648.jpg)

Auf diesem Foto ist der Lötkolben im Einsatz, während die Verbindungen zwischen den Drähten und der Sensorplatine verlötet werden.

---

### 3.3 Arduino Firmware

Die Firmware auf dem Arduino MKR WAN 1310 ist in C++ geschrieben und modular aufgebaut. Sie übernimmt das zyklische Auslesen der Sensoren und die optionale Datenübertragung per LoRaWAN. Die Ausführung erfolgt über die klassische `setup()`/`loop()`-Struktur von Arduino.

#### Aufbau und Sensorintegration

- **DHT22 (innen/außen)**\
  Zwei digitale Sensoren zur Messung von Temperatur und Luftfeuchtigkeit sind an den Pins `A0` (innen) und `A1` (außen) angeschlossen. Die Werte werden zusätzlich als Heat Index berechnet.

- **HX711 mit Load Cells**\
  Vier Dehnungsmessstreifen sind über einen HX711-Messverstärker mit dem Arduino verbunden. Der HX711 verwendet die Pins `A5` (DATA) und `A6` (CLOCK).\
  Die Werte werden als Gewicht in Gramm oder Kilogramm ausgegeben.

#### Datenverarbeitung

Alle 500 ms wird geprüft, ob neue Sensorwerte erfasst werden sollen. Dabei werden folgende Schritte durchgeführt:

1. Temperatur und Luftfeuchtigkeit innen/außen messen
2. Heat Index für beide Positionen berechnen
3. Gewicht über HX711 auslesen
4. Serielle Ausgabe der Messwerte
5. (optional) Senden der Daten über LoRa

Bei fehlgeschlagenen Ablesungen (`NaN`) wird der Messzyklus abgebrochen.

#### Serielle Konsole

Über die serielle Schnittstelle (9600 Baud) sind zwei Kommandos verfügbar:

- `t` – startet Tare-Vorgang (Nullstellung) des HX711
- `r` – startet geführte Kalibrierung mit bekanntem Gewicht (inkl. optionalem Speichern im Flash)

Die Firmware gibt kontinuierlich strukturierte Daten im Textformat aus, z. B.:

```
INSIDE	-- Temperature: 35.1°C  Humidity: 65.2%  Heat index: 38.3°C
OUTSIDE	-- Temperature: 27.4°C  Humidity: 52.0%  Heat index: 28.7°C
WEIGHT	-- Weight: 32.5 kg
```

#### Kalibrierung und Flash-Speicher

Der Kalibrierungsfaktor für den HX711 wird im EEPROM des Arduino gespeichert. Beim Start wird dieser Wert geladen und zur Berechnung verwendet. Die Kalibrierung erfolgt interaktiv über den seriellen Monitor. Die Speicherung erfolgt nur auf Bestätigung.

#### LoRaWAN (optional)

Die Firmware unterstützt LoRaWAN, wenn das Makro `LORAWAN` aktiviert ist. In diesem Fall wird alle 5 Sekunden ein Datenpaket mit folgenden Werten gesendet:

- Zeitstempel (`uint64_t`)
- Temperatur innen (`float`)
- Luftfeuchte innen (`float`)
- Temperatur außen (`float`)
- Luftfeuchte außen (`float`)
- Gewicht (`float`)

Das Format ist binär (28 Byte) und wird über den internen LoRa-Modem des MKR WAN 1310 gesendet. Die Verbindung erfolgt über OTAA mit App EUI und App Key aus der Datei `arduino_secrets.h`.

---

### 3.4 Backend Microservices

Das Backend besteht aus mehreren Microservices, die in Java mit Quarkus und Spring Boot implementiert sind:

- **Core-Service**: Verwaltung der Stammdaten der Bienenstöcke, Registrierung neuer Völker und zentrale Schnittstelle für Client-Anfragen.

- **Data-Collector-Service**: Nimmt die Sensordaten entgegen, empfängt sie via REST API vom Gateway und verteilt sie über RabbitMQ asynchron an weitere Services.

- **Data-Analysis-Service**: Speichert die Zeitreihen-Daten persistent in einer MariaDB-Datenbank, bietet Abfrage- und Echtzeit-Subscription-APIs via GraphQL.

- **Gateway-Service**: Verbindet Data-Producers mit Data-Collector-Services, sorgt für Load Balancing und Health Checks, registriert sich und verwaltet dynamisch Service-Endpoints über Consul.

Ein Besonderheit ist die eingebaute Fehlertoleranz: Sollte die Netzwerkverbindung temporär ausfallen, speichert der Data-Producer die Messwerte lokal in einer kleinen H2-Datenbank. Sobald die Verbindung wieder steht, werden die Daten automatisch nachgesendet. Praktisch ist zudem, dass das Backend Dummy-Daten erzeugen kann, was beim Testen enorm geholfen hat. Consul sorgt dafür, dass die Lastverteilung im System dynamisch funktioniert, was die Skalierbarkeit verbessert.

---

### 3.5 Frontend Webanwendung

Das Frontend ist eine moderne React-basierte Webanwendung, die:

- Die Sensordaten in Echtzeit per GraphQL Subscription empfängt.
- Historische Messwerte per GraphQL Query abruft.
- Zeitreihendiagramme mit Chart.js visualisiert, farblich differenziert nach Sensortyp.
- Aktuelle Messwerte in übersichtlichen Kacheln mit Zeitstempel anzeigt.
- Die Bedienung intuitiv gestaltet, sodass Imker schnell den Zustand ihrer Bienenstöcke überblicken können.

---

## 4. Ergebnisse

- Ein funktionierender Prototyp für automatisierte Erfassung und Überwachung von Bienenstöcken wurde realisiert.
- Die intuitive Web-Oberfläche erlaubt das Monitoring in Echtzeit und die Analyse von Trends.
- Das System ist modular aufgebaut und kann leicht um weitere Sensoren oder Funktionen erweitert werden.
- Die Datenverarbeitung ist robust, da Messwerte bei Verbindungsproblemen zwischengespeichert und später automatisch nachgesendet werden.
- Als nächste Schritte planen wir den Live-Datenversand über LoRa und eine bessere Unterstützung für frisch besiedelte Bienenstöcke. Für diesen Zweck haben wir bewusst den Arduino MKR WAN 1310 beschafft, da er die nötige LoRa-Funktionalität bietet.

Am 27. Juni 2025 wurde die **technische Präsentation** erfolgreich durchgeführt. Dabei konnte der vollständige Funktionsumfang des Systems demonstriert werden.

Für die Live-Demo wurde als Substitution für einen echten Bienenstock eine Bierkiste verwendet. Diese Wahl beruhte auf zwei Gründen: Zum einen entspricht das Gewicht einer Bierkiste in etwa dem eines Bienenstocks, sodass die Lastsensoren realistische Messwerte liefern konnten. Zum anderen minimierte die Bierkiste das Risiko von Schäden, die bei einem lebenden Bienenvolk auftreten könnten. Zur Kalibrierung der Wägesensoren diente ein schweres Buch als Referenzgewicht.

![Bierkiste vor der Präsentation](./assembly-photos/IMG_5522.HEIC)

---

## 5. Fazit und Ausblick

BeeRduino zeigt, wie moderne IoT-Technologien und Microservice-Architekturen in der Imkerei einen echten Mehrwert schaffen können. Unser Prototyp bietet Imkern eine zuverlässige Lösung zur Überwachung ihrer Bienenvölker, die frühzeitige Maßnahmen bei kritischen Zuständen ermöglicht.

In Zukunft möchten wir LoRa zur Datenübertragung nutzen, die Batterielaufzeit optimieren und eine automatisierte Anomalieerkennung implementieren. Auch eine mobile App ist angedacht, um die Bedienbarkeit weiter zu verbessern.

---

*Diese Dokumentation wurde im Rahmen des Lehrveranstaltung **MUS2UE: Mobile und ubiquitäre Systeme (SE.ma VZ SS25)** im Studiengang  **FH OÖ Campus Hagenberg** erstellt.*
