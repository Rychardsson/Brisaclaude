package com.example.brisa.repositories;

import com.example.brisa.models.roles.InstitutionRoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InstitutionRoleRepository extends JpaRepository<InstitutionRoleModel, Long> {
    Optional<InstitutionRoleModel> findByNameIgnoreCase(String name);
}
