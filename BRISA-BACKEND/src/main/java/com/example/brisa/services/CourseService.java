package com.example.brisa.services;

import com.example.brisa.dtos.course.CourseAlertRequestDTO;
import com.example.brisa.dtos.course.CourseAlertResponseDTO;
import com.example.brisa.dtos.course.CourseClassImportResponseDTO;
import com.example.brisa.dtos.course.CourseProgressionImportResponseDTO;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.course.CourseAssignmentModel;
import com.example.brisa.models.course.CourseModel;
import com.example.brisa.models.course.CourseProgressionModel;
import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.CourseAssignmentRepository;
import com.example.brisa.repositories.CourseProgressionRepository;
import com.example.brisa.repositories.CourseRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.KnowledgeAreaRepository;
import com.example.brisa.repositories.StageCandidateRepository;
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
    private final AcademicRoleRepository academicRoleRepository;

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
            com.example.brisa.models.course.CourseModel c = new com.example.brisa.models.course.CourseModel();
            c.setName(dto.name().trim());
            c.setDescription(dto.description());
            if (dto.competencia() != null && !dto.competencia().trim().isEmpty()) {
                String kaName = dto.competencia().trim();
                com.example.brisa.models.KnowledgeAreaModel ka = knowledgeAreaRepository.findByNameIgnoreCase(kaName)
                        .orElseGet(() -> knowledgeAreaRepository.save(new com.example.brisa.models.KnowledgeAreaModel(null, kaName)));
                c.setKnowledgeArea(ka);
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

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                String courseName = getCellValueAsString(row.getCell(0));
                String competencia = getCellValueAsString(row.getCell(1));
                String description = getCellValueAsString(row.getCell(2));

                if (courseName == null || courseName.isBlank()) {
                    skippedRows++;
                    continue;
                }

                totalProcessed++;
                String normalizedName = courseName.trim();

                CourseModel course = courseRepository.findByNameIgnoreCase(normalizedName).orElse(null);
                if (course == null) {
                    course = new CourseModel();
                    course.setName(normalizedName);
                    course.setDescription(description);
                    if (competencia != null && !competencia.isBlank()) {
                        course.setKnowledgeArea(getOrCreateKnowledgeArea(competencia));
                    }
                    course = courseRepository.save(course);
                    createdCourses++;
                    createdCourseNames.add(course.getName());
                }

                CourseAssignmentModel assignment = courseAssignmentRepository
                        .findByCourseIdAndClassId(course.getId(), classId);
                if (assignment == null) {
                    CourseAssignmentModel newAssignment = new CourseAssignmentModel();
                    newAssignment.setCourse(course);
                    newAssignment.setClassModel(classModel);
                    newAssignment.setRequired(true);
                    courseAssignmentRepository.save(newAssignment);
                    backfillProgressionsForAssignedCourse(course, classId);
                    assignedCourses++;
                } else {
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
        Map<String, EnrollmentModel> enrollmentByCpf = enrollments.stream()
                .filter(e -> e.getPeople() != null && e.getPeople().getCpf() != null)
                .collect(Collectors.toMap(
                        e -> normalizeCpf(e.getPeople().getCpf()),
                        e -> e,
                        (a, b) -> a
                ));

        Map<String, PeopleModel> stagePeopleByCpf = stageCandidateRepository.findByClassIdWithPeople(classId).stream()
            .filter(sc -> sc.getPeople() != null && sc.getPeople().getCpf() != null)
            .collect(Collectors.toMap(
                sc -> normalizeCpf(sc.getPeople().getCpf()),
                StageCandidateModel::getPeople,
                (a, b) -> a
            ));

        AcademicRoleModel alunoRole = academicRoleRepository.findByName("ALUNO")
            .orElseGet(() -> academicRoleRepository.save(new AcademicRoleModel("ALUNO", "Aluno")));

        Map<Long, EnrollmentModel> enrollmentsToUpdate = new HashMap<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return new CourseProgressionImportResponseDTO(0, 0, 0, 0, 0, new ArrayList<>(), new ArrayList<>());
            }

            for (int rowNum = 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null) continue;

                String courseName = getCellValueAsString(row.getCell(0));
                String cpf = getCellValueAsString(row.getCell(1));
                Double grade = getCellValueAsDouble(row.getCell(2));
                Double progress = getCellValueAsDouble(row.getCell(3));

                if (courseName == null || courseName.isBlank() || cpf == null || cpf.isBlank()) {
                    skippedRows++;
                    continue;
                }

                totalProcessed++;

                Optional<CourseModel> courseOpt = courseRepository.findByNameIgnoreCase(courseName.trim());
                if (courseOpt.isEmpty()) {
                    unknownCourses.add(courseName.trim());
                    skippedRows++;
                    continue;
                }

                CourseModel course = courseOpt.get();
                EnrollmentModel enrollment = enrollmentByCpf.get(normalizeCpf(cpf));
                if (enrollment == null || enrollment.getPeople() == null) {
                    PeopleModel candidatePerson = stagePeopleByCpf.get(normalizeCpf(cpf));
                    if (candidatePerson != null) {
                        EnrollmentModel newEnrollment = new EnrollmentModel();
                        newEnrollment.setPeople(candidatePerson);
                        newEnrollment.setClassModel(classModel);
                        newEnrollment.setAcademicRole(alunoRole);
                        newEnrollment.setEnrollmentDate(LocalDate.now());
                        newEnrollment.setStatus("MATRICULADO");
                        enrollment = enrollmentRepository.save(newEnrollment);
                        enrollmentByCpf.put(normalizeCpf(cpf), enrollment);
                    } else {
                        peopleNotInClassCpfs.add(cpf);
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
                        .findByCourseIdAndPeopleId(course.getId(), peopleId)
                        .orElse(null);

                if (progression == null) {
                    progression = new CourseProgressionModel();
                    progression.setCourse(course);
                    progression.setPeople(enrollment.getPeople());
                    progression.setDate(LocalDate.now());
                    createdProgressions++;
                } else {
                    updatedProgressions++;
                }

                double completion = progress == null ? 0.0 : Math.max(0.0, Math.min(100.0, progress));
                progression.setCompletionPercentage(completion);
                progression.setStatus(statusFromCompletion(completion));
                progression.setLastAccess(completion > 0 ? LocalDate.now() : null);
                courseProgressionRepository.save(progression);

                if (grade != null) {
                    enrollment.setGrade(grade);
                    enrollmentsToUpdate.put(enrollment.getId(), enrollment);
                    updatedGrades++;
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
        if (input.getDescription() != null) existing.setDescription(input.getDescription());
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

        List<CourseProgressionModel> pendingProgressions = courseProgressionRepository
                .findByCourseIdAndClassId(courseId, classId)
                .stream()
                .filter(p -> "não iniciado".equalsIgnoreCase(p.getStatus())
                          || "em andamento".equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        int totalSent   = 0;
        int totalFailed = 0;
        List<String> failedEmails = new ArrayList<>();

        for (CourseProgressionModel progression : pendingProgressions) {
            String email = progression.getPeople().getEmail();
            String name  = progression.getPeople().getName();
            int    pct   = (int) Math.round(progression.getCompletionPercentage());

            try {
                String htmlContent = buildAlertEmailHtml(
                        name,
                        request.message(),
                        course.getName(),
                        pct
                );
                emailService.sendEmail(email, request.subject(), htmlContent);
                totalSent++;
            } catch (MessagingException | IOException e) {
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
        CourseModel course = findById(courseId);

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
            courseAssignmentRepository.save(assignment);
        } else {
            if (existingAssignment.isRequired() != required) {
                existingAssignment.setRequired(required);
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

        return html
                .replace("${studentName}",          studentName)
                .replace("${message}",              message)
                .replace("${courseName}",           courseName)
                .replace("${completionPercentage}", String.valueOf(completionPercentage));
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

            Long peopleId = enrollment.getPeople().getId();
            if (existingPeopleIds.contains(peopleId)) continue;

            CourseProgressionModel progression = new CourseProgressionModel();
            progression.setCourse(course);
            progression.setPeople(enrollment.getPeople());
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

    private com.example.brisa.models.KnowledgeAreaModel getOrCreateKnowledgeArea(String name) {
        String kaName = name == null ? "" : name.trim();
        if (kaName.isEmpty()) return null;
        return knowledgeAreaRepository.findByNameIgnoreCase(kaName)
                .orElseGet(() -> knowledgeAreaRepository.save(new com.example.brisa.models.KnowledgeAreaModel(null, kaName)));
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