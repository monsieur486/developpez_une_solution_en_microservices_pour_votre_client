#!/usr/bin/env bash
# Construit et démarre la pile complète : base, registre, passerelle, microservices et front.
set -euo pipefail

docker compose --profile fullstack up -d --build
# Le port du front peut être changé dans .env : on l'y relit pour afficher la bonne adresse.
port_webclient=$(grep -E '^PORT_WEBCLIENT=' .env 2>/dev/null | cut -d= -f2 || true)

echo "Pile démarrée :"
echo "  interface web : http://localhost:${port_webclient:-8080}"
echo "  passerelle    : http://localhost:9000"
echo "  registre      : http://localhost:8761"
