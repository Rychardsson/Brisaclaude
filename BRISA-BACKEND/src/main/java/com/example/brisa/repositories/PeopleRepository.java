package com.example.brisa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.brisa.models.PeopleModel;

public interface PeopleRepository extends JpaRepository<PeopleModel, Long> {
    
    Optional<PeopleModel> findByEmail(String email);
    Optional<PeopleModel> findByEmailIgnoreCase(String email);

    Optional<PeopleModel> findByCpf(String cpf);
    
    boolean existsByEmail(String email);
    
    // Método otimizado para buscar múltiplos emails de uma vez
    List<PeopleModel> findAllByEmailIn(List<String> emails);
    
    // Retorna todas as pessoas ordenadas por nome em ordem alfabética
    List<PeopleModel> findAllByOrderByNameAsc();

    @Query("select p from PeopleModel p where coalesce(p.softDeleted, false) = false order by lower(p.name) asc")
    List<PeopleModel> findAllActiveOrderByNameAsc();

    @Query("select p from PeopleModel p where coalesce(p.softDeleted, false) = false")
    List<PeopleModel> findAllActive();

    @Query("select p from PeopleModel p where p.id = :id and coalesce(p.softDeleted, false) = false")
    Optional<PeopleModel> findActiveById(@Param("id") Long id);

    @Query("select p from PeopleModel p where p.email in :emails and coalesce(p.softDeleted, false) = false")
    List<PeopleModel> findAllActiveByEmailIn(@Param("emails") List<String> emails);
    
    // Find seed users by email prefix (e.g. 'candidate')
    List<PeopleModel> findByEmailStartingWith(String prefix);
}
