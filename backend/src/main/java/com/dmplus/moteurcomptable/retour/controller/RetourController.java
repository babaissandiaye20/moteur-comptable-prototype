package com.dmplus.moteurcomptable.retour.controller;

import com.dmplus.moteurcomptable.retour.dto.RetourDtos.AvisDto;
import com.dmplus.moteurcomptable.retour.dto.RetourDtos.NouveauRetourDto;
import com.dmplus.moteurcomptable.retour.entity.Retour;
import com.dmplus.moteurcomptable.retour.service.RetourService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** La grille de retours du testeur, dans l'application. */
@RestController
@RequestMapping("/api/retours")
public class RetourController {

    private final RetourService service;

    public RetourController(RetourService service) {
        this.service = service;
    }

    @GetMapping
    public List<Retour> lister() {
        return service.lister();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Retour ajouter(@RequestBody NouveauRetourDto d) {
        return service.ajouter(d.fonctionnalite(), d.ouTester(), d.critere());
    }

    @PutMapping("/{id}")
    public Retour noter(@PathVariable Long id, @RequestBody AvisDto d) {
        return service.noter(id, d.statut(), d.observation());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        service.supprimer(id);
    }
}
