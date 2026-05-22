package com.example.brisa.services;

import com.example.brisa.dtos.stage.ApprovedImportResponseDTO;
import com.example.brisa.dtos.stage.CandidateImportDTO;
import com.example.brisa.dtos.stage.CandidateImportResponseDTO;
import com.example.brisa.dtos.stage.CandidateRowErrorDTO;
import com.example.brisa.dtos.stage.StageCandidateRequestDTO;
import com.example.brisa.dtos.stage.StageCandidateResponseDTO;
import com.example.brisa.dtos.stage.StageRequestDTO;
import com.example.brisa.dtos.stage.StageResponseDTO;
import com.example.brisa.dtos.stage.WaitlistConvokeRequestDTO;
import com.example.brisa.dtos.stage.WaitlistConvokeResponseDTO;
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
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StageService {

    private static final DateTimeFormatter BRAZILIAN_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final StageRepository stageRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final ClassRepository classRepository;
    private final PeopleRepository peopleRepository;
    private final PeopleIntegrationService peopleIntegrationService;
    private final ExcelImportHelper excelImportHelper;
    private final EmailService emailService;

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

    @Transactional
    public CandidateImportResponseDTO importCandidatesFromExcel(Long stageId, MultipartFile file) throws IOException {
        StageModel stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + stageId));

        List<CandidateImportDTO> candidatesList = parseExcelFile(file);
        return importCandidatesInBatches(stage, candidatesList);
    }

    @Transactional
    public ApprovedImportResponseDTO importApprovedCandidatesFromExcel(Long stageId, MultipartFile file) throws IOException {
        StageModel stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + stageId));

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new ValidationException(List.of("A planilha de aprovados esta vazia."));
            }

            Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));
            Integer cpfIndex = excelImportHelper.findColumn(headers, List.of("cpf", "cpf_aluno"), 0);
            Integer statusIndex = excelImportHelper.findColumn(headers, List.of("status", "situacao", "situacao final"), 1);
            Integer nameIndex = excelImportHelper.findColumn(headers, List.of("nome", "nome completo", "name"), 2);
            Integer notesIndex = excelImportHelper.findColumn(headers, List.of("observacoes", "observacoes internas", "notes"), null);

            List<StageCandidateModel> existingCandidates = stageCandidateRepository.findByStageId(stageId);
            Map<String, StageCandidateModel> candidatesByCpf = existingCandidates.stream()
                    .filter(candidate -> candidate.getPeople() != null && candidate.getPeople().getCpf() != null)
                    .collect(Collectors.toMap(
                            candidate -> normalizeDocument(candidate.getPeople().getCpf()),
                            candidate -> candidate,
                            (left, right) -> left,
                            LinkedHashMap::new
                    ));
            Map<String, StageCandidateModel> candidatesByName = existingCandidates.stream()
                    .filter(candidate -> candidate.getPeople() != null && candidate.getPeople().getName() != null)
                    .collect(Collectors.toMap(
                            candidate -> normalizeText(candidate.getPeople().getName()),
                            candidate -> candidate,
                            (left, right) -> left,
                            LinkedHashMap::new
                    ));

            Set<Long> processedCandidateIds = new HashSet<>();
            List<String> warnings = new ArrayList<>();
            int totalProcessed = 0;
            int approvedCount = 0;
            int waitlistCount = 0;
            int rejectedCount = 0;
            int conflictsCount = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (excelImportHelper.isRowEmpty(row)) {
                    continue;
                }

                totalProcessed++;
                String cpf = excelImportHelper.getString(row, cpfIndex);
                String name = excelImportHelper.getString(row, nameIndex);
                String statusRaw = excelImportHelper.getString(row, statusIndex);
                String notes = excelImportHelper.getString(row, notesIndex);

                StageCandidateModel candidate = resolveCandidate(candidatesByCpf, candidatesByName, cpf, name);
                if (candidate == null) {
                    warnings.add("Linha " + (rowIndex + 1) + ": candidato nao encontrado na etapa.");
                    continue;
                }

                StageStatus targetStatus = parseStatus(statusRaw);
                if (targetStatus == StageStatus.APROVADO || targetStatus == StageStatus.LISTA_ESPERA) {
                    List<String> conflicts = detectActiveConflicts(candidate, stage);
                    if (!conflicts.isEmpty()) {
                        targetStatus = StageStatus.EM_ANALISE;
                        conflictsCount++;
                        warnings.add("Linha " + (rowIndex + 1) + ": " + candidate.getPeople().getName() + " possui conflito com outro programa vigente.");
                    }
                }

                candidate.setStatus(targetStatus);
                if (notes != null && !notes.isBlank()) {
                    candidate.setNotes(notes);
                }
                stageCandidateRepository.save(candidate);
                processedCandidateIds.add(candidate.getId());

                switch (targetStatus) {
                    case APROVADO -> approvedCount++;
                    case LISTA_ESPERA -> waitlistCount++;
                    case REPROVADO -> rejectedCount++;
                    default -> {
                    }
                }
            }

            for (StageCandidateModel candidate : existingCandidates) {
                if (processedCandidateIds.contains(candidate.getId())) {
                    continue;
                }
                candidate.setStatus(StageStatus.REPROVADO);
                stageCandidateRepository.save(candidate);
                rejectedCount++;
            }

            return new ApprovedImportResponseDTO(
                    totalProcessed,
                    approvedCount,
                    waitlistCount,
                    rejectedCount,
                    conflictsCount,
                    warnings
            );
        }
    }

    @Transactional
    public WaitlistConvokeResponseDTO convokeWaitlist(Long stageId, WaitlistConvokeRequestDTO request) {
        StageModel stage = stageRepository.findById(stageId)
                .orElseThrow(() -> new ResourceNotFoundException("Stage not found with id: " + stageId));

        int requestedCount = request.getConvokeCount() == null ? 0 : request.getConvokeCount();
        if (requestedCount <= 0) {
            throw new ValidationException(List.of("Informe uma quantidade valida para convocacao."));
        }

        LocalDate deadline = parseDeadline(request.getDeadline());
        if (request.getDeadline() != null && deadline == null) {
            throw new ValidationException(List.of("Prazo para confirmacao invalido."));
        }

        List<StageCandidateModel> waitlistCandidates = stageCandidateRepository.findByStageId(stageId).stream()
                .filter(candidate -> candidate.getStatus() == StageStatus.LISTA_ESPERA)
                .sorted(Comparator
                        .comparing((StageCandidateModel candidate) -> candidate.getCreatedAt(), Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(candidate -> candidate.getPeople() != null ? candidate.getPeople().getName() : "", String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<StageCandidateModel> selectedCandidates = waitlistCandidates.stream()
                .limit(requestedCount)
                .toList();

        List<String> candidateNames = new ArrayList<>();
        for (StageCandidateModel candidate : selectedCandidates) {
            candidate.setStatus(StageStatus.EM_ANALISE);
            candidate.setNotes(buildWaitlistNotes(candidate.getNotes(), deadline, request.getNotes()));
            stageCandidateRepository.save(candidate);
            candidateNames.add(candidate.getPeople() != null ? candidate.getPeople().getName() : "Candidato");
            sendWaitlistNotification(stage, candidate, deadline, request.getNotes());
        }

        return new WaitlistConvokeResponseDTO(requestedCount, selectedCandidates.size(), candidateNames);
    }

    private List<CandidateImportDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<CandidateImportDTO> candidatesList = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                CandidateImportDTO candidate = new CandidateImportDTO();
                candidate.setRow(i + 1);
                candidate.setName(getCellValueAsString(row.getCell(0)));
                candidate.setEmail(getCellValueAsString(row.getCell(1)));
                candidate.setCpf(getCellValueAsString(row.getCell(2)));
                candidate.setStatus(parseStatus(getCellValueAsString(row.getCell(3))));
                candidate.setNotes(getCellValueAsString(row.getCell(4)));

                if (candidate.getName() != null && !candidate.getName().isEmpty()
                        && candidate.getEmail() != null && !candidate.getEmail().isEmpty()) {
                    candidatesList.add(candidate);
                }
            }
        }

        return candidatesList;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

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
            return StageStatus.APROVADO;
        }

        String normalized = normalizeText(statusStr);
        if (normalized.contains("espera")) {
            return StageStatus.LISTA_ESPERA;
        }
        if (normalized.contains("analise")) {
            return StageStatus.EM_ANALISE;
        }
        if (normalized.contains("aprov")) {
            return StageStatus.APROVADO;
        }
        if (normalized.contains("reprov") || normalized.contains("naosele") || normalized.contains("nao selecion") || normalized.contains("desclass")) {
            return StageStatus.REPROVADO;
        }

        try {
            return StageStatus.valueOf(statusStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return StageStatus.APROVADO;
        }
    }

    @Transactional
    public CandidateImportResponseDTO importCandidatesInBatches(StageModel stage, List<CandidateImportDTO> candidatesList) {
        List<String> duplicateCandidates = new ArrayList<>();
        List<String> createdPeople = new ArrayList<>();
        int successfullyInserted = 0;
        int newPeopleCreated = 0;
        List<CandidateRowErrorDTO> rowErrors = new ArrayList<>();

        List<String> allEmails = candidatesList.stream()
                .map(CandidateImportDTO::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .distinct()
                .toList();

        List<PeopleModel> existingPeople = peopleRepository.findAllByEmailIn(allEmails);
        Map<String, PeopleModel> peopleByEmail = existingPeople.stream()
                .collect(Collectors.toMap(PeopleModel::getEmail, person -> person));

        List<StageCandidateModel> existingCandidates = stageCandidateRepository.findByStageId(stage.getId());
        Set<Long> existingPeopleIds = existingCandidates.stream()
                .map(candidate -> candidate.getPeople().getId())
                .collect(Collectors.toSet());

        List<StageCandidateModel> candidatesToInsert = new ArrayList<>();

        for (CandidateImportDTO candidateDTO : candidatesList) {
            PeopleModel person = peopleByEmail.get(candidateDTO.getEmail());

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

            if (existingPeopleIds.contains(person.getId())) {
                duplicateCandidates.add(person.getName());
                continue;
            }

            try {
                List<String> alerts = peopleIntegrationService.detectActiveConflicts(person.getId(), stage.getClassModel().getId());
                boolean hasNivelamentoConflict = alerts.stream().anyMatch(alert -> alert != null && alert.toLowerCase().contains("nivelament"));
                if (hasNivelamentoConflict) {
                    rowErrors.add(new CandidateRowErrorDTO(candidateDTO.getRow(), alerts));
                    continue;
                }
            } catch (Exception ex) {
                rowErrors.add(new CandidateRowErrorDTO(candidateDTO.getRow(), List.of("Erro ao validar conflito: " + ex.getMessage())));
                continue;
            }

            StageCandidateModel candidate = new StageCandidateModel();
            candidate.setStage(stage);
            candidate.setPeople(person);
            candidate.setStatus(candidateDTO.getStatus() != null ? candidateDTO.getStatus() : StageStatus.APROVADO);
            candidate.setNotes(candidateDTO.getNotes());

            candidatesToInsert.add(candidate);
            existingPeopleIds.add(person.getId());
        }

        if (!candidatesToInsert.isEmpty()) {
            stageCandidateRepository.saveAll(candidatesToInsert);
            successfullyInserted = candidatesToInsert.size();
        }

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

    private StageCandidateModel resolveCandidate(
            Map<String, StageCandidateModel> candidatesByCpf,
            Map<String, StageCandidateModel> candidatesByName,
            String cpf,
            String name
    ) {
        String normalizedCpf = normalizeDocument(cpf);
        if (!normalizedCpf.isBlank() && candidatesByCpf.containsKey(normalizedCpf)) {
            return candidatesByCpf.get(normalizedCpf);
        }

        String normalizedName = normalizeText(name);
        if (!normalizedName.isBlank() && candidatesByName.containsKey(normalizedName)) {
            return candidatesByName.get(normalizedName);
        }

        return null;
    }

    private List<String> detectActiveConflicts(StageCandidateModel candidate, StageModel stage) {
        if (candidate == null || candidate.getPeople() == null || candidate.getPeople().getId() == null) {
            return List.of();
        }

        try {
            return peopleIntegrationService.detectActiveConflicts(candidate.getPeople().getId(), stage.getClassModel().getId());
        } catch (Exception ex) {
            return List.of();
        }
    }

    private LocalDate parseDeadline(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String trimmed = value.trim();
        try {
            if (trimmed.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(trimmed);
            }
            if (trimmed.matches("\\d{2}/\\d{2}/\\d{4}")) {
                return LocalDate.parse(trimmed, BRAZILIAN_DATE_FORMATTER);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String buildWaitlistNotes(String previousNotes, LocalDate deadline, String notes) {
        StringBuilder builder = new StringBuilder();
        if (previousNotes != null && !previousNotes.isBlank()) {
            builder.append(previousNotes.trim());
        }

        if (builder.length() > 0) {
            builder.append(" | ");
        }

        builder.append("Convocado da lista de espera");
        if (deadline != null) {
            builder.append(" ate ").append(deadline.format(BRAZILIAN_DATE_FORMATTER));
        }
        if (notes != null && !notes.isBlank()) {
            builder.append(". ").append(notes.trim());
        }

        return builder.toString();
    }

    private void sendWaitlistNotification(StageModel stage, StageCandidateModel candidate, LocalDate deadline, String notes) {
        if (!emailService.isMailConfigured()) {
            return;
        }

        String recipient = candidate.getPeople() != null ? candidate.getPeople().getEmail() : null;
        if (recipient == null || recipient.isBlank()) {
            return;
        }

        String candidateName = candidate.getPeople().getName() == null ? "Candidato" : candidate.getPeople().getName();
        String classCode = stage.getClassModel() != null ? stage.getClassModel().getCode() : "turma";
        String deadlineLabel = deadline != null ? deadline.format(BRAZILIAN_DATE_FORMATTER) : "consulte o edital";

        StringBuilder html = new StringBuilder();
        html.append("<p>Ola, ").append(candidateName).append(".</p>");
        html.append("<p>Voce foi convocado da lista de espera da turma <strong>").append(classCode).append("</strong>.</p>");
        html.append("<p>Prazo para confirmacao: <strong>").append(deadlineLabel).append("</strong>.</p>");
        if (notes != null && !notes.isBlank()) {
            html.append("<p>Observacoes: ").append(notes.trim()).append("</p>");
        }
        html.append("<p>Se necessario, entre em contato com a equipe do programa.</p>");

        try {
            emailService.sendEmailSync(recipient, "Convocacao da lista de espera - BRISA", html.toString());
        } catch (Exception ignored) {
        }
    }

    private String normalizeDocument(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("\\D", "");
    }

    private String normalizeText(String value) {
        return excelImportHelper.normalize(value).replace(" ", "");
    }
}
