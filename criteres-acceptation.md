# Critères d'acceptation du prototype

Format : **Étant donné** (le contexte), **quand** (l'action), **alors** (le résultat attendu).
Statut : ✅ vérifié par une requête réelle le 22/11/2026 · ⬜ à vérifier dans la page · ❌ hors prototype.

## A. Paramétrage

### A1. Opération
| # | Critère | Statut |
|---|---|---|
| A1.1 | Quand je crée l'opération ACHAT, alors elle apparaît dans la liste | ✅ (données de démo) |
| A1.2 | Étant donné qu'ACHAT existe, quand je la recrée, alors c'est refusé : « existe déjà » (409) | ⬜ |
| A1.3 | Quand je crée une opération sans code, alors c'est refusé : « code obligatoire » | ⬜ |

### A2. Journal
| # | Critère | Statut |
|---|---|---|
| A2.1 | Quand je crée le journal PLA avec son code Sage PLC, alors il apparaît dans la liste | ✅ |
| A2.2 | Un journal en double est refusé | ⬜ |

### A3. Événement
| # | Critère | Statut |
|---|---|---|
| A3.1 | Quand je crée ACQ_OBLIGATION rattaché à ACHAT et au journal PLA, alors il est créé | ✅ |
| A3.2 | Quand je le rattache à une opération qui n'existe pas, alors c'est refusé (404) | ✅ |
| A3.3 | Quand je le rattache à un journal inexistant ou fermé, alors c'est refusé | ⬜ |
| A3.4 | Un événement a toujours exactement une opération et un journal (impossible d'en mettre deux) | ✅ (une seule colonne) |

### A4. Rubrique
| # | Critère | Statut |
|---|---|---|
| A4.1 | Quand je crée R01 avec la nature ACTIF, alors elle est créée | ✅ |
| A4.2 | Une rubrique sans nature est refusée | ⬜ |

### A5. Compte
| # | Critère | Statut |
|---|---|---|
| A5.1 | Quand je crée le compte 2745, alors il est créé et ouvert | ✅ |
| A5.2 | Un compte en double est refusé | ⬜ |

### A6. Mapping
| # | Critère | Statut |
|---|---|---|
| A6.1 | Quand je crée R01 + OBLIGATION + ETAT → 2745, alors la ligne est créée | ✅ |
| A6.2 | Quand je pointe vers un compte inexistant, alors c'est refusé | ✅ |
| A6.3 | Quand je pointe vers un compte fermé, alors c'est refusé | ⬜ |
| A6.4 | Une deuxième ligne pour la même combinaison est refusée | ⬜ |
| A6.5 | Une classe ou une catégorie laissée vide vaut « * » (toutes) | ✅ |

### A7. Recette
| # | Critère | Statut |
|---|---|---|
| A7.1 | Quand je crée une recette avec au moins un débit et un crédit et que je la publie, alors elle est PUBLIEE en version 1 | ✅ |
| A7.2 | Quand je publie une recette sans ligne au crédit (ou au débit), alors c'est refusé | ✅ |
| A7.3 | Quand je crée une recette pour un événement qui en a déjà une, alors elle prend la version suivante | ✅ (v2) |
| A7.4 | Une recette publiée ne peut plus recevoir de ligne : « créez une nouvelle version » | ✅ (règle dans l'entité) |
| A7.5 | Une ligne avec une rubrique inexistante est refusée | ⬜ |

## B. Génération des écritures

| # | Critère | Statut |
|---|---|---|
| B1 | Étant donné la recette d'achat publiée, quand SIGAP envoie un achat d'obligation d'État (nominal 2 000 000, frais 20 000, total 2 020 000), alors l'écriture est : 2745 D 2 000 000 · 6311 D 20 000 · 5211 C 2 020 000, statut PROPOSEE | ✅ |
| B2 | Le même événement pour une obligation de **société** utilise le compte 2746 (le mapping le plus précis gagne) | ✅ |
| B3 | Quand un montant facultatif (frais, coupon couru) est absent ou à 0, alors sa ligne n'est pas créée | ✅ |
| B4 | Quand un montant obligatoire est absent, alors c'est refusé : « montant manquant » | ✅ |
| B5 | Quand débit ≠ crédit, alors c'est refusé et **rien n'est enregistré** | ✅ |
| B6 | Quand un montant est négatif, alors c'est refusé | ⬜ |
| B7 | Quand le même événement est renvoyé, alors aucune nouvelle écriture : on reçoit l'écriture existante (200) | ✅ |
| B8 | Quand le même identifiant arrive avec un contenu différent, alors c'est refusé et rien n'est écrasé (409) | ✅ |
| B9 | Quand l'événement n'est pas paramétré, alors c'est refusé (404) | ✅ |
| B10 | Quand aucun mapping ne couvre la rubrique, alors c'est refusé avec la combinaison exacte à ajouter | ✅ |
| B11 | Quand aucune recette publiée n'est en vigueur à la date, alors c'est refusé | ⬜ |
| B12 | L'écriture garde la recette et la version utilisées ; une nouvelle version ne change pas les écritures passées | ✅ |
| B13 | Le compte est recopié dans la ligne : un mapping modifié plus tard ne change pas l'écriture | ✅ (par construction) |
| B14 | Le moteur ne calcule aucun montant : il range ceux reçus | ✅ (par construction) |

## C. Validation et numérotation

| # | Critère | Statut |
|---|---|---|
| C1 | Quand le créateur valide sa propre écriture, alors c'est refusé (4 yeux) | ✅ |
| C2 | Quand un autre utilisateur valide, alors le statut passe à VALIDEE et le numéro est attribué | ✅ |
| C3 | Les numéros suivent par journal et par exercice, sans trou : PLA-2026-0001, PLA-2026-0002, REV-2026-0001 | ✅ |
| C4 | Une écriture validée ne peut plus être validée ni modifiée | ✅ |
| C5 | Une validation sans nom d'utilisateur est refusée | ⬜ |

## D. Consultation
| # | Critère | Statut |
|---|---|---|
| D1 | La liste montre les écritures, les plus récentes d'abord, avec leurs totaux | ✅ |
| D2 | Le détail montre chaque ligne : rubrique, compte, débit ou crédit | ✅ |

## E. Hors du prototype (à annoncer au lead)
| Fonction | Pourquoi plus tard |
|---|---|
| Contre-passation d'une écriture validée | Prochaine étape : écriture inverse liée à l'originale |
| Périodes et clôture | Bloquer une écriture dans un mois clôturé |
| Extourne automatique des ICNE | Liée aux périodes |
| Export vers Sage | Après le choix du format |
| Contraintes d'équilibre et d'immutabilité **en base** (triggers) | Le prototype les garantit dans le code ; la version finale les doublera en base |
| Rejet d'un lot entier | Le prototype traite un événement à la fois |
