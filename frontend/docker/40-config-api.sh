#!/bin/sh
# Génère config.js avec l'adresse du back fournie par la variable MOTEUR_API.
set -e
printf 'window.MOTEUR_API = "%s";\n' "${MOTEUR_API%/}" > /usr/share/nginx/html/config.js
echo "config.js : MOTEUR_API=${MOTEUR_API%/}"
