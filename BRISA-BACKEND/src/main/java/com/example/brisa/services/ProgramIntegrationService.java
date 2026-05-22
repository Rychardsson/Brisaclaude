package com.example.brisa.services;

import com.example.brisa.dtos.program.ProgramClassCreateRequestDTO;
import com.example.brisa.dtos.program.ProgramClassCreateResponseDTO;
import com.example.brisa.dtos.program.ProgramClassTemplateDTO;
import com.example.brisa.dtos.program.ProgramOverviewItemDTO;
import com.example.brisa.dtos.program.ProgramOverviewResponseDTO;
import com.example.brisa.dtos.program.ProgramSummaryDTO;
import com.example.brisa.dtos.program.ProgramTabCountsDTO;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.InstitutionModel;
import com.example.brisa.models.ProgramInstitutionModel;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.StageModel;
import com.example.brisa.models.course.CourseAssignmentModel;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.CourseAssignmentRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.InstitutionRepository;
import com.example.brisa.repositories.ProgramRepository;
import com.example.brisa.repositories.ProjectGroupRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import com.example.brisa.repositories.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramIntegrationService {

    private static final List<String> DEFAULT_COTAS = List.of(
            "PCD/Neurodivergente",
            "Negro/Pardo",
            "Mulheres",
            "45+"
    );

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MMM/yyyy", new Locale("pt", "BR"));

    private final ProgramRepository programRepository;
    private final ClassRepository classRepository;
    private final InstitutionRepository institutionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final StageRepository stageRepository;

    @Transactional(readOnly = true)
    public ProgramOverviewResponseDTO getOverview(
            String tab,
            String search,
            String status,
            String etapa,
            Integer year,
            String localidade
    ) {
        List<ProgramModel> programs = programRepository.findAllWithRelations();
        List<EnrollmentModel> enrollments = enrollmentRepository.findAllWithRelations();
        List<StageCandidateModel> candidates = stageCandidateRepository.findAllWithRelations();
        Map<Long, Long> projectCountsByClass = projectGroupRepository.countGroupedByClassId().stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));

        Map<Long, List<EnrollmentModel>> enrollmentsByClass = enrollments.stream()
                .collect(Collectors.groupingBy(item -> item.getClassModel().getId()));
        Map<Long, List<StageCandidateModel>> candidatesByClass = candidates.stream()
                .collect(Collectors.groupingBy(item -> item.getStage().getClassModel().getId()));

        List<ProgramOverviewItemDTO> allItems = new ArrayList<>();
        for (ProgramModel program : programs) {
            if (program.getClasses() == null || program.getClasses().isEmpty()) {
                allItems.add(buildProgramOverviewItem(program, null, List.of(), List.of(), 0L));
                continue;
            }

            List<ClassModel> sortedClasses = program.getClasses().stream()
                    .sorted(Comparator.comparing(ClassModel::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                    .toList();

            for (ClassModel classModel : sortedClasses) {
                allItems.add(buildProgramOverviewItem(
                        program,
                        classModel,
                        enrollmentsByClass.getOrDefault(classModel.getId(), List.of()),
                        candidatesByClass.getOrDefault(classModel.getId(), List.of()),
                        projectCountsByClass.getOrDefault(classModel.getId(), 0L)
                ));
            }
        }

        List<ProgramOverviewItemDTO> filteredItems = allItems.stream()
                .filter(item -> matchesTab(item, tab))
                .filter(item -> matchesSearch(item, search))
                .filter(item -> matchesText(item.status(), status))
                .filter(item -> matchesText(item.etapaAtual(), etapa))
                .filter(item -> matchesYear(item, year))
                .filter(item -> isBlank(localidade) || contains(item.localidade(), normalize(localidade)))
                .sorted(Comparator
                        .comparing((ProgramOverviewItemDTO item) -> statusPriority(item.status()))
                        .thenComparing(ProgramOverviewItemDTO::nome, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        ProgramSummaryDTO summary = new ProgramSummaryDTO(
                allItems.size(),
                allItems.stream().filter(item -> "andamento".equalsIgnoreCase(item.status())).count(),
                allItems.stream().filter(item -> "inscricao".equalsIgnoreCase(item.status())).count(),
                allItems.stream().filter(item -> "Imersao".equalsIgnoreCase(item.etapaAtual())).count(),
                allItems.stream().filter(item -> "Nivelamento".equalsIgnoreCase(item.etapaAtual())).count(),
                allItems.stream().filter(item -> "encerrado".equalsIgnoreCase(item.status())).count()
        );

        ProgramTabCountsDTO tabs = new ProgramTabCountsDTO(
                allItems.stream().filter(item -> "andamento".equalsIgnoreCase(item.status())).count(),
                allItems.stream().filter(item -> "inscricao".equalsIgnoreCase(item.status())).count(),
                allItems.size(),
                allItems.stream().filter(item -> "encerrado".equalsIgnoreCase(item.status())).count()
        );

        return new ProgramOverviewResponseDTO(summary, tabs, filteredItems);
    }

    @Transactional(readOnly = true)
    public List<ProgramClassTemplateDTO> getClassTemplates() {
        List<ProgramModel> programs = programRepository.findAllWithRelations();
        Map<Long, List<CourseAssignmentModel>> assignmentsByClass = courseAssignmentRepository.findAllWithRelations().stream()
                .collect(Collectors.groupingBy(item -> item.getClassModel().getId()));

        return programs.stream()
                .sorted(Comparator.comparing(ProgramModel::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(program -> buildTemplate(program, assignmentsByClass))
                .toList();
    }

    @Transactional
    public ProgramClassCreateResponseDTO createClassFromProgram(Long programId, ProgramClassCreateRequestDTO request) {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getNomeTurma())) {
            errors.add("Nome da turma e obrigatorio.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        ProgramModel program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Programa nao encontrado com id: " + programId));

        if (classRepository.findByCode(request.getNomeTurma().trim()).isPresent()) {
            throw new ValidationException(List.of("Ja existe uma turma com esse nome."));
        }

        InstitutionModel location = resolveInstitution(request.getInstituicaoId(), request.getInstituicaoNome(), request.getLocalidade());
        ClassModel sourceClass = selectSourceClass(program);

        ClassModel classModel = new ClassModel();
        classModel.setCode(request.getNomeTurma().trim());
        classModel.setProgram(program);
        classModel.setLocality(request.getLocalidade());
        classModel.setLocation(location);
        classModel.setQtdVagas(request.getQtdVagas());
        classModel.setPublicationDate(request.getPublicationDate());
        classModel.setApplicationStartDate(request.getApplicationStartDate());
        classModel.setApplicationEndDate(request.getApplicationEndDate());
        classModel.setLevelingSelectionAnnouncementDate(request.getLevelingSelectionAnnouncementDate());
        classModel.setLevelingConfirmationStartDate(request.getLevelingConfirmationStartDate());
        classModel.setLevelingConfirmationEndDate(request.getLevelingConfirmationEndDate());
        classModel.setLevelingStartDate(request.getLevelingStartDate());
        classModel.setLevelingEndDate(request.getLevelingEndDate());
        classModel.setLevelingFinalExamDate(request.getLevelingFinalExamDate());
        classModel.setImmersionSelectionAnnouncementDate(request.getImmersionSelectionAnnouncementDate());
        classModel.setImmersionConfirmationStartDate(request.getImmersionConfirmationStartDate());
        classModel.setImmersionConfirmationEndDate(request.getImmersionConfirmationEndDate());
        classModel.setImmersionStartDate(request.getImmersionStartDate());
        classModel.setImmersionEndDate(request.getImmersionEndDate());
        classModel.setPartialEvaluationDate(request.getPartialEvaluationDate());
        classModel.setFinalEvaluationDate(request.getFinalEvaluationDate());
        classModel.setCertificateIssueDate(request.getCertificateIssueDate());
        classModel.setStartDate(firstNonNull(
                request.getApplicationStartDate(),
                request.getLevelingStartDate(),
                request.getImmersionStartDate(),
                program.getStartDate()
        ));
        classModel.setEndDate(firstNonNull(
                request.getCertificateIssueDate(),
                request.getFinalEvaluationDate(),
                request.getImmersionEndDate(),
                request.getLevelingEndDate(),
                program.getEndDate()
        ));

        if (sourceClass != null) {
            classModel.setDefaultSelectionCapacity(sourceClass.getDefaultSelectionCapacity());
            classModel.setDefaultLevelingCapacity(sourceClass.getDefaultLevelingCapacity());
            classModel.setDefaultImmersionCapacity(sourceClass.getDefaultImmersionCapacity());
            classModel.setImmersionTeamSize(sourceClass.getImmersionTeamSize());
            if (classModel.getQtdVagas() == null) {
                classModel.setQtdVagas(sourceClass.getQtdVagas());
            }
        }

        ClassModel saved = classRepository.save(classModel);
        ensureDefaultStages(saved);
        cloneCourseAssignments(sourceClass, saved);

        return new ProgramClassCreateResponseDTO(
                saved.getId(),
                saved.getCode(),
                program.getId(),
                program.getName(),
                saved.getLocality(),
                saved.getLocation() != null ? saved.getLocation().getId() : null
        );
    }

    private ProgramOverviewItemDTO buildProgramOverviewItem(
            ProgramModel program,
            ClassModel classModel,
            List<EnrollmentModel> enrollments,
            List<StageCandidateModel> candidates,
            long projectCount
    ) {
        long inscritos = candidates.stream()
                .map(candidate -> candidate.getPeople().getId())
                .distinct()
                .count();
        long ativos = enrollments.stream()
                .filter(this::isAlunoEnrollment)
                .filter(enrollment -> isActiveStatus(enrollment.getStatus()))
                .map(enrollment -> enrollment.getPeople().getId())
                .distinct()
                .count();
        long inscricao = countCandidatesByStage(candidates, "inscri");
        long selecao = countCandidatesByStage(candidates, "sele");
        long nivelamento = countCandidatesByStage(candidates, "nivelamento");
        long imersao = countCandidatesByStage(candidates, "imersao");
        long orientadores = enrollments.stream()
                .filter(enrollment -> enrollment.getAcademicRole() != null)
                .filter(enrollment -> enrollment.getAcademicRole().getName() != null)
                .filter(enrollment -> normalize(enrollment.getAcademicRole().getName()).contains("orient"))
                .map(enrollment -> enrollment.getPeople().getId())
                .distinct()
                .count();

        String status = resolveProgramStatus(program, classModel);
        String etapaAtual = resolveCurrentStage(status, classModel, candidates);

        return new ProgramOverviewItemDTO(
                program.getId(),
                classModel != null ? classModel.getId() : null,
                program.getName(),
                classModel != null ? defaultIfBlank(classModel.getCode(), "Sem turma") : "Sem turma",
                partnerLabel(program, classModel),
                localityLabel(classModel),
                periodLabel(program, classModel),
                program.getExecutor() != null ? program.getExecutor() : "-",
                status,
                etapaAtual,
                inscricao,
                selecao,
                inscritos,
                ativos,
                nivelamento,
                imersao,
                projectCount,
                orientadores,
                nextMilestone(classModel),
                progressPercentage(status, etapaAtual, program, classModel)
        );
    }

    private ProgramClassTemplateDTO buildTemplate(
            ProgramModel program,
            Map<Long, List<CourseAssignmentModel>> assignmentsByClass
    ) {
        ClassModel sourceClass = selectSourceClass(program);
        List<String> courseNames = sourceClass == null
                ? List.of()
                : assignmentsByClass.getOrDefault(sourceClass.getId(), List.of()).stream()
                .map(assignment -> assignment.getCourse().getName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        return new ProgramClassTemplateDTO(
                program.getId(),
                program.getName(),
                partnerLabel(program, sourceClass),
                program.getClasses() == null ? 0 : program.getClasses().size(),
                templateStatus(program),
                defaultIfBlank(program.getTargetAudience(), "Publico alvo nao informado."),
                defaultIfBlank(program.getLevelingModality(), "Remoto e assincrono"),
                defaultIfBlank(program.getLevelingDuration(), "2 meses"),
                defaultIfBlank(program.getImmersionDuration(), "6 meses"),
                firstNonNull(program.getImmersionWorkloadHours(), sourceClass != null ? sourceClass.getDefaultImmersionCapacity() : null, 480),
                firstNonNull(sourceClass != null ? sourceClass.getDefaultLevelingCapacity() : null, 250),
                firstNonNull(sourceClass != null ? sourceClass.getDefaultImmersionCapacity() : null, 50),
                parseList(program.getQuotaCriteria(), DEFAULT_COTAS),
                courseNames,
                defaultIfBlank(program.getEvaluationCriteria(), "Avaliação do grupo, pares e orientador.")
        );
    }

    private InstitutionModel resolveInstitution(Long institutionId, String institutionName, String locality) {
        if (institutionId != null) {
            return institutionRepository.findById(institutionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Instituição não encontrada com id: " + institutionId));
        }

        if (isBlank(institutionName)) {
            return null;
        }

        return institutionRepository.findByNameIgnoreCase(institutionName.trim())
                .orElseGet(() -> {
                    InstitutionModel institution = new InstitutionModel();
                    institution.setName(institutionName.trim());
                    institution.setAcronym(inferAcronym(institutionName));
                    institution.setState(inferState(locality));
                    return institutionRepository.save(institution);
                });
    }

    private ClassModel selectSourceClass(ProgramModel program) {
        if (program.getClasses() == null || program.getClasses().isEmpty()) {
            return null;
        }
        return program.getClasses().stream()
                .sorted(Comparator.comparing(
                        (ClassModel item) -> firstNonNull(item.getEndDate(), item.getImmersionEndDate(), item.getFinalEvaluationDate()),
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .findFirst()
                .orElse(null);
    }

    private void ensureDefaultStages(ClassModel classModel) {
        for (String stageName : List.of("SELECAO", "NIVELAMENTO", "IMERSAO")) {
            if (stageRepository.existsByNameAndClassModelId(stageName, classModel.getId())) {
                continue;
            }
            StageModel stage = new StageModel();
            stage.setName(stageName);
            stage.setClassModel(classModel);
            stage.setDescription(stageName);
            stageRepository.save(stage);
        }
    }

    private void cloneCourseAssignments(ClassModel sourceClass, ClassModel targetClass) {
        if (sourceClass == null || targetClass == null) {
            return;
        }

        List<CourseAssignmentModel> assignments = courseAssignmentRepository.findByClassId(sourceClass.getId());
        for (CourseAssignmentModel assignment : assignments) {
            if (assignment.getCourse() == null) {
                continue;
            }

            CourseAssignmentModel existing = courseAssignmentRepository.findByCourseIdAndClassId(
                    assignment.getCourse().getId(),
                    targetClass.getId()
            );
            if (existing != null) {
                existing.setRequired(assignment.isRequired());
                courseAssignmentRepository.save(existing);
                continue;
            }

            CourseAssignmentModel clonedAssignment = new CourseAssignmentModel();
            clonedAssignment.setClassModel(targetClass);
            clonedAssignment.setCourse(assignment.getCourse());
            clonedAssignment.setRequired(assignment.isRequired());
            courseAssignmentRepository.save(clonedAssignment);
        }
    }

    private String partnerLabel(ProgramModel program, ClassModel classModel) {
        if (program.getProgramInstitutions() != null && !program.getProgramInstitutions().isEmpty()) {
            return program.getProgramInstitutions().stream()
                    .map(ProgramInstitutionModel::getInstitution)
                    .filter(Objects::nonNull)
                    .map(institution -> defaultIfBlank(institution.getAcronym(), institution.getName()))
                    .filter(value -> !isBlank(value))
                    .findFirst()
                    .orElse("-");
        }

        if (!isBlank(program.getPartnerNames())) {
            return parseList(program.getPartnerNames(), List.of()).stream()
                    .findFirst()
                    .orElse("-");
        }

        if (classModel != null && classModel.getLocation() != null) {
            return defaultIfBlank(classModel.getLocation().getAcronym(), classModel.getLocation().getName());
        }

        return "-";
    }

    private String localityLabel(ClassModel classModel) {
        if (classModel == null) {
            return "-";
        }
        if (!isBlank(classModel.getLocality())) {
            return classModel.getLocality();
        }
        if (classModel.getLocation() == null) {
            return "-";
        }
        String state = defaultIfBlank(classModel.getLocation().getState(), "");
        String name = defaultIfBlank(classModel.getLocation().getName(), "");
        if (isBlank(state)) {
            return name;
        }
        return name + " - " + state;
    }

    private String periodLabel(ProgramModel program, ClassModel classModel) {
        LocalDate start = classModel != null
                ? firstNonNull(classModel.getStartDate(), classModel.getLevelingStartDate(), classModel.getApplicationStartDate(), program.getStartDate())
                : program.getStartDate();
        LocalDate end = classModel != null
                ? firstNonNull(classModel.getEndDate(), classModel.getCertificateIssueDate(), classModel.getImmersionEndDate(), program.getEndDate())
                : program.getEndDate();

        if (start == null || end == null) {
            return "-";
        }
        return capitalizeMonth(start) + " -> " + capitalizeMonth(end);
    }

    private String resolveProgramStatus(ProgramModel program, ClassModel classModel) {
        LocalDate today = LocalDate.now();
        LocalDate start = classModel != null
                ? firstNonNull(classModel.getApplicationStartDate(), classModel.getStartDate(), classModel.getLevelingStartDate(), program.getStartDate())
                : program.getStartDate();
        LocalDate end = classModel != null
                ? firstNonNull(classModel.getCertificateIssueDate(), classModel.getEndDate(), classModel.getImmersionEndDate(), program.getEndDate())
                : program.getEndDate();

        if (end != null && end.isBefore(today)) {
            return "encerrado";
        }
        if (start != null && start.isAfter(today)) {
            return "inscricao";
        }
        return "andamento";
    }

    private String resolveCurrentStage(String status, ClassModel classModel, List<StageCandidateModel> candidates) {
        if ("encerrado".equalsIgnoreCase(status)) {
            return "Encerrado";
        }

        if (classModel == null) {
            return "Inscricao";
        }

        boolean hasImersao = candidates.stream()
                .anyMatch(candidate -> normalize(candidate.getStage().getName()).contains("imersao"));
        if (hasImersao) {
            return "Imersao";
        }

        boolean hasNivelamento = candidates.stream()
                .anyMatch(candidate -> normalize(candidate.getStage().getName()).contains("nivelamento"));
        if (hasNivelamento) {
            return "Nivelamento";
        }

        LocalDate today = LocalDate.now();
        if (classModel.getImmersionStartDate() != null && !today.isBefore(classModel.getImmersionStartDate())) {
            return "Imersao";
        }
        if (classModel.getLevelingStartDate() != null && !today.isBefore(classModel.getLevelingStartDate())) {
            return "Nivelamento";
        }
        return "Inscricao";
    }

    private long countCandidatesByStage(List<StageCandidateModel> candidates, String stageKeyword) {
        return candidates.stream()
                .filter(candidate -> normalize(candidate.getStage().getName()).contains(stageKeyword))
                .map(candidate -> candidate.getPeople().getId())
                .distinct()
                .count();
    }

    private String nextMilestone(ClassModel classModel) {
        if (classModel == null) {
            return "-";
        }

        LocalDate today = LocalDate.now();
        Map<String, LocalDate> milestones = new LinkedHashMap<>();
        milestones.put("Encerramento de inscricoes", classModel.getApplicationEndDate());
        milestones.put("Divulgacao dos selecionados", classModel.getLevelingSelectionAnnouncementDate());
        milestones.put("Inicio do nivelamento", classModel.getLevelingStartDate());
        milestones.put("Prova final do nivelamento", classModel.getLevelingFinalExamDate());
        milestones.put("Início da imersão", classModel.getImmersionStartDate());
        milestones.put("Avaliação parcial", classModel.getPartialEvaluationDate());
        milestones.put("Avaliação final", classModel.getFinalEvaluationDate());
        milestones.put("Emissao dos certificados", classModel.getCertificateIssueDate());

        return milestones.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBefore(today))
                .sorted(Map.Entry.comparingByValue())
                .findFirst()
                .map(entry -> entry.getKey() + " em " + today.until(entry.getValue()).getDays() + " dias")
                .orElse("-");
    }

    private int progressPercentage(String status, String etapaAtual, ProgramModel program, ClassModel classModel) {
        if ("encerrado".equalsIgnoreCase(status)) {
            return 100;
        }

        LocalDate start = classModel != null
                ? firstNonNull(classModel.getApplicationStartDate(), classModel.getStartDate(), classModel.getLevelingStartDate(), program.getStartDate())
                : program.getStartDate();
        LocalDate end = classModel != null
                ? firstNonNull(classModel.getCertificateIssueDate(), classModel.getEndDate(), classModel.getImmersionEndDate(), program.getEndDate())
                : program.getEndDate();

        if (start != null && end != null && !LocalDate.now().isBefore(start) && end.isAfter(start)) {
            long totalDays = java.time.temporal.ChronoUnit.DAYS.between(start, end);
            long elapsedDays = java.time.temporal.ChronoUnit.DAYS.between(start, LocalDate.now());
            if (totalDays > 0) {
                return (int) Math.max(5, Math.min(95, Math.round((elapsedDays * 100.0) / totalDays)));
            }
        }

        return switch (normalize(etapaAtual)) {
            case "imersao" -> 65;
            case "nivelamento" -> 42;
            default -> 15;
        };
    }

    private String templateStatus(ProgramModel program) {
        ClassModel sourceClass = selectSourceClass(program);
        return "andamento".equalsIgnoreCase(resolveProgramStatus(program, sourceClass))
                ? "Ativo"
                : "Modelo disponivel";
    }

    private List<String> parseList(String rawValue, List<String> fallback) {
        if (isBlank(rawValue)) {
            return fallback;
        }
        return List.of(rawValue.split("[,;]")).stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }

    private boolean matchesTab(ProgramOverviewItemDTO item, String tab) {
        if (isBlank(tab) || "todos".equalsIgnoreCase(tab)) {
            return true;
        }
        if ("ativos".equalsIgnoreCase(tab)) {
            return "andamento".equalsIgnoreCase(item.status());
        }
        if ("espera".equalsIgnoreCase(tab)) {
            return "inscricao".equalsIgnoreCase(item.status());
        }
        if ("encerrados".equalsIgnoreCase(tab)) {
            return "encerrado".equalsIgnoreCase(item.status());
        }
        return true;
    }

    private boolean matchesYear(ProgramOverviewItemDTO item, Integer year) {
        if (year == null || item.periodo() == null || "-".equals(item.periodo())) {
            return true;
        }
        return item.periodo().contains(String.valueOf(year));
    }

    private boolean matchesSearch(ProgramOverviewItemDTO item, String search) {
        if (isBlank(search)) {
            return true;
        }
        String normalized = normalize(search);
        return contains(item.nome(), normalized)
                || contains(item.turma(), normalized)
                || contains(item.parceiro(), normalized)
                || contains(item.localidade(), normalized);
    }

    private boolean matchesText(String value, String expected) {
        if (isBlank(expected)) {
            return true;
        }
        return normalize(value).equals(normalize(expected));
    }

    private boolean contains(String value, String normalizedSearch) {
        return normalize(value).contains(normalizedSearch);
    }

    private int statusPriority(String status) {
        if ("andamento".equalsIgnoreCase(status)) {
            return 0;
        }
        if ("inscricao".equalsIgnoreCase(status)) {
            return 1;
        }
        return 2;
    }

    private boolean isAlunoEnrollment(EnrollmentModel enrollment) {
        return enrollment.getAcademicRole() == null
                || enrollment.getAcademicRole().getName() == null
                || "ALUNO".equalsIgnoreCase(enrollment.getAcademicRole().getName());
    }

    private boolean isActiveStatus(String status) {
        String normalized = normalize(status);
        return normalized.contains("matric")
                || normalized.contains("ativo")
                || normalized.contains("andamento")
                || normalized.contains("pend");
    }

    private String capitalizeMonth(LocalDate date) {
        String value = MONTH_FORMATTER.format(date).replace(".", "");
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private String inferAcronym(String institutionName) {
        if (isBlank(institutionName)) {
            return null;
        }
        String[] parts = institutionName.trim().split("\\s+");
        if (parts.length == 1 && institutionName.length() <= 8) {
            return institutionName.trim().toUpperCase(Locale.ROOT);
        }
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return builder.toString();
    }

    private String inferState(String locality) {
        if (isBlank(locality) || !locality.contains("-")) {
            return null;
        }
        String[] parts = locality.split("-");
        return parts[parts.length - 1].trim().toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String defaultIfBlank(String candidate, String fallback) {
        return isBlank(candidate) ? fallback : candidate.trim();
    }

    @SafeVarargs
    private final <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
