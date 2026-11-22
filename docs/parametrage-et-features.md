# Paramétrage livré et fonctionnalités

## Organisation du code

```
com.dmplus.moteurcomptable
├── parametrage   controller · service · repository · entity · dto · config (données de démo)
├── ecriture      controller · service · repository · entity · dto
└── shared        config (CORS, audit) · controller (santé) · entity (traçabilité, suppression douce) · exception
```

Architecture en couches pour le prototype : controller → service → repository → base. Le passage en hexagonal se fera après validation : `GenerateurEcriture` et les règles des entités iront dans le domaine, les repositories deviendront des adapters, `ResolveurCompte` est déjà un port.

## Paramétrage livré pour la démonstration

Créé au premier démarrage par `DonneesDemo`, à travers `ParametrageService` (donc avec les mêmes contrôles qu'un paramétrage manuel). Comptes donnés à titre d'exemple.

| Élément | Contenu |
|---|---|
| Opérations | ACHAT, ENCAISSEMENT, REMBOURSEMENT |
| Journaux | PLA (placements), REV (revenus des placements) |
| Rubriques | R01 valeur d'inscription (actif), R04 compte de règlement (actif), R05 revenus (produit), R06 décote (produit), R08 frais (charge), R16 coupon couru acheté (actif) |
| Comptes | 2745, 2746, 2764, 5211, 6311, 7722 |
| Mapping | R01 + OBLIGATION + ETAT → 2745 ; R01 + OBLIGATION + SOCIETE → 2746 ; R04 → 5211 ; R05 et R06 → 7722 ; R08 → 6311 ; R16 → 2764 |
| Événements | ACQ_OBLIGATION (ACHAT, PLA), COUPON_OBLIGATION (ENCAISSEMENT, REV), REMB_OBLIGATION (REMBOURSEMENT, PLA) |

Recettes :

| Événement | Lignes |
|---|---|
| ACQ_OBLIGATION | R01 D `nominal` · R16 D `coupon_couru` (facultatif) · R08 D `frais` (facultatif) · R06 C `decote` (facultatif) · R04 C `total_paye` |
| COUPON_OBLIGATION | R04 D `coupon` · R05 C `coupon` |
| REMB_OBLIGATION | R04 D `nominal` · R01 C `nominal` |

## Fonctionnalités et API

### Paramétrage — `/api/parametrage`
| Fonctionnalité | Méthode et URL | Contrôles |
|---|---|---|
| Tout lire | `GET /api/parametrage` | — |
| Créer une opération | `POST /operations` | Code obligatoire et unique |
| Créer un journal | `POST /journaux` | Code unique |
| Créer un événement | `POST /evenements` | Opération et journal existants, journal ouvert |
| Créer une rubrique | `POST /rubriques` | Nature obligatoire |
| Créer un compte | `POST /comptes` | Code unique |
| Créer un mapping | `POST /mappings` | Rubrique existante, compte ouvert, combinaison unique |
| Créer une recette | `POST /recettes` | Événement et rubriques existants ; version suivante automatique ; publication si débit et crédit présents |
| Publier une recette | `POST /recettes/{id}/publier` | Brouillon seulement |
| Retirer une recette | `POST /recettes/{id}/retirer` | Les écritures passées la gardent |
| Supprimer (doux) | `DELETE /operations/{code}`, `/journaux/{code}`, `/evenements/{code}`, `/rubriques/{code}`, `/comptes/{code}`, `/mappings/{id}` | Refusé si l'élément est encore utilisé |

### Génération et validation — `/api`
| Fonctionnalité | Méthode et URL | Résultat |
|---|---|---|
| Recevoir un événement | `POST /api/evenements` | 201 écriture créée · 200 doublon (écriture existante renvoyée) · 409 collision · 422 règle métier non respectée |
| Lister les écritures | `GET /api/ecritures` | Les plus récentes d'abord, avec totaux |
| Détail d'une écriture | `GET /api/ecritures/{id}` | Lignes, compte, débit, crédit |
| Valider | `POST /api/ecritures/{id}/valider` | Statut VALIDEE + numéro ; refusé pour le créateur (4 yeux) |

### Technique — `shared`
| Fonctionnalité | URL | Rôle |
|---|---|---|
| Santé | `GET /api/sante` | Répond `{"statut":"ok"}`. Appelé toutes les 10 minutes par un cron pour que l'hébergeur ne mette pas l'application en veille |

En-têtes : `X-Utilisateur` donne l'utilisateur courant (traçabilité et 4 yeux). Erreurs : toujours `{ "code": "...", "message": "..." }`.

## Exemple d'événement envoyé par SIGAP

```json
{
  "source": "SIGAP",
  "eventId": "ACQ-0001",
  "evenement": "ACQ_OBLIGATION",
  "dateValeur": "2026-11-23",
  "classeActif": "OBLIGATION",
  "categorie": "ETAT",
  "referenceTitre": "OAT-SN-2030",
  "utilisateur": "gestionnaire",
  "montants": { "nominal": 2000000, "frais": 20000, "total_paye": 2020000 }
}
```

Écriture produite : 2745 débit 2 000 000 · 6311 débit 20 000 · 5211 crédit 2 020 000.

## Hors du prototype

Contre-passation, périodes et clôture, extourne automatique, export vers le logiciel comptable, contraintes d'équilibre et d'immutabilité doublées en base (triggers), rejet d'un lot entier, authentification.
