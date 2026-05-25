package com.example.brisa.services;

import com.example.brisa.enums.AdvisorRoleType;
import com.example.brisa.exceptions.ResourceNotFoundException;
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
    public List<AdvisorModel> findAll(String roleType) {
        AdvisorRoleType resolvedRole = resolveRoleType(roleType);
        if (resolvedRole == null) {
            return advisorRepository.findAllByOrderByNameAsc();
        }
        return advisorRepository.findByRoleTypeOrderByNameAsc(resolvedRole);
    }

    @Transactional(readOnly = true)
    public AdvisorModel findById(Long id) {
        return advisorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Membro da equipe acadêmica não encontrado."));
    }

    @Transactional
    public AdvisorModel create(AdvisorModel advisor) {
        prepare(advisor);
        validate(advisor, null);
        return advisorRepository.save(advisor);
    }

    @Transactional
    public AdvisorModel update(Long id, AdvisorModel advisorDetails) {
        AdvisorModel advisor = findById(id);

        advisor.setCpf(advisorDetails.getCpf());
        advisor.setName(advisorDetails.getName());
        advisor.setRoleType(advisorDetails.getRoleType());
        advisor.setEmail(advisorDetails.getEmail());
        advisor.setFormation(advisorDetails.getFormation());
        advisor.setBirthDate(advisorDetails.getBirthDate());
        advisor.setActive(advisorDetails.getActive());

        prepare(advisor);
        validate(advisor, id);

        return advisorRepository.save(advisor);
    }

    @Transactional
    public void delete(Long id) {
        AdvisorModel advisor = findById(id);
        advisorRepository.delete(advisor);
    }

    private void prepare(AdvisorModel advisor) {
        if (advisor.getCpf() != null) {
            advisor.setCpf(advisor.getCpf().replaceAll("\\D", ""));
        }
        if (advisor.getName() != null) {
            advisor.setName(advisor.getName().trim());
        }
        if (advisor.getEmail() != null) {
            advisor.setEmail(advisor.getEmail().trim().toLowerCase());
            if (advisor.getEmail().isBlank()) {
                advisor.setEmail(null);
            }
        }
        if (advisor.getFormation() != null) {
            advisor.setFormation(advisor.getFormation().trim());
            if (advisor.getFormation().isBlank()) {
                advisor.setFormation(null);
            }
        }
        if (advisor.getRoleType() == null) {
            advisor.setRoleType(AdvisorRoleType.ORIENTADOR);
        }
        if (advisor.getActive() == null) {
            advisor.setActive(Boolean.TRUE);
        }
    }

    private void validate(AdvisorModel advisor, Long currentId) {
        if (advisor.getName() == null || advisor.getName().isBlank()) {
            throw new ValidationException(List.of("Nome da equipe acadêmica é obrigatório."));
        }

        if (advisor.getCpf() == null || advisor.getCpf().isBlank()) {
            throw new ValidationException(List.of("CPF é obrigatório."));
        }

        if (advisor.getCpf().length() != 11) {
            throw new ValidationException(List.of("CPF deve conter 11 dígitos."));
        }

        advisorRepository.findByCpf(advisor.getCpf())
                .filter(existing -> currentId == null || !existing.getId().equals(currentId))
                .ifPresent(existing -> {
                    throw new ValidationException(List.of("Já existe um membro da equipe acadêmica com este CPF."));
                });

        if (advisor.getEmail() != null && !advisor.getEmail().contains("@")) {
            throw new ValidationException(List.of("Informe um e-mail válido."));
        }
    }

    private AdvisorRoleType resolveRoleType(String rawRoleType) {
        if (rawRoleType == null || rawRoleType.trim().isEmpty()) {
            return null;
        }
        String normalized = rawRoleType.trim().toUpperCase();
        try {
            AdvisorRoleType resolved = AdvisorRoleType.valueOf(normalized);
            // Only ORIENTADOR and GESTOR are accepted as explicit roles for creation/update
            if (resolved != AdvisorRoleType.ORIENTADOR && resolved != AdvisorRoleType.GESTOR) {
                throw new ValidationException(List.of("Tipo de perfil acadêmico inválido."));
            }
            return resolved;
        } catch (IllegalArgumentException ex) {
            throw new ValidationException(List.of("Tipo de perfil acadêmico inválido."));
        }
    }
}
