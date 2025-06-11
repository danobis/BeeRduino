# Dokumentation

## Problembeschreibung
Bienen sind essenziell für ein funktionierendes Ökosystem. Durch Umweltveränderungen und den Klimawandel sind Bienenvölker zunehmend gefährdet, insbesondere in ländlichen Regionen, in denen digitale Überwachungsmöglichkeiten fehlen.

## Lösungsansatz
BeeRduino ist ein Sensorsystem zur Erfassung von Umweltdaten wie Temperatur, Luftfeuchtigkeit, Gewicht und Standort eines Bienenstocks. Die Sensoren werden über einen Mikrocontroller (ESP32) angesteuert, die erfassten Daten per MQTT übertragen und in einem Java-Backend (Quarkus) gespeichert. Ein Web-Frontend ermöglicht die Visualisierung dieser Daten.

## Ergebnisse
- Aufbau eines funktionierenden Sensorsystems mit ESP32
- Stabile MQTT-Datenübertragung
- Backend-Implementierung in Java mit Quarkus
- Frontend zur Visualisierung der gesammelten Daten
- Beitrag zu einer digitalen, nachhaltigen Imkerei im Smart-Village-Kontext
