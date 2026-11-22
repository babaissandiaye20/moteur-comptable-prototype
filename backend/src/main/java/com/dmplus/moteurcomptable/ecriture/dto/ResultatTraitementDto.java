package com.dmplus.moteurcomptable.ecriture.dto;

/** statut : TRAITE (écriture créée) ou DOUBLON (déjà reçu, on renvoie l'écriture existante). */
public record ResultatTraitementDto(String statut, String message, EcritureDto ecriture) {
}
