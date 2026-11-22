package com.dmplus.moteurcomptable.retour.repository;

import com.dmplus.moteurcomptable.retour.entity.Retour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RetourRepository extends JpaRepository<Retour, Long> {
    List<Retour> findAllByOrderByOrdreAsc();

    @Query("select coalesce(max(r.ordre), 0) from Retour r")
    int dernierOrdre();
}
