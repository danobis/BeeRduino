# BeeRduino

<div align="center">
  <img src="https://github.com/user-attachments/assets/f67fd0d1-dd92-4d45-b6be-9ea535b3ac1a" alt="BeeRduino" width="300"/>
</div>

This repository contains the full implementation and documentation for the BeeRduino project — a smart beehive monitoring system focused on sustainability and Smart Village applications.

## Authors
- Daniel Hametner
- Christopher Nobis

## Folder Structure

### `arduino/`
Contains the firmware for the Arduino-based sensor node. This code is responsible for collecting environmental data (e.g. temperature, humidity, weight) and transmitting it via MQTT.

### `backend/`
Java application built with Quarkus. It receives MQTT messages, processes and stores sensor data, and provides an API for accessing the data.

### `frontend/`
The web interface for visualizing sensor data from the beehive, including time-series graphs and current measurements.

### `docs/`
Contains documentation for the entire project, including system architecture, data flow, written reports, and diagrams. The main written report is in `Dokumentation.md`.

### `presentation/`
Contains the final project presentation (PDF and PowerPoint).

## Notes
- The system is designed for low-cost, outdoor deployment.
- It contributes to sustainable agriculture and biodiversity monitoring in rural areas.
- This project was developed as part of the course **MUS2UE: Mobile und ubiquitäre Systeme (SE.ma VZ SS25)** at **FH OÖ Campus Hagenberg**.


