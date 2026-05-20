package com.example.brisa.services;

import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.AdvisorModel;
import com.example.brisa.repositories.AdvisorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvisorService {

    private final AdvisorRepository advisorRepository;

    @Transactional(readOnly = true)
    public List<AdvisorModel> findAll() {
        return advisorRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public AdvisorModel create(AdvisorModel advisor) {
        if (advisor.getCpf() != null && !advisor.getCpf().trim().isEmpty()) {
            if (advisorRepository.findByCpf(advisor.getCpf().trim()).isPresent()) {
                throw new ValidationException(java.util.List.of("Orientador com este CPF já existe"));
            }
        }
        if (advisor.getName() == null || advisor.getName().trim().isEmpty()) {
            throw new ValidationException(java.util.List.of("Nome do orientador é obrigatório"));
        }
        return advisorRepository.save(advisor);
    }
}
