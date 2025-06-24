#!/bin/bash
docker compose --env-file .env.prod -p beehive-monitor -f docker-compose.prod.yml up
read -p "Press Enter to continue..."
