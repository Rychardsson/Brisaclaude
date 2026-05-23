package com.example.brisa.services;

import com.example.brisa.dtos.people.PeopleCreateLinkRequestDTO;
import com.example.brisa.dtos.people.PeopleCreateLinkResponseDTO;
import com.example.brisa.dtos.people.PeopleFilterOptionDTO;
import com.example.brisa.dtos.people.PeopleLinkExistingRequestDTO;
import com.example.brisa.dtos.people.PeopleListItemDTO;
import com.example.brisa.dtos.people.PeopleOverviewResponseDTO;
import com.example.brisa.dtos.people.PeopleReferenceDataDTO;
import com.example.brisa.dtos.people.PeopleSummaryDTO;
import com.example.brisa.dtos.people.PeopleTabCountsDTO;
import com.example.brisa.enums.StageStatus;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.StageModel;
import com.example.brisa.models.auth.UserModel;
import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.InstitutionRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.ProgramRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import com.example.brisa.repositories.StageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PeopleIntegrationService {

    private static final List<String> STATUS_OPTIONS = List.of(
            "Ativa",
            "Pendente",
            "Concluida",
            "Reprovada",
            "Desclassificada"
    );

    private static final List<String> COTA_OPTIONS = List.of(
            "Ampla Concorrencia",
            "PCD/Neurodivergente",
            "Negro/Pardo",
            "Mulher",
            "45+"
    );

    private static final List<String> GENERO_OPTIONS = List.of(
            "Feminino",
            "Masculino",
            "Outro",
            "Não informado"
    );

    private static final List<String> TIPO_FORMACAO_OPTIONS = List.of(
            "Engenharia",
            "Outros cursos de exatas ou tecnologicos",
            "Tecnico na area de exatas concluido",
            "Computacao ou cursos relacionados a TIC"
    );

    private static final List<String> STATUS_FORMACAO_OPTIONS = List.of(
            "Cursando",
            "Concluido",
            "Trancado",
            "Não informado"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PeopleRepository peopleRepository;
    private final ProgramRepository programRepository;
    private final ClassRepository classRepository;
    private final StageRepository stageRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicRoleRepository academicRoleRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public PeopleOverviewResponseDTO getOverview(
            String tab,
            String search,
            Long programId,
            Long stageId,
            String status,
            String quota,
            String gender,
            String state,
            String city,
            String institution
    ) {
        List<PeopleModel> people = peopleRepository.findAllActiveOrderByNameAsc();
        Map<Long, List<EnrollmentModel>> enrollmentsByPeople = enrollmentRepository.findAllWithRelations().stream()
                .collect(Collectors.groupingBy(enrollment -> enrollment.getPeople().getId()));
        Map<Long, List<StageCandidateModel>> candidatesByPeople = stageCandidateRepository.findAllWithRelations().stream()
                .collect(Collectors.groupingBy(candidate -> candidate.getPeople().getId()));

        List<PeopleListItemDTO> allItems = people.stream()
                .map(person -> buildPeopleListItem(
                        person,
                        enrollmentsByPeople.getOrDefault(person.getId(), List.of()),
                        candidatesByPeople.getOrDefault(person.getId(), List.of())
                ))
                .sorted(Comparator.comparing(PeopleListItemDTO::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<PeopleListItemDTO> filteredItems = allItems.stream()
                .filter(item -> matchesTab(item, tab))
                .filter(item -> matchesSearch(item, search))
                .filter(item -> matchesProgram(item, enrollmentsByPeople.getOrDefault(item.id(), List.of()), programId))
                .filter(item -> matchesStage(item, candidatesByPeople.getOrDefault(item.id(), List.of()), stageId))
                .filter(item -> matchesText(item.status(), status))
                .filter(item -> matchesText(item.cota(), quota))
                .filter(item -> matchesText(item.genero(), gender))
                .filter(item -> matchesText(item.uf(), state))
                .filter(item -> matchesText(item.cidade(), city))
                .filter(item -> matchesText(item.instituicao(), institution))
                .toList();

        PeopleSummaryDTO summary = new PeopleSummaryDTO(
                allItems.size(),
                allItems.stream().filter(this::isAtivaOuPendente).count(),
                allItems.stream().filter(this::isEmProgramaAtivo).count(),
                allItems.stream().filter(item -> "Nivelamento".equalsIgnoreCase(item.etapaAtual())).count(),
                allItems.stream().filter(item -> "Imersao".equalsIgnoreCase(item.etapaAtual())).count(),
                people.stream().filter(person -> isCreatedWithinLastDays(person.getCreatedAt(), 30)).count()
        );

        PeopleTabCountsDTO tabs = new PeopleTabCountsDTO(
                allItems.stream().filter(this::isAtivaOuPendente).count(),
                allItems.stream().filter(this::isEmProgramaAtivo).count(),
                allItems.size()
        );

        return new PeopleOverviewResponseDTO(summary, tabs, filteredItems);
    }

    @Transactional(readOnly = true)
    public PeopleReferenceDataDTO getReferenceData() {
        List<ProgramModel> programs = programRepository.findAllWithRelations();
        List<ClassModel> classes = classRepository.findAll();
        List<StageModel> stages = stageRepository.findAll();
        List<PeopleModel> people = peopleRepository.findAllActive();

        List<PeopleFilterOptionDTO> programOptions = programs.stream()
                .sorted(Comparator.comparing(ProgramModel::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(program -> new PeopleFilterOptionDTO(program.getId(), program.getName()))
                .toList();

        List<PeopleFilterOptionDTO> classOptions = classes.stream()
                .sorted(Comparator.comparing(ClassModel::getCode, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(classModel -> new PeopleFilterOptionDTO(classModel.getId(), classModel.getCode()))
                .toList();

        List<PeopleFilterOptionDTO> stageOptions = stages.stream()
                .sorted(Comparator.comparing(StageModel::getName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .map(stage -> new PeopleFilterOptionDTO(stage.getId(), stageLabel(stage.getName())))
                .toList();

        return new PeopleReferenceDataDTO(
                programOptions,
                classOptions,
                stageOptions,
                STATUS_OPTIONS,
                COTA_OPTIONS,
                GENERO_OPTIONS,
                uniqueSortedValues(people.stream().map(PeopleModel::getState).toList()),
                uniqueSortedValues(people.stream().map(PeopleModel::getCity).toList()),
                uniqueSortedValues(flattenInstitutionValues(people)),
                uniqueSortedValues(people.stream().map(PeopleModel::getEducationLevel).toList()),
                STATUS_FORMACAO_OPTIONS
        );
    }

    @Transactional
    public PeopleCreateLinkResponseDTO createOnly(PeopleCreateLinkRequestDTO request) {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getNome())) {
            errors.add("Nome e obrigatorio.");
        }
        if (isBlank(request.getCpf())) {
            errors.add("CPF e obrigatorio.");
        }
        if (isBlank(request.getEmail())) {
            errors.add("Email e obrigatorio.");
        }
        if (request.getDataNascimento() == null) {
            errors.add("Data de nascimento e obrigatoria.");
        }
        if (isBlank(request.getCota())) {
            errors.add("Cota é obrigatória.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        PeopleModel person = findExistingPerson(request.getCpf(), request.getEmail()).orElse(null);
        boolean personCreated = false;

        if (person == null) {
            person = new PeopleModel();
            personCreated = true;
        }

        mergePersonData(person, request);
        reactivateIfSoftDeleted(person);
        person = peopleRepository.save(person);

        Long personId = person.getId();
        List<EnrollmentModel> personEnrollments = enrollmentRepository.findAllWithRelations().stream()
                .filter(item -> Objects.equals(item.getPeople().getId(), personId))
                .toList();
        List<StageCandidateModel> personCandidates = stageCandidateRepository.findAllWithRelations().stream()
                .filter(item -> Objects.equals(item.getPeople().getId(), personId))
                .toList();

        return new PeopleCreateLinkResponseDTO(
                personId,
                personCreated,
                false,
                false,
                List.of(),
                buildPeopleListItem(person, personEnrollments, personCandidates)
        );
    }

    @Transactional
    public PeopleCreateLinkResponseDTO linkExistingPerson(PeopleLinkExistingRequestDTO request) {
        List<String> errors = new ArrayList<>();
        if (request.getPeopleId() == null) {
            errors.add("ID da pessoa é obrigatório.");
        }
        if (request.getTurmaId() == null) {
            errors.add("Turma é obrigatória.");
        }
        if (request.getEtapaId() == null) {
            errors.add("Etapa é obrigatória.");
        }
        if (isBlank(request.getStatusInicial())) {
            errors.add("Status inicial é obrigatório.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        PeopleModel person = peopleRepository.findActiveById(request.getPeopleId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada."));

        ClassModel classModel = classRepository.findById(request.getTurmaId())
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada."));

        StageModel stage = stageRepository.findById(request.getEtapaId())
                .orElseThrow(() -> new ResourceNotFoundException("Etapa não encontrada."));

        Optional<EnrollmentModel> existingEnrollment = enrollmentRepository.findAllWithRelations().stream()
                .filter(e -> Objects.equals(e.getPeople().getId(), request.getPeopleId()) &&
                        Objects.equals(e.getClassModel().getId(), request.getTurmaId()))
                .findFirst();

        if (existingEnrollment.isPresent()) {
            throw new ValidationException(List.of("Pessoa já está vinculada a esta turma."));
        }

        AcademicRoleModel alunoRole = academicRoleRepository.findByName("ALUNO")
                .orElseGet(() -> academicRoleRepository.save(new AcademicRoleModel("ALUNO", "Aluno")));

        EnrollmentModel enrollment = new EnrollmentModel();
        enrollment.setPeople(person);
        enrollment.setClassModel(classModel);
        enrollment.setAcademicRole(alunoRole);
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(normalizeEnrollmentStatus(request.getStatusInicial()));
        enrollmentRepository.save(enrollment);

        StageCandidateModel candidate = new StageCandidateModel();
        candidate.setPeople(person);
        candidate.setStage(stage);
        candidate.setStatus(toStageStatus(request.getStatusInicial()));
        stageCandidateRepository.save(candidate);

        Long personId = person.getId();

        List<String> alerts = detectActiveConflicts(personId, classModel.getId());
        // Bloquear se houver conflito de nivelamento
        boolean hasNivelamentoConflict = alerts.stream().anyMatch(a -> normalize(a).contains("nivelament"));
        if (hasNivelamentoConflict) {
            throw new ValidationException(alerts);
        }

        List<EnrollmentModel> personEnrollments = enrollmentRepository.findAllWithRelations().stream()
                .filter(item -> Objects.equals(item.getPeople().getId(), personId))
                .toList();
        List<StageCandidateModel> personCandidates = stageCandidateRepository.findAllWithRelations().stream()
                .filter(item -> Objects.equals(item.getPeople().getId(), personId))
                .toList();

        return new PeopleCreateLinkResponseDTO(
                personId,
                false,
                true,
                !alerts.isEmpty(),
                alerts,
                buildPeopleListItem(person, personEnrollments, personCandidates)
        );
    }

    @Transactional
    public PeopleCreateLinkResponseDTO createOrLink(PeopleCreateLinkRequestDTO request) {
        List<String> errors = new ArrayList<>();
        if (isBlank(request.getNome())) {
            errors.add("Nome é obrigatório.");
        }
        if (isBlank(request.getCpf())) {
            errors.add("CPF é obrigatório.");
        }
        if (isBlank(request.getEmail())) {
            errors.add("Email é obrigatório.");
        }
        if (request.getDataNascimento() == null) {
            errors.add("Data de nascimento é obrigatória.");
        }
        if (request.getTurmaId() == null) {
            errors.add("Turma é obrigatória.");
        }
        if (request.getEtapaId() == null) {
            errors.add("Etapa inicial é obrigatória.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        ClassModel classModel = classRepository.findById(request.getTurmaId())
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com id: " + request.getTurmaId()));
        StageModel stage = stageRepository.findById(request.getEtapaId())
                .orElseThrow(() -> new ResourceNotFoundException("Etapa não encontrada com id: " + request.getEtapaId()));
        if (!Objects.equals(stage.getClassModel().getId(), classModel.getId())) {
            throw new ValidationException(List.of("A etapa selecionada não pertence à turma informada."));
        }

        PeopleModel person = findExistingPerson(request.getCpf(), request.getEmail()).orElse(null);
        boolean personCreated = false;

        if (person == null) {
            person = new PeopleModel();
            personCreated = true;
        }

        mergePersonData(person, request);
        reactivateIfSoftDeleted(person);
        person = peopleRepository.save(person);

        AcademicRoleModel alunoRole = academicRoleRepository.findByName("ALUNO")
                .orElseGet(() -> academicRoleRepository.save(new AcademicRoleModel("ALUNO", "Aluno")));

        boolean linkCreated = false;
        EnrollmentModel enrollment = enrollmentRepository.findByPeopleIdAndClassModelId(person.getId(), classModel.getId())
                .orElse(null);

        if (enrollment == null) {
            enrollment = new EnrollmentModel();
            enrollment.setPeople(person);
            enrollment.setClassModel(classModel);
            enrollment.setAcademicRole(alunoRole);
            enrollment.setEnrollmentDate(LocalDate.now());
            enrollment.setStatus(normalizeEnrollmentStatus(request.getStatusInicial()));
            enrollmentRepository.save(enrollment);
            linkCreated = true;
        } else if (!isBlank(request.getStatusInicial())) {
            enrollment.setStatus(normalizeEnrollmentStatus(request.getStatusInicial()));
            enrollmentRepository.save(enrollment);
        }

        StageCandidateModel stageCandidate = stageCandidateRepository.findByStageIdAndPeopleId(stage.getId(), person.getId())
                .orElse(null);

        if (stageCandidate == null) {
            stageCandidate = new StageCandidateModel();
            stageCandidate.setStage(stage);
            stageCandidate.setPeople(person);
        }
        stageCandidate.setStatus(toStageStatus(request.getStatusInicial()));
        stageCandidateRepository.save(stageCandidate);

        Long personId = person.getId();
        List<String> alerts = detectActiveConflicts(personId, classModel.getId());

        // Se houver conflito de nivelamento, bloquear a operação e retornar erro de validação
        boolean hasNivelamentoConflict = alerts.stream().anyMatch(a -> normalize(a).contains("nivelament"));
        if (hasNivelamentoConflict) {
            throw new ValidationException(alerts);
        }

        List<EnrollmentModel> personEnrollments = enrollmentRepository.findAllWithRelations().stream()
                .filter(item -> Objects.equals(item.getPeople().getId(), personId))
                .toList();
        List<StageCandidateModel> personCandidates = stageCandidateRepository.findAllWithRelations().stream()
                .filter(item -> Objects.equals(item.getPeople().getId(), personId))
                .toList();

        return new PeopleCreateLinkResponseDTO(
                personId,
                personCreated,
                linkCreated,
                !alerts.isEmpty(),
                alerts,
                buildPeopleListItem(person, personEnrollments, personCandidates)
        );
    }

    private Optional<PeopleModel> findExistingPerson(String cpf, String email) {
        String normalizedCpf = normalizeCpf(cpf);
        if (!normalizedCpf.isEmpty()) {
            Optional<PeopleModel> byExactCpf = peopleRepository.findByCpf(normalizedCpf);
            if (byExactCpf.isPresent()) {
                return byExactCpf;
            }
            Optional<PeopleModel> byNormalizedCpf = peopleRepository.findAll().stream()
                    .filter(person -> normalizedCpf.equals(normalizeCpf(person.getCpf())))
                    .findFirst();
            if (byNormalizedCpf.isPresent()) {
                return byNormalizedCpf;
            }
        }
        if (!isBlank(email)) {
            return peopleRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT));
        }
        return Optional.empty();
    }

    private void mergePersonData(PeopleModel person, PeopleCreateLinkRequestDTO request) {
        person.setCpf(normalizeCpf(request.getCpf()));
        person.setName(request.getNome().trim());
        person.setEmail(request.getEmail().trim().toLowerCase(Locale.ROOT));
        person.setBirthDate(request.getDataNascimento());
        person.setGender(defaultIfBlank(request.getGenero(), person.getGender()));
        person.setQuotaCategory(defaultIfBlank(request.getCota(), person.getQuotaCategory()));
        person.setPhone(defaultIfBlank(request.getTelefone(), person.getPhone()));
        person.setLinkedin(defaultIfBlank(request.getLinkedin(), person.getLinkedin()));
        person.setAddress(defaultIfBlank(request.getEndereco(), person.getAddress()));
        person.setAddressExtra(defaultIfBlank(request.getComplementoEndereco(), person.getAddressExtra()));
        person.setState(defaultIfBlank(request.getEstado(), person.getState()));
        person.setCity(defaultIfBlank(request.getCidade(), person.getCity()));
        person.setZipCode(defaultIfBlank(request.getCep(), person.getZipCode()));
        person.setEducationLevel(defaultIfBlank(request.getTipoFormacao(), person.getEducationLevel()));
        person.setInstitutionName(defaultIfBlank(request.getInstituicao(), person.getInstitutionName()));
        person.setCourseName(defaultIfBlank(request.getCurso(), person.getCourseName()));
        person.setEducationStatus(defaultIfBlank(request.getStatusFormacao(), person.getEducationStatus()));
        if (request.getDataConclusao() != null) {
            person.setEducationCompletionDate(request.getDataConclusao());
        }
    }

    private void reactivateIfSoftDeleted(PeopleModel person) {
        if (Boolean.TRUE.equals(person.getSoftDeleted())) {
            person.setSoftDeleted(false);
        }
    }

    public List<String> detectActiveConflicts(Long peopleId, Long targetClassId) {
        List<EnrollmentModel> enrollments = enrollmentRepository.findAllWithRelations().stream()
                .filter(item -> Objects.equals(item.getPeople().getId(), peopleId))
                .filter(item -> !Objects.equals(item.getClassModel().getId(), targetClassId))
                .filter(this::isAlunoEnrollment)
                .toList();

        ClassModel targetClass = classRepository.findById(targetClassId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada com id: " + targetClassId));

        List<String> alerts = new ArrayList<>();
        for (EnrollmentModel enrollment : enrollments) {
            if (!isAtivoLike(enrollment.getStatus())) {
                continue;
            }

            if (overlaps(targetClass, enrollment.getClassModel())) {
                alerts.add("Conflito: aluno já vinculado a programa simultâneo em andamento.");
                return alerts;
            }
        }

        // Verifica se já existe candidato em etapa de nivelamento em outra turma que se sobrepõe a esta
        List<StageCandidateModel> otherNivelamentoCandidates = stageCandidateRepository.findAllWithRelations().stream()
                .filter(c -> Objects.equals(c.getPeople().getId(), peopleId))
                .filter(c -> c.getStage() != null && c.getStage().getClassModel() != null)
                .filter(c -> !Objects.equals(c.getStage().getClassModel().getId(), targetClassId))
                .filter(c -> normalize(c.getStage().getName()).contains("nivelament"))
                .toList();

        for (StageCandidateModel candidate : otherNivelamentoCandidates) {
            // considera apenas candidatos que não foram reprovados
            if (candidate.getStatus() == StageStatus.REPROVADO) {
                continue;
            }
            ClassModel otherClass = candidate.getStage().getClassModel();
            if (overlaps(targetClass, otherClass)) {
                String code = otherClass.getCode() != null ? otherClass.getCode() : String.valueOf(otherClass.getId());
                alerts.add(String.format("Erro: pessoa já está em nivelamento na turma '%s'. Não é permitido participar de dois nivelamentos simultâneos.", code));
                return alerts;
            }
        }

        if (!enrollments.isEmpty()) {
            alerts.add("Atenção: aluno já vinculado a outro programa.");
        }
        return alerts;
    }

    private boolean overlaps(ClassModel a, ClassModel b) {
        LocalDate aStart = firstNonNull(a.getApplicationStartDate(), a.getStartDate(), a.getLevelingStartDate(), a.getImmersionStartDate());
        LocalDate aEnd = firstNonNull(a.getCertificateIssueDate(), a.getEndDate(), a.getImmersionEndDate(), a.getFinalEvaluationDate());
        LocalDate bStart = firstNonNull(b.getApplicationStartDate(), b.getStartDate(), b.getLevelingStartDate(), b.getImmersionStartDate());
        LocalDate bEnd = firstNonNull(b.getCertificateIssueDate(), b.getEndDate(), b.getImmersionEndDate(), b.getFinalEvaluationDate());

        if (aStart == null || aEnd == null || bStart == null || bEnd == null) {
            return false;
        }

        return !aEnd.isBefore(bStart) && !bEnd.isBefore(aStart);
    }

    private PeopleListItemDTO buildPeopleListItem(
            PeopleModel person,
            List<EnrollmentModel> enrollments,
            List<StageCandidateModel> candidates
    ) {
        List<EnrollmentModel> studentEnrollments = enrollments.stream()
                .filter(this::isAlunoEnrollment)
                .toList();

        EnrollmentModel primaryEnrollment = selectPrimaryEnrollment(studentEnrollments);
        StageCandidateModel primaryCandidate = selectPrimaryCandidate(primaryEnrollment, candidates);
        List<String> programLabels = buildProgramLabels(studentEnrollments);

        String etapaAtual = resolveEtapaAtual(primaryEnrollment, primaryCandidate);
        String status = resolveStatus(primaryEnrollment, primaryCandidate);
        String institution = firstNonBlank(
                person.getInstitutionName(),
                primaryEnrollment != null ? institutionLabel(primaryEnrollment.getClassModel()) : null
        );

        return new PeopleListItemDTO(
                person.getId(),
                person.getName(),
                formatCpf(person.getCpf()),
                person.getEmail(),
                defaultIfBlank(person.getGender(), "Não informado"),
                calculateAge(person.getBirthDate()),
                defaultIfBlank(person.getQuotaCategory(), "Não informado"),
                defaultIfBlank(person.getCity(), "-"),
                defaultIfBlank(person.getState(), "-"),
                defaultIfBlank(institution, "-"),
                defaultIfBlank(person.getEducationLevel(), "-"),
                defaultIfBlank(person.getCourseName(), "-"),
                programLabels,
                etapaAtual,
                status,
                formatDate(firstNonNull(person.getUpdatedAt(), person.getCreatedAt()))
        );
    }

    private List<String> buildProgramLabels(List<EnrollmentModel> enrollments) {
        return enrollments.stream()
                .map(item -> item.getClassModel().getProgram().getName())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    private EnrollmentModel selectPrimaryEnrollment(List<EnrollmentModel> enrollments) {
        LocalDate today = LocalDate.now();
        return enrollments.stream()
                .sorted(Comparator
                        .comparing((EnrollmentModel enrollment) -> isCurrentClass(enrollment.getClassModel(), today)).reversed()
                        .thenComparing((EnrollmentModel enrollment) -> enrollmentStatusPriority(enrollment.getStatus()))
                        .thenComparing(
                                (EnrollmentModel enrollment) -> firstNonNull(
                                        enrollment.getClassModel().getEndDate(),
                                        enrollment.getClassModel().getImmersionEndDate(),
                                        enrollment.getClassModel().getFinalEvaluationDate()
                                ),
                                Comparator.nullsLast(Comparator.reverseOrder())
                        )
                )
                .findFirst()
                .orElse(null);
    }

    private StageCandidateModel selectPrimaryCandidate(EnrollmentModel primaryEnrollment, List<StageCandidateModel> candidates) {
        if (primaryEnrollment == null) {
            return candidates.stream()
                    .sorted(Comparator.comparing(candidate -> stagePriority(candidate.getStage().getName())))
                    .findFirst()
                    .orElse(null);
        }

        return candidates.stream()
                .filter(candidate -> Objects.equals(candidate.getStage().getClassModel().getId(), primaryEnrollment.getClassModel().getId()))
                .sorted(Comparator.comparing(candidate -> stagePriority(candidate.getStage().getName())))
                .findFirst()
                .orElse(null);
    }

    private String resolveEtapaAtual(EnrollmentModel enrollment, StageCandidateModel candidate) {
        if (candidate != null) {
            return stageLabel(candidate.getStage().getName());
        }
        if (enrollment == null) {
            return "-";
        }
        if (isConcluded(enrollment.getStatus())) {
            return "Concluida";
        }
        return "Nivelamento";
    }

    private String resolveStatus(EnrollmentModel enrollment, StageCandidateModel candidate) {
        if (enrollment != null && !isBlank(enrollment.getStatus())) {
            return displayStatus(enrollment.getStatus());
        }
        if (candidate != null && candidate.getStatus() == StageStatus.REPROVADO) {
            return "Reprovada";
        }
        return "Pendente";
    }

    private boolean matchesTab(PeopleListItemDTO item, String tab) {
        if (isBlank(tab) || "todas".equalsIgnoreCase(tab)) {
            return true;
        }
        if ("ativos".equalsIgnoreCase(tab)) {
            return isAtivaOuPendente(item);
        }
        if ("programas".equalsIgnoreCase(tab)) {
            return isEmProgramaAtivo(item);
        }
        return true;
    }

    private boolean matchesSearch(PeopleListItemDTO item, String search) {
        if (isBlank(search)) {
            return true;
        }
        String normalized = normalize(search);
        return contains(item.nome(), normalized)
                || contains(item.cpf(), normalized)
                || contains(item.email(), normalized)
                || item.programas().stream().anyMatch(program -> contains(program, normalized));
    }

    private boolean matchesProgram(PeopleListItemDTO item, List<EnrollmentModel> enrollments, Long programId) {
        if (programId == null) {
            return true;
        }
        return enrollments.stream()
                .anyMatch(enrollment -> Objects.equals(enrollment.getClassModel().getProgram().getId(), programId));
    }

    private boolean matchesStage(PeopleListItemDTO item, List<StageCandidateModel> candidates, Long stageId) {
        if (stageId == null) {
            return true;
        }
        return candidates.stream().anyMatch(candidate -> Objects.equals(candidate.getStage().getId(), stageId));
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

    private boolean isAtivaOuPendente(PeopleListItemDTO item) {
        return "Ativa".equalsIgnoreCase(item.status()) || "Pendente".equalsIgnoreCase(item.status());
    }

    private boolean isEmProgramaAtivo(PeopleListItemDTO item) {
        return isAtivaOuPendente(item) && !item.programas().isEmpty();
    }

    private boolean isCreatedWithinLastDays(LocalDateTime dateTime, int days) {
        return dateTime != null && dateTime.toLocalDate().isAfter(LocalDate.now().minusDays(days));
    }

    private boolean isAlunoEnrollment(EnrollmentModel enrollment) {
        return enrollment.getAcademicRole() == null
                || enrollment.getAcademicRole().getName() == null
                || "ALUNO".equalsIgnoreCase(enrollment.getAcademicRole().getName());
    }

    private int calculateAge(LocalDate birthDate) {
        if (birthDate == null) {
            return 0;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private boolean isCurrentClass(ClassModel classModel, LocalDate today) {
        LocalDate start = firstNonNull(classModel.getLevelingStartDate(), classModel.getStartDate(), classModel.getApplicationStartDate());
        LocalDate end = firstNonNull(classModel.getCertificateIssueDate(), classModel.getEndDate(), classModel.getImmersionEndDate());
        return start != null && end != null && !today.isBefore(start) && !today.isAfter(end);
    }

    private int enrollmentStatusPriority(String status) {
        String normalized = normalizeEnrollmentStatus(status);
        return switch (normalized) {
            case "ATIVA" -> 0;
            case "PENDENTE" -> 1;
            case "CONCLUIDA" -> 2;
            case "REPROVADA", "DESCLASSIFICADA" -> 3;
            default -> 4;
        };
    }

    private int stagePriority(String stageName) {
        String normalized = normalize(stageName);
        if (normalized.contains("imersao")) {
            return 0;
        }
        if (normalized.contains("nivelamento")) {
            return 1;
        }
        if (normalized.contains("selecao") || normalized.contains("inscricao")) {
            return 2;
        }
        return 3;
    }

    private String stageLabel(String stageName) {
        String normalized = normalize(stageName);
        if (normalized.contains("imersao")) {
            return "Imersao";
        }
        if (normalized.contains("nivelamento")) {
            return "Nivelamento";
        }
        if (normalized.contains("selecao") || normalized.contains("inscricao")) {
            return "Inscricao";
        }
        return defaultIfBlank(stageName, "-");
    }

    private String displayStatus(String status) {
        return switch (normalizeEnrollmentStatus(status)) {
            case "ATIVA" -> "Ativa";
            case "CONCLUIDA" -> "Concluida";
            case "REPROVADA" -> "Reprovada";
            case "DESCLASSIFICADA" -> "Desclassificada";
            default -> "Pendente";
        };
    }

    private boolean isConcluded(String status) {
        return "CONCLUIDA".equals(normalizeEnrollmentStatus(status));
    }

    private boolean isAtivoLike(String status) {
        String normalized = normalizeEnrollmentStatus(status);
        return "ATIVA".equals(normalized) || "PENDENTE".equals(normalized);
    }

    private String normalizeEnrollmentStatus(String status) {
        String normalized = normalize(status);
        if (normalized.contains("reprov")) {
            return "REPROVADA";
        }
        if (normalized.contains("desclass")) {
            return "DESCLASSIFICADA";
        }
        if (normalized.contains("conclu")) {
            return "CONCLUIDA";
        }
        if (normalized.contains("pend")) {
            return "PENDENTE";
        }
        if (normalized.contains("matric") || normalized.contains("ativo") || normalized.contains("andamento")) {
            return "ATIVA";
        }
        return "PENDENTE";
    }

    private StageStatus toStageStatus(String status) {
        String normalized = normalize(status);
        if (normalized.contains("reprov") || normalized.contains("desclass")) {
            return StageStatus.REPROVADO;
        }
        if (normalized.contains("espera")) {
            return StageStatus.LISTA_ESPERA;
        }
        if (normalized.contains("analise")) {
            return StageStatus.EM_ANALISE;
        }
        return StageStatus.APROVADO;
    }

    private String institutionLabel(ClassModel classModel) {
        if (classModel == null || classModel.getLocation() == null) {
            return null;
        }
        return firstNonBlank(classModel.getLocation().getAcronym(), classModel.getLocation().getName());
    }

    private List<String> flattenInstitutionValues(List<PeopleModel> people) {
        Set<String> values = new LinkedHashSet<>();
        people.stream()
                .map(PeopleModel::getInstitutionName)
                .filter(value -> !isBlank(value))
                .forEach(values::add);
        institutionRepository.findAll().stream()
                .map(institution -> firstNonBlank(institution.getAcronym(), institution.getName()))
                .filter(value -> !isBlank(value))
                .forEach(values::add);
        return new ArrayList<>(values);
    }

    private List<String> uniqueSortedValues(Collection<String> values) {
        return values.stream()
                .filter(value -> !isBlank(value))
                .map(String::trim)
                .collect(Collectors.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER)))
                .stream()
                .toList();
    }

    private String formatCpf(String cpf) {
        String digits = normalizeCpf(cpf);
        if (digits.length() != 11) {
            return defaultIfBlank(cpf, "-");
        }
        return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9);
    }

    private String normalizeCpf(String cpf) {
        if (cpf == null) {
            return "";
        }
        return cpf.replaceAll("\\D", "");
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime == null ? "-" : DATE_FORMATTER.format(dateTime.toLocalDate());
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value;
            }
        }
        return null;
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
