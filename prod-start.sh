#!/usr/bin/env bash
# Construit et démarre le socle : base de données, registre et passerelle.
set -euo pipefail

docker compose --profile fullstack up -d --build
echo "Socle démarré : registre sur http://localhost:8761, passerelle sur http://localhost:9000"
