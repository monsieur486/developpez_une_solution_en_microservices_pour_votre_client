# P09 — Dépistage du risque de diabète (microservices)

Application de suivi de patients et de dépistage du risque de diabète de
type 2, découpée en microservices Spring Boot : les praticiens consultent les
patients, ajoutent des notes médicales et obtiennent une évaluation du risque
calculée à partir des termes déclencheurs présents dans les notes.

Stack : Java 17, Spring Boot / Spring Cloud (Eureka, Gateway, OpenFeign),
PostgreSQL, MongoDB, Thymeleaf, Docker Compose.

## Architecture

| Module | Rôle | Port |
|---|---|---|
| `ms-eureka` | Annuaire des services (découverte) | 8761 |
| `ms-gateway` | Passerelle : point d'entrée unique des API | 9000 |
| `ms-patients` | Données démographiques des patients (PostgreSQL) | 9100 |
| `ms-notes` | Notes médicales des patients (MongoDB) | 9200 |
| `ms-risque` | Calcul du niveau de risque de diabète | 9300 |
| `ms-webclient` | Interface web Thymeleaf | 8080 |

## Jeu de démonstration

Au premier démarrage, les bases sont amorcées avec le jeu de démonstration du
projet : quatre patients (seed Liquibase de ms-patients) et leurs neuf notes
médicales (seed Mongock de ms-notes). Les exemples de la Javadoc et de la
collection Bruno s'appuient sur ce jeu :

| Id | Patient | Évaluation attendue |
|---|---|---|
| 1 | TestNone | None |
| 2 | TestBorderline | Borderline |
| 3 | TestInDanger | In Danger |
| 4 | TestEarlyOnset | Early onset |

## Prérequis

- Java 17 (le wrapper Maven `./mvnw` est fourni dans chaque module)
- Docker et Docker Compose
- Un fichier `.env` à la racine, créé depuis le modèle versionné :

```bash
cp dist.env .env
```

## Démarrage

### En développement

Les dépendances (bases de données) tournent en conteneur, les applications
Java se lancent en local :

```bash
./dev-start.sh                             # démarre PostgreSQL, MongoDB et mongo-express
cd ms-eureka && ./mvnw spring-boot:run     # puis chaque microservice, dans l'ordre :
# ms-gateway, ms-patients, ms-notes, ms-risque, ms-webclient
./dev-stop.sh                              # arrête les conteneurs de dépendances
```

### En production (pile complète conteneurisée)

```bash
./prod-start.sh   # construit et démarre toute la pile (profil fullstack)
./prod-stop.sh    # arrête toute la pile
./maj.sh          # déploie une mise à jour : arrêt, git pull, reconstruction
```

L'interface web est alors disponible sur <http://localhost:8080>
(identifiants de dev : `app_user` / `app_password`, voir `dist.env`).

## Déploiements de démonstration

Deux déploiements sont publiés derrière des proxys Apache (serveur externe) :

- <https://p09docker.mr486.com> — la pile docker-compose de ce dépôt ;
- <https://p09kub.mr486.com> — une déclinaison **Kubernetes** (minikube) qui
  démontre l'évolution de l'architecture : l'annuaire Eureka y est remplacé par
  la découverte de services native du cluster (DNS + Services). Cette
  déclinaison vit dans un répertoire local `kubernetes/` non versionné.

## Qualité

Chaque module applique le même outillage : Checkstyle (bloquant), JaCoCo avec
un plancher de 90 % de lignes couvertes, et un site Maven de rapports :

```bash
cd ms-patients          # ou tout autre module
./mvnw clean verify     # tests + Checkstyle + contrôle de couverture
./mvnw clean verify site   # génère target/site/ (Javadoc, JaCoCo, Surefire…)
```

## API

Toutes les API passent par la passerelle (`http://localhost:9000`), avec une
authentification HTTP Basic (`app_user` / `app_password` en dev). Une
collection [Bruno](https://www.usebruno.com/) est fournie dans `Bruno-Api/`.

| Méthode | URL | Rôle |
|---|---|---|
| GET | `/ms-patients/patients` | Liste des patients |
| GET | `/ms-patients/patients/{id}` | Détail d'un patient |
| POST | `/ms-patients/patients` | Création d'un patient |
| PUT | `/ms-patients/patients/{id}` | Modification d'un patient |
| GET | `/ms-notes/patients/{id}/notes` | Notes médicales d'un patient |
| POST | `/ms-notes/patients/{id}/notes` | Ajout d'une note |
| GET | `/ms-risque/patients/{id}/evaluation` | Évaluation du risque de diabète |

La documentation Swagger agrégée est servie par la passerelle :
<http://localhost:9000/swagger-ui.html>.

### Exemple : évaluation du risque

Requête :

```http
GET http://localhost:9000/ms-risque/patients/3/evaluation
Authorization: Basic app_user:app_password
```

Réponse :

```json
{
  "level": "In Danger"
}
```
