#!/usr/bin/env bash
# Déploie une mise à jour : arrêt de la pile, récupération du code, reconstruction.
set -euo pipefail

echo "1/3 Arrêt de la pile..."
docker compose --profile fullstack down

echo "2/3 Récupération de la dernière version..."
git pull

echo "3/3 Reconstruction et redémarrage..."
./prod-start.sh
