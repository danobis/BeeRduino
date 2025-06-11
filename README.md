# BeeRduino

<div align="center">
  <img src="https://github.com/user-attachments/assets/f67fd0d1-dd92-4d45-b6be-9ea535b3ac1a" alt="BeeRduino" width="300"/>
</div>

This repository contains the full implementation and documentation for the BeeRduino project — a smart beehive monitoring system focused on sustainability and Smart Village applications.

## Folder Structure

### 
Contains the firmware for the ESP32-based sensor node. This code is responsible for collecting data from the sensors and publishing it via MQTT.

### 
Java application built with Quarkus. It receives MQTT messages, processes and stores sensor data, and provides a REST API.

### 
The web interface for visualizing environmental data from the beehive, such as temperature, humidity, and weight over time.

### 
Documentation related to the project, including system diagrams, architecture, written reports, and images. Contains , the main written report.

### 
Contains the final presentation slides in PDF and/or PowerPoint format.

## Notes
- All components are designed with a focus on low-power operation and remote deployment.
- The project is aligned with Smart Village and sustainability goals.


