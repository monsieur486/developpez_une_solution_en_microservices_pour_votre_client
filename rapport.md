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
- [ ] (architecture) `GlobalExceptionHandler` (9 méthodes) : justifier ou découper
- [x] (code propre) Retirer `@Repository` de `PatientRepository` *(IA, autorisé — voir journal)*
- [x] (code propre) Contraindre les valeurs de `gender` *(IA, autorisé — voir journal)*

### Phase 5 — Validation, configuration, README
- [x] (architecture) En-tête `--liquibase formatted sql` + `--changeset` sur `001` et `002` *(généré par l'IA à ma demande, voir journal)*
- [x] (sécurité) `@Past` sur `birthDate`, `@Size(max = 255)` sur les chaînes de `PatientForm` *(IA, autorisé — voir journal)*
- [ ] (architecture) Mettre à jour la description de `ms-commun` + justifier le partage des DTO
- [ ] (architecture) Isoler les données de test (context Liquibase)
- [ ] (sécurité) Retirer les mots de passe par défaut des `application*.yml` et du `docker-compose.yml`
- [ ] (code propre) Réduire `application-docker.yml` aux seules différences avec `application.yml`
