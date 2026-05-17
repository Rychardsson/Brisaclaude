package com.example.brisa.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
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
    
    // Find seed users by email prefix (e.g. 'candidate')
    List<PeopleModel> findByEmailStartingWith(String prefix);
}
