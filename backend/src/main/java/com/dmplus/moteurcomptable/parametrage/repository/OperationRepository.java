package com.dmplus.moteurcomptable.parametrage.repository;

import com.dmplus.moteurcomptable.parametrage.entity.Operation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OperationRepository extends JpaRepository<Operation, String> {
}
