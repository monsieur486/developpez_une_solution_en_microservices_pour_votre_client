#!/usr/bin/env bash
# Arrête les bases de développement en conservant leurs données.
# Pour repartir de bases vierges, utiliser explicitement : docker compose down -v
set -euo pipefail

docker compose down
echo "Bases arrêtées. Les volumes de données sont conservés."
