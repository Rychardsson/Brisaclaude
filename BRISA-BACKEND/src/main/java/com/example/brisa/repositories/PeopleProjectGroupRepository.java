package com.example.brisa.repositories;

import com.example.brisa.models.PeopleProjectGroupModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface PeopleProjectGroupRepository extends JpaRepository<PeopleProjectGroupModel, Long> {
    void deleteByProjectGroupId(Long projectGroupId);
    List<PeopleProjectGroupModel> findByPeople_IdIn(Collection<Long> peopleIds);
}
