# ---------- Étape 1 : préparer les fichiers du site ----------
FROM alpine:3.20 AS build
WORKDIR /site
COPY index.html tables.html tables.svg styles.css app.js favicon.svg retours-financier.xlsx ./
# config.js est généré au démarrage (étape 2) : on retire la version locale
RUN rm -f config.js

# ---------- Étape 2 : servir le site avec nginx ----------
FROM nginx:1.27-alpine
COPY --from=build /site /usr/share/nginx/html
# nginx remplace ${PORT} au démarrage (Render impose son port)
COPY docker/default.conf.template /etc/nginx/templates/default.conf.template
# Écrit config.js à partir de la variable MOTEUR_API (adresse du back)
COPY docker/40-config-api.sh /docker-entrypoint.d/40-config-api.sh
RUN chmod +x /docker-entrypoint.d/40-config-api.sh
ENV PORT=8080 MOTEUR_API=http://localhost:8091
EXPOSE 8080
