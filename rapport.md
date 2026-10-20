# Revue pédagogique — Sprint 1 : `ms-patients` (+ `ms-commun`)

**Date de la revue** : 2026-10-20 (2ᵉ passage de la journée)
**Périmètre** : `ms-commun`, `ms-patients`. `ms-webclient` n'est pas encore relu.
**Mesures** : `./mvnw -pl ms-commun,ms-patients verify` → **6 tests verts**, SpotBugs 0 bug,
PMD 4 violations, Checkstyle 27 violations (toutes des Javadoc manquantes). Le build ne passe
qu'en forçant `-Dtestcontainers.version=1.21.4` (voir Phase 1).

> **Mise à jour (modifications IA autorisées)** : 14 points réglés par l'IA à ma demande. Build vert sans option : 13 tests, Checkstyle 0, SpotBugs 0, PMD 1 (`GlobalExceptionHandler`), Javadoc sans avertissement.
>
> **Progrès depuis le passage précédent : 4 points réglés, 6 restants, 7 nouveaux.**
> Le service est écrit avec de vrais tests métier, et le CRUD complet est exposé.

## En bref
Gros bond en avant : contrôleur, service, mapper, DTO et exceptions métier sont là,
dans des couches propres. Les tests du service sont **de bonne facture**. Trois priorités :
réparer Testcontainers, sortir la conversion formulaire → entité du service, et renforcer
la validation des entrées.

## Points forts
- **Tests du service exemplaires sur l'essentiel** : Mockito pur (aucun contexte Spring),
  service construit à la main avec son mock, `@DisplayName` en français, et uniquement des
  règles métier testées (introuvable, doublon à la création, doublon au renommage).
  `verify(..., never()).save(...)` prouve que rien n'est écrit en cas de rejet.
- **Le cas subtil est pensé** : modifier l'adresse d'un patient ne déclenche pas le contrôle
  d'unicité (`identiteModifiee`). Sans lui, un patient ne pourrait plus jamais être mis à jour.
- **Le test de contexte avec Testcontainers** vérifie d'un coup Liquibase + `ddl-auto: validate`
  sur un vrai PostgreSQL. C'est le bon outil.
- **Les couches sont respectées** : l'entité ne sort jamais du contrôleur (`PatientMapper` → `PatientDto`),
  injection par constructeur partout (`@RequiredArgsConstructor`), exceptions métier qui
  héritent de `ms-commun`. Le `GlobalExceptionHandler` les traduit donc en 404 / 409 sans rien ajouter.
- **Des logs sans donnée personnelle** : `patient {} créé` ne journalise que l'id, ni le nom
  ni la date de naissance. C'est un bon réflexe pour des données de santé.
- Ton choix sur les doublons est tranché : ils sont gardés, intégrés à une vraie règle et testés.

## Phase 1 — Outillage et tests   ⚠️ à améliorer
### Testcontainers 1.21.3 est incompatible avec ton Docker 29   ·   axe : architecture
- **Constat** : `MsPatientsApplicationTests` échoue avec
  `client version 1.32 is too old. Minimum supported API version is 1.40`.
  Spring Boot 3.5.6 impose Testcontainers 1.21.3, trop ancien pour Docker 29.9.
  Testé : avec `-Dtestcontainers.version=1.21.4`, le test passe.
- **Pourquoi ça compte** : le jury lancera `./mvnw verify` ; un build rouge à cause de l'outillage
  masque la qualité de tes tests.
- **Exemple** : Spring Boot expose les versions qu'il gère sous forme de propriétés, qu'on peut surcharger.
- **À toi de jouer** : surcharge `testcontainers.version` dans les `<properties>` du parent, avec un
  commentaire qui explique pourquoi (sinon quelqu'un la retirera).

### Tests : deux manques et une assertion faible   ·   axe : code propre
- **Constat 1** : il **manque** un test de création réussie. Or `appliqueLeFormulaire` recopie
  6 champs, et c'est la seule logique non couverte du service (9 lignes non couvertes selon JaCoCo).
  Un champ oublié (téléphone non recopié, par exemple) ne serait détecté par aucun test.
- **Constat 2** : il **manque** des tests de la couche web. Rien ne vérifie qu'un formulaire
  sans nom renvoie 400, qu'un id inconnu renvoie 404, ni qu'un appel **sans identifiants renvoie 401**.
  Pour une API de données de santé, le 401 compte.
- **Constat 3** : `.hasMessageContaining("1")` est une assertion trop faible. N'importe quel message
  qui contient le chiffre 1 la satisfait.
- **Exemple** : pour vérifier ce qui part vers la base, utilise un `ArgumentCaptor<Patient>` sur `save(...)`
  et compare chaque champ à une valeur en dur. Pour la couche web, `@WebMvcTest(PatientController.class)` + `MockMvc`.
- **À toi de jouer** : écris le test de création avec captor, durcis le message attendu
  (`hasMessage("Aucun patient avec l'id: 1")`), puis écris 3 tests MockMvc (400, 404, 401).

## Phase 2 — Checkstyle   ⚠️ à améliorer
- **Constat** : 27 violations, **toutes** `MissingJavadocType` / `MissingJavadocMethod`
  (6 dans `ms-commun`, 21 dans `ms-patients`). Le reste du style est propre : imports explicites
  et ordonnés, pas de nombre magique.
- **À toi de jouer** : écris la Javadoc (format maison : description, exemple, puis balises), ou demande-la
  à l'IA en la traçant ⚙️ dans le journal. Commence par `PatientService` : c'est le contrat métier.

## Phase 3 — PMD   ⚠️ à améliorer
### La conversion formulaire → entité est dans le service   ·   axe : architecture
- **Constat** : `PatientMapper` convertit entité → DTO, mais la conversion inverse
  (`appliqueLeFormulaire`) se trouve dans `PatientService`.
- **Pourquoi ça compte** : une même responsabilité (convertir) est répartie dans deux classes. Le
  jour où tu ajoutes un champ, tu dois penser aux deux. Le service doit garder les **règles**, pas la recopie de champs.
- **À toi de jouer** : déplace cette méthode dans `PatientMapper` (par exemple `appliquer(form, patient)`).
  Le test avec captor du point précédent te protège pendant le déplacement.

### Pas de transaction sur les écritures   ·   axe : architecture
- **Constat** : `updatePatient` enchaîne lecture → contrôle d'unicité → écriture sans `@Transactional`.
- **Pourquoi ça compte** : sans transaction, chaque appel au repository ouvre la sienne. Deux requêtes
  simultanées peuvent passer le contrôle de doublon en même temps.
- **À toi de jouer** : annote les méthodes d'écriture (et regarde ce que `readOnly = true` apporte aux lectures).

### Violations PMD à traiter (4)   ·   axe : code propre
- `MsPatientsApplicationTests` : `MutableStaticState`. Le conteneur doit être `static final`.
- `MsPatientsApplication` : `UseUtilityClass`. C'est un faux positif : une classe `@SpringBootApplication` ne peut pas
  être utilitaire. Exclus `*Application` de cette règle dans `config/pmd/ruleset.xml`, plutôt que d'ajouter un constructeur privé.
- `SecuriteApiConfiguration` : `SignatureDeclareThrowsException`. Ce `throws Exception` est imposé par
  `HttpSecurity.build()`. Supprime l'alerte localement avec `@SuppressWarnings("PMD.SignatureDeclareThrowsException")`
  et une ligne de justification.
- `GlobalExceptionHandler` : `TooManyMethods` (9 méthodes). Cette classe vient du socle généré. Sois prêt à expliquer
  au jury pourquoi elle reste groupée (un seul point de traduction des erreurs) ou découpe-la.

### Points du passage précédent encore ouverts
- `@Repository` est toujours présent sur `PatientRepository` (inutile avec Spring Data).
- `gender` accepte toujours n'importe quelle chaîne. `"X"` passe la validation, et le Sprint 3 en dépendra.

## Phase 4 — SpotBugs   ✅ conforme
0 bug, FindSecBugs compris, sur les deux modules.

## Phase 5 — Validation, configuration, README   ⚠️ à améliorer
### La validation de `PatientForm` est incomplète   ·   axe : code propre (sécurité)
- **Constat** : rien n'interdit une date de naissance dans le futur, ni un nom de 300 caractères.
  Ce dernier provoque une **erreur 500** venue de PostgreSQL (`VARCHAR(255)`) au lieu d'un 400 explicite.
- **Exemple** : `@Past` sur la date et `@Size(max = …)` sur les chaînes, en alignant la limite sur la colonne SQL.
- **À toi de jouer** : ajoute ces contraintes, puis couvre-les par un test MockMvc (400 attendu).

### Les DTO patients sont placés dans `ms-commun`   ·   axe : architecture
- **Constat** : `PatientDto` et `PatientForm` sont partagés entre `ms-patients` et `ms-webclient` via `ms-commun`,
  dont la description dit encore « exceptions communes et traitement uniforme des erreurs ».
- **Pourquoi ça compte** : partager le contrat évite de le dupliquer, mais couple les deux microservices.
  Un changement côté patients oblige à reconstruire le front. Les deux choix se défendent, à condition de le **dire**.
- **À toi de jouer** : mets à jour la `<description>` de `ms-commun` et prépare une phrase pour la soutenance.

## Par où commencer
1. Surcharger `testcontainers.version` → build vert sans option en ligne de commande.
2. Test de création avec `ArgumentCaptor`, puis déplacement de `appliqueLeFormulaire` dans le mapper.
3. Validation (`@Past`, `@Size`, contrainte sur `gender`) + 3 tests MockMvc (400 / 404 / 401).

## Suivi — à cocher au fil de l'eau

### Phase 1 — Outillage et tests
- [x] (architecture) Déclarer `ms-patients` et `ms-webclient` dans le `pom.xml` parent
- [x] (code propre) Définir `bootstrap.version`
- [x] (architecture) Créer `SecuriteApiConfiguration` dans `ms-commun`
- [x] (architecture) Ajouter Spring Security (optionnelle) au `pom.xml` de `ms-commun`
- [x] (architecture) Écrire `PatientServiceTest` (Mockito, règles métier)
- [x] (architecture) Surcharger `testcontainers.version` (1.21.4) dans le parent, avec commentaire *(IA, autorisé — voir journal)*
- [x] (code propre) Test de création réussie avec `ArgumentCaptor<Patient>` *(IA, autorisé — voir journal)*
- [x] (code propre) Remplacer `hasMessageContaining("1")` par le message exact *(IA, autorisé — voir journal)*
- [x] (architecture) Tests MockMvc : 400 (formulaire invalide), 404 (id inconnu), 401 (sans identifiants) *(IA, autorisé — voir journal)*

### Phase 2 — Checkstyle
- [x] (présentation) Javadoc FR : `ms-patients` (21 manques), en commençant par `PatientService` *(IA, autorisé — voir journal)*
- [x] (présentation) Javadoc FR : `SecuriteApiConfiguration`, `PatientDto`, `PatientForm` (6 manques) *(IA, autorisé — voir journal)*

### Phase 3 — PMD
- [x] (architecture) Trancher sur les doublons : gardé, intégré à une règle, testé
- [x] (architecture) Déplacer `appliqueLeFormulaire` du service vers `PatientMapper` *(IA, autorisé — voir journal)*
- [x] (architecture) `@Transactional` sur les écritures du service *(IA, autorisé — voir journal)*
- [x] (code propre) Conteneur Testcontainers en `static final` *(IA, autorisé — voir journal)*
- [x] (code propre) Exclure `*Application` de `UseUtilityClass` dans le ruleset PMD *(IA, autorisé — voir journal)*
- [x] (code propre) `@SuppressWarnings` justifié sur `SecuriteApiConfiguration.filtreDeSecurite` *(IA, autorisé — voir journal)*
- [x] (architecture) `GlobalExceptionHandler` (9 méthodes) : justifier ou découper *(découpé par l'IA : `ReponsesErreur`)* — toujours signalé par PMD (le constructeur ajouté compte) : `@SuppressWarnings` justifié ou découpage
- [x] (code propre) Retirer `@Repository` de `PatientRepository` *(IA, autorisé — voir journal)*
- [x] (code propre) Contraindre les valeurs de `gender` *(IA, autorisé — voir journal)*

### Phase 5 — Validation, configuration, README
- [x] (architecture) En-tête `--liquibase formatted sql` + `--changeset` sur `001` et `002` *(généré par l'IA à ma demande, voir journal)*
- [x] (sécurité) `@Past` sur `birthDate`, `@Size(max = 255)` sur les chaînes de `PatientForm` *(IA, autorisé — voir journal)*
- [x] (architecture) Mettre à jour la description de `ms-commun` + justifier le partage des DTO
- [x] (architecture) Isoler les données de test (context Liquibase)
- [x] (sécurité) Retirer les mots de passe par défaut des `application*.yml` et du `docker-compose.yml`
- [x] (sécurité) 🐞 **Régression Docker** : `ms-patients` et `ms-patients-migration` ne reçoivent pas `POSTGRES_PASSWORD` dans `docker-compose.yml` → le placeholder `${POSTGRES_PASSWORD}` part tel quel et PostgreSQL refuse (`password authentication failed`)
- [x] (sécurité) Aligner `ms-webclient` : `application*.yml` gardent `${APP_PASSWORD:app_password}`
- [x] (code propre) Réduire `application-docker.yml` aux seules différences avec `application.yml`

---

# Revue pédagogique — Sprint 1 : `ms-webclient`

**Date de la revue** : 2026-10-20
**Mesures** : `./mvnw -pl ms-commun,ms-webclient verify` → build vert, **aucun test**, SpotBugs 0,
PMD 1 (`SignatureDeclareThrowsException` sur `SecurityConfiguration`), Checkstyle 34
(toutes des Javadoc manquantes : 11 classes, 23 méthodes).

## En bref
Le front couvre tout le Sprint 1 : liste, détail, ajout et modification, avec une architecture
nette (contrôleurs → `PatientApiService` → `ClientPasserelle` → gateway). Il y a **un vrai bug** :
un doublon refusé par l'API s'affiche comme « service indisponible ». Et il **manque tous les tests**.

## Points forts
- **`ClientPasserelle` est le seul point de contact avec la gateway.** Il traduit les erreurs HTTP
  en exceptions métier (`ResourceNotFoundException`, `ServiceIndisponibleException`) : les
  contrôleurs ne voient jamais `RestTemplate`.
- **Le schéma Post/Redirect/Get est respecté**, avec un message flash après création ou
  modification : un rafraîchissement de la page ne renvoie pas le formulaire.
- **`@Valid` + `BindingResult`** réaffichent le formulaire avec ses erreurs, grâce au `PatientForm`
  partagé (mêmes règles côté front et côté API).
- **Les templates sont sûrs** : `th:text` partout (pas de `th:utext`, donc pas d'injection HTML),
  formulaires en `th:action` (jeton CSRF ajouté automatiquement), déconnexion en POST, liste
  déroulante M/F alignée sur la validation.
- `GestionnaireErreursWeb` sépare bien le 404 (avertissement) du 503 / 500 (erreur avec pile d'appels).

## Phase 1 — Tests   ❌ manquant
### Aucun test dans `ms-webclient`   ·   axe : architecture
- **Constat** : `No tests to run`. Pourtant `ClientPasserelle` **décide** : 404 → introuvable,
  le reste → indisponible. C'est exactement ce qui doit être testé, et un test aurait révélé le bug ci-dessous.
- **Exemple** : `MockRestServiceServer.bindTo(restTemplate)` simule la gateway :
  `.andRespond(withStatus(HttpStatus.CONFLICT))`, puis on vérifie l'exception levée.
- **À toi de jouer** : écris `ClientPasserelleTest` (404, 409, gateway injoignable), puis un
  `@WebMvcTest` sur `PatientsController` (formulaire invalide → vue `patient-ajout` réaffichée).

## Phase 3 — PMD et architecture   ⚠️ à améliorer
### Un doublon s'affiche « service indisponible »   ·   axe : architecture
- **Constat** : `ClientPasserelle.echange` n'attrape que `HttpClientErrorException.NotFound`.
  Le 409 renvoyé par `ms-patients` (patient déjà existant) est aussi une `RestClientException` :
  il tombe donc dans le second `catch` et devient `ServiceIndisponibleException`.
  Créer deux fois `TestNone` affiche « Le service ms-patients est momentanément indisponible »
  et journalise une **erreur** avec sa pile d'appels.
- **Pourquoi ça compte** : on fait passer une erreur de **saisie** pour une **panne**. L'utilisateur
  croit que le service est tombé, et l'exploitant reçoit une fausse alerte.
- **Exemple** : distingue les familles d'erreur. Une 4xx relève de l'appelant, une 5xx ou une
  absence de réponse relève du service (le `GlobalExceptionHandler` de `ms-commun` fait déjà ce tri).
- **À toi de jouer** : traduis le 409 en `DuplicateException`, puis réaffiche le formulaire avec
  un message, par exemple avec `resultatDeLiaison.reject(...)`. Commence par le test.

### Petits points   ·   axe : code propre
- `versFormulaire` (dans `PatientDetailController`) est une conversion : elle a plus sa place dans
  un mapper que dans un contrôleur, comme tu l'as fait dans `ms-patients`.
- Les champs optionnels laissés vides arrivent en `""` et non en `null` : l'API enregistre une adresse
  vide plutôt qu'une adresse absente. Regarde `StringTrimmerEditor` (`@InitBinder`).
- `SecurityConfiguration` : les identifiants sont toujours injectés par `@Value` sur des champs, et
  le `throws Exception` est à justifier (`@SuppressWarnings`, comme dans `ms-commun`).
- Nommage des routes : `/app/dashboard` affiche la **liste des patients**. `/app/patients` dirait
  ce que fait la page.

## Phase 5 — Javadoc   ⚠️ à améliorer
- 34 Javadoc manquantes. Commence par `ClientPasserelle` et `PatientApiService` : ce sont les contrats.

## Par où commencer
1. `ClientPasserelleTest` avec `MockRestServiceServer` : écris le cas 409 **avant** de corriger.
2. Corriger la traduction du 409 et réafficher le formulaire avec le message de doublon.
3. `@WebMvcTest` du formulaire d'ajout, puis la Javadoc.

## Suivi `ms-webclient` — à cocher au fil de l'eau

> **Point du 2026-10-20 (4ᵉ passage, corrections par l'IA à ma demande)** : 32 tests verts, Checkstyle 0,
> PMD 0, SpotBugs 0, Javadoc sans avertissement. Le test de `PatientApiService` a révélé un **bug réel** :
> les dates partaient en tableau `[1966,12,31]` vers la gateway (corrigé via `RestTemplateBuilder`).
> Constante `ConstantesToast.NIVEAU_ERREUR` jamais utilisée : à supprimer ou à employer.
>
> **Point du 2026-10-20 (3ᵉ passage)** : 26 tests verts, PMD 1 (`SecurityConfiguration`), Checkstyle 35 (Javadoc). Les autres 4xx deviennent `AppelRefuseException` (choix documenté et testé). 4 points réglés, 1 nouveau.
>
> **Point du 2026-10-20 (2ᵉ passage)** : bug du 409 corrigé en TDD, 15 tests verts, Checkstyle 35 (Javadoc), PMD 6. 3 points réglés, 5 nouveaux.

- [x] (architecture) `ClientPasserelleTest` : appel abouti, corps transmis, 404 → introuvable, injoignable → indisponible
- [x] (architecture) ⚠️ Réécrire `erreur4xxAutreQue404_leveServiceIndisponible` : il **fige le bug** (une 4xx n'est pas une panne). Remplacer par 409 → `DuplicateException`, rouge avant correction
- [x] (architecture) Traduire le 409 en `DuplicateException` au lieu de « service indisponible »
- [x] (code propre) Réafficher le formulaire avec un message en cas de doublon
- [x] (architecture) `@WebMvcTest` sur `PatientsController` et `PatientDetailController` (tests unitaires écrits ; manquent : formulaire invalide, route, redirection vers `/login` sans session)
- [x] (code propre) Remplacer le test par réflexion `doublon_repondConflit` par une vérification HTTP réelle (MockMvc)
- [x] (code propre) PMD `LooseCoupling` (5, tests) : déclarer `Model` et `RedirectAttributes` plutôt que les implémentations
- [x] (architecture) Décider et justifier le sort des autres 4xx (400, 401, 403), encore traduites en « service indisponible »
- [x] (code propre) Supprimer `ConstantesToast.NIVEAU_ERREUR` (jamais utilisée) ou l'employer
- [x] (code propre) Factoriser le `catch (DuplicateException)` dupliqué dans les deux contrôleurs (optionnel)
- [x] (code propre) Sortir `versFormulaire` du contrôleur *(IA, autorisé — voir journal)*
- [x] (code propre) Champs vides → `null` (`StringTrimmerEditor`) *(IA, autorisé — voir journal)*
- [x] (architecture) `SecurityConfiguration` : identifiants en paramètres de méthode `@Bean`, `@SuppressWarnings` justifié *(IA, autorisé — voir journal)*
- [x] (code propre) Renommer `/app/dashboard` en `/app/patients` (optionnel) *(IA, autorisé — voir journal)*
- [x] (architecture) Tester `PatientApiService` (0 % : les chemins `CHEMIN_PATIENTS + "/" + id` ne sont vérifiés nulle part) — `MockRestServiceServer` vérifierait aussi l'en-tête Basic *(IA, autorisé — voir journal)*
- [x] (présentation) Javadoc FR : 35 manques (classes, méthodes) *(IA, autorisé — voir journal)*

---

# Revue pédagogique — Sprint 2 : démarrage de `ms-notes`

*Revue du 2026-10-20, sur le travail non commité de la branche `feat/sprint1-ms-patients`.*

*Point d'étape du 2026-10-20 : build de nouveau vert (BOM Mongock 5.5.1, `dbchangelogs` exclu de JaCoCo), 46 tests verts, Checkstyle signale 4 Javadoc manquantes dans `ms-notes`. 1 point réglé, 9 restants, dont la branche.*

## En bref

Le squelette de `ms-notes` est bien pensé : un Dockerfile qui reprend le modèle de `ms-patients`,
aucun mot de passe par défaut, un `Clock` injectable pour dater les notes et un `prod-start.sh`
qui attend que la passerelle connaisse vraiment les routes. Mais **le build du projet entier est
cassé** : deux dépendances Mongock n'ont pas de version. Le travail est aussi posé sur la branche
du Sprint 1, alors que la PR #1 n'est pas encore fusionnée.

## Points forts

- **Dockerfile de `ms-notes`** : il copie bien les pom de tous les modules, y compris le sien.
- **Secrets** : `MONGO_INITDB_ROOT_PASSWORD` et `APP_PASSWORD` n'ont pas de valeur par défaut, et
  les tests ont leur propre mot de passe inventé dans `src/test/resources/config/`.
- **`HorlogeConfiguration`** : la date d'une note viendra d'un `Clock` injecté, donc les tests
  pourront fixer l'heure (test déterministe).
- **Profils pour les données de démo** (`MONGOCK_PROFILES`, défaut `prod`) : le même raisonnement
  que `LIQUIBASE_CONTEXTS`, donc facile à expliquer au jury.
- **`prod-start.sh`** : l'attente active des routes (404 = pas encore connue, 401 = service
  atteint) évite de déclarer la pile prête trop tôt, et le tout sans aucun secret.

## Phase 1 — Outillage   ❌ bloquant
### Les dépendances Mongock n'ont pas de version   ·   axe : architecture
- **Constat** : `mongock-springboot-v3` et `mongodb-springdata-v4-driver` ne sont gérés ni par le
  BOM Spring Boot ni par le pom parent. Comme `ms-notes` est déclaré dans les `<modules>` du parent,
  **Maven refuse de lire tout le réacteur** : `./mvnw verify` échoue aussi pour `ms-patients` et
  `ms-webclient`, et même `./mvnw -pl ms-commun install`.
- **Pourquoi ça compte** : on ne commite que du vert, et tes autres modules ne se construisent plus.
- **Exemple** : déclarer une fois le BOM Mongock dans le `<dependencyManagement>` du parent, comme
  pour Testcontainers.
  ```xml
  <dependency>
      <groupId>io.mongock</groupId>
      <artifactId>mongock-bom</artifactId>
      <version>${mongock.version}</version>
      <type>pom</type>
      <scope>import</scope>
  </dependency>
  ```
- **À toi de jouer** : choisir une version de Mongock 5.x compatible Spring Boot 3, l'ajouter dans
  `<properties>`, puis vérifier avec `./mvnw -pl ms-notes -am verify`.

### Le package de migrations Mongock n'existe pas encore   ·   axe : architecture
- **Constat** : `mongock.migration-scan-package` pointe vers `com.mr486.msnotes.dbchangelogs`, qui
  n'existe pas. C'est logique à ce stade, mais il faudra le vérifier au premier démarrage.
- **À toi de jouer** : y écrire le `@ChangeUnit` des 9 notes de démo, marqué `@Profile("demo")`, et
  prouver par un test qu'il ne s'exécute pas avec le profil `prod`.

### Aucun test pour l'instant   ·   axe : code propre
- **Constat** : il n'y a pas encore de dossier `src/test/java`. Les dépendances Testcontainers
  MongoDB sont prêtes.
- **À toi de jouer** : comme prévu, commencer par `@DataMongoTest` + `MongoDBContainer` sur la
  requête « notes d'un patient, de la plus ancienne à la plus récente ».

## Phase 5 — Docker, configuration, Javadoc   ⚠️ à améliorer
### Les 4 autres Dockerfiles ne connaissent pas `ms-notes`   ·   axe : architecture
- **Constat** : `ms-eureka`, `ms-gateway`, `ms-patients` et `ms-webclient` ne copient pas
  `ms-notes/pom.xml`. C'est la même régression qu'au Sprint 1 : leur build Docker échouera avec
  « Child module … does not exist ».
- **À toi de jouer** : ajouter la ligne `COPY ms-notes/pom.xml ms-notes/` dans ces 4 Dockerfiles.

### La pile Docker ne contient ni MongoDB ni `ms-notes`   ·   axe : architecture
- **Constat** : `docker-compose.yml` n'a pas de service `notes-db` (pourtant attendu par
  `application-docker.yml`) ni `ms-notes`. `dist.env` ne déclare pas `MONGO_*` ni `MONGOCK_PROFILES`.
  Conséquences : `dev-start.sh` annonce « Bases démarrées » mais ne lance que PostgreSQL, et
  `prod-start.sh` attendra la route `ms-notes` pendant 5 minutes avant d'échouer.
- **À toi de jouer** : ajouter `notes-db` (image `mongo`, port lié à `127.0.0.1`, volume, mot de
  passe en `:?`), puis `ms-notes` (profil `fullstack`, `SPRING_PROFILES_ACTIVE: docker`, dépendant de
  `notes-db` et d'Eureka), et compléter `dist.env`.

### Petits points   ·   axe : présentation / sécurité
- `application.yml` n'est pas commenté, contrairement à celui de `ms-patients`.
- `MsNotesApplication` et `HorlogeConfiguration` n'ont pas de Javadoc : Checkstyle la réclamera.
- (sécurité) L'application se connecte avec le compte **root** de MongoDB
  (`MONGO_INITDB_ROOT_*`). C'est acceptable pour ce projet, mais un compte limité à `notesdb`
  serait plus propre. C'est un bon point à mentionner au jury (moindre privilège).

## Par où commencer
1. Basculer sur une branche `feat/sprint2-ms-notes` (tes modifications non commitées te suivent).
2. Rendre le build vert : versions Mongock dans le parent.
3. Compléter les 4 Dockerfiles, puis `docker-compose.yml` et `dist.env`.

## Suivi `ms-notes` — à cocher au fil de l'eau

### Phase 1 — Outillage et tests
- [x] (architecture) Créer la branche `feat/sprint2-ms-notes` avant le premier commit du Sprint 2
- [x] (architecture) Gérer la version de Mongock dans le pom parent (BOM) → `./mvnw verify` vert
- [x] (code propre) Premier test Testcontainers MongoDB (fait en `@SpringBootTest` : démarrage + données de démo)
- [x] (architecture) Créer le `@ChangeUnit` des 9 notes de démo sous `@Profile("demo")`

### Phase 5 — Docker, configuration, Javadoc
- [x] (architecture) `COPY ms-notes/pom.xml ms-notes/` dans les Dockerfiles d'Eureka, de la gateway, de ms-patients et de ms-webclient
- [x] (architecture) Services `notes-db` et `ms-notes` dans `docker-compose.yml`
- [x] (sécurité) `MONGO_INITDB_ROOT_USERNAME`, `MONGO_INITDB_ROOT_PASSWORD` et `MONGOCK_PROFILES` dans `dist.env`
- [x] (présentation) Commenter `application.yml` de `ms-notes` — fait (ajustements autorisés)
- [x] (présentation) Javadoc du code ajouté (26 manques Checkstyle) — faite (ajustements autorisés)
- [ ] (sécurité) Optionnel : compte MongoDB limité à `notesdb` plutôt que root

### Point d'étape du 2026-10-20 (soir) — fonctionnalité notes de bout en bout

**Progrès** : le Sprint 2 fonctionne de bout en bout. L'API `ms-notes` (GET/POST), la migration
Mongock des 9 notes sous `@Profile("demo")` (avec son test et sa contre-épreuve), la carte « Notes »
et le formulaire d'ajout du webclient sont en place, ainsi que les Dockerfiles, le compose,
`dist.env` et le README. `./mvnw clean verify` est vert : 57 tests (14 + 4 + 39), SpotBugs 0.
Checkstyle relève 27 Javadoc manquantes (reportées à l'étape 5) et PMD 3 `LooseCoupling` sur
`org.bson.Document` dans le changelog.

**Problème `.env` résolu** (🐞) : `./mvnw -pl ms-notes spring-boot:run` lance l'application depuis
`ms-notes/`, où `optional:file:.env` ne trouve rien. Spring laisse alors le texte
`${MONGO_INITDB_ROOT_PASSWORD}` tel quel comme mot de passe, d'où le « storedKey mismatch » dans les
journaux de MongoDB. Correctif d'outillage dans le pom parent : `workingDirectory` =
`${maven.multiModuleProjectDirectory}`. Vérifié : `ms-notes` et `ms-patients` démarrent sans
variable exportée, et les 9 notes sont en base.

#### Points ouverts
- **L'historique n'affiche pas de date** · axe : architecture. `NoteDto` ne transporte que
  `content` : le mapper perd `createdDate`. Or un historique sans date est difficile à lire pour
  un praticien. Au Sprint 1, tu as séparé ce qui entre (`PatientForm`) de ce qui sort
  (`PatientDto`). La même séparation règle le problème ici : le formulaire ne porte que le
  contenu, la réponse porte le contenu et la date.
- **La fiche patient dépend de `ms-notes`** · axe : architecture. Si `ms-notes` est arrêté,
  `PatientDetailController` échoue et toute la fiche devient inaccessible. Faut-il plutôt afficher
  le patient avec un message « notes indisponibles » ? C'est une décision à prendre et à
  justifier devant le jury (isolement des pannes entre microservices).
- **Il manque les tests de l'API `ms-notes`** · axe : code propre. Il n'y a ni MockMvc (401 sans
  identifiants, 400 sur une note vide, 201) ni test de `NoteService`. Avec `Clock.fixed` et un
  `ArgumentCaptor`, on prouve que la date vient de l'horloge et que le `patientId` est bien celui
  de l'URL.
- **(sécurité) Le contenu d'une note n'a pas de taille maximale** : `@NotBlank` seul accepte un
  texte de plusieurs mégaoctets.
- **Les notes de démo visent les identifiants 1 à 4** : cela suppose que PostgreSQL a attribué ces
  identifiants aux 4 patients de démo, dans cet ordre. C'est acceptable pour une démo, mais à
  savoir expliquer.
- **Petits points** :
  - `LocalDateTime.now()` suivi d'une conversion en `Instant` peut devenir simplement
    `Instant.now()` ;
  - le volume `database-data` gagnerait à s'appeler `notes_data` ;
  - `ms-notes` attend Eureka en `service_started`, alors que la gateway l'attend en
    `service_healthy`.
- **Branche** : tout est encore non commité sur `feat/sprint1-ms-patients`.

- [x] (outillage) Lancer `spring-boot:run` depuis la racine pour que `.env` soit lu
- [x] (architecture) Créer `feat/sprint2-ms-notes` et y commiter le Sprint 2
- [x] (architecture) Exposer la date de création des notes (séparer formulaire et réponse)
- [x] (architecture) Décider du comportement de la fiche patient quand `ms-notes` est indisponible
- [x] (code propre) Tests MockMvc de `NoteController` (401, 400, 201) et test de `NoteService` avec `Clock.fixed`
- [x] (sécurité) `@Size(max = …)` sur le contenu d'une note
- [x] (code propre) Traiter les 3 `LooseCoupling` de PMD dans le changelog

### Ajustements du 2026-10-20 (autorisés), branche `feat/sprint2-ms-notes`

Ces ajustements ont été **écrits par l'IA**, à ta demande (« je t'autorise à apporter les
ajustements »). Ils sont donc à relire et à savoir expliquer :
- `NoteForm` (saisie, 5000 caractères au plus) est séparé de `NoteDto` (texte et date) ;
  l'historique est daté ;
- la fiche reste consultable sans `ms-notes`, avec un avertissement à la place des notes ;
- `@EnableMongock` est déplacé dans `MongockConfiguration`, sans quoi les tests de tranche
  exigeaient une base ;
- tests `NoteControllerTest`, `NoteServiceTest` et `NoteRepositoryTest`, plus un test de
  non-régression sur la date des notes de démo ;
- les notes de démo sont datées dans le passé : avant, elles étaient dans le futur et passaient
  devant une note saisie juste après le démarrage ;
- le volume s'appelle désormais `notes_data` ; Javadoc et YAML commentés ; README complété.

**Vérifié** : `./mvnw clean verify` vert (14 + 12 + 41 = 67 tests), avec Checkstyle, PMD et
SpotBugs à 0. Les 6 images Docker se construisent. Parcours réel dans le webclient (avec Eureka et
la gateway de l'IDE) : connexion, fiche datée, ajout d'une note, puis fiche consultable avec
`ms-notes` arrêté.

**Reste à toi** :
- [ ] Relire les ajustements ci-dessus et remplir Décision / Vérification dans `utilisation-ia.md`
- [x] (optionnel) Supprimer l'ancien volume orphelin : `docker volume rm livrables_database-data`
- [ ] (sécurité, optionnel) Compte MongoDB limité à `notesdb` plutôt que root

---

# Revue pédagogique — Sprint 3 (`ms-risque` et fiche patient)

*Revue du 2026-10-20, branche `feat/sprint3-ms-risque`, code non commité.*

## En bref
Gros pas en avant : le service tourne avec Feign + Eureka, la gestion des erreurs distantes est
propre et testée, et les **4 cas officiels donnent le bon niveau**. Mais ils le donnent en partie
**par chance** : le comptage des déclencheurs est faux dans le cas général, « Fumeuse » n'est pas
détecté, et le format renvoyé diffère du PDF du Sprint 3. Côté intégration, le module n'est pas
encore dans le pom parent, Docker et les scripts.

Mesures : `ms-risque` 23 tests verts, PMD 1, SpotBugs 0, Checkstyle 24 (Javadoc absente) ;
`ms-webclient` 43 tests verts, Checkstyle 2 (Javadoc de `EvaluationApiService`).

## Points forts
- Clients **Feign** déclaratifs (`PatientClient`, `NoteClient`) résolus par **Eureka** : choix
  défendable (appel interne direct, sans repasser par la gateway).
- Traduction des erreurs Feign en exceptions de `ms-commun` (404 → introuvable, 4xx → refusé,
  reste → indisponible), **testée** dans `PatientServiceTest` et `NotesServiceTest`.
- **Horloge injectée** (`HorlogeConfiguration` + `Clock.fixed`) et test de l'anniversaire non
  encore passé : exactement la bonne démarche.
- Seuils en **constantes nommées**, trous des règles **tranchés et testés** (30 ans pile → règles
  des plus de 30 ans ; jeune avec 2 déclencheurs → None).
- Mot de passe sans valeur par défaut, `config/application.yml` de test qui se superpose.
- Webclient : `EvaluationApiService` réutilise `ClientPasserelle`, avec un `@RestClientTest` qui
  vérifie URL, en-tête Basic et texte brut.

## Phase 3 — Algorithme (cœur du sprint)   ❌ à corriger
### On compte des occurrences par note, pas des termes distincts   ·   axe : code propre
- **Constat** : `compteDeclencheurs` ajoute 1 pour chaque couple (note, terme). Un même terme
  présent dans deux notes compte deux fois.
- **Pourquoi ça compte** : le patient 4 obtient 8 au lieu de 7 (« Réaction » est dans deux notes),
  le patient 2 obtient 3 au lieu de 2 (« anormale » deux fois). Le bon niveau sort quand même, mais
  un patient de 40 ans dont six notes disent « anormal » passerait **In Danger** avec un seul terme.
- **Exemple** : la question à se poser est « *quels* termes apparaissent dans le dossier ? »,
  pas « combien de fois ». Une structure qui ignore les doublons répond à la première.
- **À toi de jouer** : écris d'abord le test rouge (même terme dans trois notes → un seul
  déclencheur), puis corrige.

### « Fumeuse » n'est pas détecté   ·   axe : code propre
- **Constat** : la liste contient `fumeur` ; « fumeuse » ne contient pas cette chaîne.
- **Pourquoi ça compte** : une patiente fumeuse perd un déclencheur. Le cahier des charges cite
  « Fumeur, Fumeuse » comme **un seul** terme : attention à ne pas le compter deux fois non plus.
- **À toi de jouer** : un test « Fumeuse » et un test « fumeur et fumeuse dans le même dossier → 1 ».
  Ton choix du radical `vertige` (qui couvre « Vertiges ») est la bonne piste : documente-le.

### Le format renvoyé n'est pas celui du PDF du Sprint 3   ·   axe : architecture
- **Constat** : `NiveauRisque` renvoie `In Danger` et `Early onset` (libellés du résumé des
  besoins) ; le PDF des résultats attendus écrit `InDanger` et `EarlyOnset`.
- **Pourquoi ça compte** : le guide insiste sur « le format de renvoi attendu ». Les deux sources
  divergent : c'est **une décision à prendre et à justifier**, pas un oubli à laisser au hasard.
- **À toi de jouer** : choisis, écris-le dans le README et aligne le gabarit du webclient (il
  compare des chaînes en dur).

### Les tests ne couvrent ni les 4 cas officiels ni les doublons   ·   axe : code propre
- **Constat** : 14 tests bien nommés, mais construits avec des notes synthétiques (un terme
  chacun, une seule note). Aucun ne rejoue les vraies notes des patients 1 à 4, aucun ne met le
  même terme dans deux notes : c'est pour ça que le bug ci-dessus passe au vert.
- **À toi de jouer** : un `@ParameterizedTest` avec les 4 cas officiels (âge, genre, textes réels
  → niveau attendu). Il règle aussi l'avertissement PMD `TooManyMethods` sur la classe de test.

### Petits points   ·   axe : code propre / architecture
- L'algorithme vit dans des méthodes privées du service : il n'est testable qu'à travers deux
  mocks. Une classe pure (âge, genre, textes → niveau) serait plus simple à tester.
- `note.getContent()` peut être `null` (lecture d'un document Mongo incomplet) : `NullPointerException`.
- `NiveauRisque.getLibelle()` écrit à la main : `@Getter` Lombok, comme ailleurs.
- `FeignSecurityConfiguration` est annotée `@Configuration` : elle s'applique donc à **tous** les
  clients Feign, ce qui rend `configuration = …` sur les clients redondant. Savoir l'expliquer.
- Pas de test du contrôleur (401 sans identifiants, 404 patient inconnu, `text/plain`).

## Phase 5 — Intégration, Javadoc, configuration   ❌ manquant
- ✅ `ms-risque` ajouté aux `<modules>` : `./mvnw clean verify` le construit (8 modules verts, 106 tests). Conséquence vérifiée : `docker build` d'`ms-eureka` échoue (« Child module /build/ms-risque does not exist »)
- Seul le Dockerfile du webclient copie `ms-risque/pom.xml` : les 4 autres (eureka, gateway,
  patients, notes) échoueront dès que le module sera déclaré.
- `docker-compose.yml`, `prod-start.sh`, `dev-start.sh`, README : rien pour `ms-risque`.
- Fichiers de l'initializr à supprimer : `ms-risque/mvnw*`, `HELP.md`, `.gitignore`, `.gitattributes`.
- **Fiche patient** : `evaluationApiService.evalueLeRisque(id)` est appelé sans filet. Si
  `ms-risque` tombe, **toute la fiche** part en page d'erreur, alors qu'on a fait l'inverse pour
  les notes au Sprint 2. Même principe à appliquer, avec son test MVC.
- Javadoc absente sur tout `ms-risque` et sur `EvaluationApiService` ; `application.yml` non commenté ;
  `spring.web.resources.add-mappings: false` comme dans `ms-patients`.

## Par où commencer
1. Test rouge sur les doublons, puis correction du comptage ; ajouter Fumeuse.
2. Test paramétré des 4 cas officiels et décision sur le format (`InDanger` ou `In Danger`).
3. Intégration : module parent, 6 Dockerfiles, compose, scripts ; fiche tolérante à la panne.

## Suivi `ms-risque` — à cocher au fil de l'eau

### Phase 3 — Algorithme et tests
- [x] (code propre) Compter les termes **distincts** du dossier, test rouge d'abord
- [x] (code propre) Détecter « Fumeuse », sans compter Fumeur + Fumeuse deux fois
- [ ] (architecture) Trancher le format `InDanger` / `In Danger` et le documenter
- [x] (code propre) Test paramétré des 4 cas officiels avec les vrais textes des notes
- [ ] (code propre) Protéger le comptage contre un contenu de note `null`
- [ ] (code propre) `@Getter` Lombok sur `NiveauRisque`
- [ ] (architecture) Option : extraire l'algorithme dans une classe pure
- [ ] (code propre) Tests MVC du contrôleur : 401, 404, `text/plain`

### Phase 5 — Intégration, Javadoc, configuration
- [x] (architecture) Ajouter `ms-risque` aux `<modules>` du pom parent
- [x] (architecture) `COPY ms-risque/pom.xml` dans les 4 Dockerfiles restants
- [x] (architecture) Service `ms-risque` dans `docker-compose.yml`, `prod-start.sh`, `dev-start.sh`
- [ ] (code propre) Supprimer `mvnw*`, `HELP.md`, `.gitignore`, `.gitattributes` du module
- [x] (architecture) Fiche patient consultable quand `ms-risque` est arrêté, avec son test
- [ ] (présentation) Javadoc FR de `ms-risque` et de `EvaluationApiService`
- [ ] (présentation) `application.yml` commenté, README complété

### Point d'étape du 2026-10-20 (23 h) — algorithme corrigé, pile complète testée
*Depuis la revue : 6 points réglés, 9 restants, 1 nouveau (important).*

Mesures : `./mvnw clean verify` vert sur les 8 modules (`ms-risque` 30 tests, `ms-webclient` 44) ;
7 images construites ; `./prod-start.sh` démarre la pile et valide la route `ms-risque`.

- ✅ Comptage des termes **distincts** (graphies regroupées : `List<List<String>>`), Fumeuse
  reconnue, test des doublons qui aurait rougi avec l'ancien code, test paramétré des 4 cas officiels.
- ✅ Vérifié en vrai, via la gateway : patients 1 à 4 → None, Borderline, In Danger, Early onset
  (`text/plain`), patient 99 → 404 JSON, sans identifiants → 401. Fiches du webclient : bon badge
  et bonne couleur pour les 4.
- ⚠️ Format toujours `In Danger` / `Early onset` : décision à écrire dans le README.

### 🆕 Un service arrêté fait passer **toute la fiche** en « Page introuvable »   ·   axe : architecture
- **Constat** (reproduit sur la pile Docker) : juste après l'arrêt de `ms-risque`, la fiche affiche
  bien l'avertissement. Quelques secondes plus tard, elle renvoie **404 « Page introuvable »**.
  Même chose en arrêtant `ms-notes` : le défaut date donc du Sprint 2, il n'avait pas été vu
  parce qu'on avait testé juste après l'arrêt.
- **Pourquoi** : la gateway route par découverte. Quand le service se désinscrit d'Eureka, sa
  route **disparaît** et la gateway répond elle-même **404**. `ClientPasserelle` traduit tout 404
  en `ResourceNotFoundException`, ce qui donne la page 404 au lieu de l'avertissement.
- **Indice** : le 404 de la gateway n'a pas de champ `microserviceName`, celui d'un microservice si.
- **À toi de jouer** : deux pistes, à choisir et à justifier.
  - Côté gateway, déclarer des routes explicites `lb://ms-risque` : sans instance, le
    load-balancer répond **503**, qui devient bien une indisponibilité. C'est aussi la « mise à
    jour de la config Gateway » demandée par le guide.
  - Côté webclient, ne traiter un 404 comme « introuvable » que s'il vient du microservice.
  - Dans les deux cas, un test qui simule le 404 de la gateway.

### Suivi — ajouts du 2026-10-20 (23 h)
- [ ] (architecture) Un service arrêté ne doit plus transformer la fiche en 404 (risque **et** notes)
