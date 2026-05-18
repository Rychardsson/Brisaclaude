package com.example.brisa.repositories;

import com.example.brisa.models.PeopleProjectGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PeopleProjectGroupRepository extends JpaRepository<PeopleProjectGroupModel, Long> {
    void deleteByProjectGroupId(Long projectGroupId);
}
