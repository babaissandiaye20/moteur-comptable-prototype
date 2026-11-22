package com.dmplus.moteurcomptable.parametrage.entity;

import com.dmplus.moteurcomptable.shared.entity.EntiteSupprimable;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Le « répertoire » : rubrique + classe d'actifs + catégorie → numéro de compte.
 * La valeur « * » veut dire « toutes ».
 */
@Entity
@Table(name = "mapping")
@SQLRestriction("is_deleted = false")
public class Mapping extends EntiteSupprimable {

    public static final String TOUS = "*";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "rubrique_code", nullable = false)
    private String rubriqueCode;
    @Column(name = "classe_actif", nullable = false)
    private String classeActif;
    @Column(nullable = false)
    private String categorie;
    @Column(name = "compte_code", nullable = false)
    private String compteCode;

    protected Mapping() {
    }

    public Mapping(String rubriqueCode, String classeActif, String categorie, String compteCode) {
        this.rubriqueCode = rubriqueCode;
        this.classeActif = classeActif;
        this.categorie = categorie;
        this.compteCode = compteCode;
    }

    public Long getId() { return id; }

    /** Plus la ligne est précise, plus son score est haut ; -1 si elle ne correspond pas. */
    public int score(String classe, String cat) {
        int s = 0;
        if (classeActif.equals(classe)) {
            s += 2;
        } else if (!TOUS.equals(classeActif)) {
            return -1;
        }
        if (categorie.equals(cat)) {
            s += 1;
        } else if (!TOUS.equals(categorie)) {
            return -1;
        }
        return s;
    }

    public String getRubriqueCode() { return rubriqueCode; }
    public String getClasseActif() { return classeActif; }
    public String getCategorie() { return categorie; }
    public String getCompteCode() { return compteCode; }
}
