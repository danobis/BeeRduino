#!/bin/bash
docker compose -p beehive-monitor -f docker-compose.dev.yml up
read -p "Press Enter to continue..."
