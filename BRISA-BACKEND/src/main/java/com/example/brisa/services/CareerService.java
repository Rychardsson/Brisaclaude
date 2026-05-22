package com.example.brisa.services;

import com.example.brisa.dtos.career.CareerAutomationSettingsRequestDTO;
import com.example.brisa.dtos.career.CareerAutomationSettingsResponseDTO;
import com.example.brisa.dtos.career.CareerFollowUpRequestDTO;
import com.example.brisa.dtos.career.CareerFollowUpResponseDTO;
import com.example.brisa.enums.LogAction;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.CareerAutomationDispatchModel;
import com.example.brisa.models.CareerAutomationSettingsModel;
import com.example.brisa.models.CareerProgressionModel;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.repositories.CareerAutomationDispatchRepository;
import com.example.brisa.repositories.CareerAutomationSettingsRepository;
import com.example.brisa.repositories.CareerProgressionRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.ProgramRepository;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
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
    private static final String DEFAULT_AUTOMATION_SUBJECT = "Acompanhamento de carreira - BRISA";
    private static final String DEFAULT_AUTOMATION_MESSAGE = "Ola! Queremos saber como esta sua trajetoria profissional apos a conclusao do programa. Seu retorno ajuda no acompanhamento dos egressos.";
    private static final String AUTOMATION_ENTITY_TYPE = "CareerAutomation";
    private static final String DISPATCH_STATUS_SENT = "SENT";
    private static final String DISPATCH_STATUS_FAILED = "FAILED";

    private final CareerProgressionRepository careerProgressionRepository;
    private final CareerAutomationSettingsRepository careerAutomationSettingsRepository;
    private final CareerAutomationDispatchRepository careerAutomationDispatchRepository;
    private final PeopleRepository peopleRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassRepository classRepository;
    private final ProgramRepository programRepository;
    private final SystemLogService systemLogService;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<CareerFollowUpResponseDTO> getFollowUps(Long peopleId, Long classId, Long programId) {
        return careerProgressionRepository.findAllWithRelations(peopleId, classId, programId).stream()
                .sorted((left, right) -> {
                    int byDate = compareNullableDates(right.getSurveyDate(), left.getSurveyDate());
                    if (byDate != 0) {
                        return byDate;
                    }
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
                    .orElseThrow(() -> new ResourceNotFoundException("Matrícula não encontrada com id: " + request.getEnrollmentId()));
        }

        PeopleModel person = resolvePerson(request.getPeopleId(), enrollment);
        ClassModel classModel = resolveClass(request.getClassId(), enrollment);
        ProgramModel program = resolveProgram(request.getProgramId(), classModel, enrollment);

        if (enrollment != null && person != null && !person.getId().equals(enrollment.getPeople().getId())) {
            throw new ValidationException(List.of("O peopleId informado não corresponde à matrícula selecionada."));
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

        logAutomationEvent(
                LogAction.SYSTEM_INFO,
                "Configuração da automação de carreira atualizada",
                "UPDATE",
                saved,
                userId,
                httpRequest,
                Map.of("enabled", saved.isEnabled())
        );

        return toAutomationResponse(saved);
    }

    public CareerAutomationSettingsResponseDTO registerAutomationTest(
            CareerAutomationSettingsRequestDTO request,
            UUID userId,
            String testRecipientEmail,
            HttpServletRequest httpRequest
    ) {
        CareerAutomationSettingsModel settings = loadOrCreateAutomationSettings();
        applyAutomationSettings(settings, request);
        CareerAutomationSettingsModel savedSettings = careerAutomationSettingsRepository.save(settings);

        if (!emailService.isMailConfigured()) {
            throw new ValidationException(List.of("O servidor de e-mail ainda não está configurado para enviar os acompanhamentos da carreira."));
        }

        String recipientEmail = trimToNull(testRecipientEmail);
        if (recipientEmail == null) {
            throw new ValidationException(List.of("Não foi possível identificar o e-mail do usuário autenticado para o teste da automação."));
        }

        String subject = buildAutomationSubject(savedSettings, true, null);
        String htmlContent = buildAutomationEmailHtml(savedSettings, "Equipe BRISA", null, null, null, true);

        try {
            emailService.sendEmailSync(recipientEmail, subject, htmlContent);
        } catch (MessagingException error) {
            logAutomationEvent(
                    LogAction.SYSTEM_WARNING,
                    "Falha ao enviar o e-mail de teste da automacao de carreira",
                    "TEST_FAILED",
                    savedSettings,
                    userId,
                    httpRequest,
                    Map.of(
                            "recipientEmail", recipientEmail,
                            "errorMessage", truncateForLog(error.getMessage(), 900)
                    )
            );

            throw new ValidationException(List.of("Nao foi possivel enviar o e-mail de teste agora. Verifique a configuracao do servidor de e-mail."));
        }

        savedSettings.setLastTestAt(LocalDateTime.now());
        CareerAutomationSettingsModel updatedSettings = careerAutomationSettingsRepository.save(savedSettings);

        logAutomationEvent(
                LogAction.SYSTEM_INFO,
                "E-mail de teste da automacao de carreira enviado",
                "TEST",
                updatedSettings,
                userId,
                httpRequest,
                Map.of("recipientEmail", recipientEmail)
        );

        return toAutomationResponse(updatedSettings);
    }

    public void processScheduledAutomation() {
        CareerAutomationSettingsModel settings = loadOrCreateAutomationSettings();
        List<Integer> checkpoints = parseCheckpointCsv(settings.getCheckpointsCsv());
        if (!settings.isEnabled() || checkpoints.isEmpty()) {
            return;
        }

        List<EnrollmentModel> scopedEnrollments = loadScopedCompletedStudentEnrollments(settings);
        if (scopedEnrollments.isEmpty()) {
            return;
        }

        Map<Long, Map<Integer, CareerAutomationDispatchModel>> dispatchesByEnrollment = loadDispatchesByEnrollment(scopedEnrollments);
        List<AutomationCandidate> candidates = buildDueCandidates(scopedEnrollments, checkpoints, dispatchesByEnrollment);
        if (candidates.isEmpty()) {
            return;
        }

        if (!emailService.isMailConfigured()) {
            logAutomationEvent(
                    LogAction.SYSTEM_WARNING,
                    "Automacao de carreira pausada por falta de configuracao do servidor de e-mail",
                    "SKIPPED",
                    settings,
                    null,
                    null,
                    Map.of(
                            "eligibleCount", candidates.size(),
                            "checkpoints", checkpoints
                    )
            );
            return;
        }

        executeAutomationBatch(settings, checkpoints, candidates);
    }

    private void executeAutomationBatch(
            CareerAutomationSettingsModel settings,
            List<Integer> checkpoints,
            List<AutomationCandidate> candidates
    ) {
        int sentCount = 0;
        int failedCount = 0;
        List<String> recipients = new ArrayList<>();
        List<Integer> processedCheckpoints = new ArrayList<>();
        String firstError = null;

        for (AutomationCandidate candidate : candidates) {
            CareerAutomationDispatchModel dispatch = candidate.dispatch() != null
                    ? candidate.dispatch()
                    : new CareerAutomationDispatchModel();

            LocalDateTime attemptTime = LocalDateTime.now();
            dispatch.setPeople(candidate.person());
            dispatch.setEnrollment(candidate.enrollment());
            dispatch.setClassModel(candidate.classModel());
            dispatch.setProgram(candidate.program());
            dispatch.setCheckpointMonths(candidate.checkpointMonths());
            dispatch.setDueDate(candidate.dueDate());
            dispatch.setRecipientEmail(candidate.person().getEmail());
            dispatch.setSubjectSnapshot(buildAutomationSubject(settings, false, candidate.checkpointMonths()));
            dispatch.setAttemptCount((dispatch.getAttemptCount() == null ? 0 : dispatch.getAttemptCount()) + 1);
            dispatch.setLastAttemptAt(attemptTime);

            try {
                emailService.sendEmailSync(
                        candidate.person().getEmail(),
                        dispatch.getSubjectSnapshot(),
                        buildAutomationEmailHtml(
                                settings,
                                candidate.person().getName(),
                                candidate.program(),
                                candidate.classModel(),
                                candidate.checkpointMonths(),
                                false
                        )
                );

                dispatch.setStatus(DISPATCH_STATUS_SENT);
                dispatch.setSentAt(attemptTime);
                dispatch.setLastError(null);
                sentCount++;
                recipients.add(candidate.person().getEmail());
            } catch (MessagingException error) {
                dispatch.setStatus(DISPATCH_STATUS_FAILED);
                dispatch.setLastError(truncateForLog(error.getMessage(), 900));
                failedCount++;
                if (firstError == null) {
                    firstError = truncateForLog(error.getMessage(), 300);
                }
            }

            processedCheckpoints.add(candidate.checkpointMonths());
            careerAutomationDispatchRepository.save(dispatch);
        }

        String operation = failedCount > 0 && sentCount == 0 ? "SEND_FAILED" : "SEND";
        String description = failedCount > 0
                ? String.format(
                        Locale.ROOT,
                        "Disparo automatico da carreira executado com %d envio(s) e %d falha(s)",
                        sentCount,
                        failedCount
                )
                : String.format(Locale.ROOT, "Disparo automatico da carreira executado com %d envio(s)", sentCount);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("sentCount", sentCount);
        details.put("failedCount", failedCount);
        details.put("eligibleCount", candidates.size());
        details.put("checkpoints", processedCheckpoints.stream().distinct().sorted().toList());
        if (!recipients.isEmpty()) {
            details.put("recipientsPreview", recipients.stream().limit(5).toList());
        }
        if (firstError != null) {
            details.put("errorMessage", firstError);
        }

        logAutomationEvent(
                failedCount > 0 ? LogAction.SYSTEM_WARNING : LogAction.SYSTEM_INFO,
                description,
                operation,
                settings,
                null,
                null,
                details
        );
    }

    private List<AutomationCandidate> buildDueCandidates(
            List<EnrollmentModel> enrollments,
            List<Integer> checkpoints,
            Map<Long, Map<Integer, CareerAutomationDispatchModel>> dispatchesByEnrollment
    ) {
        LocalDate today = LocalDate.now();
        List<Integer> descendingCheckpoints = checkpoints.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted(Comparator.reverseOrder())
                .toList();

        List<AutomationCandidate> candidates = new ArrayList<>();

        for (EnrollmentModel enrollment : enrollments) {
            if (enrollment.getCompletionDate() == null) {
                continue;
            }

            Map<Integer, CareerAutomationDispatchModel> dispatchMap = dispatchesByEnrollment.getOrDefault(enrollment.getId(), Map.of());

            for (Integer checkpoint : descendingCheckpoints) {
                LocalDate dueDate = enrollment.getCompletionDate().plusMonths(checkpoint);
                if (dueDate.isAfter(today)) {
                    continue;
                }

                CareerAutomationDispatchModel dispatch = dispatchMap.get(checkpoint);
                if (dispatch != null && DISPATCH_STATUS_SENT.equalsIgnoreCase(String.valueOf(dispatch.getStatus()))) {
                    continue;
                }

                candidates.add(new AutomationCandidate(
                        enrollment,
                        enrollment.getPeople(),
                        enrollment.getClassModel(),
                        enrollment.getClassModel() != null ? enrollment.getClassModel().getProgram() : null,
                        checkpoint,
                        dueDate,
                        dispatch
                ));
                break;
            }
        }

        return candidates;
    }

    private Map<Long, Map<Integer, CareerAutomationDispatchModel>> loadDispatchesByEnrollment(List<EnrollmentModel> enrollments) {
        List<Long> enrollmentIds = enrollments.stream()
                .map(EnrollmentModel::getId)
                .filter(Objects::nonNull)
                .toList();

        if (enrollmentIds.isEmpty()) {
            return Map.of();
        }

        return careerAutomationDispatchRepository.findByEnrollment_IdIn(enrollmentIds).stream()
                .filter(item -> item.getEnrollment() != null && item.getEnrollment().getId() != null && item.getCheckpointMonths() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getEnrollment().getId(),
                        Collectors.toMap(
                                CareerAutomationDispatchModel::getCheckpointMonths,
                                item -> item,
                                (left, right) -> right
                        )
                ));
    }

    private List<EnrollmentModel> loadScopedCompletedStudentEnrollments(CareerAutomationSettingsModel settings) {
        return enrollmentRepository.findAllWithRelations().stream()
                .filter(this::isStudentEnrollment)
                .filter(item -> item.getCompletionDate() != null)
                .filter(item -> item.getPeople() != null && !isBlank(item.getPeople().getEmail()))
                .filter(item -> matchesAutomationScope(item, settings))
                .toList();
    }

    private boolean matchesAutomationScope(EnrollmentModel enrollment, CareerAutomationSettingsModel settings) {
        if (settings.getProgram() != null) {
            Long programId = enrollment.getClassModel() != null && enrollment.getClassModel().getProgram() != null
                    ? enrollment.getClassModel().getProgram().getId()
                    : null;
            if (!Objects.equals(programId, settings.getProgram().getId())) {
                return false;
            }
        }

        if (settings.getClassModel() != null) {
            Long classId = enrollment.getClassModel() != null ? enrollment.getClassModel().getId() : null;
            return Objects.equals(classId, settings.getClassModel().getId());
        }

        return true;
    }

    private boolean isStudentEnrollment(EnrollmentModel enrollment) {
        return enrollment.getAcademicRole() != null
                && "aluno".equals(normalizeText(enrollment.getAcademicRole().getName()));
    }

    private String buildAutomationEmailHtml(
            CareerAutomationSettingsModel settings,
            String recipientName,
            ProgramModel program,
            ClassModel classModel,
            Integer checkpointMonths,
            boolean testMessage
    ) {
        String safeRecipientName = HtmlUtils.htmlEscape(defaultIfBlank(recipientName, "participante"));
        String safeProgramName = HtmlUtils.htmlEscape(program != null && !isBlank(program.getName()) ? program.getName() : "BRISA");
        String safeClassCode = HtmlUtils.htmlEscape(classModel != null && !isBlank(classModel.getCode()) ? classModel.getCode() : "turma em acompanhamento");
        String safeBaseMessage = HtmlUtils.htmlEscape(defaultIfBlank(settings.getMessage(), DEFAULT_AUTOMATION_MESSAGE))
                .replace("\n", "<br>");

        String checkpointCopy = testMessage
                ? "Este e um e-mail de teste da automacao de acompanhamento da carreira."
                : String.format(
                        Locale.ROOT,
                        "Este contato faz parte do checkpoint de %d meses apos a conclusao do seu ciclo no programa.",
                        checkpointMonths
                );

        String safeCheckpointCopy = HtmlUtils.htmlEscape(checkpointCopy);

        return """
                <html>
                  <body style="margin:0;padding:24px;background:#f4f7fb;font-family:Arial,sans-serif;color:#13233f;">
                    <div style="max-width:640px;margin:0 auto;background:#ffffff;border:1px solid #dfe7f1;border-radius:20px;overflow:hidden;">
                      <div style="padding:24px 28px;background:linear-gradient(135deg,#0f766e 0%,#14b8a6 100%);color:#ffffff;">
                        <p style="margin:0 0 8px;font-size:12px;letter-spacing:0.18em;text-transform:uppercase;font-weight:700;">BRISA</p>
                        <h1 style="margin:0;font-size:26px;line-height:1.2;">Acompanhamento de carreira</h1>
                      </div>
                      <div style="padding:28px;">
                        <p style="margin:0 0 16px;font-size:16px;">Ola, %s!</p>
                        <p style="margin:0 0 14px;font-size:15px;line-height:1.6;">%s</p>
                        <p style="margin:0 0 14px;font-size:15px;line-height:1.6;">%s</p>
                        <div style="margin:18px 0;padding:16px 18px;border:1px solid #d8e7f0;border-radius:16px;background:#f8fbff;">
                          <p style="margin:0 0 8px;font-size:12px;text-transform:uppercase;letter-spacing:0.12em;color:#64748b;font-weight:700;">Escopo do acompanhamento</p>
                          <p style="margin:0;font-size:14px;line-height:1.6;"><strong>Programa:</strong> %s<br><strong>Turma:</strong> %s</p>
                        </div>
                        <p style="margin:0 0 14px;font-size:15px;line-height:1.6;">Se possivel, responda este e-mail com sua situacao profissional atual. Seu retorno ajuda o BRISA a acompanhar o impacto real da formacao.</p>
                        <p style="margin:24px 0 0;font-size:14px;line-height:1.6;">Abracos,<br><strong>Equipe BRISA</strong></p>
                      </div>
                    </div>
                  </body>
                </html>
                """.formatted(
                safeRecipientName,
                safeBaseMessage,
                safeCheckpointCopy,
                safeProgramName,
                safeClassCode
        );
    }

    private String buildAutomationSubject(CareerAutomationSettingsModel settings, boolean testMessage, Integer checkpointMonths) {
        String baseSubject = defaultIfBlank(trimToNull(settings.getSubject()), DEFAULT_AUTOMATION_SUBJECT);

        if (testMessage) {
            return "[Teste] " + baseSubject;
        }

        if (checkpointMonths == null) {
            return baseSubject;
        }

        return baseSubject + " - " + checkpointMonths + " meses";
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
            settings.setSubject(DEFAULT_AUTOMATION_SUBJECT);
            settings.setMessage(DEFAULT_AUTOMATION_MESSAGE);
            settings.setCheckpointsCsv(DEFAULT_CHECKPOINTS.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
            return careerAutomationSettingsRepository.save(settings);
        });
    }

    private void applyAutomationSettings(CareerAutomationSettingsModel settings, CareerAutomationSettingsRequestDTO request) {
        settings.setEnabled(request.isEnabled());
        settings.setProgram(resolveProgramForSettings(request.getProgramId()));
        settings.setClassModel(resolveClassForSettings(request.getClassId(), settings.getProgram()));
        settings.setSubject(defaultIfBlank(trimToNull(request.getSubject()), DEFAULT_AUTOMATION_SUBJECT));
        settings.setMessage(defaultIfBlank(trimToNull(request.getMessage()), DEFAULT_AUTOMATION_MESSAGE));
        settings.setCheckpointsCsv(normalizeCheckpoints(request.getCheckpoints()));
    }

    private void logAutomationEvent(
            LogAction action,
            String description,
            String operation,
            CareerAutomationSettingsModel settings,
            UUID userId,
            HttpServletRequest httpRequest,
            Map<String, Object> extras
    ) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("operation", operation);
        details.put("enabled", settings.isEnabled());
        details.put("programId", settings.getProgram() != null ? settings.getProgram().getId() : null);
        details.put("classId", settings.getClassModel() != null ? settings.getClassModel().getId() : null);
        details.put("checkpoints", parseCheckpointCsv(settings.getCheckpointsCsv()));
        if (extras != null && !extras.isEmpty()) {
            details.putAll(extras);
        }

        systemLogService.createLog(
                action,
                description,
                AUTOMATION_ENTITY_TYPE,
                settings.getId() != null ? settings.getId().toString() : null,
                userId,
                httpRequest,
                details
        );
    }

    private ProgramModel resolveProgramForSettings(Long programId) {
        if (programId == null) {
            return null;
        }

        return programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Programa nao encontrado com id: " + programId));
    }

    private ClassModel resolveClassForSettings(Long classId, ProgramModel scopedProgram) {
        if (classId == null) {
            return null;
        }

        ClassModel classModel = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada com id: " + classId));

        if (scopedProgram != null && (classModel.getProgram() == null || !scopedProgram.getId().equals(classModel.getProgram().getId()))) {
            throw new ValidationException(List.of("A turma selecionada nao pertence ao programa escolhido para a automacao."));
        }

        return classModel;
    }

    private PeopleModel resolvePerson(Long peopleId, EnrollmentModel enrollment) {
        if (enrollment != null) {
            return enrollment.getPeople();
        }
        if (peopleId == null) {
            throw new ValidationException(List.of("Nao foi possivel resolver a pessoa do acompanhamento."));
        }
        return peopleRepository.findById(peopleId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa nao encontrada com id: " + peopleId));
    }

    private ClassModel resolveClass(Long classId, EnrollmentModel enrollment) {
        if (enrollment != null) {
            return enrollment.getClassModel();
        }
        if (classId == null) {
            return null;
        }
        return classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada com id: " + classId));
    }

    private ProgramModel resolveProgram(Long programId, ClassModel classModel, EnrollmentModel enrollment) {
        if (classModel != null && classModel.getProgram() != null) {
            return classModel.getProgram();
        }
        if (enrollment != null && enrollment.getClassModel() != null) {
            return enrollment.getClassModel().getProgram();
        }
        if (programId == null) {
            return null;
        }
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
        if ("empregado".equals(normalized)) {
            return "EMPREGADO";
        }
        if ("desempregado".equals(normalized)) {
            return "DESEMPREGADO";
        }
        if ("sem resposta".equals(normalized) || "sem_resposta".equals(normalized)) {
            return "SEM_RESPOSTA";
        }
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
        if (isBlank(csv)) {
            return DEFAULT_CHECKPOINTS;
        }

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

    private String truncateForLog(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int compareNullableDates(LocalDate left, LocalDate right) {
        if (left == null && right == null) {
            return 0;
        }
        if (left == null) {
            return -1;
        }
        if (right == null) {
            return 1;
        }
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

    private record AutomationCandidate(
            EnrollmentModel enrollment,
            PeopleModel person,
            ClassModel classModel,
            ProgramModel program,
            Integer checkpointMonths,
            LocalDate dueDate,
            CareerAutomationDispatchModel dispatch
    ) {
    }
}
