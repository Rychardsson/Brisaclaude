package com.example.brisa.repositories;

import com.example.brisa.models.CareerAutomationSettingsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerAutomationSettingsRepository extends JpaRepository<CareerAutomationSettingsModel, Long> {
    Optional<CareerAutomationSettingsModel> findTopByOrderByIdAsc();
}
