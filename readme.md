# Médilabo Solutions — dépistage du risque de diabète

Application en microservices qui aide les praticiens d'une clinique à repérer les patients
exposés au diabète de type 2. Projet 9 du parcours *Développeur d'application Java*
(OpenClassrooms).

| Sprint | Besoin | État |
|---|---|---|
| 1 | Consulter, ajouter et mettre à jour les informations personnelles des patients | ✅ livré |
| 2 | Consulter l'historique des notes d'un patient et y ajouter une note (MongoDB) | 🚧 en cours |
| 3 | Évaluer le risque de diabète d'un patient | ⏳ à venir |

## Architecture

```
                    navigateur
                        │
                        ▼
                ┌───────────────┐
                │ ms-webclient  │  :8080   interface Thymeleaf (connexion par formulaire)
                └───────┬───────┘
                        │ HTTP Basic
                        ▼
                ┌───────────────┐          ┌─────────────┐
                │  ms-gateway   │  :9000 ◄─┤  ms-eureka  │ :8761  registre des services
                └───────┬───────┘          └─────────────┘
                        │ /ms-patients/**  (routage par découverte)
                        ▼
                ┌───────────────┐          ┌─────────────┐
                │  ms-patients  │  :9100 ──► PostgreSQL  │ :5432
                └───────────────┘  ×2      └─────────────┘
```

- **Le front ne parle qu'à la gateway.** Il ne connaît ni Eureka ni l'adresse des
  microservices back.
- **La gateway route par découverte** : toute application inscrite dans Eureka sous le nom
  `ms-xxx` devient joignable sous `/ms-xxx/**`, sans configuration de route à écrire.
- **Les API back exigent une authentification HTTP Basic**, avec un utilisateur technique.
  Le front, lui, connecte le praticien par un formulaire.

| Module | Rôle |
|---|---|
| `ms-commun` | Bibliothèque partagée : DTO échangés, exceptions communes, sécurité HTTP Basic et gestionnaire d'erreurs uniforme (activés par `@Import`) |
| `ms-eureka` | Registre des services (Spring Cloud Netflix Eureka) |
| `ms-gateway` | Point d'entrée unique des API (Spring Cloud Gateway) |
| `ms-patients` | API REST des fiches patients, sur PostgreSQL, avec un schéma géré par Liquibase |
| `ms-webclient` | Interface web des praticiens (Spring MVC, Thymeleaf, Bootstrap) |

**Stack** : Java 17, Spring Boot 3.5, Spring Cloud 2025.0, Maven multi-modules, Lombok,
PostgreSQL 16, Liquibase, Docker Compose.

## Prérequis

- Java 17 et Docker (avec Docker Compose v2). Maven est fourni par le wrapper `./mvnw`.
- Les ports 8080, 8761, 9000 et 5432 libres sur le poste.

## Configuration des secrets

Aucun mot de passe n'a de valeur par défaut dans le code. Ils se définissent dans un fichier
`.env`, jamais versionné, copié depuis le modèle :

```bash
cp dist.env .env
# puis adapter les valeurs
```

| Variable | Rôle |
|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Base des patients |
| `APP_USERNAME`, `APP_PASSWORD` | Utilisateur technique des API, qui sert aussi d'identifiant de connexion au front |
| `LIQUIBASE_CONTEXTS` | `demo` insère les 4 patients de test ; le défaut `prod` n'en insère aucun |
| `REPLICA_MS_PATIENTS`, `PORT_WEBCLIENT` | Optionnels : nombre d'instances de `ms-patients` (2 par défaut), port du front (8080 par défaut) |

Sans `POSTGRES_PASSWORD` ou `APP_PASSWORD`, `docker compose` refuse de démarrer et indique
la variable manquante.

## Lancer l'application

### Avec Docker (pile complète)

```bash
./prod-start.sh        # construit les images puis démarre toute la pile
./prod-stop.sh         # arrête la pile, en conservant les données
./maj.sh               # arrête, récupère la dernière version (git pull), reconstruit et redémarre
```

Ouvrir ensuite http://localhost:8080 et se connecter avec `APP_USERNAME` / `APP_PASSWORD`.

Au démarrage, un conteneur `ms-patients-migration` applique les migrations Liquibase **une
seule fois**. Les instances de `ms-patients` ne démarrent qu'une fois cette migration réussie.

### En développement (applications dans l'IDE)

```bash
./dev-start.sh         # démarre seulement PostgreSQL
```

Lancer ensuite, dans cet ordre, `MsEurekaApplication`, `MsGatewayApplication`,
`MsPatientsApplication` et `MsWebclientApplication`, depuis l'IDE ou avec Maven :

```bash
./mvnw -pl ms-commun install -DskipTests    # à refaire après chaque modification de ms-commun
./mvnw -pl ms-patients spring-boot:run      # sans -am : spring-boot:run échouerait sur le pom parent
```

> **Arrêter les applications de l'IDE avant de lancer la pile Docker** : elles occupent les
> mêmes ports. Un conteneur créé pendant que son port était pris peut rester bloqué
> (« Created », voire sans réseau). On le recrée alors avec
> `docker compose --profile fullstack up -d --force-recreate <service>`.

## API

Toutes les routes passent par la gateway et exigent l'authentification HTTP Basic.

| Méthode | Route (via la gateway) | Réponse |
|---|---|---|
| `GET` | `/ms-patients/patients` | 200, liste des patients |
| `GET` | `/ms-patients/patients/{id}` | 200, fiche du patient · 404 si inconnu |
| `POST` | `/ms-patients/patients` | 201, fiche créée · 400 si invalide · 409 si le patient existe déjà |
| `PUT` | `/ms-patients/patients/{id}` | 200, fiche mise à jour · 400 · 404 · 409 |

Exemple :

```bash
curl -u "$APP_USERNAME:$APP_PASSWORD" http://localhost:9000/ms-patients/patients/1
```

Les erreurs ont toutes le même format :

```json
{"timestamp":"…","microserviceName":"ms-patients","path":"/patients/999",
 "errorCode":"NOT_FOUND","messages":["Aucun patient avec l'id: 999"]}
```

### Règles de saisie d'un patient

- **Obligatoires** : prénom, nom, date de naissance (dans le passé, format `yyyy-MM-dd`),
  genre (`M` ou `F`).
- **Optionnels** : adresse postale et téléphone. Un champ laissé vide est enregistré
  `null`, pas `""`.
- **Unicité** : deux patients ne peuvent pas avoir les mêmes nom, prénom, date de naissance et
  genre.

## Données de démonstration

Avec `LIQUIBASE_CONTEXTS=demo`, la base reçoit les 4 patients des cas de test :

| Nom | Prénom | Naissance | Genre | Adresse | Téléphone |
|---|---|---|---|---|---|
| TestNone | Test | 1966-12-31 | F | 1 Brookside St | 100-222-3333 |
| TestBorderline | Test | 1945-06-24 | M | 2 High St | 200-333-4444 |
| TestInDanger | Test | 2004-06-18 | M | 3 Club Road | 300-444-5555 |
| TestEarlyOnset | Test | 2002-06-28 | F | 4 Valley Dr | 400-555-6666 |

Pour repartir d'une base vierge : `docker compose down -v` (supprime le volume de données).

## Tests et qualité

```bash
./mvnw clean verify          # tests + analyses Checkstyle, PMD/CPD, SpotBugs/FindSecBugs, JaCoCo
./mvnw site                  # rapports HTML dans */target/site/
```

- **Tests unitaires** : JUnit 5, Mockito, AssertJ.
- **Couche web** : `@WebMvcTest` + MockMvc (sécurité, CSRF, validation, statuts d'erreur).
- **Appels HTTP du front** : `@RestClientTest` + `MockRestServiceServer`, avec le vrai
  `RestTemplate` de production.
- **Intégration** : Testcontainers sur un vrai PostgreSQL. Docker doit tourner pendant les tests.
- La couverture JaCoCo est un **repère**, pas un objectif : on teste le comportement métier,
  pas les délégations.
- Les règles de style et d'analyse sont dans `config/` (Checkstyle, PMD, exclusions SpotBugs).

## Structure du dépôt

```
config/              règles Checkstyle, PMD, SpotBugs partagées par tous les modules
ms-*/                un dossier par microservice, chacun avec son Dockerfile
docker-compose.yml   pile complète (profil « fullstack ») ou base seule
dist.env             modèle des variables d'environnement (à copier en .env)
rapport.md           revues de code et checklist de suivi qualité
utilisation-ia.md    journal de l'usage de l'IA dans le projet
```

## Recommandations Green Code

*À rédiger à l'étape 6 du projet : enjeux du Green Code et pistes d'amélioration propres à ce
projet.*
