package com.example.brisa.repositories;

import com.example.brisa.models.ProgramAddendumModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProgramAddendumRepository extends JpaRepository<ProgramAddendumModel, Long> {
    List<ProgramAddendumModel> findByProgramIdOrderByAddendumNumberAsc(Long programId);
}

