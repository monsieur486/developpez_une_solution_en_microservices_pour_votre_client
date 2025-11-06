docker compose --profile fullstack up -d --build
docker compose --profile fullstack up -d --scale ms-patients=3 --scale ms-notes=3 --scale ms-risque=3
