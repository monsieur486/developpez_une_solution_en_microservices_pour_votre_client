# P09 — Dépistage du risque de diabète (microservices)

Application de suivi de patients et de dépistage du risque de diabète de
type 2, découpée en microservices Spring Boot : les praticiens consultent les
patients, ajoutent des notes médicales et obtiennent une évaluation du risque
calculée à partir des termes déclencheurs présents dans les notes.

Stack : Java 17, Spring Boot / Spring Cloud (Eureka, Gateway), WebFlux de bout
en bout (WebClient), PostgreSQL, MongoDB, Thymeleaf, Docker Compose.

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

## Green Code — pistes d'écoconception

Dans le cadre de la politique de protection de l'environnement, ce projet
s'appuie sur les référentiels d'écoconception numérique : le
[RGESN v2](https://ecoresponsable.numerique.gouv.fr/publications/referentiel-general-ecoconception/)
(78 critères, 9 thématiques), les
[115 bonnes pratiques GreenIT](https://github.com/cnumr/best-practices) et le
[GR491](https://gr491.isit-europe.org/). Principe directeur : la sobriété
d'abord (ne pas produire/transférer ce qui n'est pas utile), puis mesurer,
puis optimiser.

**Déjà en place dans le projet :**

- **Pagination côté API** (patients par 20, notes par 5) : on ne transfère et
  n'affiche que le nécessaire.
- **Réactif de bout en bout** (WebFlux/Netty) : moins de threads et de mémoire
  par requête, appels parallèles (patient + notes) qui réduisent la latence.
- **Dimensionnement au besoin réel** : réplicas différenciés (3 uniquement pour
  ms-risque), limites mémoire par conteneur, heap JVM borné.
- **Extinction des environnements** inutilisés (`dev-stop.sh`, `prod-stop.sh`,
  `k8s-stop.sh`) et builds Docker multi-étapes.

**Actions suggérées :**

1. **Mesurer avant d'optimiser** : suivre CPU/RAM réels via Actuator +
   Micrometer ; intégrer les règles d'écoconception Java de
   [Creedengo](https://creedengo.org/) (ex-ecoCode, plugin SonarQube) à
   l'outillage qualité ; scorer l'interface web avec
   [EcoIndex](https://www.ecoindex.fr/).
2. **Alléger les images Docker** : baser l'exécution sur `eclipse-temurin:17-jre`
   (voire un runtime `jlink` sur mesure) au lieu du JDK complet — environ
   deux fois moins de stockage et de transfert par déploiement.
3. **Réduire le temps et le coût de démarrage des JVM** : activer AppCDS, ou
   étudier une compilation native (GraalVM) pour les services les plus
   sollicités — moins de RAM et de CPU au repos.
4. **Sobriété du front** : servir Bootstrap localement plutôt que depuis un CDN,
   activer la compression et les en-têtes de cache HTTP, limiter polices et
   images.
5. **Éviter le sur-transfert de données** : exposer des projections (champs
   réellement utilisés par l'écran) plutôt que des entités complètes ;
   ms-risque pourrait compter les déclencheurs côté ms-notes pour ne pas
   transférer le contenu intégral des notes.
6. **Adapter la puissance à la demande** : autoscaling (HPA Kubernetes) plutôt
   que des réplicas fixes, extinction planifiée des environnements de
   démonstration hors des heures d'usage.
7. **Sobriété fonctionnelle** : questionner chaque nouvelle fonctionnalité
   (« ce besoin justifie-t-il son coût environnemental ? ») — c'est le premier
   critère du RGESN.

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
| GET | `/ms-patients/patients?page=0` | Patients par pages de 20 |
| GET | `/ms-patients/patients/{id}` | Détail d'un patient |
| POST | `/ms-patients/patients` | Création d'un patient |
| PUT | `/ms-patients/patients/{id}` | Modification d'un patient |
| GET | `/ms-notes/patients/{id}/notes` | Toutes les notes d'un patient (pour le calcul du risque) |
| GET | `/ms-notes/patients/{id}/notes/pagines?page=0` | Notes par pages de 5 (plus récentes d'abord) |
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
