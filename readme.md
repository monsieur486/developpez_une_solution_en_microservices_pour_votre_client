# Médilabo Solutions — dépistage du risque de diabète

Application en microservices qui aide les praticiens d'une clinique à repérer les patients
exposés au diabète de type 2. Projet 9 du parcours *Développeur d'application Java*
(OpenClassrooms).

> **Étape 4 — documentation des API.** Sans rien changer au métier, chaque service publie
> son contrat OpenAPI, et les trois sont réunis dans une interface Swagger unique servie par
> la gateway.
>
> Le contrat de chaque service est vérifié par un test : servi sans identifiants, avec la gateway
> pour serveur, et listé dans le sélecteur de l'interface.

| Étape | Besoin | État |
|---|---|---|
| 1 | Consulter, ajouter et mettre à jour les informations personnelles des patients | ✅ livré |
| 2 | Consulter l'historique des notes d'un patient et y ajouter une note (MongoDB) | ✅ livré |
| 3 | Évaluer le risque de diabète d'un patient | ✅ livré |
| 4 | Documenter les API (OpenAPI, Swagger) | ✅ livré |
| 5 | Documenter le code (Javadoc, configuration commentée) | ⏳ à venir |

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
       ┌────────────────┼────────────────┐   routes explicites lb://
       ▼                ▼                ▼
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ ms-patients │  │  ms-notes   │  │  ms-risque  │
│ :9100    ×2 │  │ :9200    ×2 │  │ :9300    ×2 │
└──────┬──────┘  └──────┬──────┘  └──────┬──────┘
       ▼                ▼                │ Feign + Eureka, HTTP Basic
PostgreSQL :5432  MongoDB :27017         └──► ms-patients, ms-notes
```

- **Le front ne parle qu'à la gateway.** Il ne connaît ni Eureka ni l'adresse des
  microservices back.
- **La gateway déclare une route par microservice** (`/ms-patients/**`, `/ms-notes/**`,
  `/ms-risque/**`), vers une adresse `lb://` résolue dans Eureka et répartie entre les instances.
  Une route créée par découverte disparaîtrait dès que son service quitte le registre : la
  gateway répondrait alors 404, que le front prendrait pour un patient introuvable. Une route
  explicite reste en place, et sans instance disponible la gateway répond **503**, qui signale
  bien une panne.
- **`ms-risque` appelle `ms-patients` et `ms-notes` directement**, par des clients Feign
  résolus dans Eureka, sans repasser par la gateway : c'est un appel interne entre services.
- **Les API back exigent une authentification HTTP Basic**, avec un utilisateur technique.
  Le front, lui, connecte le praticien par un formulaire.

| Module | Port | Rôle |
|---|---|---|
| `ms-commun` | — | Bibliothèque partagée : DTO échangés, exceptions communes, sécurité HTTP Basic et gestionnaire d'erreurs uniforme, socle de la documentation OpenAPI (activés par `@Import`) |
| `ms-eureka` | 8761 | Registre des services (Spring Cloud Netflix Eureka) |
| `ms-gateway` | 9000 | Point d'entrée unique des API (Spring Cloud Gateway), interface Swagger agrégée |
| `ms-patients` | 9100 | API REST des fiches patients, sur PostgreSQL, avec un schéma géré par Liquibase |
| `ms-notes` | 9200 | API REST des notes des praticiens, sur MongoDB, amorcée par Mongock |
| `ms-risque` | 9300 | Évaluation du risque de diabète, sans base : interroge `ms-patients` et `ms-notes` (OpenFeign) |
| `ms-webclient` | 8080 | Interface web des praticiens (Spring MVC, Thymeleaf, Bootstrap) |

**Stack** : Java 17, Spring Boot 3.5, Spring Cloud 2025.0, Maven multi-modules, Lombok,
PostgreSQL 16, Liquibase, MongoDB 8, Mongock, springdoc-openapi, Docker Compose.

## Prérequis

- Java 17 et Docker (avec Docker Compose v2). Maven est fourni par le wrapper `./mvnw`.
- Les ports 8080, 8761, 9000, 5432 et 27017 libres sur le poste.

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
| `MONGO_INITDB_ROOT_USERNAME`, `MONGO_INITDB_ROOT_PASSWORD` | Base des notes |
| `APP_USERNAME`, `APP_PASSWORD` | Utilisateur technique des API, qui sert aussi d'identifiant de connexion au front |
| `LIQUIBASE_CONTEXTS` | `demo` insère les 4 patients de test ; le défaut `prod` n'en insère aucun |
| `MONGOCK_PROFILES` | `demo` insère les 9 notes de test ; le défaut `prod` n'en insère aucune |
| `REPLICA_MS_PATIENTS`, `REPLICA_MS_NOTES`, `REPLICA_MS_RISQUE`, `PORT_WEBCLIENT` | Optionnels : nombre d'instances de chaque microservice métier (2 par défaut), port du front (8080 par défaut) |

Sans `POSTGRES_PASSWORD`, `MONGO_INITDB_ROOT_PASSWORD` ou `APP_PASSWORD`, `docker compose`
refuse de démarrer et indique la variable manquante.

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

`prod-start.sh` n'annonce la pile **prête** qu'une fois la gateway capable de router vers
chaque microservice : les services doivent d'abord se déclarer auprès du registre, puis la
gateway rafraîchir son cache des instances. En attendant, une route répond 503 (aucune
instance connue) ; dès qu'elle atteint son service, elle répond 401 faute d'identifiants. Le script
patiente 60 s, puis vérifie toutes les 10 s, pendant 5 minutes au plus ; au-delà, il nomme
les routes manquantes et s'arrête en erreur.

### En développement (applications dans l'IDE)

```bash
./dev-start.sh         # démarre seulement PostgreSQL et MongoDB
```

Lancer ensuite, dans cet ordre, `MsEurekaApplication`, `MsGatewayApplication`, `MsPatientsApplication`, `MsNotesApplication`, `MsRisqueApplication`, `MsWebclientApplication`, depuis l'IDE ou avec Maven :

```bash
./mvnw -pl ms-commun install -DskipTests    # à refaire après chaque modification de ms-commun
./mvnw -pl ms-patients spring-boot:run      # sans -am : spring-boot:run échouerait sur le pom parent
```

`spring-boot:run` lance chaque module **depuis la racine du dépôt** (réglage du pom parent), là
où se trouve `.env`. Lancé depuis le dossier du module, il ne trouverait pas `.env` : les mots de
passe resteraient non résolus et les bases refuseraient la connexion.

> **Arrêter les applications de l'IDE avant de lancer la pile Docker** : elles occupent les
> mêmes ports. Un conteneur créé pendant que son port était pris peut rester bloqué
> (« Created », voire sans réseau). On le recrée alors avec
> `docker compose --profile fullstack up -d --force-recreate <service>`.

## Accès

| Ressource | URL | Identifiants |
|---|---|---|
| Interface web | http://localhost:8080 | `APP_USERNAME` / `APP_PASSWORD` |
| Gateway | http://localhost:9000 | idem, en HTTP Basic |
| Registre Eureka | http://localhost:8761 | — |
| Interface Swagger | http://localhost:9000/swagger-ui/index.html | bouton **Authorize** : `APP_USERNAME` / `APP_PASSWORD` |

## API

Toutes les routes passent par la gateway et exigent l'authentification HTTP Basic.

| Méthode | Route (via la gateway) | Réponse |
|---|---|---|
| `GET` | `/ms-patients/patients` | 200, liste des patients |
| `GET` | `/ms-patients/patients/{id}` | 200, fiche du patient · 404 si inconnu |
| `POST` | `/ms-patients/patients` | 201, fiche créée · 400 si invalide · 409 si le patient existe déjà |
| `PUT` | `/ms-patients/patients/{id}` | 200, fiche mise à jour · 400 · 404 · 409 |
| `GET` | `/ms-notes/patients/{id}/notes` | 200, notes du patient, de la plus récente à la plus ancienne (liste vide s'il n'en a pas) |
| `POST` | `/ms-notes/patients/{id}/notes` | 201, note créée et datée · 400 si le contenu est vide ou dépasse 5000 caractères |
| `GET` | `/ms-risque/evaluation/{id}` | 200, patient et niveau de risque (`{"patientId":3,"niveau":"In Danger"}`) · 404 si le patient est inconnu · 503 si `ms-patients` ou `ms-notes` est injoignable · 502 s'il refuse l'appel |

Exemple :

```bash
curl -u "$APP_USERNAME:$APP_PASSWORD" http://localhost:9000/ms-patients/patients/1
```

Une note se saisit par son seul contenu (`{"content":"…"}`) et se restitue avec sa date
d'enregistrement, attribuée par `ms-notes` (`{"content":"…","createdDate":"2026-10-20T16:13:39Z"}`).
L'identifiant technique et le patient rattaché restent internes au service. Le texte est
conservé tel quel, casse comprise : l'évaluation du risque y recherche les termes déclencheurs.

Les erreurs ont toutes le même format :

```json
{"timestamp":"…","microserviceName":"ms-patients","path":"/patients/999",
 "errorCode":"NOT_FOUND","messages":["Aucun patient avec l'id: 999"]}
```

Les statuts distinguent la responsabilité : **4xx**, la demande est en cause et le message
explique quoi corriger ; **5xx**, le service est en cause et seul un message générique est
renvoyé, la cause restant dans les journaux :

- `503` : un microservice appelé est injoignable, ou la gateway ne lui connaît aucune instance ;
- `502` : un microservice appelé a refusé la requête (identifiants techniques erronés, droits
  insuffisants). Le défaut vient de l'appel émis par le service, pas de la demande du client :
  un 4xx l'accuserait à tort ;
- `500` : toute autre erreur non anticipée.

### Règles de saisie d'un patient

- **Obligatoires** : prénom, nom, date de naissance (dans le passé, format `yyyy-MM-dd`),
  genre (`M` ou `F`).
- **Optionnels** : adresse postale et téléphone. Un champ laissé vide est enregistré
  `null`, pas `""`.
- **Unicité** : deux patients ne peuvent pas avoir les mêmes nom, prénom, date de naissance et
  genre.

### Règles de l'évaluation du risque

Le niveau dépend de l'âge du patient, de son genre et du **nombre de termes déclencheurs
distincts** cités dans l'ensemble de ses notes :

| Niveau | 30 ans ou plus | Homme de moins de 30 ans | Femme de moins de 30 ans |
|---|---|---|---|
| `None` | 0 ou 1 terme | 0 à 2 termes | 0 à 3 termes |
| `Borderline` | 2 à 5 termes | — | — |
| `In Danger` | 6 ou 7 termes | 3 ou 4 termes | 4 à 6 termes |
| `Early onset` | 8 termes ou plus | 5 termes ou plus | 7 termes ou plus |

Termes déclencheurs : Hémoglobine A1C, Microalbumine, Taille, Poids, Fumeur/Fumeuse, Anormal,
Cholestérol, Vertiges, Rechute, Réaction, Anticorps.

Choix d'interprétation, chacun couvert par un test :

- **Termes distincts** : un terme cité dans plusieurs notes, ou plusieurs fois dans la même,
  compte une seule fois. Fumeur et Fumeuse sont deux graphies d'un **même** terme.
- **Casse ignorée, recherche par sous-chaîne** : « vertige » reconnaît « Vertiges » comme
  « Vertige », « anormal » reconnaît « anormales ». « fume » ou « fumer » ne comptent pas.
- **Cas que le cahier des charges ne couvre pas** : un patient de 30 ans pile suit les règles des
  30 ans ou plus ; un seul terme après 30 ans, ou trop peu de termes avant, donne `None`.
- **Âge** en années révolues, calculé à la date du jour (horloge injectable, figée dans les tests).
- **Libellés** : ceux du résumé des besoins fonctionnels, espaces compris (`In Danger`,
  `Early onset`), plutôt que l'écriture sans espace du tableau des résultats attendus.

## Documentation des API

Une seule interface Swagger, servie par la gateway, donne accès aux contrats des trois
microservices ; le sélecteur en haut de page bascule de l'un à l'autre.

| Ressource | URL |
|---|---|
| Interface Swagger | http://localhost:9000/swagger-ui/index.html |
| Contrat de `ms-patients` | http://localhost:9000/ms-patients/v3/api-docs |
| Contrat de `ms-notes` | http://localhost:9000/ms-notes/v3/api-docs |
| Contrat de `ms-risque` | http://localhost:9000/ms-risque/v3/api-docs |

| Où | Quoi |
|---|---|
| `pom.xml` des services | dépendance `springdoc-openapi-starter-webmvc-api` ; la gateway prend la variante `webflux-ui`, qui sert l'interface |
| `ms-commun` — `DocumentationApiConfiguration` | déclare le schéma de sécurité `basicAuth` et le serveur du contrat |
| `ms-commun` — `SecuriteApiConfiguration` | ouvre `/v3/api-docs/**` : un contrat protégé laisserait le sélecteur vide |
| chaque contrôleur | `@OpenAPIDefinition` et `@SecurityRequirement` sur la classe, `@Tag` sur chaque opération |
| `ms-gateway` — `application.yml` | liste les contrats présentés dans le sélecteur |

Chaque service déclare son propre nom comme serveur du contrat, lu depuis
`spring.application.name` : les appels d'essai passent ainsi par la gateway, qui retire ce
préfixe avant de transmettre, au lieu de viser le service en direct.

## Données de démonstration

Avec `LIQUIBASE_CONTEXTS=demo`, la base reçoit les 4 patients des cas de test :

| Nom | Prénom | Naissance | Genre | Adresse | Téléphone |
|---|---|---|---|---|---|
| TestNone | Test | 1966-12-31 | F | 1 Brookside St | 100-222-3333 |
| TestBorderline | Test | 1945-06-24 | M | 2 High St | 200-333-4444 |
| TestInDanger | Test | 2004-06-18 | M | 3 Club Road | 300-444-5555 |
| TestEarlyOnset | Test | 2002-06-28 | F | 4 Valley Dr | 400-555-6666 |

Résultats attendus de l'évaluation, vérifiés par un test paramétré et sur la pile Docker :
`TestNone` → `None`, `TestBorderline` → `Borderline`, `TestInDanger` → `In Danger`,
`TestEarlyOnset` → `Early onset`.

Avec `MONGOCK_PROFILES=demo`, une migration Mongock insère au démarrage de `ms-notes` 9 notes
de démonstration rattachées à ces 4 patients (identifiants 1 à 4, dans l'ordre du tableau), si
la collection est vide. Sans ce réglage
(défaut `prod`), la migration est écartée sans être inscrite au journal de Mongock : elle
s'appliquera si `demo` est activé plus tard.

Pour repartir de bases vierges : `docker compose down -v` (supprime les volumes de données).

## Tests et qualité

```bash
./mvnw clean verify          # tests + analyses Checkstyle, PMD/CPD, SpotBugs/FindSecBugs, JaCoCo
./mvnw clean verify site site:stage   # rapports HTML agrégés dans target/staging/index.html
```

- **Tests unitaires** : JUnit 5, Mockito, AssertJ.
- **Couche web** : `@WebMvcTest` + MockMvc (sécurité, CSRF, validation, statuts d'erreur).
- **Appels HTTP du front** : `@RestClientTest` + `MockRestServiceServer`, avec le vrai
  `RestTemplate` de production.
- **Intégration** : Testcontainers sur un vrai PostgreSQL et un vrai MongoDB. Docker doit
  tourner pendant les tests.
- **Tests déterministes** : la date des notes et l'âge des patients viennent d'un `Clock`
  injecté, figé dans les tests (`Clock.fixed`).
- **Algorithme du risque** : tests paramétrés (`@CsvSource`, `@MethodSource`) sur chaque seuil,
  chaque choix d'interprétation et les 4 cas officiels avec le texte exact de leurs notes.
- **Routes de la gateway** : `@SpringBootTest` sans Eureka, qui vérifie qu'un service sans
  instance répond 503 et non 404, et que l'interface Swagger liste les trois contrats.
- **Contrats OpenAPI** : `@WebMvcTest` avec springdoc, qui vérifie que `/v3/api-docs` est servi
  sans identifiants et annonce la gateway pour serveur.
- **Tranches légères** : Mongock est activé dans une classe `@Configuration` dédiée, et non sur
  la classe principale, pour que `@WebMvcTest` et `@DataMongoTest` n'exigent pas de base complète.
- La couverture JaCoCo est un **repère**, pas un objectif : on teste le comportement métier,
  pas les délégations.
- Les règles de style et d'analyse sont dans `config/` (Checkstyle, PMD, exclusions SpotBugs).

## Structure du dépôt

```
config/              règles Checkstyle, PMD, SpotBugs partagées par tous les modules
ms-*/                un dossier par microservice, chacun avec son Dockerfile
docs/captures/       captures d'écran des erreurs, présentées en fin de README
docker-compose.yml   pile complète (profil « fullstack ») ou bases seules
dist.env             modèle des variables d'environnement (à copier en .env)
rapport.md           revues de code et checklist de suivi qualité
utilisation-ia.md    journal de l'usage de l'IA dans le projet
```

## Recommandations Green Code

*À rédiger à l'étape 6 du projet : enjeux du Green Code et pistes d'amélioration propres à ce
projet.*

## Captures des erreurs et exceptions

Chaque erreur que l'application sait produire, telle que la voit le praticien dans l'interface web
ou un développeur dans l'interface Swagger. Captures prises sur la pile Docker complète
(`./prod-start.sh`), données de démonstration ; aucune tentative refusée n'a modifié les bases.
L'en-tête `Authorization` des commandes `curl` affichées par Swagger est masqué.

### Interface web — saisie et navigation

**Connexion refusée** : identifiant ou mot de passe incorrect.

![Connexion refusée](docs/captures/01-connexion-refusee.png)

**Création d'un patient sans les champs obligatoires** : la validation de `PatientForm` liste
chaque champ manquant et le signale en rouge.

![Champs obligatoires manquants](docs/captures/02-patient-champs-obligatoires.png)

**Date de naissance dans le futur** (`@Past`).

![Date de naissance future](docs/captures/03-patient-date-future.png)

**Patient déjà enregistré** : `ms-patients` répond 409, le formulaire reste rempli et un
avertissement s'affiche.

![Patient en doublon](docs/captures/04-patient-doublon.png)

**Modification invalide** : le formulaire de mise à jour applique les mêmes règles que la création.

![Modification invalide](docs/captures/05-patient-modification-invalide.png)

**Patient inconnu** : `ms-patients` répond 404, l'interface affiche son message.

![Patient introuvable](docs/captures/06-patient-introuvable.png)

**Adresse inexistante dans l'interface** : page 404 générique.

![Page introuvable](docs/captures/07-page-introuvable.png)

**Note vide**, puis **note de plus de 5000 caractères** (`NoteForm`).

![Note vide](docs/captures/08-note-vide.png)

![Note trop longue](docs/captures/09-note-trop-longue.png)

### API — à travers l'interface Swagger

**401 sans identifiants.** L'API répond aussi `WWW-Authenticate: Basic`, qui ferait ouvrir au
navigateur sa propre fenêtre de connexion : cet en-tête a été retiré pour la capture, le statut et
le corps sont ceux de l'API.

![401 sans identifiants](docs/captures/10-api-401-sans-identifiants.png)

**400** : patient invalide, chaque règle enfreinte est nommée.

![400 patient invalide](docs/captures/11-api-400-patient-invalide.png)

**404** : patient inconnu.

![404 patient inconnu](docs/captures/12-api-404-patient-inconnu.png)

**409** : patient déjà enregistré.

![409 doublon](docs/captures/13-api-409-patient-doublon.png)

**400** : note vide.

![400 note vide](docs/captures/14-api-400-note-vide.png)

**404** : évaluation du risque d'un patient inconnu. Comme toutes les API, `ms-risque` répond en
JSON, succès comme erreur.

![404 évaluation d'un patient inconnu](docs/captures/15-api-404-risque-patient-inconnu.png)

### Interruptions de service

**`ms-risque` arrêté** : la fiche reste consultable, un avertissement remplace le risque.

![Fiche sans ms-risque](docs/captures/16-fiche-sans-ms-risque.png)

**Aucune instance de `ms-risque`** : la route explicite de la gateway reste en place et répond 503,
au lieu du 404 qui faisait croire à un patient introuvable.

![503 de la gateway](docs/captures/17-api-503-gateway-sans-instance.png)

**`ms-notes` arrêté** : la fiche reste consultable. Les notes sont remplacées par un avertissement,
et le risque aussi, puisque `ms-risque` a besoin des notes pour l'évaluer.

![Fiche sans ms-notes](docs/captures/18-fiche-sans-ms-notes.png)

**`ms-notes` arrêté, vu depuis l'API** : `ms-risque` ne peut pas lire les notes et répond 503, en
nommant le service en cause.

![503 de ms-risque sans ms-notes](docs/captures/19-api-503-risque-sans-ms-notes.png)

**`ms-patients` arrêté** : sans fiche, rien ne peut s'afficher ; la page signale une interruption
temporaire.

![Liste sans ms-patients](docs/captures/20-liste-sans-ms-patients.png)

**Appel refusé en aval (502)** : `ms-risque` a été lancé avec un mot de passe factice. Il
l'accepte en entrée, mais `ms-patients` refuse son appel. Le message reste générique, sans nommer
le service ni le statut reçu, qui ne figurent que dans les journaux.

![502 appel refusé](docs/captures/21-api-502-appel-refuse.png)

### Ce qui n'est pas encore capturé

- **La page d'erreur interne (500)** de l'interface web : aucune action normale ne la déclenche.
