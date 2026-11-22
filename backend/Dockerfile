# ---------- Étape 1 : compiler et tester avec Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Les dépendances d'abord : cette couche reste en cache tant que pom.xml ne change pas
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# ---------- Étape 2 : image d'exécution légère (JRE seul, sans Maven ni sources) ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S moteur && adduser -S moteur -G moteur
COPY --from=build /app/target/moteur-comptable-*.jar app.jar
USER moteur
# Render fournit PORT ; la mémoire de la JVM s'adapte au conteneur
ENV PORT=8080 JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT}/api/sante" || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
