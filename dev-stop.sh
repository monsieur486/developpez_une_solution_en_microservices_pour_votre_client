#!/usr/bin/env bash
# Arrête la base de développement en conservant ses données.
# Pour repartir d'une base vierge, utiliser explicitement : docker compose down -v
set -euo pipefail

docker compose down
echo "Base arrêtée. Le volume de données est conservé."
