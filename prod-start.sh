docker compose --profile fullstack up -d --build
docker compose --profile fullstack up -d --scale ms-patients=2 --scale ms-notes=2 --scale ms-risque=3
