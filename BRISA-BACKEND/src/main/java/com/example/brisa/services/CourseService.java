package com.example.brisa.services;

import com.example.brisa.dtos.course.CourseAlertRequestDTO;
import com.example.brisa.dtos.course.CourseAlertResponseDTO;
import com.example.brisa.dtos.course.CourseClassImportResponseDTO;
import com.example.brisa.dtos.course.CourseProgressionImportResponseDTO;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.AdvisorModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.course.CourseAssignmentModel;
import com.example.brisa.models.course.CourseModel;
import com.example.brisa.models.course.CourseProgressionModel;
import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import com.example.brisa.repositories.AdvisorRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.CourseAssignmentRepository;
import com.example.brisa.repositories.CourseProgressionRepository;
import com.example.brisa.repositories.CourseRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.KnowledgeAreaRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import com.example.brisa.repositories.StageRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CourseProgressionRepository courseProgressionRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final ClassRepository classRepository;
    private final KnowledgeAreaRepository knowledgeAreaRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final StageRepository stageRepository;
    private final AcademicRoleRepository academicRoleRepository;
    private final AdvisorRepository advisorRepository;
    private final ExcelImportHelper excelImportHelper;

    public List<CourseModel> findAll() {
        return courseRepository.findAll();
    }

    public CourseModel findById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com id: " + id));
    }

    public List<CourseProgressionModel> findProgressionsByClassId(Long classId) {
        return courseProgressionRepository.findByClassId(classId);
    }

    public List<CourseProgressionModel> findProgressionsByCourseId(Long courseId) {
        return courseProgressionRepository.findByCourseId(courseId);
    }

    // Create a single course. If knowledge area name is provided, find or create it.
    public com.example.brisa.models.course.CourseModel createCourse(com.example.brisa.models.course.CourseModel course) {
        if (course.getKnowledgeArea() != null && course.getKnowledgeArea().getName() != null) {
            String kaName = course.getKnowledgeArea().getName().trim();
            if (!kaName.isEmpty()) {
                com.example.brisa.models.KnowledgeAreaModel ka = knowledgeAreaRepository.findByNameIgnoreCase(kaName)
                        .orElseGet(() -> {
                            com.example.brisa.models.KnowledgeAreaModel newKa = new com.example.brisa.models.KnowledgeAreaModel();
                            newKa.setName(kaName);
                            return knowledgeAreaRepository.save(newKa);
                        });
                course.setKnowledgeArea(ka);
                }
        }
        return courseRepository.save(course);
    }

    // Bulk import courses from frontend; each DTO contains name, description and competencia (knowledge area name)
    @Transactional
    public java.util.List<com.example.brisa.models.course.CourseModel> importCourses(java.util.List<com.example.brisa.dtos.course.CourseImportDTO> dtos) {
        java.util.List<com.example.brisa.models.course.CourseModel> created = new java.util.ArrayList<>();
        for (com.example.brisa.dtos.course.CourseImportDTO dto : dtos) {
            if (dto.name() == null || dto.name().trim().isEmpty()) continue;
            com.example.brisa.models.course.CourseModel c = resolveCourse(dto.code(), dto.name().trim()).orElseGet(com.example.brisa.models.course.CourseModel::new);
            c.setName(dto.name().trim());
            String courseCode = trimToNull(dto.code());
            if (courseCode != null) {
                c.setCode(courseCode);
            }
            c.setDescription(dto.description());
            if (dto.workload() != null) {
                c.setWorkload(dto.workload());
            }
            if (dto.inclusionDate() != null) {
                c.setInclusionDate(dto.inclusionDate());
            }
            String knowledgeAreaName = firstNonBlank(dto.knowledgeArea(), dto.competencia());
            if (knowledgeAreaName != null) {
                c.setKnowledgeArea(getOrCreateKnowledgeArea(knowledgeAreaName));
            }
            if (dto.subArea() != null && !dto.subArea().trim().isEmpty()) {
                c.setSubArea(getOrCreateKnowledgeArea(dto.subArea()));
            }
            created.add(courseRepository.save(c));
        }
        return created;
    }

    @Transactional
    public CourseClassImportResponseDTO importCoursesToClassFromExcel(Long classId, MultipartFile file) throws IOException {
        com.example.brisa.models.ClassModel classModel = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));

        int totalProcessed = 0;
        int createdCourses = 0;
        int assignedCourses = 0;
        int alreadyAssigned = 0;
        int skippedRows = 0;
        List<String> createdCourseNames = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return new CourseClassImportResponseDTO(0, 0, 0, 0, 0, createdCourseNames);
            }

            Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));
            Integer codeIndex = excelImportHelper.findColumn(headers, List.of("codigo curso", "código curso", "codigocurso", "codigo_curso", "code"), null);
            Integer nameIndex = excelImportHelper.findColumn(headers, List.of("nome curso", "nome_curso", "curso", "name", "nome"), 0);
            Integer workloadIndex = excelImportHelper.findColumn(headers, List.of("carga horaria", "carga_horaria", "workload", "horas"), null);
            Integer areaIndex = excelImportHelper.findColumn(headers, List.of("area conhecimento", "área conhecimento", "area_conhecimento", "competencia", "knowledgearea"), 1);
            Integer subAreaIndex = excelImportHelper.findColumn(headers, List.of("sub area", "sub_area", "subárea", "subarea"), null);
            Integer inclusionDateIndex = excelImportHelper.findColumn(headers, List.of("data inclusao", "data_inclusao", "data inclusão", "inclusiondate"), null);
            Integer descriptionIndex = excelImportHelper.findColumn(headers, List.of("descricao", "descrição", "description"), 2);

            Integer advisorIndex = excelImportHelper.findColumn(headers, List.of("orientador", "nome orientador", "nome_orientador", "prof orientador", "gestor", "advisor"), null);
            Integer advisorCpfIndex = excelImportHelper.findColumn(headers, List.of("cpf orientador", "cpf_orientador", "advisorcpf"), null);

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                String courseCode = codeIndex == null ? null : excelImportHelper.getString(row, codeIndex);
                String courseName = excelImportHelper.getString(row, nameIndex);
                String competencia = areaIndex == null ? null : excelImportHelper.getString(row, areaIndex);
                String subArea = subAreaIndex == null ? null : excelImportHelper.getString(row, subAreaIndex);
                String description = descriptionIndex == null ? null : excelImportHelper.getString(row, descriptionIndex);
                Double workload = workloadIndex == null ? null : excelImportHelper.getDouble(row, workloadIndex);
                LocalDate inclusionDate = inclusionDateIndex == null ? null : excelImportHelper.getDate(row, inclusionDateIndex);
                String advisorName = advisorIndex == null ? null : excelImportHelper.getString(row, advisorIndex);
                String advisorCpf = advisorCpfIndex == null ? null : excelImportHelper.getString(row, advisorCpfIndex);

                if (courseName == null || courseName.isBlank()) {
                    skippedRows++;
                    continue;
                }

                totalProcessed++;
                String normalizedName = courseName.trim();

                CourseModel course = resolveCourse(courseCode, normalizedName).orElse(null);
                if (course == null) {
                    course = new CourseModel();
                    course.setName(normalizedName);
                    createdCourses++;
                    createdCourseNames.add(course.getName());
                }
                String normalizedCode = trimToNull(courseCode);
                if (normalizedCode != null) {
                    course.setCode(normalizedCode);
                }
                course.setDescription(description);
                if (workload != null) {
                    course.setWorkload(workload);
                }
                if (inclusionDate != null) {
                    course.setInclusionDate(inclusionDate);
                }
                if (competencia != null && !competencia.isBlank()) {
                    course.setKnowledgeArea(getOrCreateKnowledgeArea(competencia));
                }
                if (subArea != null && !subArea.isBlank()) {
                    course.setSubArea(getOrCreateKnowledgeArea(subArea));
                }
                course = courseRepository.save(course);

                CourseAssignmentModel assignment = courseAssignmentRepository
                        .findByCourseIdAndClassId(course.getId(), classId);
                AdvisorModel advisor = resolveAdvisor(advisorCpf, advisorName).orElse(null);
                if (assignment == null) {
                    CourseAssignmentModel newAssignment = new CourseAssignmentModel();
                    newAssignment.setCourse(course);
                    newAssignment.setClassModel(classModel);
                    newAssignment.setRequired(true);
                    newAssignment.setAdvisor(advisor);
                    courseAssignmentRepository.save(newAssignment);
                    backfillProgressionsForAssignedCourse(course, classId);
                    assignedCourses++;
                } else {
                    if (advisor != null) {
                        assignment.setAdvisor(advisor);
                        courseAssignmentRepository.save(assignment);
                    }
                    alreadyAssigned++;
                }
            }
        }

        return new CourseClassImportResponseDTO(
                totalProcessed,
                createdCourses,
                assignedCourses,
                alreadyAssigned,
                skippedRows,
                createdCourseNames
        );
    }

    @Transactional
    public CourseProgressionImportResponseDTO importProgressionsFromExcel(Long classId, MultipartFile file) throws IOException {
        com.example.brisa.models.ClassModel classModel = classRepository.findById(classId)
            .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));

        int totalProcessed = 0;
        int createdProgressions = 0;
        int updatedProgressions = 0;
        int updatedGrades = 0;
        int skippedRows = 0;

        Set<String> unknownCourses = new HashSet<>();
        Set<String> peopleNotInClassCpfs = new HashSet<>();

        List<EnrollmentModel> enrollments = enrollmentRepository.findByClassModelId(classId);
        Map<String, EnrollmentModel> enrollmentByCpf = new HashMap<>();
        Map<String, EnrollmentModel> enrollmentByEmail = new HashMap<>();
        Map<String, EnrollmentModel> enrollmentByName = new HashMap<>();
        for (EnrollmentModel enrollment : enrollments) {
            cacheEnrollment(enrollment, enrollmentByCpf, enrollmentByEmail, enrollmentByName);
        }

        Map<String, PeopleModel> stagePeopleByCpf = stageCandidateRepository.findByClassIdWithPeople(classId).stream()
            .filter(sc -> sc.getPeople() != null && sc.getPeople().getCpf() != null)
            .collect(Collectors.toMap(
                sc -> normalizeCpf(sc.getPeople().getCpf()),
                StageCandidateModel::getPeople,
                (a, b) -> a
            ));
        Map<String, PeopleModel> stagePeopleByEmail = stageCandidateRepository.findByClassIdWithPeople(classId).stream()
            .filter(sc -> sc.getPeople() != null && sc.getPeople().getEmail() != null)
            .collect(Collectors.toMap(
                sc -> normalizeEmail(sc.getPeople().getEmail()),
                StageCandidateModel::getPeople,
                (a, b) -> a
            ));
        Map<String, PeopleModel> stagePeopleByName = stageCandidateRepository.findByClassIdWithPeople(classId).stream()
            .filter(sc -> sc.getPeople() != null && sc.getPeople().getName() != null)
            .collect(Collectors.toMap(
                sc -> normalizeName(sc.getPeople().getName()),
                StageCandidateModel::getPeople,
                (a, b) -> a
            ));

        AcademicRoleModel alunoRole = academicRoleRepository.findByName("ALUNO")
            .orElseGet(() -> academicRoleRepository.save(new AcademicRoleModel("ALUNO", "Aluno")));

        Map<Long, EnrollmentModel> enrollmentsToUpdate = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            for (int sheetIndex = 0; sheetIndex < workbook.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                if (sheet == null || sheet.getPhysicalNumberOfRows() == 0 || isCompletionSummarySheet(sheet.getSheetName())) {
                    continue;
                }

                for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                    Row row = sheet.getRow(rowNum);
                    if (row == null || excelImportHelper.isRowEmpty(row)) continue;

                Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));
                Integer courseIndex = excelImportHelper.findColumn(headers, List.of("codigo curso", "código curso", "codigo_curso", "curso", "nome curso", "nome_curso", "course"), null);
                Integer cpfIndex = excelImportHelper.findColumn(headers, List.of("cpf", "cpf_aluno"), null);
                Integer emailIndex = excelImportHelper.findColumn(headers, List.of("email", "e-mail", "endereco de email", "endereço de e-mail"), null);
                Integer nameIndex = excelImportHelper.findColumn(headers, List.of("nome completo", "nome do aluno", "aluno", "nome", "name"), null);
                Integer gradeIndex = excelImportHelper.findColumn(headers, List.of("nota", "grade"), null);
                Integer progressIndex = excelImportHelper.findColumn(headers, List.of("progresso", "percentual", "completion", "completion_percentage"), null);
                Integer frequencyIndex = excelImportHelper.findColumn(headers, List.of("frequencia", "frequência", "frequency"), null);
                Integer statusIndex = excelImportHelper.findColumn(headers, List.of("status", "situacao", "situação"), null);
                Integer dateIndex = excelImportHelper.findColumn(headers, List.of("data realizacao", "data_realizacao", "data", "date"), null);

                String courseName = courseIndex == null ? sheet.getSheetName() : excelImportHelper.getString(row, courseIndex);
                String cpf = cpfIndex == null ? null : excelImportHelper.getString(row, cpfIndex);
                String email = emailIndex == null ? null : excelImportHelper.getString(row, emailIndex);
                String studentName = nameIndex == null ? null : excelImportHelper.getString(row, nameIndex);
                Double grade = gradeIndex == null ? null : excelImportHelper.getDouble(row, gradeIndex);
                Double progress = readPercentage(row, progressIndex);
                Double frequency = readPercentage(row, frequencyIndex);
                String importedStatus = statusIndex == null ? null : excelImportHelper.getString(row, statusIndex);
                LocalDate realizationDate = dateIndex == null ? null : excelImportHelper.getDate(row, dateIndex);

                if (courseName == null || courseName.isBlank() || allBlank(cpf, email, studentName)) {
                    skippedRows++;
                    continue;
                }

                totalProcessed++;

                CourseModel course = resolveCourse(courseName, courseName.trim())
                        .orElseGet(() -> {
                            CourseModel newCourse = new CourseModel();
                            newCourse.setName(courseName.trim());
                            return courseRepository.save(newCourse);
                        });
                EnrollmentModel enrollment = resolveEnrollment(enrollmentByCpf, enrollmentByEmail, enrollmentByName, cpf, email, studentName);
                if (enrollment == null || enrollment.getPeople() == null) {
                    PeopleModel candidatePerson = resolveStagePerson(stagePeopleByCpf, stagePeopleByEmail, stagePeopleByName, cpf, email, studentName);
                    if (candidatePerson != null) {
                        EnrollmentModel newEnrollment = new EnrollmentModel();
                        newEnrollment.setPeople(candidatePerson);
                        newEnrollment.setClassModel(classModel);
                        newEnrollment.setAcademicRole(alunoRole);
                        newEnrollment.setEnrollmentDate(LocalDate.now());
                        newEnrollment.setStatus("MATRICULADO");
                        enrollment = enrollmentRepository.save(newEnrollment);
                        cacheEnrollment(enrollment, enrollmentByCpf, enrollmentByEmail, enrollmentByName);
                    } else {
                        peopleNotInClassCpfs.add(firstNonBlank(cpf, email, studentName, "linha " + (rowNum + 1)));
                        skippedRows++;
                        continue;
                    }
                }

                CourseAssignmentModel assignment = courseAssignmentRepository
                        .findByCourseIdAndClassId(course.getId(), classId);
                if (assignment == null) {
                    CourseAssignmentModel newAssignment = new CourseAssignmentModel();
                    newAssignment.setCourse(course);
                    newAssignment.setClassModel(enrollment.getClassModel());
                    newAssignment.setRequired(true);
                    courseAssignmentRepository.save(newAssignment);
                }

                Long peopleId = enrollment.getPeople().getId();
                CourseProgressionModel progression = courseProgressionRepository
                        .findByCourseIdAndPeopleIdAndClassModelId(course.getId(), peopleId, classId)
                        .orElse(null);

                if (progression == null) {
                    progression = new CourseProgressionModel();
                    progression.setCourse(course);
                    progression.setPeople(enrollment.getPeople());
                    progression.setClassModel(classModel);
                    progression.setDate(realizationDate == null ? LocalDate.now() : realizationDate);
                    createdProgressions++;
                } else {
                    updatedProgressions++;
                }
                progression.setClassModel(classModel);

                double completion = progress == null ? 0.0 : Math.max(0.0, Math.min(100.0, progress));
                progression.setCompletionPercentage(completion);
                progression.setGrade(grade);
                progression.setFrequency(frequency);
                if (realizationDate != null) {
                    progression.setDate(realizationDate);
                }
                progression.setStatus(normalizeProgressionStatus(importedStatus, completion));
                progression.setLastAccess(completion > 0 ? LocalDate.now() : null);
                courseProgressionRepository.save(progression);

                if (grade != null) {
                    enrollment.setGrade(grade);
                    enrollmentsToUpdate.put(enrollment.getId(), enrollment);
                    updatedGrades++;
                }
                if (frequency != null) {
                    enrollment.setFrequency(frequency);
                    enrollmentsToUpdate.put(enrollment.getId(), enrollment);
                }
            }
            }
        }

        if (!enrollmentsToUpdate.isEmpty()) {
            enrollmentRepository.saveAll(new ArrayList<>(enrollmentsToUpdate.values()));
        }

        return new CourseProgressionImportResponseDTO(
                totalProcessed,
                createdProgressions,
                updatedProgressions,
                updatedGrades,
                skippedRows,
                new ArrayList<>(unknownCourses),
                new ArrayList<>(peopleNotInClassCpfs)
        );
    }

    // Update an existing course
    @Transactional
    public com.example.brisa.models.course.CourseModel updateCourse(Long id, com.example.brisa.models.course.CourseModel input) {
        com.example.brisa.models.course.CourseModel existing = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com id: " + id));

        if (input.getName() != null) existing.setName(input.getName());
        if (input.getCode() != null) existing.setCode(trimToNull(input.getCode()));
        if (input.getDescription() != null) existing.setDescription(input.getDescription());
        if (input.getInclusionDate() != null) existing.setInclusionDate(input.getInclusionDate());
        // update workload if provided (non-zero)
        if (input.getWorkload() != 0) existing.setWorkload(input.getWorkload());

        if (input.getKnowledgeArea() != null && input.getKnowledgeArea().getName() != null) {
            String kaName = input.getKnowledgeArea().getName().trim();
            if (!kaName.isEmpty()) {
                com.example.brisa.models.KnowledgeAreaModel ka = knowledgeAreaRepository.findByNameIgnoreCase(kaName)
                        .orElseGet(() -> knowledgeAreaRepository.save(new com.example.brisa.models.KnowledgeAreaModel(null, kaName)));
                existing.setKnowledgeArea(ka);
            }
        }
        if (input.getSubArea() != null && input.getSubArea().getName() != null) {
            String subAreaName = input.getSubArea().getName().trim();
            if (!subAreaName.isEmpty()) {
                existing.setSubArea(getOrCreateKnowledgeArea(subAreaName));
            }
        }

        return courseRepository.save(existing);
    }

    // Delete a course and related assignments/progressions
    @Transactional
    public void deleteCourse(Long id) {
        com.example.brisa.models.course.CourseModel course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com id: " + id));

        // remove course assignments
        java.util.List<com.example.brisa.models.course.CourseAssignmentModel> assignments =
                courseAssignmentRepository.findByCourseId(id);
        if (assignments != null && !assignments.isEmpty()) {
            courseAssignmentRepository.deleteAll(assignments);
        }

        // remove progressions
        java.util.List<CourseProgressionModel> progs = courseProgressionRepository.findByCourseId(id);
        if (progs != null && !progs.isEmpty()) {
            courseProgressionRepository.deleteAll(progs);
        }

        courseRepository.delete(course);
    }

    /**
     * Envia email de alerta para todos os alunos com status "não iniciado" ou "em andamento"
     * em um curso específico dentro de uma turma.
     */
    public CourseAlertResponseDTO sendCourseAlert(Long courseId, Long classId, CourseAlertRequestDTO request) {
        CourseModel course = findById(courseId);

        // Filtra apenas alunos que estão na etapa de nivelamento
        Set<Long> nivelamentoPeopleIds = stageRepository
                .findByNameAndClassModelId("NIVELAMENTO", classId)
                .map(nivelamentoStage ->
                    stageCandidateRepository.findByStageId(nivelamentoStage.getId())
                        .stream()
                        .map(sc -> sc.getPeople().getId())
                        .collect(Collectors.toSet())
                )
                .orElse(Set.of());

        if (nivelamentoPeopleIds.isEmpty()) {
            return new CourseAlertResponseDTO(0, 0, List.of());
        }

        String subject = hasText(request == null ? null : request.subject())
                ? request.subject().trim()
                : "Pendencia no curso " + course.getName();
        String message = hasText(request == null ? null : request.message())
                ? request.message().trim()
                : "Identificamos pendencias neste curso. Acesse a plataforma e regularize sua situacao dentro do prazo.";

        List<CourseProgressionModel> pendingProgressions = courseProgressionRepository
                .findByCourseIdAndClassId(courseId, classId)
                .stream()
                .filter(p -> nivelamentoPeopleIds.contains(p.getPeople().getId()))
                .filter(this::isPendingCourseProgression)
                .collect(Collectors.toList());

        if (!emailService.isMailConfigured()) {
            return new CourseAlertResponseDTO(pendingProgressions.size(), 0, List.of());
        }

        int totalSent   = 0;
        int totalFailed = 0;
        List<String> failedEmails = new ArrayList<>();

        for (CourseProgressionModel progression : pendingProgressions) {
            PeopleModel people = progression.getPeople();
            String email = people == null ? null : people.getEmail();
            String name = people == null ? "Aluno(a)" : firstNonBlank(people.getName(), "Aluno(a)");
            int pct = (int) Math.round(progression.getCompletionPercentage());

            if (!hasText(email)) {
                failedEmails.add(name + " (sem e-mail)");
                totalFailed++;
                continue;
            }

            try {
                String htmlContent = buildAlertEmailHtml(
                        name,
                        message,
                        course.getName(),
                        pct
                );
                // Use synchronous send so failures are reported to this caller
                emailService.sendEmailSync(email.trim(), subject, htmlContent);
                totalSent++;
            } catch (MessagingException | IOException | RuntimeException e) {
                System.err.println("Falha ao enviar email para " + email + ": " + e.getMessage());
                failedEmails.add(email);
                totalFailed++;
            }
        }

        return new CourseAlertResponseDTO(totalSent, totalFailed, failedEmails);
    }

    // Retorna uma lista de cursos sugeridos (backfill) para uma turma.
    // Se a turma não tem cursos associados (nenhuma progressão), retorna os primeiros N cursos cadastrados.
    public List<CourseModel> findBackfillCourses(Long classId) {
        List<CourseModel> all = courseRepository.findAll();

        // 1) Prefira associações persistidas na tabela course_assignments
        List<com.example.brisa.models.course.CourseAssignmentModel> assignments =
            courseAssignmentRepository.findByClassId(classId);

        if (assignments != null && !assignments.isEmpty()) {
            return assignments.stream()
                .map(a -> a.getCourse())
                .collect(Collectors.toList());
        }

        // 2) Fallback: se existirem progressões (dados legados), retorne os cursos presentes
        List<CourseProgressionModel> progs = courseProgressionRepository.findByClassId(classId);
        if (progs == null || progs.isEmpty()) {
            return all.subList(0, Math.min(6, all.size()));
        }

        java.util.Set<Long> progressionCourseIds = progs.stream()
            .map(p -> p.getCourse().getId())
            .collect(java.util.stream.Collectors.toSet());

        return all.stream()
            .filter(c -> progressionCourseIds.contains(c.getId()))
            .collect(java.util.stream.Collectors.toList());
    }

    // Associa um curso a uma turma: cria progressões para todos os alunos matriculados que ainda não
    // possuam progressão neste curso na turma (status = "não iniciado").
    @Transactional
    public void assignCourseToClass(Long courseId, Long classId) {
        assignCourseToClass(courseId, classId, true);
    }

    @Transactional
    public void assignCourseToClass(Long courseId, Long classId, boolean required) {
        assignCourseToClass(courseId, classId, required, null);
    }

    @Transactional
    public void assignCourseToClass(Long courseId, Long classId, boolean required, Long advisorId) {
        CourseModel course = findById(courseId);
        AdvisorModel advisor = advisorId == null
                ? null
                : advisorRepository.findById(advisorId)
                        .orElseThrow(() -> new ResourceNotFoundException("Orientador não encontrado"));

        // Persist assignment if ainda não existe, or update the required flag
        com.example.brisa.models.course.CourseAssignmentModel existingAssignment =
                courseAssignmentRepository.findByCourseIdAndClassId(courseId, classId);

        if (existingAssignment == null) {
            com.example.brisa.models.course.CourseAssignmentModel assignment =
                    new com.example.brisa.models.course.CourseAssignmentModel();
            assignment.setCourse(course);
            com.example.brisa.models.ClassModel cls = classRepository.findById(classId)
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
            assignment.setClassModel(cls);
            assignment.setRequired(required);
            assignment.setAdvisor(advisor);
            courseAssignmentRepository.save(assignment);
        } else {
            if (existingAssignment.isRequired() != required || advisor != null) {
                existingAssignment.setRequired(required);
                if (advisor != null) {
                    existingAssignment.setAdvisor(advisor);
                }
                courseAssignmentRepository.save(existingAssignment);
            }
        }

        // Create missing "not started" progressions for all enrolled students in the class.
        backfillProgressionsForAssignedCourse(course, classId);
    }

    // Retorna associações (assignments) para uma turma
    public List<com.example.brisa.models.course.CourseAssignmentModel> findAssignmentsByClassId(Long classId) {
        return courseAssignmentRepository.findByClassId(classId);
    }

    // Remove associação do curso com a turma: exclui todas as progressões deste curso para a turma.
    @Transactional
    public void removeCourseFromClass(Long courseId, Long classId) {
        // remove assignment persistida
        courseAssignmentRepository.deleteByCourseIdAndClassId(courseId, classId);

        // remove progressões relacionadas
        List<CourseProgressionModel> progs = courseProgressionRepository.findByCourseIdAndClassId(courseId, classId);
        if (progs != null && !progs.isEmpty()) {
            courseProgressionRepository.deleteAll(progs);
        }
    }

    /**
     * Carrega o template HTML e substitui os placeholders.
     */
    private String buildAlertEmailHtml(String studentName, String message,
                                        String courseName, int completionPercentage)
            throws IOException {

        ClassPathResource resource = new ClassPathResource("templates/email/course-alert.html");
        String html = new String(Files.readAllBytes(Path.of(resource.getURI())));

        // Some templates were accidentally saved as a Java-like string (with leading/trailing
        // quotes and escaped newlines/quotes). Normalize that case so replacements work.
        if (html != null && html.startsWith("\"") && html.endsWith("\"")) {
            // remove surrounding quotes
            html = html.substring(1, html.length() - 1)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");
        }

        return html
                .replace("${studentName}",          safeEmailValue(studentName))
                .replace("${message}",              safeEmailValue(message))
                .replace("${courseName}",           safeEmailValue(courseName))
                .replace("${completionPercentage}", String.valueOf(completionPercentage));
    }

    private boolean isPendingCourseProgression(CourseProgressionModel progression) {
        if (progression == null || progression.getCompletionPercentage() >= 100.0) {
            return false;
        }

        String normalizedStatus = normalizeStatus(progression.getStatus());
        if (normalizedStatus.isBlank()) return true;
        if (normalizedStatus.startsWith("nao")) return true;
        if (normalizedStatus.contains("concluido")
                || normalizedStatus.contains("realizado")
                || normalizedStatus.contains("finalizado")
                || normalizedStatus.contains("aprovado")
                || normalizedStatus.contains("cancelado")
                || normalizedStatus.contains("inativo")) {
            return false;
        }
        return true;
    }

    private String normalizeStatus(String status) {
        return excelImportHelper.normalize(status).replaceAll("[^a-z0-9]+", "");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String safeEmailValue(String value) {
        return value == null ? "" : value;
    }

    private void backfillProgressionsForAssignedCourse(CourseModel course, Long classId) {
        List<EnrollmentModel> enrollments = enrollmentRepository.findByClassModelId(classId);
        if (enrollments == null || enrollments.isEmpty()) {
            return;
        }

        Set<Long> existingPeopleIds = courseProgressionRepository
                .findByCourseIdAndClassId(course.getId(), classId)
                .stream()
                .filter(p -> p.getPeople() != null && p.getPeople().getId() != null)
                .map(p -> p.getPeople().getId())
                .collect(Collectors.toSet());

        List<CourseProgressionModel> toCreate = new ArrayList<>();

        for (EnrollmentModel enrollment : enrollments) {
            if (enrollment.getPeople() == null || enrollment.getPeople().getId() == null) continue;
            if (!isStudentEnrollment(enrollment) || !isActiveEnrollment(enrollment)) continue;

            Long peopleId = enrollment.getPeople().getId();
            if (existingPeopleIds.contains(peopleId)) continue;

            CourseProgressionModel progression = new CourseProgressionModel();
            progression.setCourse(course);
            progression.setPeople(enrollment.getPeople());
            progression.setClassModel(enrollment.getClassModel());
            progression.setDate(LocalDate.now());
            progression.setCompletionPercentage(0.0);
            progression.setStatus("não iniciado");
            progression.setLastAccess(null);
            toCreate.add(progression);

            existingPeopleIds.add(peopleId);
        }

        if (!toCreate.isEmpty()) {
            courseProgressionRepository.saveAll(toCreate);
        }
    }

    private boolean isStudentEnrollment(EnrollmentModel enrollment) {
        return enrollment.getAcademicRole() == null
                || enrollment.getAcademicRole().getName() == null
                || "ALUNO".equalsIgnoreCase(enrollment.getAcademicRole().getName());
    }

    private boolean isActiveEnrollment(EnrollmentModel enrollment) {
        String normalized = enrollment.getStatus() == null ? "" : enrollment.getStatus().trim().toLowerCase();
        return normalized.isBlank()
                || normalized.contains("ativa")
                || normalized.contains("ativo")
                || normalized.contains("andamento")
                || normalized.contains("pendente");
    }

    private com.example.brisa.models.KnowledgeAreaModel getOrCreateKnowledgeArea(String name) {
        String kaName = name == null ? "" : name.trim();
        if (kaName.isEmpty()) return null;
        return knowledgeAreaRepository.findByNameIgnoreCase(kaName)
                .orElseGet(() -> knowledgeAreaRepository.save(new com.example.brisa.models.KnowledgeAreaModel(null, kaName)));
    }

    private Optional<CourseModel> resolveCourse(String codeOrName, String fallbackName) {
        String code = trimToNull(codeOrName);
        if (code != null) {
            Optional<CourseModel> byCode = courseRepository.findByCodeIgnoreCase(code);
            if (byCode.isPresent()) {
                return byCode;
            }
        }

        String name = trimToNull(fallbackName);
        return name == null ? Optional.empty() : courseRepository.findByNameIgnoreCase(name);
    }

    private Optional<AdvisorModel> resolveAdvisor(String cpf, String name) {
        String normalizedCpf = normalizeCpf(cpf);
        if (!normalizedCpf.isBlank()) {
            Optional<AdvisorModel> byCpf = advisorRepository.findAll().stream()
                    .filter(advisor -> normalizedCpf.equals(normalizeCpf(advisor.getCpf())))
                    .findFirst();
            if (byCpf.isPresent()) {
                return byCpf;
            }
        }

        String normalizedName = excelImportHelper.normalize(name).replace(" ", "");
        if (normalizedName.isBlank()) {
            return Optional.empty();
        }
        return advisorRepository.findAll().stream()
                .filter(advisor -> excelImportHelper.normalize(advisor.getName()).replace(" ", "").equals(normalizedName))
                .findFirst();
    }

    private void cacheEnrollment(
            EnrollmentModel enrollment,
            Map<String, EnrollmentModel> byCpf,
            Map<String, EnrollmentModel> byEmail,
            Map<String, EnrollmentModel> byName
    ) {
        if (enrollment == null || enrollment.getPeople() == null) {
            return;
        }

        PeopleModel people = enrollment.getPeople();
        String cpf = normalizeCpf(people.getCpf());
        if (!cpf.isBlank()) {
            byCpf.putIfAbsent(cpf, enrollment);
        }

        String email = normalizeEmail(people.getEmail());
        if (!email.isBlank()) {
            byEmail.putIfAbsent(email, enrollment);
        }

        String name = normalizeName(people.getName());
        if (!name.isBlank()) {
            byName.putIfAbsent(name, enrollment);
        }
    }

    private EnrollmentModel resolveEnrollment(
            Map<String, EnrollmentModel> byCpf,
            Map<String, EnrollmentModel> byEmail,
            Map<String, EnrollmentModel> byName,
            String cpf,
            String email,
            String name
    ) {
        String normalizedCpf = normalizeCpf(cpf);
        if (!normalizedCpf.isBlank() && byCpf.containsKey(normalizedCpf)) {
            return byCpf.get(normalizedCpf);
        }

        String normalizedEmail = normalizeEmail(email);
        if (!normalizedEmail.isBlank() && byEmail.containsKey(normalizedEmail)) {
            return byEmail.get(normalizedEmail);
        }

        String normalizedName = normalizeName(name);
        return normalizedName.isBlank() ? null : byName.get(normalizedName);
    }

    private PeopleModel resolveStagePerson(
            Map<String, PeopleModel> byCpf,
            Map<String, PeopleModel> byEmail,
            Map<String, PeopleModel> byName,
            String cpf,
            String email,
            String name
    ) {
        String normalizedCpf = normalizeCpf(cpf);
        if (!normalizedCpf.isBlank() && byCpf.containsKey(normalizedCpf)) {
            return byCpf.get(normalizedCpf);
        }

        String normalizedEmail = normalizeEmail(email);
        if (!normalizedEmail.isBlank() && byEmail.containsKey(normalizedEmail)) {
            return byEmail.get(normalizedEmail);
        }

        String normalizedName = normalizeName(name);
        return normalizedName.isBlank() ? null : byName.get(normalizedName);
    }

    private Double readPercentage(Row row, Integer index) {
        if (row == null || index == null) {
            return null;
        }

        String raw = excelImportHelper.getString(row, index);
        if (raw != null && raw.contains("%")) {
            try {
                return Double.parseDouble(raw.replace("%", "").replace(".", "").replace(",", ".").trim());
            } catch (NumberFormatException ignored) {
            }
        }

        Double numeric = excelImportHelper.getDouble(row, index);
        if (numeric != null && numeric > 0.0 && numeric <= 1.0) {
            return numeric * 100.0;
        }
        return numeric;
    }

    private boolean isCompletionSummarySheet(String sheetName) {
        String normalized = normalizeName(sheetName);
        return normalized.contains("alunosconclusao") || normalized.equals("conclusao");
    }

    private boolean allBlank(String... values) {
        for (String value : values) {
            if (trimToNull(value) != null) {
                return false;
            }
        }
        return true;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private String normalizeName(String value) {
        return excelImportHelper.normalize(value).replaceAll("[^a-z0-9]+", "");
    }

    private String normalizeProgressionStatus(String importedStatus, double completion) {
        String normalized = trimToNull(importedStatus);
        if (normalized == null) {
            return statusFromCompletion(completion);
        }

        String status = excelImportHelper.normalize(normalized);
        if (status.contains("realizado") || status.contains("concluido") || status.contains("finalizado")) {
            return "realizado";
        }
        if (status.contains("nao") || status.contains("pendente") || status.contains("iniciado")) {
            return "não-realizado";
        }
        return normalized;
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            String trimmed = trimToNull(value);
            if (trimmed != null) {
                return trimmed;
            }
        }
        return null;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> {
                String v = cell.getStringCellValue();
                yield v == null ? null : v.trim();
            }
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                if (Math.floor(value) == value) {
                    yield String.valueOf((long) value);
                }
                yield String.valueOf(value);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> {
                CellType cached = cell.getCachedFormulaResultType();
                if (cached == CellType.STRING) {
                    String v = cell.getStringCellValue();
                    yield v == null ? null : v.trim();
                }
                if (cached == CellType.NUMERIC) {
                    double value = cell.getNumericCellValue();
                    if (Math.floor(value) == value) {
                        yield String.valueOf((long) value);
                    }
                    yield String.valueOf(value);
                }
                yield null;
            }
            default -> null;
        };
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> cell.getNumericCellValue();
                case STRING -> {
                    String value = cell.getStringCellValue();
                    if (value == null) yield null;
                    String normalized = value.trim().replace("%", "").replace(",", ".");
                    if (normalized.isEmpty()) yield null;
                    yield Double.parseDouble(normalized);
                }
                case FORMULA -> {
                    CellType cached = cell.getCachedFormulaResultType();
                    if (cached == CellType.NUMERIC) {
                        yield cell.getNumericCellValue();
                    }
                    if (cached == CellType.STRING) {
                        String value = cell.getStringCellValue();
                        if (value == null) yield null;
                        String normalized = value.trim().replace("%", "").replace(",", ".");
                        if (normalized.isEmpty()) yield null;
                        yield Double.parseDouble(normalized);
                    }
                    yield null;
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    private String normalizeCpf(String cpf) {
        if (cpf == null) return "";
        return cpf.replaceAll("\\D", "");
    }

    private String statusFromCompletion(double completion) {
        if (completion >= 100.0) return "concluído";
        if (completion > 0.0) return "em andamento";
        return "não iniciado";
    }
}
