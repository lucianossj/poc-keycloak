#!/bin/bash

# Stop Keycloak and PostgreSQL services
echo "Stopping Keycloak and PostgreSQL..."

docker-compose down

echo "Services stopped."
