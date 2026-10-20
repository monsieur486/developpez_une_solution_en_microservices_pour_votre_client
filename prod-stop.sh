#!/usr/bin/env bash
# Arrête la pile complète en conservant les données.
set -euo pipefail

docker compose --profile fullstack down
echo "Pile arrêtée. Les volumes de données sont conservés."
