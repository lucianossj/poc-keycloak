#!/bin/bash

# Start Keycloak and PostgreSQL services
echo "Starting Keycloak and PostgreSQL..."

docker-compose up -d

echo ""
echo "Services are starting..."
echo "Keycloak will be available at: http://localhost:8080"
echo "Check docker-compose.yml for admin credentials."
echo ""
echo "PostgreSQL is available at: localhost:5432"
echo "Check docker-compose.yml for database credentials."
echo ""
echo "Use 'docker-compose logs -f' to view logs"
