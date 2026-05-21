package com.example.brisa.services;

import com.example.brisa.dtos.stage.*;
import com.example.brisa.enums.StageStatus;
import com.example.brisa.exceptions.ConflictException;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.StageModel;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import com.example.brisa.repositories.StageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StageService {
    
    private final StageRepository stageRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final ClassRepository classRepository;
    private final PeopleRepository peopleRepository;
    private final PeopleIntegrationService peopleIntegrationService;

    public List<StageResponseDTO> findAll() {
        return stageRepository.findAll().stream()
                .map(StageResponseDTO::new)
                .collect(Collectors.toList());
    }

    public StageResponseDTO findById(Long id) {
        StageModel stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + id));
        return new StageResponseDTO(stage);
    }

    public List<StageResponseDTO> findByClassId(Long classId) {
        return stageRepository.findByClassModelId(classId).stream()
                .map(StageResponseDTO::new)
                .collect(Collectors.toList());
    }

    public Map<Long, Long> getCandidatesCountByClassId(Long classId) {
        List<Object[]> results = stageCandidateRepository.countCandidatesByClassId(classId);
        Map<Long, Long> countsMap = new HashMap<>();
        
        for (Object[] result : results) {
            Long stageId = ((Number) result[0]).longValue();
            Long count = ((Number) result[1]).longValue();
            countsMap.put(stageId, count);
        }
        
        return countsMap;
    }

    @Transactional
    public StageResponseDTO create(StageRequestDTO stageRequestDTO) {
        if (stageRequestDTO.getName() == null || stageRequestDTO.getName().trim().isEmpty()) {
            throw new ValidationException(List.of("Stage name is required"));
        }

        if (stageRequestDTO.getClassId() == null) {
            throw new ValidationException(List.of("Class ID is required"));
        }

        ClassModel classModel = classRepository.findById(stageRequestDTO.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("Class not found with id: " + stageRequestDTO.getClassId()));

        if (stageRepository.existsByNameAndClassModelId(stageRequestDTO.getName(), stageRequestDTO.getClassId())) {
            throw new ConflictException("Stage with name '" + stageRequestDTO.getName() + "' already exists for this class");
        }

        StageModel stage = new StageModel();
        stage.setName(stageRequestDTO.getName());
        stage.setClassModel(classModel);
        stage.setDescription(stageRequestDTO.getDescription());

        StageModel savedStage = stageRepository.save(stage);
        return new StageResponseDTO(savedStage);
    }

    @Transactional
    public StageResponseDTO update(Long id, StageRequestDTO stageRequestDTO) {
        StageModel stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + id));

        if (stageRequestDTO.getName() != null && !stageRequestDTO.getName().trim().isEmpty()) {
            stage.setName(stageRequestDTO.getName());
        }

        if (stageRequestDTO.getDescription() != null) {
            stage.setDescription(stageRequestDTO.getDescription());
        }

        StageModel updatedStage = stageRepository.save(stage);
        return new StageResponseDTO(updatedStage);
    }

    @Transactional
    public void delete(Long id) {
        StageModel stage = stageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + id));
        stageRepository.delete(stage);
    }

    // Stage Candidate Methods

    public List<StageCandidateResponseDTO> findAllCandidates() {
        return stageCandidateRepository.findAll().stream()
                .map(StageCandidateResponseDTO::new)
                .collect(Collectors.toList());
    }

    public StageCandidateResponseDTO findCandidateById(Long id) {
        StageCandidateModel candidate = stageCandidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage candidate not found with id: " + id));
        return new StageCandidateResponseDTO(candidate);
    }

    public List<StageCandidateResponseDTO> findCandidatesByStageId(Long stageId) {
        return stageCandidateRepository.findByStageId(stageId).stream()
                .map(StageCandidateResponseDTO::new)
                .collect(Collectors.toList());
    }

    public List<StageCandidateResponseDTO> findCandidatesByPeopleId(Long peopleId) {
        return stageCandidateRepository.findByPeopleId(peopleId).stream()
                .map(StageCandidateResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public StageCandidateResponseDTO addCandidate(StageCandidateRequestDTO requestDTO) {
        if (requestDTO.getStageId() == null) {
            throw new ValidationException(List.of("Stage ID is required"));
        }

        if (requestDTO.getPeopleId() == null) {
            throw new ValidationException(List.of("People ID is required"));
        }

        if (requestDTO.getStatus() == null) {
            throw new ValidationException(List.of("Status is required"));
        }

        StageModel stage = stageRepository.findById(requestDTO.getStageId())
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + requestDTO.getStageId()));

        PeopleModel people = peopleRepository.findById(requestDTO.getPeopleId())
                .orElseThrow(() -> new ResourceNotFoundException("People not found with id: " + requestDTO.getPeopleId()));

        if (stageCandidateRepository.existsByStageIdAndPeopleId(requestDTO.getStageId(), requestDTO.getPeopleId())) {
            throw new ConflictException("This person is already a candidate for this stage");
        }

        StageCandidateModel candidate = new StageCandidateModel();
        candidate.setStage(stage);
        candidate.setPeople(people);
        candidate.setStatus(requestDTO.getStatus());
        candidate.setNotes(requestDTO.getNotes());

        StageCandidateModel savedCandidate = stageCandidateRepository.save(candidate);
        return new StageCandidateResponseDTO(savedCandidate);
    }

    @Transactional
    public StageCandidateResponseDTO updateCandidate(Long id, StageCandidateRequestDTO requestDTO) {
        StageCandidateModel candidate = stageCandidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage candidate not found with id: " + id));

        if (requestDTO.getStatus() != null) {
            candidate.setStatus(requestDTO.getStatus());
        }

        if (requestDTO.getNotes() != null) {
            candidate.setNotes(requestDTO.getNotes());
        }

        StageCandidateModel updatedCandidate = stageCandidateRepository.save(candidate);
        return new StageCandidateResponseDTO(updatedCandidate);
    }

    @Transactional
    public void deleteCandidate(Long id) {
        StageCandidateModel candidate = stageCandidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stage candidate not found with id: " + id));
        stageCandidateRepository.delete(candidate);
    }

    // Import Candidates from Excel

    @Transactional
    public CandidateImportResponseDTO importCandidatesFromExcel(Long stageId, MultipartFile file) throws IOException {
        // Valida se a etapa existe
        StageModel stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + stageId));

        // Parse do arquivo Excel
        List<CandidateImportDTO> candidatesList = parseExcelFile(file);

        // Importa os candidatos em lotes
        return importCandidatesInBatches(stage, candidatesList);
    }

    private List<CandidateImportDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<CandidateImportDTO> candidatesList = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Começa da linha 1 (pula o cabeçalho na linha 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                CandidateImportDTO candidate = new CandidateImportDTO();

                // Coluna A (0): Nome
                    candidate.setRow(i + 1);
                    candidate.setName(getCellValueAsString(row.getCell(0)));

                    // Coluna B (1): Email
                    candidate.setEmail(getCellValueAsString(row.getCell(1)));

                    // Coluna C (2): CPF (opcional)
                    candidate.setCpf(getCellValueAsString(row.getCell(2)));

                    // Coluna D (3): Status (opcional, padrão APROVADO)
                    String statusStr = getCellValueAsString(row.getCell(3));
                    candidate.setStatus(parseStatus(statusStr));

                    // Coluna E (4): Observações (opcional)
                    candidate.setNotes(getCellValueAsString(row.getCell(4)));

                    // Adiciona apenas se tiver nome e email
                    if (candidate.getName() != null && !candidate.getName().isEmpty()
                            && candidate.getEmail() != null && !candidate.getEmail().isEmpty()) {
                        candidatesList.add(candidate);
                    }
                }
        }

        return candidatesList;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }

    private StageStatus parseStatus(String statusStr) {
        if (statusStr == null || statusStr.trim().isEmpty()) {
            return StageStatus.APROVADO; // Padrão
        }

        String normalized = statusStr.trim().toUpperCase();
        
        // Aceita variações em português
        if (normalized.contains("APROV")) {
            return StageStatus.APROVADO;
        } else if (normalized.contains("REPROV")) {
            return StageStatus.REPROVADO;
        }

        // Tenta converter diretamente
        try {
            return StageStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return StageStatus.APROVADO; // Padrão se não reconhecer
        }
    }

    @Transactional
    public CandidateImportResponseDTO importCandidatesInBatches(StageModel stage, List<CandidateImportDTO> candidatesList) {
        List<String> duplicateCandidates = new ArrayList<>();
        List<String> createdPeople = new ArrayList<>();
        int successfullyInserted = 0;
        int newPeopleCreated = 0;
        List<CandidateRowErrorDTO> rowErrors = new ArrayList<>();

        // Extrai todos os emails de uma vez
        List<String> allEmails = candidatesList.stream()
                .map(CandidateImportDTO::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .distinct()
                .toList();

        // Busca todas as pessoas existentes por email
        List<PeopleModel> existingPeople = peopleRepository.findAllByEmailIn(allEmails);
        Map<String, PeopleModel> peopleByEmail = existingPeople.stream()
                .collect(Collectors.toMap(PeopleModel::getEmail, p -> p));

        // Busca todos os candidatos já existentes nesta etapa
        List<StageCandidateModel> existingCandidates = stageCandidateRepository.findByStageId(stage.getId());
        Set<Long> existingPeopleIds = existingCandidates.stream()
                .map(c -> c.getPeople().getId())
                .collect(Collectors.toSet());

        // Processa cada candidato
        List<StageCandidateModel> candidatesToInsert = new ArrayList<>();

        for (CandidateImportDTO candidateDTO : candidatesList) {
            PeopleModel person = peopleByEmail.get(candidateDTO.getEmail());

            // Se a pessoa não existe, cria uma nova
            if (person == null) {
                person = new PeopleModel();
                person.setName(candidateDTO.getName());
                person.setEmail(candidateDTO.getEmail());
                person.setCpf(candidateDTO.getCpf());
                person = peopleRepository.save(person);

                peopleByEmail.put(person.getEmail(), person);
                createdPeople.add(person.getName());
                newPeopleCreated++;
            }

            // Verifica se já é candidato nesta etapa
            if (existingPeopleIds.contains(person.getId())) {
                duplicateCandidates.add(person.getName());
                continue;
            }

            // Checa conflito de nivelamento em outras turmas
            try {
                List<String> alerts = peopleIntegrationService.detectActiveConflicts(person.getId(), stage.getClassModel().getId());
                boolean hasNivelamentoConflict = alerts.stream().anyMatch(a -> a != null && a.toLowerCase().contains("nivelament"));
                if (hasNivelamentoConflict) {
                    // Registra erro por linha e não cria o candidato
                    int rowNumber = candidateDTO.getRow();
                    rowErrors.add(new CandidateRowErrorDTO(rowNumber, alerts));
                    continue;
                }
            } catch (Exception ex) {
                int rowNumber = candidateDTO.getRow();
                rowErrors.add(new CandidateRowErrorDTO(rowNumber, List.of("Erro ao validar conflito: " + ex.getMessage())));
                continue;
            }

            // Cria o candidato
            StageCandidateModel candidate = new StageCandidateModel();
            candidate.setStage(stage);
            candidate.setPeople(person);
            candidate.setStatus(candidateDTO.getStatus() != null ? candidateDTO.getStatus() : StageStatus.APROVADO);
            candidate.setNotes(candidateDTO.getNotes());

            candidatesToInsert.add(candidate);
            existingPeopleIds.add(person.getId()); // Evita duplicatas no próprio lote
        }

        // Salva todos os candidatos de uma vez
        if (!candidatesToInsert.isEmpty()) {
            stageCandidateRepository.saveAll(candidatesToInsert);
            successfullyInserted = candidatesToInsert.size();
        }

        // Monta a resposta
        CandidateImportResponseDTO response = new CandidateImportResponseDTO();
        response.setTotalProcessed(candidatesList.size());
        response.setSuccessfullyInserted(successfullyInserted);
        response.setAlreadyInStage(duplicateCandidates.size());
        response.setNewPeopleCreated(newPeopleCreated);
        response.setDuplicateCandidates(duplicateCandidates);
        response.setCreatedPeople(createdPeople);
        response.setRowErrors(rowErrors);

        return response;
    }
}
