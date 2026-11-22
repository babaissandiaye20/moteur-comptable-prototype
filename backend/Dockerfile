# Étape 1 : compiler le JAR avec Maven
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# Étape 2 : image légère qui exécute le JAR
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/moteur-comptable-*.jar app.jar
# La plateforme fournit PORT ; la mémoire de la JVM s'adapte au conteneur.
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
