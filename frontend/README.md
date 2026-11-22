# Moteur comptable · front

Page statique (HTML, CSS, JS) qui appelle l'API du back.

- `config.js` : adresse du back (à fixer avec l'URL Render).
- `tables.html` : les tables expliquées, pour le lead.

## Déployer sur Vercel
Importer le dépôt, Framework Preset : *Other*, aucune commande de build.

## Ou en Docker (multi-étapes, nginx)
```bash
docker build -t moteur-front . && docker run -p 8080:8080 -e MOTEUR_API=https://<back>.onrender.com moteur-front
```
