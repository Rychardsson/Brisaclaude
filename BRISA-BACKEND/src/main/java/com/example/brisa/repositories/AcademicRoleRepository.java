package com.example.brisa.repositories;

import com.example.brisa.models.roles.AcademicRoleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AcademicRoleRepository extends JpaRepository<AcademicRoleModel, Long> {
    Optional<AcademicRoleModel> findByName(String name);
    Optional<AcademicRoleModel> findByNameIgnoreCase(String name);
}
