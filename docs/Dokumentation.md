# BeeRduino Projektdokumentation

<div align="center">
  <img src="https://github.com/user-attachments/assets/f67fd0d1-dd92-4d45-b6be-9ea535b3ac1a" alt="BeeRduino" width="300"/>
</div>

## Autoren
- Daniel Hametner
- Christopher Nobis

## 1. Problembeschreibung

Die traditionelle Imkerei steht vor großen Herausforderungen bei der Überwachung der Bienenvölker. Wichtige Umweltparameter wie Temperatur, Luftfeuchtigkeit und Gewicht der Bienenstöcke werden häufig manuell, unregelmäßig oder gar nicht erfasst. Dies erschwert die rechtzeitige Erkennung von Problemen wie Krankheiten, Schädlingen oder Umwelteinflüssen, die die Gesundheit und Produktivität der Bienen negativ beeinflussen können. Die Folge sind nicht nur wirtschaftliche Verluste für Imkerinnen und Imker, sondern auch eine Bedrohung der ökologischen Vielfalt und des Ökosystems.

Daher besteht ein großer Bedarf an einer automatisierten und verlässlichen Lösung zur kontinuierlichen Überwachung von Bienenstöcken in Echtzeit.

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

---

### 2.2 Technische Highlights

Die Sensordaten werden direkt auf dem Arduino ausgelesen und als JSON-Nachrichten an unser Backend gesendet. Dort fängt ein Service die Daten auf, verteilt sie über RabbitMQ an verschiedene Services und speichert sie schließlich in MariaDB-Datenbanken. Das sorgt für eine saubere Trennung und ermöglicht eine effiziente Verarbeitung selbst bei hohen Datenmengen.

Ein besonderer Pluspunkt ist die eingebaute Fehlertoleranz: Sollte die Netzwerkverbindung temporär ausfallen, speichert der Data-Producer die Messwerte lokal in einer kleinen H2-Datenbank. Sobald die Verbindung wieder steht, werden die Daten automatisch nachgesendet. Mithilfe der GraphQL-Schnittstelle können sowohl historische Daten als auch Echtzeit-Updates per Subscriptions abgefragt werden. Praktisch ist dabei auch, dass das Backend Dummy-Daten erzeugen kann, was beim Testen enorm geholfen. Consul sorgt dafür, dass alle Microservices sich automatisch finden und die Lastverteilung im System dynamisch funktioniert, was die Skalierbarkeit erheblich verbessert.

---

## 3. Implementierung

### 3.1 Hardware und Sensorik

Für die Datenerfassung setzen wir auf bewährte Komponenten, die zuverlässig und stromsparend sind. Zentraler Mikrocontroller ist ein **Arduino MKR WAN 1310**, basierend auf dem SAMD21 Cortex-M0+ 32-bit ARM-Prozessor. Dieser verfügt über integriertes LoRa-Funkmodul, um in späteren Ausbaustufen auch lange Funkreichweiten zu ermöglichen.

Für die Umweltmessung nutzen wir zwei **DHT22** Sensor-Boards (jeweils innen und außen am Bienenstock), die Temperatur und Luftfeuchtigkeit erfassen. Die Sensoren sind als kleine Breakout-Boards montiert und bieten einen digitalen Datenausgang, der einfach mit dem Arduino verbunden werden kann.

Der Gewichtssensor besteht aus vier einzelnen Dehnungsmessstreifen (Load Cells), die an den Ecken einer stabilen Platte montiert sind. Diese sind über den **HX711** Messverstärker mit dem Arduino verbunden und ermöglichen so die Erfassung des Gewichts des Bienenstocks.

![Waage mit angebrachtem Sensor](./assembly-photos/IMG_4644.png)

Auf diesem Bild sieht man die Waageplatte mit den vier an den Ecken befestigten Dehnungsmessstreifen. Die dünnen Drähte führen zu einem kleinen Messverstärker.

![Gewichtssensor (Load Cell)](./assembly-photos/IMG_4645.png)

Hier ist eine der vier Load Cells im Detail zu sehen. Diese Sensoren messen die Belastung durch eine Veränderung ihres elektrischen Widerstands.

Für die Auswertung der Sensorsignale nutzen wir einen HX711-Verstärkerchip, der die sehr kleinen elektrischen Änderungen verstärkt und digitalisiert.

![Arduino-Schaltkreis mit angeschlossener Load Cell](./assembly-photos/arduino-circuit.png)

Das Schaltbild zeigt die Verbindung der vier Load Cells mit dem HX711-Modul, das wiederum an einen Arduino angeschlossen ist. Die Lastsensoren sind parallel verschaltet und liefern ein kombiniertes Signal. Der HX711 wandelt das analoge Signal in digitale Werte um, die der Arduino ausliest.

![Lastzellen-Verkabelung mit HX711](./assembly-photos/HX711_4x50kg_load_cell_diagram.png)

Diese Grafik visualisiert die genaue Verdrahtung der Load Cells: Jeder Sensor hat vier Anschlüsse (E+, E-, A+, A-), die zusammen mit dem HX711 verbunden sind. Die Signale werden so kombiniert und an den Arduino weitergeleitet.

### 3.2 Lötarbeiten und Montage

Eine wichtige Phase der Implementierung war das Löten der feinen Drähte an die Lastsensoren und den HX711-Verstärker. Aufgrund der geringen Größe der Bauteile und der Drahtstärke erfordert dies sorgfältiges Arbeiten und gutes Equipment.

![Lötarbeiten am Gewichtssensor](./assembly-photos/IMG_4648.jpg)

Auf diesem Foto ist der Lötkolben im Einsatz, während die Verbindungen zwischen den Drähten und der Sensorplatine verlötet werden. Ein stabiler und dauerhafter Kontakt ist hier essenziell, um Messfehler oder Ausfälle zu verhindern.

---

### 3.3 Arduino Firmware

Die Firmware auf dem Arduino ist verantwortlich für:

- Das zyklische Auslesen der Temperatur- und Luftfeuchtigkeitssensoren (DHT22), jeweils innen und außen am Bienenstock.
- Das Auslesen der Gewichtswerte vom HX711-Signalwandler.
- Die Fehlerüberprüfung bei Sensorablesungen, um ungültige Messwerte auszuschließen.
- Die Ausgabe der Messwerte auf die serielle Schnittstelle zur lokalen Überwachung im Debugmodus.
- Die Übertragung der gesammelten Daten an das Backend.

Die Software ist modular aufgebaut und nutzt Debug-Makros, um im Entwicklungsmodus umfangreiche Informationen auszugeben. Das Hauptprogramm liest alle zwei Sekunden alle Sensorwerte und führt die Berechnung eines Wärmeindex durch, der das thermische Empfinden besser beschreibt als reine Temperaturwerte.

---

### 3.4 Backend Microservices

Das Backend besteht aus mehreren Microservices, die in Java mit Quarkus und Spring Boot implementiert sind:

- **Core-Service**: Verwaltung der Stammdaten der Bienenstöcke, Registrierung neuer Völker und zentrale Schnittstelle für Client-Anfragen.

- **Data-Collector-Service**: Nimmt die Sensordaten entgegen, empfängt sie via REST API vom Gateway und verteilt sie über RabbitMQ asynchron an weitere Services.

- **Data-Analysis-Service**: Speichert die Zeitreihen-Daten persistent in einer MariaDB-Datenbank, bietet Abfrage- und Echtzeit-Subscription-APIs via GraphQL.

- **Gateway-Service**: Verbindet Data-Producers mit Data-Collector-Services, sorgt für Load Balancing und Health Checks, registriert sich und verwaltet dynamisch Service-Endpoints über Consul.

Diese Microservice-Architektur gewährleistet Skalierbarkeit, hohe Verfügbarkeit und Ausfallsicherheit.

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
- Als nächste Schritte planen wir den Live-Datenversand über LoRa und eine bessere Unterstützung für frisch besiedelte Bienenstöcke.

Am 27. Juni 2025 wurde die **technische Präsentation** erfolgreich durchgeführt. Dabei konnte der vollständige Funktionsumfang des Systems demonstriert werden.

Für die Live-Demo wurde als Substitution für einen echten Bienenstock eine Bierkiste verwendet. Diese Wahl beruhte auf zwei Gründen: Zum einen entspricht das Gewicht einer Bierkiste in etwa dem eines Bienenstocks, sodass die Lastsensoren realistische Messwerte liefern konnten. Zum anderen minimierte die Bierkiste das Risiko von Schäden, die bei einem lebenden Bienenvolk auftreten könnten. Zur Kalibrierung der Wägesensoren diente ein schweres Buch als Referenzgewicht.

![Bierkiste vor der Präsentation](./assembly-photos/IMG_5522.HEIC)

---

## 5. Fazit und Ausblick

BeeRduino zeigt, wie moderne IoT-Technologien und Microservice-Architekturen in der Imkerei einen echten Mehrwert schaffen können. Unser Prototyp bietet Imkern eine zuverlässige Lösung zur Überwachung ihrer Bienenvölker, die frühzeitige Maßnahmen bei kritischen Zuständen ermöglicht.

In Zukunft möchten wir LoRa zur Datenübertragung nutzen, die Batterielaufzeit optimieren und eine automatisierte Anomalieerkennung implementieren. Auch eine mobile App ist angedacht, um die Bedienbarkeit weiter zu verbessern.

---

*Diese Dokumentation wurde im Rahmen des Lehrveranstaltung **MUS2UE: Mobile und ubiquitäre Systeme (SE.ma VZ SS25)** im Studiengang  **FH OÖ Campus Hagenberg** erstellt.*

---

*Hinweis: Die Fotos wurden mit Einverständnis des Teams für die Veröffentlichung auf der Studiengangs-Homepage bereitgestellt.*
