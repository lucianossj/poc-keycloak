#!/bin/bash

# Reset all data - stops services and removes volumes
echo "WARNING: This will delete all data!"
read -p "Are you sure? (y/N) " -n 1 -r
echo

if [[ $REPLY =~ ^[Yy]$ ]]
then
    docker-compose down -v
    echo "All data has been removed."
else
    echo "Operation cancelled."
fi
