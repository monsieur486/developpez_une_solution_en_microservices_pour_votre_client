# Creedengo — exemple de mise en place sur un module

Démonstration réelle des règles d'écoconception **[Creedengo](https://creedengo.org/)**
(ex-ecoCode, projet du collectif Green Code Initiative) appliquées au module
**ms-risque**, dans le prolongement de la section « Green Code » du README.
Creedengo fournit des règles SonarQube qui détectent automatiquement des motifs
de code énergivores.

## Mise en place (reproduite ici, à reproduire telle quelle)

1. **SonarQube éphémère avec le plugin Java** (aucune installation permanente) :

   ```bash
   curl -sL -o creedengo-java-plugin-2.2.0.jar \
     "https://github.com/green-code-initiative/creedengo-java/releases/download/2.2.0/creedengo-java-plugin-2.2.0.jar"
   docker run -d --name sonar-creedengo -p 9999:9000 \
     -v "$PWD/creedengo-java-plugin-2.2.0.jar:/opt/sonarqube/extensions/plugins/creedengo-java-plugin-2.2.0.jar:ro" \
     sonarqube:community
   # attendre que http://localhost:9999 réponde, se connecter (admin/admin),
   # puis créer un jeton d'analyse (Mon compte → Sécurité)
   ```

2. **Profil qualité** : copier « Sonar way » (Java), y **activer les règles du
   dépôt `creedengo-java`** (17 règles dans la version 2.2.0) et définir ce
   profil par défaut.

3. **Analyse du module** :

   ```bash
   cd ms-risque
   ./mvnw verify org.sonarsource.scanner.maven:sonar-maven-plugin:5.0.0.4389:sonar \
     -Dsonar.host.url=http://localhost:9999 \
     -Dsonar.token=<jeton> \
     -Dsonar.projectKey=ms-risque
   ```

4. **Nettoyage** : `docker rm -f sonar-creedengo` (l'exercice ne laisse rien
   derrière lui).

## Résultats obtenus sur ms-risque

L'analyse remonte **46 détections Creedengo** (sur 71 issues au total, le reste
venant des règles Sonar classiques) :

| Règle | Occurrences | Ce qu'elle détecte |
|---|---|---|
| `GCI82` | 44 | Variable locale jamais réassignée → la déclarer `final` (aide le compilateur/JIT à optimiser) |
| `GCI2` | 1 | Chaîne de `if/else if` remplaçable par un `switch` (`tableswitch` plus efficace que des comparaisons successives) — `EvaluationService.determineNiveau` |
| `GCI76` | 1 | Collection statique (risque de rétention mémoire sur la durée de vie de l'application) — `EvaluationService.TERMES_DECLENCHEURS` |

## Lecture critique des résultats

- **`GCI82` (final)** : gain unitaire minime mais gratuit et massif (44 sites) ;
  automatisable par l'IDE en une passe. Bon candidat à une correction en lot.
- **`GCI2` (switch)** : pertinent sur le principe ; dans notre cas la chaîne de
  `if` de `determineNiveau` teste des **conditions composées** (âge, genre,
  seuils), pas une valeur unique — la transformation en `switch` ne serait ni
  directe ni forcément plus lisible. À arbitrer, pas à appliquer aveuglément.
- **`GCI76` (collection statique)** : **faux positif dans notre cas** — la règle
  vise les collections statiques *mutables* qui grossissent sans borne ;
  `TERMES_DECLENCHEURS` est un `List.of(...)` **immuable** de 11 éléments,
  exactement le bon usage. À neutraliser avec justification
  (`@SuppressWarnings` ou marquage « accepté » dans SonarQube).

C'est l'enseignement principal de l'exercice : l'outil **objective** la
démarche Green Code (des critères mesurables, comme Checkstyle pour le style),
mais chaque détection demande un **jugement humain** — les corrections
automatiques aveugles ne sont pas de l'écoconception.

## Pour généraliser (proposition)

- Étendre l'analyse aux autres modules (même commande, un `projectKey` par
  module) et corriger par lots : d'abord les `GCI82` (mécanique), puis les cas
  à arbitrer.
- Adosser un SonarQube permanent (conteneur dans le `docker-compose`, profil
  qualité versionné) et intégrer l'analyse au rituel de vérification
  (`./mvnw verify` + analyse avant chaque livraison), avec un seuil bloquant
  sur les nouvelles détections uniquement (« Clean as You Code »).
- Compléter par la mesure d'usage réel (Actuator/Micrometer) et un score
  [EcoIndex](https://www.ecoindex.fr/) sur l'interface web — Creedengo couvre
  le code, pas le service complet (cf. RGESN, section Green Code du README).
