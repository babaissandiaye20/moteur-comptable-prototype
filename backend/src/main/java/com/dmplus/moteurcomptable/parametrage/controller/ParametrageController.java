package com.dmplus.moteurcomptable.parametrage.controller;

import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.CompteDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.EvenementDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.JournalDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.MappingDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.OperationDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.RecetteDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.RubriqueDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.UtilisateurDto;
import com.dmplus.moteurcomptable.parametrage.entity.Compte;
import com.dmplus.moteurcomptable.parametrage.entity.Evenement;
import com.dmplus.moteurcomptable.parametrage.entity.Journal;
import com.dmplus.moteurcomptable.parametrage.entity.Mapping;
import com.dmplus.moteurcomptable.parametrage.entity.Operation;
import com.dmplus.moteurcomptable.parametrage.entity.Recette;
import com.dmplus.moteurcomptable.parametrage.entity.Rubrique;
import com.dmplus.moteurcomptable.parametrage.service.ParametrageService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Les URL du paramétrage. Aucune règle métier ici : le controller traduit HTTP vers Java
 * et délègue au service, qui porte les contraintes.
 */
@RestController
@RequestMapping("/api/parametrage")
public class ParametrageController {

    private final ParametrageService service;

    public ParametrageController(ParametrageService service) {
        this.service = service;
    }

    @GetMapping
    public ParametrageService.Vue tout() {
        return service.tout();
    }

    @PostMapping("/operations")
    @ResponseStatus(HttpStatus.CREATED)
    public Operation creerOperation(@RequestBody OperationDto d) {
        return service.creerOperation(d.code(), d.libelle());
    }

    @PostMapping("/journaux")
    @ResponseStatus(HttpStatus.CREATED)
    public Journal creerJournal(@RequestBody JournalDto d) {
        return service.creerJournal(d.code(), d.libelle(), d.codeExterne());
    }

    @PostMapping("/evenements")
    @ResponseStatus(HttpStatus.CREATED)
    public Evenement creerEvenement(@RequestBody EvenementDto d) {
        return service.creerEvenement(d.code(), d.libelle(), d.operationCode(), d.journalCode());
    }

    @PostMapping("/rubriques")
    @ResponseStatus(HttpStatus.CREATED)
    public Rubrique creerRubrique(@RequestBody RubriqueDto d) {
        return service.creerRubrique(d.code(), d.libelle(), d.nature());
    }

    @PostMapping("/comptes")
    @ResponseStatus(HttpStatus.CREATED)
    public Compte creerCompte(@RequestBody CompteDto d) {
        return service.creerCompte(d.code(), d.libelle());
    }

    @PostMapping("/mappings")
    @ResponseStatus(HttpStatus.CREATED)
    public Mapping creerMapping(@RequestBody MappingDto d) {
        return service.creerMapping(d.rubriqueCode(), d.classeActif(), d.categorie(), d.compteCode());
    }

    @PostMapping("/recettes")
    @ResponseStatus(HttpStatus.CREATED)
    public Recette creerRecette(@RequestBody RecetteDto d) {
        return service.creerRecette(d.evenementCode(), d.dateEffet(), d.lignes(), d.utilisateur(), Boolean.TRUE.equals(d.publier()));
    }

    // ---------- Suppression douce (204 = supprimé) ----------

    @DeleteMapping("/operations/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerOperation(@PathVariable String code, @RequestParam(defaultValue = "false") boolean cascade) { service.supprimerOperation(code, cascade); }

    @DeleteMapping("/journaux/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerJournal(@PathVariable String code, @RequestParam(defaultValue = "false") boolean cascade) { service.supprimerJournal(code, cascade); }

    @DeleteMapping("/evenements/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerEvenement(@PathVariable String code, @RequestParam(defaultValue = "false") boolean cascade) { service.supprimerEvenement(code, cascade); }

    @DeleteMapping("/rubriques/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerRubrique(@PathVariable String code, @RequestParam(defaultValue = "false") boolean cascade) { service.supprimerRubrique(code, cascade); }

    @DeleteMapping("/comptes/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerCompte(@PathVariable String code, @RequestParam(defaultValue = "false") boolean cascade) { service.supprimerCompte(code, cascade); }

    @DeleteMapping("/mappings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerMapping(@PathVariable Long id) { service.supprimerMapping(id); }

    /** Brouillon : effacé. Publiée : retirée, car les écritures passées la gardent. */
    @DeleteMapping("/recettes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimerRecette(@PathVariable Long id) { service.supprimerRecette(id); }

    /** Retirer une recette publiée. */
    @PostMapping("/recettes/{id}/retirer")
    public Recette retirerRecette(@PathVariable Long id) { return service.retirerRecette(id); }

    @PostMapping("/recettes/{id}/publier")
    public Recette publierRecette(@PathVariable Long id, @RequestBody UtilisateurDto d) {
        return service.publierRecette(id, d.utilisateur());
    }
}
