# Moteur comptable · back

Spring Boot 4 (Java 21). Un événement métier + une recette + un mapping = une écriture équilibrée.

- Code : `src/main/java/com/dmplus/moteurcomptable/` rangé par fonctionnalité (`parametrage`, `ecriture`, `shared`).
- Documentation : `docs/` (schéma des tables, chaque colonne expliquée, paramétrage et fonctionnalités, critères d'acceptation, grille de retours Excel).
- Build GitHub : `.github/workflows/build.yml` (compilation, tests, image Docker).

## Lancer en local
```bash
mvn package && PORT=8091 MOTEUR_DONNEES_DEMO=true java -jar target/moteur-comptable-*.jar   # base H2 en mémoire + exemple
```

## Déployer sur Render
Docker (Dockerfile multi-étapes). Variables :

| Variable | Valeur |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<hôte-neon>/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | identifiants Neon (secrets) |
| `MOTEUR_CORS_ORIGINES` | adresse du front, ex. `https://*.vercel.app` |
| `MOTEUR_DONNEES_DEMO` | `false` en production (base vide) |

Health check : `/api/sante`. Réveil : cron Render (`render.yaml`, payant) ou workflow GitHub `reveil-render.yml` (gratuit, variable `BACK_URL`).
