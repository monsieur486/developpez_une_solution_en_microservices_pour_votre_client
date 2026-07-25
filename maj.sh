#!/usr/bin/env bash
# Déploiement d'une mise à jour : arrêt de la pile, récupération du code,
# reconstruction et redémarrage.
set -e

docker compose --profile fullstack down
git pull
docker compose --profile fullstack up -d --build
