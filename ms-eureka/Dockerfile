# Étape de construction : compile le module.
# Le contexte de build est la racine du dépôt, car le module hérite du pom parent :
# construire depuis son seul dossier échouerait.
FROM maven:3.9.11-eclipse-temurin-17 AS builder
WORKDIR /build

# Les descripteurs d'abord : tant qu'ils ne changent pas, Docker réutilise le cache des
# dépendances téléchargées, ce qui évite de tout retélécharger à chaque modification de code.
COPY pom.xml .
# Le pom parent déclare ms-commun : son descripteur doit exister, même s'il n'est pas construit.
COPY ms-commun/pom.xml ms-commun/
COPY ms-eureka/pom.xml ms-eureka/
COPY ms-gateway/pom.xml ms-gateway/
COPY ms-patients/pom.xml ms-patients/
COPY ms-notes/pom.xml ms-notes/
COPY ms-risque/pom.xml ms-risque/
COPY ms-webclient/pom.xml ms-webclient/
RUN mvn -B -pl ms-eureka -am dependency:go-offline -DskipTests

COPY config/ config/
COPY ms-eureka/src ms-eureka/src
# Les analyses qualité sont inutiles ici : elles tournent au build local et produisent
# leurs rapports à ce moment-là.
RUN mvn -B -pl ms-eureka -am package -DskipTests \
    -Dcheckstyle.skip -Dpmd.skip -Dcpd.skip -Dspotbugs.skip -Djacoco.skip

# Étape d'exécution : une image JRE suffit à lancer l'application et réduit la surface
# exposée par rapport à un JDK complet (ni compilateur ni outils de développement).
FROM eclipse-temurin:17-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
EXPOSE 8761
COPY --from=builder /build/ms-eureka/target/*.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
