# BeeRduino Projektdokumentation

## 1. Problembeschreibung

Die traditionelle Imkerei steht vor großen Herausforderungen bei der Überwachung der Bienenvölker. Wichtige Umweltparameter wie Temperatur, Luftfeuchtigkeit und Gewicht der Bienenstöcke werden häufig manuell, unregelmäßig oder gar nicht erfasst. Dies erschwert die rechtzeitige Erkennung von Problemen wie Krankheiten, Schädlingen oder Umwelteinflüssen, die die Gesundheit und Produktivität der Bienen negativ beeinflussen können. Die Folge sind nicht nur wirtschaftliche Verluste für Imkerinnen und Imker, sondern auch eine Bedrohung der ökologischen Vielfalt und des Ökosystems.

Daher besteht ein großer Bedarf an einer automatisierten, verlässlichen und skalierbaren Lösung zur kontinuierlichen Überwachung von Bienenstöcken in Echtzeit.

---

## 2. Lösungsansatz

### 2.1 Systemüberblick

BeeRduino ist ein intelligentes Monitoring-System für Bienenstöcke, das auf moderner Sensorik und einer Cloud-nativen Microservice-Architektur basiert. Unser Ziel war es, eine ganzheitliche Lösung zu schaffen, die von der Datenerfassung direkt am Bienenstock bis zur intuitiven Visualisierung der Messwerte reicht.

Das System besteht aus drei Kernkomponenten:

- **Arduino-Firmware**: Erfasst regelmäßig Sensordaten (Temperatur, Luftfeuchtigkeit, Gewicht) über Sensoren wie den DHT22 und eine Wägesensorplattform (HX711). Die Daten werden über das MQTT-Protokoll drahtlos an das Backend gesendet.

- **Backend Microservices**: Implementiert mit Quarkus und Spring Boot, bestehen aus mehreren Microservices, die jeweils unterschiedliche Aufgaben übernehmen:
  - **Core-Service** verwaltet Stammdaten der Bienenstöcke und koordiniert die Registrierung neuer Völker.
  - **Data-Collector-Service** empfängt Messdaten vom Arduino via Gateway und verteilt sie asynchron per RabbitMQ.
  - **Data-Analysis-Service** speichert und verarbeitet die Messwerte, stellt historische Daten und Live-Updates via GraphQL bereit.
  - **Gateway-Service** fungiert als API-Gateway mit Service-Discovery (Consul) und Lastverteilung.

- **Frontend Webanwendung**: Realisiert mit React und Apollo Client, visualisiert Messdaten in Echtzeit und historisch über interaktive Zeitreihendiagramme (Chart.js). Sie bietet eine intuitive Oberfläche für Imker, um jederzeit den Zustand ihrer Bienenvölker im Blick zu behalten.

---

### 2.2 Technische Highlights

- **Datenfluss**: Sensordaten werden auf dem Arduino ausgelesen, als JSON via MQTT publiziert, vom Backend aufgenommen, in RabbitMQ verteilt und in MariaDB-Datenbanken gespeichert.

- **Fehlertoleranz**: Der Data-Producer speichert Messwerte lokal in einer eingebetteten H2-Datenbank, falls die Netzwerkverbindung unterbrochen ist, und sendet diese später automatisch nach.

- **GraphQL Schnittstellen**: Ermöglichen flexible Abfragen historischer Daten und Subscriptions für Echtzeit-Updates.

- **Service Discovery & Skalierbarkeit**: Konsul steuert die dynamische Erkennung und Lastverteilung der Microservice-Instanzen.

---

## 3. Ergebnisse

- Ein **funktionierender Prototyp**, der die automatisierte Erfassung und Überwachung von Bienenstöcken ermöglicht.

- **Echtzeit-Visualisierung** von Temperatur, Luftfeuchtigkeit und Gewicht im Frontend mit historischen Trendanalysen.

- **Modulare Architektur**, die einfach um weitere Sensoren oder Funktionen erweitert werden kann.

- **Robuste Datenverarbeitung** durch asynchrone Kommunikation mit RabbitMQ und automatische Wiederholung fehlgeschlagener Datenübertragungen.

- Als nächster Schritt ist die Integration von **LoRa-Funktechnik** geplant, um die Reichweite der Sensordatenübertragung zu erhöhen, sowie eine verbesserte Unterstützung für frisch besiedelte Bienenstöcke.

---

## 4. Implementierung

### 4.1 Arduino Firmware

- Nutzung von DHT22-Sensoren für Temperatur und Luftfeuchtigkeit innen und außen am Bienenstock.
- Wägesensor (HX711) zur Gewichtsmessung mit Kalibrierungslogik.
- Debugging über serielle Schnittstelle.
- Daten werden alle 2 Sekunden ausgelesen und als JSON via MQTT gesendet.

### 4.2 Backend Microservices

- Quarkus-basierte Microservices mit GraphQL API.
- RabbitMQ als zentrales Message-Broker-System.
- MariaDB-Datenbanken für Stammdaten und Zeitreihendaten.
- Fallback-Mechanismus im Producer-Service mit H2-Datenbank zur Speicherung nicht übermittelter Messwerte.

### 4.3 Frontend

- React Webanwendung mit Apollo Client für GraphQL-Integration.
- Chart.js für Zeitreihendiagramme mit Live-Updates via GraphQL-Subscriptions.
- Dynamische Anzeige aktueller Messwerte mit Farbkodierung für Grenzwerte.

---

## 5. Fazit und Ausblick

BeeRduino zeigt, wie moderne IoT- und Cloud-Technologien genutzt werden können, um eine bisher manuell betriebene Domäne wie die Imkerei effizienter und zuverlässiger zu gestalten. 

Unser Prototyp ermöglicht es Imkern, schnell und unkompliziert den Zustand ihrer Bienenvölker zu überwachen und frühzeitig auf kritische Zustände zu reagieren. 

In Zukunft wollen wir die Lösung durch LoRa-Funktechnik erweitern, die Batterielaufzeit verbessern und Algorithmen zur automatisierten Anomalieerkennung integrieren. Zudem planen wir eine mobile App für noch mehr Benutzerfreundlichkeit.

---

## 6. Marketingfotos

(Beigefügte Fotos der Hardware-Installation, des Frontend-Dashboards und des Betriebs in der Praxis)

---

*Diese Dokumentation wurde im Rahmen des Projekts BeeRduino im Studiengang [Studiengangsname] erstellt.*

