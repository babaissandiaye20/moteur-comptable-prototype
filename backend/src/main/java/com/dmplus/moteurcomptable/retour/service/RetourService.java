package com.dmplus.moteurcomptable.retour.service;

import com.dmplus.moteurcomptable.retour.entity.Retour;
import com.dmplus.moteurcomptable.retour.entity.StatutRetour;
import com.dmplus.moteurcomptable.retour.repository.RetourRepository;
import com.dmplus.moteurcomptable.shared.exception.MoteurException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RetourService {

    private final RetourRepository retours;

    public RetourService(RetourRepository retours) {
        this.retours = retours;
    }

    @Transactional(readOnly = true)
    public List<Retour> lister() {
        return retours.findAllByOrderByOrdreAsc();
    }

    public Retour ajouter(String fonctionnalite, String ouTester, String critere) {
        if (critere == null || critere.isBlank()) {
            throw new MoteurException("CHAMP_REQUIS", "Décrivez ce qu'il faut vérifier");
        }
        String f = fonctionnalite == null || fonctionnalite.isBlank() ? "Autre" : fonctionnalite.trim();
        return retours.save(new Retour(retours.dernierOrdre() + 1, f, ouTester, critere.trim(), true));
    }

    public Retour noter(Long id, StatutRetour statut, String observation) {
        Retour r = retours.findById(id)
                .orElseThrow(() -> new MoteurException("RETOUR_INCONNU", "Point " + id + " introuvable"));
        String obs = observation == null ? null : observation.strip();
        if (obs != null && obs.length() > 2000) {
            throw new MoteurException("OBSERVATION_TROP_LONGUE", "L'observation dépasse 2 000 caractères");
        }
        r.noter(statut, obs);
        return r;
    }

    /** Seuls les points ajoutés par le testeur se suppriment ; la grille de départ reste. */
    public void supprimer(Long id) {
        Retour r = retours.findById(id)
                .orElseThrow(() -> new MoteurException("RETOUR_INCONNU", "Point " + id + " introuvable"));
        if (!r.isAjouteParTesteur()) {
            throw new MoteurException("POINT_DE_DEPART", "Ce point fait partie de la grille de départ : mettez-le plutôt en « Non testé »");
        }
        retours.delete(r);
    }
}
