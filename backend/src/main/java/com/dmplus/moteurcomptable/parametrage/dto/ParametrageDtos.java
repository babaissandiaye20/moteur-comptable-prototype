package com.dmplus.moteurcomptable.parametrage.dto;

import com.dmplus.moteurcomptable.parametrage.entity.Nature;
import com.dmplus.moteurcomptable.parametrage.service.ParametrageService.LigneSaisie;

import java.time.LocalDate;
import java.util.List;

/** Les corps des requêtes de paramétrage. */
public final class ParametrageDtos {

    private ParametrageDtos() {
    }

    public record OperationDto(String code, String libelle) {}
    public record JournalDto(String code, String libelle, String codeExterne) {}
    public record EvenementDto(String code, String libelle, String operationCode, String journalCode) {}
    public record RubriqueDto(String code, String libelle, Nature nature) {}
    public record CompteDto(String code, String libelle) {}
    public record MappingDto(String rubriqueCode, String classeActif, String categorie, String compteCode) {}
    public record RecetteDto(String evenementCode, LocalDate dateEffet, String utilisateur,
                             Boolean publier, List<LigneSaisie> lignes) {}
    public record UtilisateurDto(String utilisateur) {}
}
