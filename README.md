# Moteur comptable · prototype

Un événement métier + une recette + un mapping = une écriture équilibrée.

- `backend/` : Spring Boot 4 (Java 21), base PostgreSQL (Neon) ou H2 en local.
- `frontend/` : page statique (HTML, CSS, JS) qui appelle l'API.
- `docs/` : schéma des tables (`tables.puml`), chaque colonne expliquée (`tables.md`), paramétrage et fonctionnalités, grille de retours Excel.

## Lancer en local
```bash
cd backend && mvn package -DskipTests && PORT=8091 java -jar target/moteur-comptable-*.jar   # base H2 en mémoire
cd frontend && python3 -m http.server 5501                                                   # puis http://127.0.0.1:5501
```

## Variables d'environnement du back
| Variable | Exemple |
|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://<hôte-neon>/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | identifiants Neon |
| `MOTEUR_CORS_ORIGINES` | adresse du front, ex. `https://moteur-comptable.vercel.app` |
| `MOTEUR_DONNEES_DEMO` | `true` pour charger l'exemple au premier démarrage |

Le front lit l'adresse du back dans `frontend/config.js`.
