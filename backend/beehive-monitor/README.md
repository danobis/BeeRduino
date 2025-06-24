# Microservices: Bienenstock-Sensorik und Stammdatenverwaltung

Dieses Repository enthält eine verteilte Anwendung zur Erfassung, Verarbeitung und Bereitstellung von Messwerten (z. B. von Sensoren in Bienenstöcken) inklusive einer Stammdatenverwaltung für Bienenstöcke. Die Architektur basiert auf mehreren Microservices, die mittels Quarkus und Spring Boot implementiert wurden.

## Überblick der Services

![beehive-monitor](./docs/images/beehive-monitor-light.png)

## Core-Service
- **Typ**: Quarkus GraphQL API
- **Persistenz**: `mariadb1`, Datenbank für Stammdaten
- **Registrierung**: `consul`

Der Core-Service stellt eine GraphQL-API zur Verwaltung der Stammdaten von Bienenstöcken bereit. Es speichert Identifikationsinformationen und Standortdaten in einer MariaDB-Datenbank. Bei der Registrierung eines neuen Bienenstocks initiiert der Core-Service über RPC (mittels `rpc-common`) einen Aufruf an den Data-Analysis-Service. Dabei wird überprüft, ob bereits Messdaten vorhanden sind. Außerdem wird ein Registereintrag angelegt, um spätere Messdaten korrekt zuordnen zu können. Zusätzlich stellt der Core-Service GraphQL-Endpunkte bereit, über die Clients die Stammdaten abfragen und pflegen können. Durch die Registrierung bei Consul wird sichergestellt, dass der Core-Service von anderen Komponenten gefunden wird und bei Bedarf skaliert werden kann.

## Data-Analysis-Service
- **Typ**: Quarkus GraphQL API
- **Persistenz**: `mariadb2`, Datenbank für 
- **Registrierung**: `consul`

Dieser Microservice bietet eine GraphQL-Schnittstelle, die für die Kommunikation mit anderen Systemen genutzt werden kann. Über diese können historische Messwerte abgefragt und Echtzeit-Updates über Subscriptions bereitgestellt werden. Der Service abonniert eingehende Messwerte über RabbitMQ und persistiert sie in einer eigenen MariaDB-Datenbank. Zusätzlich implementiert er einen RPC-Endpunkt, den der Core-Service beim Anlegen eines Bienenstocks aufruft, um vorhandene Messdaten zu bestätigen oder neu anzulegen. Über die GraphQL-API können Clients sowohl vergangene Daten analysieren als auch Live-Datenströme empfangen. Durch die Registrierung in Consul ist der Data-Analysis-Service für RPC-Aufrufe und Discovery erreichbar.

## Data-Collector-Service
- **Typ**: Quarkus REST API
- **Persistenz**: es erfolgt keine direkte Persistenz, sondern eine Übergabe an RabbitMQ
- **Registrierung**: `consul`

Der Data-Collector-Service ist eine RESTful-API, die Messwerte vom Data-Producer über das Gateway entgegennimmt. Er führt keine eigene dauerhafte Persistenz durch, sondern publiziert alle eingehenden Messdaten über RabbitMQ im Publish-Subscribe-Modell. Dadurch können mehrere Consumer (z. B. der Data-Analysis-Service) die Daten simultan verarbeiten. Um Lastspitzen abzufangen, können mehrere Instanzen dieses Services parallel hinter dem API-Gateway betrieben werden. Jeder Collector registriert sich in Consul und das Gateway nutzt diese Informationen für Load Balancing und Health Checks.

## Data-Producer-Service
- **Typ**: Quarkus Application
- **Persistenz**: `measurements`, H2 Datenbank als Fallback für fehlgeschlagene Sendeversuche

Dieser Dienst generiert in regelmäßigen Abständen Messwerte, wie beispielsweise simulierte Sensordaten für Bienenstöcke. Diese werden über den Gateway-Service an die Data-Collector-Instanzen gesendet. Resilience4j stellt sicher, dass bei fehlgeschlagenen Requests automatische Retries durchgeführt werden. Gelingt das Senden auch nach mehreren Versuchen nicht, speichert der Producer die Messwerte lokal in einer eingebetteten H2-Datenbank als Fallback. Ein Scheduler liest alle 15 Minuten die fehlgeschlagenen Einträge aus der H2-Datenbank aus und sendet sie gebündelt erneut an den Collector. Nach erfolgreichem Versand werden die betreffenden Datensätze aus der H2-Datenbank gelöscht. Dadurch gehen keine Messwerte verloren, selbst wenn das Netzwerk oder Collector-Komponenten zeitweise nicht verfügbar sind.

## Gateway-Service
- **Typ**: Spring Boot Cloud / API Gateway
- **Registrierung**: `consul`

Ein API Gateway fungiert als Fassade, das eingehende Anfragen vom Data-Producer entgegennimmt und auf mehrere Data-Collector-Instanzen verteilt. Es verwendet Consul zur dynamischen Service-Discovery der Collector-Instanzen und führt Health Checks durch, um nur gesunde Endpunkte zu nutzen. Zusätzlich bietet das Gateway Logging und Monitoring, beispielsweise mittels Spring Actuator, um Metriken und den Status einzusehen. Es können Lastverteilungsstrategien (z. B. `round-robin`) konfiguriert werden, um die eingehenden Messdaten effizient zu verteilen. Der Gateway-Service selbst registriert sich ebenfalls in Consul, sodass er Teil des Gesamtnetzwerks ist und von Services gefunden werden kann, die Clients direkt bedienen.

# Infrastruktur-Komponenten

## RabbitMQ

RabbitMQ dient als zentrales Message-Broker-System und ermöglicht die asynchrone Kommunikation zwischen den Microservices. Es setzt auf das Publish-Subscribe-Muster, mit dem Messwert-Events verteilt werden können, sodass verschiedene Konsumenten parallel arbeiten können. Darüber hinaus unterstützt RabbitMQ RPC-Aufrufe, die es dem Core-Service und dem Data-Analysis-Service erlauben, auf einfache Weise synchron miteinander zu kommunizieren. Die konkrete Anbindung erfolgt über eine gemeinsame Bibliothek namens `rabbitmq-common`, die generische Implementierungen für das Publish/Subscribe-Verfahren und das RPC-Muster bereitstellt und so Wiederverwendbarkeit und Konsistenz sicherstellt.

## MariaDB

1. **Messwerte-DB (`master_schema`)**: `mariadb1`, Genutzt vom Data-Analysis-Service
2. **Stammdaten-DB (`data_schema`)**: `mariadb2`, Genutzt vom Core-Service

Für persistente Daten werden zwei getrennte MariaDB-Datenbanken verwendet, um Verantwortlichkeiten klar zu trennen und potenziell unterschiedliche Betriebsanforderungen abzubilden. Die erste Datenbank hält historische Messwerte und wird vom Data-Analysis-Service genutzt. Sie speichert Zeitreihen großer Datenmengen, die über RabbitMQ ankommen, und ermöglicht Analysen sowie GraphQL-Abfragen historischer und aktueller Messdaten. Die zweite Datenbank beherbergt Stammdaten der Bienenstöcke und wird vom Core-Service verwaltet. In beiden Fällen werden JDBC-URLs und Zugangsdaten über Konfigurationsmechanismen (z. B. Umgebungsvariablen oder ConfigMaps/Secrets in Kubernetes) hinterlegt, sodass die Services zur Laufzeit flexibel auf unterschiedliche Umgebungen abgestimmt werden können.

## H2

Im Data-Producer-Service findet H2 ausschließlich als lokale Fallback-Persistenz Verwendung. Solange die Verbindung zum Data-Collector-Service (über das Gateway) verfügbar ist, werden Messwerte direkt versendet. Falls jedoch alle Retry-Versuche fehlschlagen, speichert der Producer die Daten in der eingebetteten H2-Datenbank zwischen. Ein Scheduler im Producer prüft in festen Intervallen, liest diese Zwischenspeicher-Einträge wieder aus und versucht erneut, die Messwerte in Batches an die Collector-Instanzen zu senden. Sobald die Übertragung erfolgreich ist, werden die entsprechenden Datensätze aus H2 entfernt, sodass kein dauerhafter Ballast entsteht.

## Consul

Consul übernimmt die Service Registry und Service Discovery im gesamten System. Jeder Microservice – Core-Service, Data-Collector-Service (jede einzelne Instanz), Data-Analysis-Service und Gateway-Service – meldet sich bei Consul an und bietet Health-Checks, sodass nur gesunde Instanzen Anfragen erhalten. Auf diese Weise kann das Gateway-Service dynamisch die verfügbaren Data-Collector-Instanzen erkennen und Anfragen gezielt verteilen. Ebenso finden RPC-Clients wie der Core-Service automatisch den Data-Analysis-Service, ohne harte Endpunkte konfigurieren zu müssen. Consul-Integration sorgt für Flexibilität beim Skalieren, da neue Instanzen sich selbst registrieren und ausfallende automatisch aus dem Pool entfernt werden.

## Registrierte Services:  
 - `core-service`  
 - `data-collector-service` (jede Instanz)  
 - `data-analysis-service`  
 - `gateway-service`

## Gemeinsame Bibliotheken / Module

## **rabbitmq-common**  
- Enthält Konfiguration und generische Clients/Server-Wrapper für Publish-Subscribe und RPC-Pattern.  
- Wird in `data-collector-service`, `data-analysis-service` und `core-service` für RPC bzw für Publish/Subscribe genutzt.  
- Enthält generische Serializer/Deserializer, Connection-Factory mit Konfiguration.

## **rpc-common**  
- Stellt Basisklassen für RPC Kommunikation bereit.

## **service-common**  
- Allgemeine Utility-Klassen/Methoden, die in allen Services wiederverwendet werden
- Stellt Basisklassen für Quarkus Services bereit.
