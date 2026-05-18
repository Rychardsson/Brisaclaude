package com.example.brisa.services;

import com.example.brisa.dtos.career.CareerAutomationSettingsRequestDTO;
import com.example.brisa.dtos.career.CareerAutomationSettingsResponseDTO;
import com.example.brisa.dtos.career.CareerFollowUpRequestDTO;
import com.example.brisa.dtos.career.CareerFollowUpResponseDTO;
import com.example.brisa.enums.LogAction;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.CareerAutomationSettingsModel;
import com.example.brisa.models.CareerProgressionModel;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.repositories.CareerAutomationSettingsRepository;
import com.example.brisa.repositories.CareerProgressionRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.ProgramRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CareerService {

    private static final List<Integer> DEFAULT_CHECKPOINTS = List.of(6, 12, 18, 24);

    private final CareerProgressionRepository careerProgressionRepository;
    private final CareerAutomationSettingsRepository careerAutomationSettingsRepository;
    private final PeopleRepository peopleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final ProgramRepository programRepository;
    private final SystemLogService systemLogService;

    @Transactional(readOnly = true)
    public List<CareerFollowUpResponseDTO> getFollowUps(Long peopleId, Long classId, Long programId) {
        return careerProgressionRepository.findAllWithRelations(peopleId, classId, programId).stream()
                .sorted((left, right) -> {
                    int byDate = compareNullableDates(right.getSurveyDate(), left.getSurveyDate());
                    if (byDate != 0) return byDate;
                    return Long.compare(right.getId(), left.getId());
                })
                .map(this::toFollowUpResponse)
                .toList();
    }

    @Transactional
    public CareerFollowUpResponseDTO createFollowUp(
            CareerFollowUpRequestDTO request,
            UUID userId,
            HttpServletRequest httpRequest
    ) {
        List<String> errors = new ArrayList<>();

        if (request.getPeopleId() == null && request.getEnrollmentId() == null) {
            errors.add("Informe peopleId ou enrollmentId para registrar o acompanhamento.");
        }

        LocalDate surveyDate = parseRequiredDate(request.getSurveyDate(), "A data da leitura e obrigatoria.", errors);
        String normalizedStatus = normalizeStatus(request.getStatus());
        if (normalizedStatus == null) {
            errors.add("Status invalido. Use EMPREGADO, DESEMPREGADO ou SEM_RESPOSTA.");
        }
        if ("EMPREGADO".equals(normalizedStatus) && isBlank(request.getPosition())) {
            errors.add("Informe o cargo quando o status for empregado.");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        EnrollmentModel enrollment = null;
        if (request.getEnrollmentId() != null) {
            enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Matricula nao encontrada com id: " + request.getEnrollmentId()));
        }

        PeopleModel person = resolvePerson(request.getPeopleId(), enrollment);
        ClassModel classModel = resolveClass(request.getClassId(), enrollment);
        ProgramModel program = resolveProgram(request.getProgramId(), classModel, enrollment);

        if (enrollment != null && person != null && !person.getId().equals(enrollment.getPeople().getId())) {
            throw new ValidationException(List.of("O peopleId informado nao corresponde a matricula selecionada."));
        }

        CareerProgressionModel entity = new CareerProgressionModel();
        entity.setPeople(person);
        entity.setEnrollment(enrollment);
        entity.setClassModel(classModel);
        entity.setProgram(program);
        entity.setSurveyDate(surveyDate);
        entity.setStatus(normalizedStatus);
        entity.setCompany(trimToNull(request.getCompany()));
        entity.setPosition(trimToNull(request.getPosition()));
        entity.setNotes(trimToNull(request.getNotes()));

        CareerProgressionModel saved = careerProgressionRepository.save(entity);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("operation", "CREATE");
        details.put("status", saved.getStatus());
        details.put("surveyDate", String.valueOf(saved.getSurveyDate()));
        details.put("peopleId", saved.getPeople() != null ? saved.getPeople().getId() : null);
        details.put("classId", saved.getClassModel() != null ? saved.getClassModel().getId() : null);
        details.put("programId", saved.getProgram() != null ? saved.getProgram().getId() : null);

        systemLogService.createLog(
                LogAction.SYSTEM_INFO,
                "Acompanhamento de carreira registrado para " + person.getName(),
                "CareerProgression",
                saved.getId().toString(),
                userId,
                httpRequest,
                details
        );

        return toFollowUpResponse(saved);
    }

    @Transactional
    public CareerAutomationSettingsResponseDTO getAutomationSettings() {
        return toAutomationResponse(loadOrCreateAutomationSettings());
    }

    @Transactional
    public CareerAutomationSettingsResponseDTO updateAutomationSettings(
            CareerAutomationSettingsRequestDTO request,
            UUID userId,
            HttpServletRequest httpRequest
    ) {
        CareerAutomationSettingsModel settings = loadOrCreateAutomationSettings();
        applyAutomationSettings(settings, request);
        CareerAutomationSettingsModel saved = careerAutomationSettingsRepository.save(settings);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("operation", "UPDATE");
        details.put("enabled", saved.isEnabled());
        details.put("programId", saved.getProgram() != null ? saved.getProgram().getId() : null);
        details.put("classId", saved.getClassModel() != null ? saved.getClassModel().getId() : null);
        details.put("checkpoints", parseCheckpointCsv(saved.getCheckpointsCsv()));

        systemLogService.createLog(
                LogAction.SYSTEM_INFO,
                "Configuracao da automacao de carreira atualizada",
                "CareerAutomation",
                saved.getId().toString(),
                userId,
                httpRequest,
                details
        );

        return toAutomationResponse(saved);
    }

    @Transactional
    public CareerAutomationSettingsResponseDTO registerAutomationTest(
            CareerAutomationSettingsRequestDTO request,
            UUID userId,
            HttpServletRequest httpRequest
    ) {
        CareerAutomationSettingsModel settings = loadOrCreateAutomationSettings();
        applyAutomationSettings(settings, request);
        settings.setLastTestAt(java.time.LocalDateTime.now());
        CareerAutomationSettingsModel saved = careerAutomationSettingsRepository.save(settings);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("operation", "TEST");
        details.put("enabled", saved.isEnabled());
        details.put("programId", saved.getProgram() != null ? saved.getProgram().getId() : null);
        details.put("classId", saved.getClassModel() != null ? saved.getClassModel().getId() : null);
        details.put("checkpoints", parseCheckpointCsv(saved.getCheckpointsCsv()));

        systemLogService.createLog(
                LogAction.SYSTEM_INFO,
                "Teste da automacao de carreira registrado",
                "CareerAutomation",
                saved.getId().toString(),
                userId,
                httpRequest,
                details
        );

        return toAutomationResponse(saved);
    }

    private CareerFollowUpResponseDTO toFollowUpResponse(CareerProgressionModel model) {
        return CareerFollowUpResponseDTO.builder()
                .id(model.getId())
                .key(buildCareerKey(
                        model.getPeople() != null ? model.getPeople().getId() : null,
                        model.getClassModel() != null ? model.getClassModel().getId() : null,
                        model.getEnrollment() != null ? model.getEnrollment().getId() : null
                ))
                .peopleId(model.getPeople() != null ? model.getPeople().getId() : null)
                .enrollmentId(model.getEnrollment() != null ? model.getEnrollment().getId() : null)
                .classId(model.getClassModel() != null ? model.getClassModel().getId() : null)
                .programId(model.getProgram() != null ? model.getProgram().getId() : null)
                .personName(model.getPeople() != null ? model.getPeople().getName() : null)
                .classCode(model.getClassModel() != null ? model.getClassModel().getCode() : null)
                .programName(model.getProgram() != null ? model.getProgram().getName() : null)
                .surveyDate(model.getSurveyDate())
                .status(model.getStatus())
                .company(model.getCompany())
                .position(model.getPosition())
                .notes(model.getNotes())
                .createdAt(model.getCreatedAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    private CareerAutomationSettingsResponseDTO toAutomationResponse(CareerAutomationSettingsModel model) {
        return CareerAutomationSettingsResponseDTO.builder()
                .id(model.getId())
                .enabled(model.isEnabled())
                .programId(model.getProgram() != null ? model.getProgram().getId() : null)
                .programName(model.getProgram() != null ? model.getProgram().getName() : null)
                .classId(model.getClassModel() != null ? model.getClassModel().getId() : null)
                .classCode(model.getClassModel() != null ? model.getClassModel().getCode() : null)
                .subject(model.getSubject())
                .message(model.getMessage())
                .checkpoints(parseCheckpointCsv(model.getCheckpointsCsv()))
                .lastTestAt(model.getLastTestAt())
                .updatedAt(model.getUpdatedAt())
                .build();
    }

    private CareerAutomationSettingsModel loadOrCreateAutomationSettings() {
        return careerAutomationSettingsRepository.findTopByOrderByIdAsc().orElseGet(() -> {
            CareerAutomationSettingsModel settings = new CareerAutomationSettingsModel();
            settings.setEnabled(true);
            settings.setSubject("Acompanhamento de carreira - BRISA");
            settings.setMessage("Ola! Queremos saber como esta sua trajetoria profissional apos a conclusao do programa. Seu retorno ajuda no acompanhamento dos egressos.");
            settings.setCheckpointsCsv(DEFAULT_CHECKPOINTS.stream().map(String::valueOf).collect(Collectors.joining(",")));
            return careerAutomationSettingsRepository.save(settings);
        });
    }

    private void applyAutomationSettings(CareerAutomationSettingsModel settings, CareerAutomationSettingsRequestDTO request) {
        settings.setEnabled(request.isEnabled());
        settings.setProgram(resolveProgramForSettings(request.getProgramId()));
        settings.setClassModel(resolveClassForSettings(request.getClassId(), settings.getProgram()));
        settings.setSubject(trimToNull(request.getSubject()));
        settings.setMessage(trimToNull(request.getMessage()));
        settings.setCheckpointsCsv(normalizeCheckpoints(request.getCheckpoints()));
    }

    private ProgramModel resolveProgramForSettings(Long programId) {
        if (programId == null) return null;
        return programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Programa nao encontrado com id: " + programId));
    }

    private ClassModel resolveClassForSettings(Long classId, ProgramModel scopedProgram) {
        if (classId == null) return null;

        ClassModel classModel = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada com id: " + classId));

        if (scopedProgram != null && (classModel.getProgram() == null || !scopedProgram.getId().equals(classModel.getProgram().getId()))) {
            throw new ValidationException(List.of("A turma selecionada nao pertence ao programa escolhido para a automacao."));
        }

        return classModel;
    }

    private PeopleModel resolvePerson(Long peopleId, EnrollmentModel enrollment) {
        if (enrollment != null) return enrollment.getPeople();
        if (peopleId == null) {
            throw new ValidationException(List.of("Nao foi possivel resolver a pessoa do acompanhamento."));
        }
        return peopleRepository.findById(peopleId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada com id: " + peopleId));
    }

    private ClassModel resolveClass(Long classId, EnrollmentModel enrollment) {
        if (enrollment != null) return enrollment.getClassModel();
        if (classId == null) return null;
        return classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada com id: " + classId));
    }

    private ProgramModel resolveProgram(Long programId, ClassModel classModel, EnrollmentModel enrollment) {
        if (classModel != null && classModel.getProgram() != null) return classModel.getProgram();
        if (enrollment != null && enrollment.getClassModel() != null) return enrollment.getClassModel().getProgram();
        if (programId == null) return null;
        return programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Programa nao encontrado com id: " + programId));
    }

    private LocalDate parseRequiredDate(String value, String message, List<String> errors) {
        if (isBlank(value)) {
            errors.add(message);
            return null;
        }

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value.trim(), formatter);
            } catch (Exception ignored) {
            }
        }

        errors.add("Data invalida para o acompanhamento.");
        return null;
    }

    private String normalizeStatus(String status) {
        String normalized = normalizeText(status);
        if ("empregado".equals(normalized)) return "EMPREGADO";
        if ("desempregado".equals(normalized)) return "DESEMPREGADO";
        if ("sem resposta".equals(normalized) || "sem_resposta".equals(normalized)) return "SEM_RESPOSTA";
        return null;
    }

    private String normalizeCheckpoints(List<Integer> checkpoints) {
        List<Integer> base = checkpoints == null || checkpoints.isEmpty() ? DEFAULT_CHECKPOINTS : checkpoints;
        return base.stream()
                .filter(item -> item != null && item > 0)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private List<Integer> parseCheckpointCsv(String csv) {
        if (isBlank(csv)) return DEFAULT_CHECKPOINTS;

        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> {
                    try {
                        return Integer.valueOf(value);
                    } catch (NumberFormatException error) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
    }

    private String normalizeText(String value) {
        return Normalizer.normalize(String.valueOf(value == null ? "" : value), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String trimToNull(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int compareNullableDates(LocalDate left, LocalDate right) {
        if (left == null && right == null) return 0;
        if (left == null) return -1;
        if (right == null) return 1;
        return left.compareTo(right);
    }

    private String buildCareerKey(Long peopleId, Long classId, Long enrollmentId) {
        return String.format(
                "%s:%s:%s",
                peopleId != null ? peopleId : "person",
                classId != null ? classId : "class",
                enrollmentId != null ? enrollmentId : "enrollment"
        );
    }
}
