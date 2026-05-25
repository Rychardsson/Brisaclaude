package com.example.brisa.repositories;

import com.example.brisa.enums.AdvisorRoleType;
import com.example.brisa.models.AdvisorModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdvisorRepository extends JpaRepository<AdvisorModel, Long> {
    Optional<AdvisorModel> findByCpf(String cpf);
    List<AdvisorModel> findAllByOrderByNameAsc();
    List<AdvisorModel> findByRoleTypeOrderByNameAsc(AdvisorRoleType roleType);
}
