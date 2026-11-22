package com.dmplus.moteurcomptable.ecriture.controller;

import com.dmplus.moteurcomptable.ecriture.dto.EcritureDto;
import com.dmplus.moteurcomptable.ecriture.dto.EvenementEntrantDto;
import com.dmplus.moteurcomptable.parametrage.dto.ParametrageDtos.UtilisateurDto;
import com.dmplus.moteurcomptable.ecriture.dto.ResultatTraitementDto;
import com.dmplus.moteurcomptable.ecriture.service.EcritureService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Les URL de la génération des écritures. Aucune règle métier ici : tout est dans EcritureService. */
@RestController
@RequestMapping("/api")
public class EcritureController {

    private final EcritureService service;

    public EcritureController(EcritureService service) {
        this.service = service;
    }

    /** 201 si l'écriture est créée ; 200 si l'événement était déjà reçu (même événement, même résultat). */
    @PostMapping("/evenements")
    public ResponseEntity<ResultatTraitementDto> recevoir(@RequestBody EvenementEntrantDto evenement) {
        ResultatTraitementDto r = service.traiter(evenement);
        HttpStatus statut = "DOUBLON".equals(r.statut()) ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(statut).body(r);
    }

    @GetMapping("/ecritures")
    public List<EcritureDto> lister() {
        return service.lister();
    }

    @GetMapping("/ecritures/{id}")
    public EcritureDto detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PostMapping("/ecritures/{id}/valider")
    public EcritureDto valider(@PathVariable Long id, @RequestBody UtilisateurDto d) {
        return service.valider(id, d.utilisateur());
    }
}
