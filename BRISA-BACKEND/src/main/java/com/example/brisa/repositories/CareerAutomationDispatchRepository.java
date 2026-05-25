package com.example.brisa.repositories;

import com.example.brisa.models.CareerAutomationDispatchModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CareerAutomationDispatchRepository extends JpaRepository<CareerAutomationDispatchModel, Long> {

    List<CareerAutomationDispatchModel> findByEnrollment_IdIn(Collection<Long> enrollmentIds);

    Optional<CareerAutomationDispatchModel> findByEnrollment_IdAndCheckpointMonths(Long enrollmentId, Integer checkpointMonths);

    Optional<CareerAutomationDispatchModel> findByResponseToken(String responseToken);
}
