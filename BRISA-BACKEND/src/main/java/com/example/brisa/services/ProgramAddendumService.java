package com.example.brisa.services;

import com.example.brisa.dtos.program.ProgramAddendumRequestDTO;
import com.example.brisa.dtos.program.ProgramAddendumResponseDTO;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.ProgramAddendumModel;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.repositories.ProgramAddendumRepository;
import com.example.brisa.repositories.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramAddendumService {

    private final ProgramRepository programRepository;
    private final ProgramAddendumRepository programAddendumRepository;

    @Transactional(readOnly = true)
    public List<ProgramAddendumResponseDTO> listByProgram(Long programId) {
        ensureProgram(programId);
        return programAddendumRepository.findByProgramIdOrderByAddendumNumberAsc(programId).stream()
                .map(ProgramAddendumResponseDTO::new)
                .toList();
    }

    @Transactional
    public ProgramAddendumResponseDTO create(Long programId, ProgramAddendumRequestDTO request) {
        ProgramModel program = ensureProgram(programId);
        validate(request);

        ProgramAddendumModel model = new ProgramAddendumModel();
        model.setProgram(program);
        model.setAddendumNumber(resolveAddendumNumber(programId, request.getAddendumNumber()));
        model.setStartDate(request.getStartDate());
        model.setEndDate(request.getEndDate());
        model.setValue(request.getValue());

        ProgramAddendumModel saved = programAddendumRepository.save(model);
        syncProgramEndDate(program);
        return new ProgramAddendumResponseDTO(saved);
    }

    @Transactional
    public ProgramAddendumResponseDTO update(Long programId, Long addendumId, ProgramAddendumRequestDTO request) {
        ProgramModel program = ensureProgram(programId);
        validate(request);

        ProgramAddendumModel model = programAddendumRepository.findById(addendumId)
                .filter(item -> item.getProgram() != null && item.getProgram().getId().equals(programId))
                .orElseThrow(() -> new ResourceNotFoundException("Aditivo não encontrado."));

        model.setAddendumNumber(resolveUpdatedAddendumNumber(programId, addendumId, request.getAddendumNumber()));
        model.setStartDate(request.getStartDate());
        model.setEndDate(request.getEndDate());
        model.setValue(request.getValue());

        ProgramAddendumModel saved = programAddendumRepository.save(model);
        syncProgramEndDate(program);
        return new ProgramAddendumResponseDTO(saved);
    }

    @Transactional
    public void delete(Long programId, Long addendumId) {
        ProgramModel program = ensureProgram(programId);
        ProgramAddendumModel model = programAddendumRepository.findById(addendumId)
                .filter(item -> item.getProgram() != null && item.getProgram().getId().equals(programId))
                .orElseThrow(() -> new ResourceNotFoundException("Aditivo não encontrado."));
        programAddendumRepository.delete(model);
        syncProgramEndDate(program);
    }

    private ProgramModel ensureProgram(Long programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Programa não encontrado."));
    }

    private void validate(ProgramAddendumRequestDTO request) {
        if (request.getStartDate() == null) {
            throw new ValidationException(List.of("Data inicial do aditivo é obrigatória."));
        }
        if (request.getEndDate() == null) {
            throw new ValidationException(List.of("Data final do aditivo é obrigatória."));
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ValidationException(List.of("Data final do aditivo não pode ser anterior à data inicial."));
        }
    }

    private Integer resolveAddendumNumber(Long programId, Integer requestedNumber) {
        if (requestedNumber != null) {
            boolean exists = programAddendumRepository.findByProgramIdOrderByAddendumNumberAsc(programId).stream()
                    .anyMatch(item -> requestedNumber.equals(item.getAddendumNumber()));
            if (exists) {
                throw new ValidationException(List.of("Já existe um aditivo com esse número neste programa."));
            }
            return requestedNumber;
        }

        return programAddendumRepository.findByProgramIdOrderByAddendumNumberAsc(programId).stream()
                .map(ProgramAddendumModel::getAddendumNumber)
                .filter(number -> number != null)
                .max(Comparator.naturalOrder())
                .map(number -> number + 1)
                .orElse(1);
    }

    private Integer resolveUpdatedAddendumNumber(Long programId, Long addendumId, Integer requestedNumber) {
        if (requestedNumber == null) {
            return programAddendumRepository.findById(addendumId)
                    .map(ProgramAddendumModel::getAddendumNumber)
                    .orElse(1);
        }

        boolean exists = programAddendumRepository.findByProgramIdOrderByAddendumNumberAsc(programId).stream()
                .anyMatch(item -> !item.getId().equals(addendumId) && requestedNumber.equals(item.getAddendumNumber()));
        if (exists) {
            throw new ValidationException(List.of("Já existe um aditivo com esse número neste programa."));
        }
        return requestedNumber;
    }

    private void syncProgramEndDate(ProgramModel program) {
        LocalDate latestEndDate = programAddendumRepository.findByProgramIdOrderByAddendumNumberAsc(program.getId()).stream()
                .map(ProgramAddendumModel::getEndDate)
                .filter(date -> date != null)
                .max(LocalDate::compareTo)
                .orElse(program.getEndDate());

        if (latestEndDate != null) {
            program.setEndDate(latestEndDate);
            programRepository.save(program);
        }
    }
}

