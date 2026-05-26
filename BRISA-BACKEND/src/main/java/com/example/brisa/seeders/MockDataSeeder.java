package com.example.brisa.seeders;

import com.example.brisa.enums.AdvisorRoleType;
import com.example.brisa.enums.LogAction;
import com.example.brisa.enums.StageStatus;
import com.example.brisa.models.AdvisorModel;
import com.example.brisa.models.CareerAutomationDispatchModel;
import com.example.brisa.models.CareerAutomationSettingsModel;
import com.example.brisa.models.CareerProgressionModel;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.InstitutionModel;
import com.example.brisa.models.KnowledgeAreaModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.PeopleProjectGroupModel;
import com.example.brisa.models.ProgramAddendumModel;
import com.example.brisa.models.ProgramInstitutionModel;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.models.ProjectGroupMeetingModel;
import com.example.brisa.models.ProjectGroupModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.StageModel;
import com.example.brisa.models.SystemLogModel;
import com.example.brisa.models.auth.UserModel;
import com.example.brisa.models.course.CourseAssignmentModel;
import com.example.brisa.models.course.CourseEvaluationModel;
import com.example.brisa.models.course.CourseModel;
import com.example.brisa.models.course.CourseProgressionModel;
import com.example.brisa.models.exam.ExamAnswerModel;
import com.example.brisa.models.exam.ExamModel;
import com.example.brisa.models.exam.ExamQuestionModel;
import com.example.brisa.models.exam.ExamResultModel;
import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.models.roles.InstitutionRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import com.example.brisa.repositories.AdvisorRepository;
import com.example.brisa.repositories.CareerAutomationDispatchRepository;
import com.example.brisa.repositories.CareerAutomationSettingsRepository;
import com.example.brisa.repositories.CareerProgressionRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.CourseAssignmentRepository;
import com.example.brisa.repositories.CourseEvaluationRepository;
import com.example.brisa.repositories.CourseProgressionRepository;
import com.example.brisa.repositories.CourseRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.ExamAnswerRepository;
import com.example.brisa.repositories.ExamQuestionRepository;
import com.example.brisa.repositories.ExamRepository;
import com.example.brisa.repositories.ExamResultRepository;
import com.example.brisa.repositories.InstitutionRepository;
import com.example.brisa.repositories.InstitutionRoleRepository;
import com.example.brisa.repositories.KnowledgeAreaRepository;
import com.example.brisa.repositories.PeopleProjectGroupRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.ProgramAddendumRepository;
import com.example.brisa.repositories.ProgramInstitutionRepository;
import com.example.brisa.repositories.ProgramRepository;
import com.example.brisa.repositories.ProjectGroupMeetingRepository;
import com.example.brisa.repositories.ProjectGroupRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import com.example.brisa.repositories.StageRepository;
import com.example.brisa.repositories.SystemLogRepository;
import com.example.brisa.repositories.UserRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.models.EnrollmentModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Component
@Order(1100)
public class MockDataSeeder implements ApplicationListener<ApplicationReadyEvent> {

    private final AcademicRoleRepository academicRoleRepository;
    private final AdvisorRepository advisorRepository;
    private final CareerAutomationDispatchRepository careerAutomationDispatchRepository;
    private final CareerAutomationSettingsRepository careerAutomationSettingsRepository;
    private final CareerProgressionRepository careerProgressionRepository;
    private final ClassRepository classRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final CourseEvaluationRepository courseEvaluationRepository;
    private final CourseProgressionRepository courseProgressionRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamRepository examRepository;
    private final ExamResultRepository examResultRepository;
    private final InstitutionRepository institutionRepository;
    private final InstitutionRoleRepository institutionRoleRepository;
    private final KnowledgeAreaRepository knowledgeAreaRepository;
    private final PeopleProjectGroupRepository peopleProjectGroupRepository;
    private final PeopleRepository peopleRepository;
    private final ProgramAddendumRepository programAddendumRepository;
    private final ProgramInstitutionRepository programInstitutionRepository;
    private final ProgramRepository programRepository;
    private final ProjectGroupMeetingRepository projectGroupMeetingRepository;
    private final ProjectGroupRepository projectGroupRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final StageRepository stageRepository;
    private final SystemLogRepository systemLogRepository;
    private final UserRepository userRepository;

    @Value("${app.mock-data.enabled:false}")
    private boolean mockDataEnabled;

    private boolean alreadySetup = false;

    public MockDataSeeder(
            AcademicRoleRepository academicRoleRepository,
            AdvisorRepository advisorRepository,
            CareerAutomationDispatchRepository careerAutomationDispatchRepository,
            CareerAutomationSettingsRepository careerAutomationSettingsRepository,
            CareerProgressionRepository careerProgressionRepository,
            ClassRepository classRepository,
            CourseAssignmentRepository courseAssignmentRepository,
            CourseEvaluationRepository courseEvaluationRepository,
            CourseProgressionRepository courseProgressionRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            ExamAnswerRepository examAnswerRepository,
            ExamQuestionRepository examQuestionRepository,
            ExamRepository examRepository,
            ExamResultRepository examResultRepository,
            InstitutionRepository institutionRepository,
            InstitutionRoleRepository institutionRoleRepository,
            KnowledgeAreaRepository knowledgeAreaRepository,
            PeopleProjectGroupRepository peopleProjectGroupRepository,
            PeopleRepository peopleRepository,
            ProgramAddendumRepository programAddendumRepository,
            ProgramInstitutionRepository programInstitutionRepository,
            ProgramRepository programRepository,
            ProjectGroupMeetingRepository projectGroupMeetingRepository,
            ProjectGroupRepository projectGroupRepository,
            StageCandidateRepository stageCandidateRepository,
            StageRepository stageRepository,
            SystemLogRepository systemLogRepository,
            UserRepository userRepository
    ) {
        this.academicRoleRepository = academicRoleRepository;
        this.advisorRepository = advisorRepository;
        this.careerAutomationDispatchRepository = careerAutomationDispatchRepository;
        this.careerAutomationSettingsRepository = careerAutomationSettingsRepository;
        this.careerProgressionRepository = careerProgressionRepository;
        this.classRepository = classRepository;
        this.courseAssignmentRepository = courseAssignmentRepository;
        this.courseEvaluationRepository = courseEvaluationRepository;
        this.courseProgressionRepository = courseProgressionRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examAnswerRepository = examAnswerRepository;
        this.examQuestionRepository = examQuestionRepository;
        this.examRepository = examRepository;
        this.examResultRepository = examResultRepository;
        this.institutionRepository = institutionRepository;
        this.institutionRoleRepository = institutionRoleRepository;
        this.knowledgeAreaRepository = knowledgeAreaRepository;
        this.peopleProjectGroupRepository = peopleProjectGroupRepository;
        this.peopleRepository = peopleRepository;
        this.programAddendumRepository = programAddendumRepository;
        this.programInstitutionRepository = programInstitutionRepository;
        this.programRepository = programRepository;
        this.projectGroupMeetingRepository = projectGroupMeetingRepository;
        this.projectGroupRepository = projectGroupRepository;
        this.stageCandidateRepository = stageCandidateRepository;
        this.stageRepository = stageRepository;
        this.systemLogRepository = systemLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void onApplicationEvent(ApplicationReadyEvent event) {
        if (alreadySetup || !mockDataEnabled) {
            return;
        }

        LocalDate today = LocalDate.now();

        Map<String, AcademicRoleModel> academicRoles = seedAcademicRoles();
        Map<String, InstitutionRoleModel> institutionRoles = seedInstitutionRoles();
        Map<String, KnowledgeAreaModel> knowledgeAreas = seedKnowledgeAreas();
        Map<String, InstitutionModel> institutions = seedInstitutions();
        Map<String, ProgramModel> programs = seedPrograms(today);

        ensureProgramInstitution(programs.get("IA"), institutions.get("UFAL"), institutionRoles.get("EXECUTORA"));
        ensureProgramInstitution(programs.get("IA"), institutions.get("SEBRAE"), institutionRoles.get("PARCEIRA"));
        ensureProgramInstitution(programs.get("DADOS"), institutions.get("IFAL"), institutionRoles.get("EXECUTORA"));
        ensureProgramInstitution(programs.get("DADOS"), institutions.get("SENAI"), institutionRoles.get("PARCEIRA"));
        ensureProgramInstitution(programs.get("FULLSTACK"), institutions.get("UNEAL"), institutionRoles.get("EXECUTORA"));
        ensureProgramInstitution(programs.get("FULLSTACK"), institutions.get("CESMAC"), institutionRoles.get("APOIO"));
        ensureProgramInstitution(programs.get("UFG"), institutions.get("UFG"), institutionRoles.get("EXECUTORA"));

        Map<String, ClassModel> classes = seedClasses(programs, institutions, today);
        Map<String, StageModel> stages = seedStages(classes);

        seedProgramAddendums(programs);
        seedAdvisors();

        Map<String, PeopleModel> staff = seedStaffPeople(institutions);
        Map<String, PeopleModel> class1Applicants = seedPeopleBatch(
                List.of("Aline Barros", "Bruno Medeiros", "Camila Duarte", "Daniel Rocha", "Ester Luna", "Felipe Moura", "Giovana Sales", "Henrique Paiva", "Isabela Farias", "Jonas Pires"),
                900000001L,
                "ia.ufal",
                "Ciencia da Computacao",
                institutions.get("UFAL").getName(),
                "Maceio",
                "AL",
                today
        );
        Map<String, PeopleModel> class2Applicants = seedPeopleBatch(
                List.of("Karen Teles", "Leandro Vilela", "Marina Porto", "Nicolas Araujo", "Olivia Campos", "Paulo Tavares"),
                900000101L,
                "ia.arapiraca",
                "Sistemas de Informacao",
                institutions.get("UFAL").getName(),
                "Arapiraca",
                "AL",
                today
        );
        Map<String, PeopleModel> class3Applicants = seedPeopleBatch(
                List.of("Quezia Nunes", "Rafael Neves", "Sara Peixoto", "Tiago Leal", "Ursula Lins", "Vinicius Rabelo", "Wesley Dantas"),
                900000201L,
                "dados.maceio",
                "Analise e Desenvolvimento de Sistemas",
                institutions.get("IFAL").getName(),
                "Maceio",
                "AL",
                today
        );
        Map<String, PeopleModel> class4Applicants = seedPeopleBatch(
                List.of("Yasmin Carvalho", "Adriano Falcao", "Bianca Serafim", "Caio Nobre", "Debora Vasconcelos", "Eduardo Gama"),
                900000301L,
                "fullstack.uneal",
                "Engenharia de Software",
                institutions.get("UNEAL").getName(),
                "Arapiraca",
                "AL",
                today.minusMonths(8)
        );

        seedStageCandidatesAndEnrollments(classes, stages, academicRoles, staff, class1Applicants, class2Applicants, class3Applicants, class4Applicants, today);

        Map<String, CourseModel> courses = seedCourses(knowledgeAreas);
        seedCourseAssignments(classes, courses);
        seedCourseEvaluations(courses, today);
        seedCourseProgressions(classes, class1Applicants, class3Applicants, class4Applicants, today);
        seedExams(classes, knowledgeAreas, class1Applicants, class4Applicants, today);
        seedProjectGroups(classes, institutions, staff, class1Applicants, class4Applicants, today);
        seedCareerData(programs, classes, class3Applicants, class4Applicants, today);
        seedUfgPresentationData(programs, classes, stages, academicRoles, knowledgeAreas, institutions, staff, today);
        seedSystemLogs(today);

        alreadySetup = true;
        System.out.println("Mock data seeding completed successfully.");
    }

    private Map<String, AcademicRoleModel> seedAcademicRoles() {
        Map<String, AcademicRoleModel> result = new LinkedHashMap<>();
        result.put("ALUNO", ensureAcademicRole("ALUNO", "Participante matriculado na turma."));
        result.put("ORIENTADOR", ensureAcademicRole("ORIENTADOR", "Acompanha grupos e estudantes na imersao."));
        result.put("COORDENADOR", ensureAcademicRole("COORDENADOR", "Coordena a execucao academica da turma."));
        result.put("GESTOR", ensureAcademicRole("GESTOR", "Apoia a gestao institucional do programa."));
        return result;
    }

    private Map<String, InstitutionRoleModel> seedInstitutionRoles() {
        Map<String, InstitutionRoleModel> result = new LinkedHashMap<>();
        result.put("EXECUTORA", ensureInstitutionRole("EXECUTORA"));
        result.put("PARCEIRA", ensureInstitutionRole("PARCEIRA"));
        result.put("APOIO", ensureInstitutionRole("APOIO"));
        return result;
    }

    private Map<String, KnowledgeAreaModel> seedKnowledgeAreas() {
        Map<String, KnowledgeAreaModel> result = new LinkedHashMap<>();
        for (String name : List.of("Programacao", "Dados", "Design", "Gestao", "Soft Skills", "Cloud", "Produto")) {
            result.put(name, ensureKnowledgeArea(name));
        }
        return result;
    }

    private Map<String, InstitutionModel> seedInstitutions() {
        Map<String, InstitutionModel> result = new LinkedHashMap<>();
        result.put("UFAL", ensureInstitution("UFAL", "Universidade Federal de Alagoas", "UFAL", "AL", "Mariana Coutinho"));
        result.put("IFAL", ensureInstitution("IFAL", "Instituto Federal de Alagoas", "IFAL", "AL", "Carlos Macedo"));
        result.put("UNEAL", ensureInstitution("UNEAL", "Universidade Estadual de Alagoas", "UNEAL", "AL", "Patricia Torres"));
        result.put("SENAI", ensureInstitution("SENAI-AL", "SENAI Alagoas", "SENAI", "AL", "Roberta Acioli"));
        result.put("SEBRAE", ensureInstitution("SEBRAE-AL", "Sebrae Alagoas", "SEBRAE", "AL", "Gustavo Lira"));
        result.put("CESMAC", ensureInstitution("CESMAC", "Centro Universitario CESMAC", "CESMAC", "AL", "Renata Ferraz"));
        result.put("UFG", ensureInstitution("UFG", "Universidade Federal de Goias", "UFG", "GO", "Coordenacao EASY/UFG"));
        return result;
    }

    private Map<String, ProgramModel> seedPrograms(LocalDate today) {
        Map<String, ProgramModel> result = new LinkedHashMap<>();

        ProgramModel programIa = ensureProgram(
                "BRISA-IA-2026",
                "BRISA One - IA Aplicada",
                "CT-IA-2026-001",
                "Nucleo BRISA",
                "Fundo Estadual de Inovacao",
                "Luciana Prado",
                BigDecimal.valueOf(980000.00),
                today.minusMonths(4),
                today.plusMonths(8),
                "Jovens e profissionais em transicao para tecnologia.",
                "Remoto com encontros quinzenais",
                "10 semanas",
                "16 semanas",
                320,
                "PCD/Neurodivergente, Negro/Pardo, Mulheres, 45+",
                "Entrega de projetos, frequencia e desempenho em trilhas.",
                "Consorcio BRISA",
                "Maceio e Arapiraca",
                "suporte.ia@brisa.mock",
                "https://brisa.mock/ia",
                "Maceio - AL",
                "Programa focado em IA aplicada a problemas reais.",
                "UFAL, Sebrae Alagoas"
        );

        ProgramModel programDados = ensureProgram(
                "BRISA-DADOS-2026",
                "BRISA One - Dados e Analytics",
                "CT-DADOS-2026-002",
                "Nucleo BRISA",
                "Agencia de Fomento Digital",
                "Marcelo Farias",
                BigDecimal.valueOf(860000.00),
                today.minusMonths(2),
                today.plusMonths(9),
                "Pessoas interessadas em analise de dados e visualizacao.",
                "Hibrido",
                "8 semanas",
                "18 semanas",
                360,
                "Negro/Pardo, Mulheres, 45+",
                "Projetos orientados, checkpoints e prova de nivelamento.",
                "Consorcio BRISA",
                "Maceio",
                "suporte.dados@brisa.mock",
                "https://brisa.mock/dados",
                "Maceio - AL",
                "Turmas com foco em BI e ciencia de dados aplicada.",
                "IFAL, SENAI Alagoas"
        );

        ProgramModel programFullstack = ensureProgram(
                "BRISA-FS-2025",
                "BRISA Labs - Desenvolvimento Full Stack",
                "CT-FS-2025-003",
                "Nucleo BRISA",
                "Fundacao de Apoio ao Desenvolvimento",
                "Fernanda Monteiro",
                BigDecimal.valueOf(720000.00),
                today.minusMonths(18),
                today.minusMonths(4),
                "Formacao acelerada em desenvolvimento web.",
                "Presencial",
                "6 semanas",
                "20 semanas",
                400,
                "PCD/Neurodivergente, Mulheres",
                "Provas praticas, projeto em grupo e apresentacao final.",
                "Consorcio BRISA",
                "Arapiraca",
                "suporte.fullstack@brisa.mock",
                "https://brisa.mock/fullstack",
                "Arapiraca - AL",
                "Ciclo concluido com alta taxa de empregabilidade.",
                "UNEAL, CESMAC"
        );

        ProgramModel programUfg = ensureProgram(
                "BRISA-UFG-2025.2",
                "Residencia em TIC BRISA/EASY/UFG 2025.2",
                "PPI-SOFTEX-UFG-2025.2",
                "BRISA em parceria com EASY/UFG",
                "MCTI - Lei de Informatica / PPI SOFTEX",
                "Coordenacao BRISA ONE",
                BigDecimal.valueOf(1260000.00),
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2026, 5, 27),
                "Graduados, graduandos e tecnicos de nivel medio em areas de exatas.",
                "100% a distancia, assincrono",
                "2 meses",
                "6 meses",
                480,
                "Mulheres 20%, negros e pardos 20%, PCD/neurodiversos 5%, 45+ 5%",
                "Prova de nivelamento, cursos concluidos, cotas, projetos e avaliacoes da imersao.",
                "BRISA / EASY / UFG",
                "Goiania - GO",
                "suporte.ufg@brisa.mock",
                "https://inscricoesrestic.brisabr.com.br/",
                "Goiania - GO",
                "Seed de apresentacao criado com base nas planilhas reais da turma UFG.",
                "UFG, EASY, BRISA"
        );

        result.put("IA", programIa);
        result.put("DADOS", programDados);
        result.put("FULLSTACK", programFullstack);
        result.put("UFG", programUfg);
        return result;
    }

    private Map<String, ClassModel> seedClasses(Map<String, ProgramModel> programs, Map<String, InstitutionModel> institutions, LocalDate today) {
        Map<String, ClassModel> result = new LinkedHashMap<>();

        result.put("IA-MACEIO", ensureClass(
                "UFAL 2026.1",
                programs.get("IA"),
                institutions.get("UFAL"),
                "Maceio - AL",
                40,
                180,
                90,
                24,
                4,
                today.minusMonths(4),
                today.plusMonths(5),
                today.minusMonths(4),
                today.minusMonths(4).plusDays(20),
                today.minusMonths(3).plusDays(5),
                today.minusMonths(3).plusDays(10),
                today.minusMonths(3),
                today.minusMonths(2).plusDays(10),
                today.minusMonths(2).plusDays(15),
                today.minusMonths(1).plusDays(5),
                today.minusMonths(1),
                today.plusMonths(4),
                today.plusDays(35),
                today.plusMonths(4).minusDays(10),
                today.plusMonths(5)
        ));

        result.put("IA-ARAPIRACA", ensureClass(
                "Arapiraca 2026.2",
                programs.get("IA"),
                institutions.get("UFAL"),
                "Arapiraca - AL",
                35,
                160,
                80,
                20,
                4,
                today.plusDays(20),
                today.plusMonths(8),
                today.plusDays(20),
                today.plusDays(25),
                today.plusDays(55),
                today.plusDays(60),
                today.plusDays(63),
                today.plusDays(78),
                today.plusDays(82),
                today.plusDays(95),
                today.plusDays(100),
                today.plusMonths(6),
                today.plusMonths(4),
                today.plusMonths(6).plusDays(10),
                today.plusMonths(8)
        ));

        result.put("DADOS-MACEIO", ensureClass(
                "Dados Maceio 2026.1",
                programs.get("DADOS"),
                institutions.get("IFAL"),
                "Maceio - AL",
                32,
                140,
                70,
                18,
                3,
                today.minusMonths(2),
                today.plusMonths(6),
                today.minusMonths(2),
                today.minusMonths(2).plusDays(12),
                today.minusMonths(1).plusDays(4),
                today.minusMonths(1).plusDays(8),
                today.minusDays(25),
                today.plusDays(25),
                today.plusDays(28),
                today.plusDays(45),
                today.plusDays(55),
                today.plusMonths(5),
                today.plusMonths(2),
                today.plusMonths(5).minusDays(7),
                today.plusMonths(6)
        ));

        result.put("FS-ARAPIRACA", ensureClass(
                "Fullstack 2025.1",
                programs.get("FULLSTACK"),
                institutions.get("UNEAL"),
                "Arapiraca - AL",
                28,
                120,
                60,
                16,
                4,
                today.minusMonths(15),
                today.minusMonths(4),
                today.minusMonths(15),
                today.minusMonths(15).plusDays(15),
                today.minusMonths(14).plusDays(5),
                today.minusMonths(14),
                today.minusMonths(13).plusDays(15),
                today.minusMonths(12).plusDays(20),
                today.minusMonths(12).plusDays(25),
                today.minusMonths(12),
                today.minusMonths(11).plusDays(10),
                today.minusMonths(6),
                today.minusMonths(8),
                today.minusMonths(4).minusDays(10),
                today.minusMonths(4)
        ));

        result.put("UFG-GOIANIA", ensureClass(
                "UFG 2025.2",
                programs.get("UFG"),
                institutions.get("UFG"),
                "Goiania - GO",
                50,
                785,
                250,
                50,
                5,
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2026, 5, 27),
                LocalDate.of(2025, 7, 1),
                LocalDate.of(2025, 7, 10),
                LocalDate.of(2025, 8, 5),
                LocalDate.of(2025, 8, 12),
                LocalDate.of(2025, 8, 18),
                LocalDate.of(2025, 10, 31),
                LocalDate.of(2025, 11, 4),
                LocalDate.of(2025, 11, 12),
                LocalDate.of(2025, 11, 24),
                LocalDate.of(2026, 5, 18),
                LocalDate.of(2026, 2, 18),
                LocalDate.of(2026, 5, 20),
                LocalDate.of(2026, 5, 27)
        ));

        return result;
    }

    private Map<String, StageModel> seedStages(Map<String, ClassModel> classes) {
        Map<String, StageModel> result = new LinkedHashMap<>();
        for (Map.Entry<String, ClassModel> entry : classes.entrySet()) {
            result.put(entry.getKey() + "-SELECAO", ensureStage(entry.getValue(), "SELECAO", "Etapa automatica: Selecao"));
            result.put(entry.getKey() + "-NIVELAMENTO", ensureStage(entry.getValue(), "NIVELAMENTO", "Etapa automatica: Nivelamento"));
            result.put(entry.getKey() + "-IMERSAO", ensureStage(entry.getValue(), "IMERSAO", "Etapa automatica: Imersao"));
        }
        return result;
    }

    private void seedProgramAddendums(Map<String, ProgramModel> programs) {
        ensureAddendum(programs.get("IA"), 1, programs.get("IA").getStartDate().plusMonths(2), programs.get("IA").getEndDate(), BigDecimal.valueOf(120000.00));
        ensureAddendum(programs.get("FULLSTACK"), 1, programs.get("FULLSTACK").getStartDate().plusMonths(3), programs.get("FULLSTACK").getEndDate(), BigDecimal.valueOf(80000.00));
        ensureAddendum(programs.get("UFG"), 1, LocalDate.of(2025, 11, 24), programs.get("UFG").getEndDate(), BigDecimal.valueOf(180000.00));
    }

    private void seedAdvisors() {
        advisorRepository.findAll().stream()
                .filter(advisor -> advisor.getRoleType() == AdvisorRoleType.PROFESSOR)
                .forEach(advisor -> {
                    advisor.setRoleType(AdvisorRoleType.ORIENTADOR);
                    advisorRepository.save(advisor);
                });

        ensureAdvisor("70000000001", "Elisa Rocha", AdvisorRoleType.GESTOR, "elisa.rocha@brisa.mock", "Administracao", LocalDate.of(1983, 4, 15));
        ensureAdvisor("70000000002", "Bruno Lima", AdvisorRoleType.ORIENTADOR, "bruno.lima@brisa.mock", "Ciencia da Computacao", LocalDate.of(1988, 8, 2));
        ensureAdvisor("70000000003", "Carla Nunes", AdvisorRoleType.ORIENTADOR, "carla.nunes@brisa.mock", "Sistemas de Informacao", LocalDate.of(1990, 11, 9));
        ensureAdvisor("70000000004", "Diego Costa", AdvisorRoleType.ORIENTADOR, "diego.costa@brisa.mock", "Engenharia de Software", LocalDate.of(1987, 2, 22));
        ensureAdvisor("70000000005", "Fernanda Melo", AdvisorRoleType.ORIENTADOR, "fernanda.melo@brisa.mock", "Design de Produto", LocalDate.of(1992, 6, 6));
    }

    private Map<String, PeopleModel> seedStaffPeople(Map<String, InstitutionModel> institutions) {
        Map<String, PeopleModel> result = new LinkedHashMap<>();
        result.put("ELISA", ensurePerson("80000000001", "Elisa Rocha", "elisa.rocha@staff.brisa.mock", "Pos-graduacao", "Rua da Gestao, 100", "Sala 1", "Maceio", "AL", "Feminino", "Ampla concorrencia", "82990000001", LocalDate.of(1983, 4, 15), institutions.get("SEBRAE").getName(), "Administracao", "Concluido", LocalDate.of(2007, 12, 20)));
        result.put("BRUNO", ensurePerson("80000000002", "Bruno Lima", "bruno.lima@staff.brisa.mock", "Mestrado", "Rua dos Cursos, 45", "Bloco B", "Maceio", "AL", "Masculino", "Ampla concorrencia", "82990000002", LocalDate.of(1988, 8, 2), institutions.get("UFAL").getName(), "Ciencia da Computacao", "Concluido", LocalDate.of(2013, 12, 15)));
        result.put("CARLA", ensurePerson("80000000003", "Carla Nunes", "carla.nunes@staff.brisa.mock", "Mestrado", "Rua da Inovacao, 12", null, "Arapiraca", "AL", "Feminino", "Mulheres", "82990000003", LocalDate.of(1990, 11, 9), institutions.get("IFAL").getName(), "Sistemas de Informacao", "Concluido", LocalDate.of(2014, 12, 18)));
        result.put("DIEGO", ensurePerson("80000000004", "Diego Costa", "diego.costa@staff.brisa.mock", "Graduacao", "Av. Brasil, 88", null, "Maceio", "AL", "Masculino", "Ampla concorrencia", "82990000004", LocalDate.of(1987, 2, 22), institutions.get("SENAI").getName(), "Engenharia de Software", "Concluido", LocalDate.of(2010, 7, 5)));
        result.put("FERNANDA", ensurePerson("80000000005", "Fernanda Melo", "fernanda.melo@staff.brisa.mock", "Mestrado", "Rua das Trilhas, 50", null, "Arapiraca", "AL", "Feminino", "Mulheres", "82990000005", LocalDate.of(1992, 6, 6), institutions.get("CESMAC").getName(), "Design de Produto", "Concluido", LocalDate.of(2016, 12, 9)));
        result.put("GUSTAVO", ensurePerson("80000000006", "Gustavo Matos", "gustavo.matos@staff.brisa.mock", "Graduacao", "Rua do Campus, 210", null, "Maceio", "AL", "Masculino", "Ampla concorrencia", "82990000006", LocalDate.of(1985, 9, 11), institutions.get("UFAL").getName(), "Gestao de Projetos", "Concluido", LocalDate.of(2009, 12, 1)));
        return result;
    }

    private Map<String, PeopleModel> seedPeopleBatch(
            List<String> names,
            long startingCpf,
            String emailPrefix,
            String courseName,
            String institutionName,
            String defaultCity,
            String defaultState,
            LocalDate referenceDate
    ) {
        Map<String, PeopleModel> result = new LinkedHashMap<>();
        String[] quotaSequence = {"Ampla concorrencia", "Negro/Pardo", "Mulheres", "PCD/Neurodivergente", "45+"};
        String[] genders = {"Feminino", "Masculino"};
        String[] cities = {defaultCity, defaultCity, "Rio Largo", "Sao Miguel dos Campos", "Arapiraca"};
        String[] courses = {courseName, "Sistemas de Informacao", "Analise de Dados", "Engenharia de Software", "Design Digital"};

        for (int index = 0; index < names.size(); index++) {
            String name = names.get(index);
            String key = name.toUpperCase(Locale.ROOT).replace(" ", "_");
            String email = buildStudentEmail(name, emailPrefix);
            String cpf = String.format(Locale.ROOT, "%011d", startingCpf + index);
            LocalDate birthDate = referenceDate.minusYears(21 + index).minusDays(index * 9L);
            String city = cities[index % cities.length];
            String state = city.equals("Rio Largo") || city.equals("Sao Miguel dos Campos") || city.equals("Arapiraca") ? "AL" : defaultState;
            String gender = genders[index % genders.length];
            String quota = quotaSequence[index % quotaSequence.length];
            String educationStatus = index % 3 == 0 ? "Concluido" : "Cursando";
            LocalDate completionDate = "Concluido".equals(educationStatus) ? birthDate.plusYears(22) : null;

            result.put(key, ensurePerson(
                    cpf,
                    name,
                    email,
                    index % 4 == 0 ? "Pos-graduacao" : "Graduacao",
                    "Rua Mock " + (index + 1),
                    index % 2 == 0 ? "Casa" : null,
                    city,
                    state,
                    gender,
                    quota,
                    "8297000" + String.format(Locale.ROOT, "%04d", index + 1),
                    birthDate,
                    institutionName,
                    courses[index % courses.length],
                    educationStatus,
                    completionDate
            ));
        }

        return result;
    }

    private void seedStageCandidatesAndEnrollments(
            Map<String, ClassModel> classes,
            Map<String, StageModel> stages,
            Map<String, AcademicRoleModel> academicRoles,
            Map<String, PeopleModel> staff,
            Map<String, PeopleModel> class1Applicants,
            Map<String, PeopleModel> class2Applicants,
            Map<String, PeopleModel> class3Applicants,
            Map<String, PeopleModel> class4Applicants,
            LocalDate today
    ) {
        ClassModel classIa = classes.get("IA-MACEIO");
        ClassModel classIaFuture = classes.get("IA-ARAPIRACA");
        ClassModel classDados = classes.get("DADOS-MACEIO");
        ClassModel classFs = classes.get("FS-ARAPIRACA");

        List<PeopleModel> iaApplicants = new ArrayList<>(class1Applicants.values());
        List<PeopleModel> iaFutureApplicants = new ArrayList<>(class2Applicants.values());
        List<PeopleModel> dadosApplicants = new ArrayList<>(class3Applicants.values());
        List<PeopleModel> fsApplicants = new ArrayList<>(class4Applicants.values());

        for (int i = 0; i < iaApplicants.size(); i++) {
            PeopleModel people = iaApplicants.get(i);
            createStageCandidate(stages.get("IA-MACEIO-SELECAO"), people, i < 8 ? StageStatus.APROVADO : i == 8 ? StageStatus.LISTA_ESPERA : StageStatus.REPROVADO, "Candidatura para IA aplicada.");
            if (i < 8) {
                createStageCandidate(stages.get("IA-MACEIO-NIVELAMENTO"), people, i < 6 ? StageStatus.APROVADO : i == 6 ? StageStatus.LISTA_ESPERA : StageStatus.REPROVADO, "Nivelamento em andamento.");
                createEnrollment(people, classIa, academicRoles.get("ALUNO"), "Matriculado", today.minusMonths(3), null, 8.1 + (i * 0.1), 86.0 + i);
            }
            if (i < 6) {
                createStageCandidate(stages.get("IA-MACEIO-IMERSAO"), people, i < 5 ? StageStatus.APROVADO : StageStatus.LISTA_ESPERA, "Elegivel para imersao.");
            }
        }

        createEnrollment(staff.get("ELISA"), classIa, academicRoles.get("COORDENADOR"), "Ativo", today.minusMonths(4), null, null, null);
        createEnrollment(staff.get("BRUNO"), classIa, academicRoles.get("ORIENTADOR"), "Ativo", today.minusMonths(4), null, null, null);
        createEnrollment(staff.get("CARLA"), classIa, academicRoles.get("ORIENTADOR"), "Ativo", today.minusMonths(2), null, null, null);
        createEnrollment(staff.get("DIEGO"), classIa, academicRoles.get("ORIENTADOR"), "Ativo", today.minusMonths(2), null, null, null);

        for (PeopleModel people : iaFutureApplicants) {
            createStageCandidate(stages.get("IA-ARAPIRACA-SELECAO"), people, StageStatus.EM_ANALISE, "Inscricao recebida para a proxima turma.");
        }
        createEnrollment(staff.get("ELISA"), classIaFuture, academicRoles.get("COORDENADOR"), "Planejamento", today.plusDays(1), null, null, null);
        createEnrollment(staff.get("FERNANDA"), classIaFuture, academicRoles.get("ORIENTADOR"), "Planejamento", today.plusDays(1), null, null, null);

        for (int i = 0; i < dadosApplicants.size(); i++) {
            PeopleModel people = dadosApplicants.get(i);
            createStageCandidate(stages.get("DADOS-MACEIO-SELECAO"), people, i < 5 ? StageStatus.APROVADO : i == 5 ? StageStatus.LISTA_ESPERA : StageStatus.REPROVADO, "Inscricao analisada pela equipe.");
            if (i < 5) {
                createStageCandidate(stages.get("DADOS-MACEIO-NIVELAMENTO"), people, i < 4 ? StageStatus.APROVADO : StageStatus.EM_ANALISE, "Participando do nivelamento.");
                LocalDate completionDate = i == 0 ? today.minusMonths(7) : i == 1 ? today.minusMonths(4) : i == 2 ? today.minusMonths(2) : null;
                String enrollmentStatus = completionDate != null ? "Concluido" : "Em andamento";
                createEnrollment(people, classDados, academicRoles.get("ALUNO"), enrollmentStatus, today.minusMonths(1), completionDate, 7.5 + (i * 0.2), 79.0 + i);
            }
        }
        createEnrollment(staff.get("GUSTAVO"), classDados, academicRoles.get("COORDENADOR"), "Ativo", today.minusMonths(2), null, null, null);
        createEnrollment(staff.get("BRUNO"), classDados, academicRoles.get("ORIENTADOR"), "Ativo", today.minusMonths(2), null, null, null);
        createEnrollment(staff.get("CARLA"), classDados, academicRoles.get("ORIENTADOR"), "Ativo", today.minusDays(20), null, null, null);

        for (int i = 0; i < fsApplicants.size(); i++) {
            PeopleModel people = fsApplicants.get(i);
            createStageCandidate(stages.get("FS-ARAPIRACA-SELECAO"), people, StageStatus.APROVADO, "Participou do processo seletivo completo.");
            createStageCandidate(stages.get("FS-ARAPIRACA-NIVELAMENTO"), people, StageStatus.APROVADO, "Concluiu o nivelamento.");
            createStageCandidate(stages.get("FS-ARAPIRACA-IMERSAO"), people, i < 5 ? StageStatus.APROVADO : StageStatus.LISTA_ESPERA, "Historico da imersao finalizado.");
            LocalDate completionDate = switch (i) {
                case 0 -> today.minusMonths(23);
                case 1 -> today.minusMonths(19);
                case 2 -> today.minusMonths(15);
                case 3 -> today.minusMonths(12);
                case 4 -> today.minusMonths(8);
                default -> today.minusMonths(5);
            };
            createEnrollment(people, classFs, academicRoles.get("ALUNO"), "Concluido", today.minusMonths(14), completionDate, 8.7 + (i * 0.1), 92.0 + i);
        }
        createEnrollment(staff.get("ELISA"), classFs, academicRoles.get("COORDENADOR"), "Concluido", today.minusMonths(15), today.minusMonths(4), null, null);
        createEnrollment(staff.get("FERNANDA"), classFs, academicRoles.get("ORIENTADOR"), "Concluido", today.minusMonths(15), today.minusMonths(4), null, null);
        createEnrollment(staff.get("DIEGO"), classFs, academicRoles.get("ORIENTADOR"), "Concluido", today.minusMonths(12), today.minusMonths(4), null, null);
    }

    private Map<String, CourseModel> seedCourses(Map<String, KnowledgeAreaModel> knowledgeAreas) {
        Map<String, CourseModel> result = new LinkedHashMap<>();
        result.put("LOGICA", ensureCourse("Logica e Pensamento Computacional", "Fundamentos para resolucao de problemas.", 40, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Soft Skills")));
        result.put("PYTHON", ensureCourse("Python para Dados", "Manipulacao de dados e automacoes.", 60, knowledgeAreas.get("Dados"), knowledgeAreas.get("Programacao")));
        result.put("GIT", ensureCourse("Git e Colaboracao", "Fluxos de versionamento e trabalho em equipe.", 20, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Soft Skills")));
        result.put("IA", ensureCourse("Projeto de IA Aplicada", "Entrega de produto com IA e dados.", 80, knowledgeAreas.get("Produto"), knowledgeAreas.get("Dados")));
        result.put("SQL", ensureCourse("Banco de Dados SQL", "Consultas, modelagem e analise relacional.", 50, knowledgeAreas.get("Dados"), knowledgeAreas.get("Cloud")));
        result.put("BI", ensureCourse("Visualizacao com Power BI", "Dashboards e storytelling de dados.", 40, knowledgeAreas.get("Dados"), knowledgeAreas.get("Produto")));
        result.put("AGIL", ensureCourse("Metodos Ageis", "Organizacao de sprints e ritos de acompanhamento.", 24, knowledgeAreas.get("Gestao"), knowledgeAreas.get("Soft Skills")));
        result.put("WEB", ensureCourse("Desenvolvimento Web Moderno", "Interfaces, APIs e integracao full stack.", 80, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Produto")));
        result.put("SPRING", ensureCourse("Backend com Spring", "APIs REST, seguranca e persistencia.", 70, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Cloud")));
        result.put("UX", ensureCourse("UX para Produtos Digitais", "Pesquisa, prototipacao e testes.", 32, knowledgeAreas.get("Design"), knowledgeAreas.get("Produto")));
        return result;
    }

    private void seedCourseAssignments(Map<String, ClassModel> classes, Map<String, CourseModel> courses) {
        assignCourse(classes.get("IA-MACEIO"), courses.get("LOGICA"), true);
        assignCourse(classes.get("IA-MACEIO"), courses.get("PYTHON"), true);
        assignCourse(classes.get("IA-MACEIO"), courses.get("GIT"), true);
        assignCourse(classes.get("IA-MACEIO"), courses.get("IA"), true);
        assignCourse(classes.get("IA-MACEIO"), courses.get("AGIL"), false);

        assignCourse(classes.get("IA-ARAPIRACA"), courses.get("LOGICA"), true);
        assignCourse(classes.get("IA-ARAPIRACA"), courses.get("PYTHON"), true);
        assignCourse(classes.get("IA-ARAPIRACA"), courses.get("GIT"), true);
        assignCourse(classes.get("IA-ARAPIRACA"), courses.get("IA"), true);

        assignCourse(classes.get("DADOS-MACEIO"), courses.get("PYTHON"), true);
        assignCourse(classes.get("DADOS-MACEIO"), courses.get("SQL"), true);
        assignCourse(classes.get("DADOS-MACEIO"), courses.get("BI"), true);
        assignCourse(classes.get("DADOS-MACEIO"), courses.get("AGIL"), false);

        assignCourse(classes.get("FS-ARAPIRACA"), courses.get("WEB"), true);
        assignCourse(classes.get("FS-ARAPIRACA"), courses.get("SPRING"), true);
        assignCourse(classes.get("FS-ARAPIRACA"), courses.get("UX"), true);
        assignCourse(classes.get("FS-ARAPIRACA"), courses.get("GIT"), true);
    }

    private void seedCourseEvaluations(Map<String, CourseModel> courses, LocalDate today) {
        ensureCourseEvaluation(courses.get("PYTHON"), "PY-AV1", "PROVA", "Avaliacao Tecnica Python", 0.0, 10.0, today.minusDays(55));
        ensureCourseEvaluation(courses.get("IA"), "IA-PROJ", "PROJETO", "Pitch de Solucao com IA", 0.0, 10.0, today.plusDays(25));
        ensureCourseEvaluation(courses.get("SQL"), "SQL-AV1", "PROVA", "Desafio de SQL", 0.0, 10.0, today.plusDays(18));
        ensureCourseEvaluation(courses.get("WEB"), "WEB-FINAL", "PROJETO", "Entrega de Aplicacao Web", 0.0, 10.0, today.minusMonths(7));
    }

    private void seedCourseProgressions(
            Map<String, ClassModel> classes,
            Map<String, PeopleModel> class1Applicants,
            Map<String, PeopleModel> class3Applicants,
            Map<String, PeopleModel> class4Applicants,
            LocalDate today
    ) {
        List<CourseAssignmentModel> iaAssignments = courseAssignmentRepository.findByClassId(classes.get("IA-MACEIO").getId());
        List<PeopleModel> iaStudents = new ArrayList<>(class1Applicants.values()).subList(0, 8);
        for (int studentIndex = 0; studentIndex < iaStudents.size(); studentIndex++) {
            PeopleModel people = iaStudents.get(studentIndex);
            for (int courseIndex = 0; courseIndex < iaAssignments.size(); courseIndex++) {
                CourseModel course = iaAssignments.get(courseIndex).getCourse();
                if (courseIndex <= 1) {
                    createCourseProgression(course, people, today.minusDays(35 - (studentIndex * 2L)), 100.0, "Concluido", today.minusDays(4 + studentIndex));
                } else if (courseIndex == 2) {
                    createCourseProgression(course, people, today.minusDays(15 - studentIndex), 72.0 + studentIndex, "Em andamento", today.minusDays(studentIndex + 1L));
                } else {
                    createCourseProgression(course, people, null, studentIndex % 2 == 0 ? 48.0 : 12.0, studentIndex % 2 == 0 ? "Em andamento" : "Nao iniciado", today.minusDays(2 + studentIndex));
                }
            }
        }

        List<CourseAssignmentModel> dadosAssignments = courseAssignmentRepository.findByClassId(classes.get("DADOS-MACEIO").getId());
        List<PeopleModel> dadosStudents = new ArrayList<>(class3Applicants.values()).subList(0, 5);
        for (int studentIndex = 0; studentIndex < dadosStudents.size(); studentIndex++) {
            PeopleModel people = dadosStudents.get(studentIndex);
            for (int courseIndex = 0; courseIndex < dadosAssignments.size(); courseIndex++) {
                CourseModel course = dadosAssignments.get(courseIndex).getCourse();
                if (courseIndex == 0 && studentIndex < 3) {
                    createCourseProgression(course, people, today.minusDays(8 + studentIndex), 100.0, "Concluido", today.minusDays(studentIndex));
                } else {
                    createCourseProgression(course, people, null, 18.0 + (courseIndex * 12.0) + studentIndex, "Em andamento", today.minusDays(1 + courseIndex));
                }
            }
        }

        List<CourseAssignmentModel> fsAssignments = courseAssignmentRepository.findByClassId(classes.get("FS-ARAPIRACA").getId());
        List<PeopleModel> fsStudents = new ArrayList<>(class4Applicants.values());
        for (int studentIndex = 0; studentIndex < fsStudents.size(); studentIndex++) {
            PeopleModel people = fsStudents.get(studentIndex);
            for (int courseIndex = 0; courseIndex < fsAssignments.size(); courseIndex++) {
                CourseModel course = fsAssignments.get(courseIndex).getCourse();
                createCourseProgression(course, people, today.minusMonths(9).plusDays(courseIndex * 7L + studentIndex), 100.0, "Concluido", today.minusMonths(4).minusDays(studentIndex));
            }
        }
    }

    private void seedExams(
            Map<String, ClassModel> classes,
            Map<String, KnowledgeAreaModel> knowledgeAreas,
            Map<String, PeopleModel> class1Applicants,
            Map<String, PeopleModel> class4Applicants,
            LocalDate today
    ) {
        ExamModel iaExam = ensureExam("Prova de Nivelamento - UFAL 2026.1", classes.get("IA-MACEIO"), classes.get("IA-MACEIO").getLevelingFinalExamDate());
        List<ExamQuestionModel> iaQuestions = new ArrayList<>();
        for (int number = 1; number <= 10; number++) {
            iaQuestions.add(ensureExamQuestion(iaExam, number, number <= 5 ? knowledgeAreas.get("Programacao") : knowledgeAreas.get("Dados"), 1.0));
        }
        List<PeopleModel> iaStudents = new ArrayList<>(class1Applicants.values()).subList(0, 8);
        for (int index = 0; index < iaStudents.size(); index++) {
            PeopleModel people = iaStudents.get(index);
            int score = 5 + (index % 5);
            ensureExamResult(iaExam, people, (double) score, 48 + (index * 3));
            for (ExamQuestionModel question : iaQuestions) {
                boolean correct = question.getQuestionNumber() <= score;
                ensureExamAnswer(people, question, correct, correct ? "Acerto" : "Erro", correct ? 1.0 : 0.0);
            }
        }

        ExamModel fsExam = ensureExam("Prova Final - Fullstack 2025.1", classes.get("FS-ARAPIRACA"), today.minusMonths(10));
        List<ExamQuestionModel> fsQuestions = new ArrayList<>();
        for (int number = 1; number <= 8; number++) {
            fsQuestions.add(ensureExamQuestion(fsExam, number, number <= 4 ? knowledgeAreas.get("Programacao") : knowledgeAreas.get("Design"), 1.0));
        }
        List<PeopleModel> fsStudents = new ArrayList<>(class4Applicants.values());
        for (int index = 0; index < fsStudents.size(); index++) {
            PeopleModel people = fsStudents.get(index);
            int score = 6 + (index % 3);
            ensureExamResult(fsExam, people, (double) score, 42 + (index * 4));
            for (ExamQuestionModel question : fsQuestions) {
                boolean correct = question.getQuestionNumber() <= score;
                ensureExamAnswer(people, question, correct, correct ? "Acerto" : "Erro", correct ? 1.0 : 0.0);
            }
        }
    }

    private void seedProjectGroups(
            Map<String, ClassModel> classes,
            Map<String, InstitutionModel> institutions,
            Map<String, PeopleModel> staff,
            Map<String, PeopleModel> class1Applicants,
            Map<String, PeopleModel> class4Applicants,
            LocalDate today
    ) {
        List<PeopleModel> iaImmersionStudents = new ArrayList<>(class1Applicants.values()).subList(0, 6);
        ProjectGroupModel groupA = ensureProjectGroup(
                "Monitor de Atendimento com IA",
                classes.get("IA-MACEIO"),
                "Time focado em classificacao automatica de chamados.",
                institutions.get("SEBRAE"),
                "Sebrae Alagoas",
                staff.get("CARLA"),
                "https://github.com/brisa/mock-monitor-atendimento",
                DayOfWeek.WEDNESDAY,
                today.minusDays(14)
        );
        ProjectGroupModel groupB = ensureProjectGroup(
                "Radar de Evasao Escolar",
                classes.get("IA-MACEIO"),
                "Projeto para priorizacao de alertas educacionais.",
                institutions.get("SENAI"),
                "SENAI Alagoas",
                staff.get("DIEGO"),
                "https://github.com/brisa/mock-radar-evasao",
                DayOfWeek.THURSDAY,
                today.minusDays(10)
        );
        addGroupMember(groupA, iaImmersionStudents.get(0));
        addGroupMember(groupA, iaImmersionStudents.get(1));
        addGroupMember(groupA, iaImmersionStudents.get(2));
        addGroupMember(groupB, iaImmersionStudents.get(3));
        addGroupMember(groupB, iaImmersionStudents.get(4));
        addGroupMember(groupB, iaImmersionStudents.get(5));
        ensureMeeting(groupA, today.minusDays(7), "COMPLETED");
        ensureMeeting(groupA, today.plusDays(7), "SCHEDULED");
        ensureMeeting(groupB, today.minusDays(5), "COMPLETED");
        ensureMeeting(groupB, today.plusDays(9), "SCHEDULED");

        List<PeopleModel> fsStudents = new ArrayList<>(class4Applicants.values()).subList(0, 4);
        ProjectGroupModel fsGroup = ensureProjectGroup(
                "Portal de Estagios Municipais",
                classes.get("FS-ARAPIRACA"),
                "Aplicacao final entregue ao ecossistema local.",
                institutions.get("CESMAC"),
                "CESMAC",
                staff.get("FERNANDA"),
                "https://github.com/brisa/mock-portal-estagios",
                DayOfWeek.MONDAY,
                today.minusMonths(8)
        );
        for (PeopleModel people : fsStudents) {
            addGroupMember(fsGroup, people);
        }
        ensureMeeting(fsGroup, today.minusMonths(8).plusDays(7), "COMPLETED");
        ensureMeeting(fsGroup, today.minusMonths(7).plusDays(7), "COMPLETED");
    }

    private void seedCareerData(
            Map<String, ProgramModel> programs,
            Map<String, ClassModel> classes,
            Map<String, PeopleModel> class3Applicants,
            Map<String, PeopleModel> class4Applicants,
            LocalDate today
    ) {
        List<PeopleModel> fullStackAlumni = new ArrayList<>(class4Applicants.values());
        List<PeopleModel> dataAlumni = new ArrayList<>(class3Applicants.values()).subList(0, 3);

        EnrollmentModel yasminEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(fullStackAlumni.get(0).getId(), classes.get("FS-ARAPIRACA").getId()).orElse(null);
        EnrollmentModel adrianoEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(fullStackAlumni.get(1).getId(), classes.get("FS-ARAPIRACA").getId()).orElse(null);
        EnrollmentModel biancaEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(fullStackAlumni.get(2).getId(), classes.get("FS-ARAPIRACA").getId()).orElse(null);
        EnrollmentModel caioEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(fullStackAlumni.get(3).getId(), classes.get("FS-ARAPIRACA").getId()).orElse(null);
        EnrollmentModel deboraEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(fullStackAlumni.get(4).getId(), classes.get("FS-ARAPIRACA").getId()).orElse(null);
        EnrollmentModel eduardoEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(fullStackAlumni.get(5).getId(), classes.get("FS-ARAPIRACA").getId()).orElse(null);

        EnrollmentModel queziaEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(dataAlumni.get(0).getId(), classes.get("DADOS-MACEIO").getId()).orElse(null);
        EnrollmentModel rafaelEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(dataAlumni.get(1).getId(), classes.get("DADOS-MACEIO").getId()).orElse(null);
        EnrollmentModel saraEnrollment = enrollmentRepository.findByPeopleIdAndClassModelId(dataAlumni.get(2).getId(), classes.get("DADOS-MACEIO").getId()).orElse(null);

        if (yasminEnrollment != null && yasminEnrollment.getCompletionDate() != null) {
            ensureCareerProgression(fullStackAlumni.get(0), yasminEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), yasminEnrollment.getCompletionDate().plusMonths(6), "EMPREGADO", "GovTech AL", "Desenvolvedora Junior", "Primeira insercao profissional apos a conclusao.");
            ensureCareerProgression(fullStackAlumni.get(0), yasminEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), yasminEnrollment.getCompletionDate().plusMonths(12), "EMPREGADO", "GovTech AL", "Desenvolvedora Backend", "Evolucao consistente na trilha backend.");
            ensureCareerProgression(fullStackAlumni.get(0), yasminEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), yasminEnrollment.getCompletionDate().plusMonths(18), "EMPREGADO", "GovTech AL", "Desenvolvedora Backend Pleno", "Promovida apos liderar uma entrega interna.");
        }

        if (adrianoEnrollment != null && adrianoEnrollment.getCompletionDate() != null) {
            ensureCareerProgression(fullStackAlumni.get(1), adrianoEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), adrianoEnrollment.getCompletionDate().plusMonths(6), "DESEMPREGADO", null, null, "Ainda em recolocacao no ecossistema local.");
            ensureCareerProgression(fullStackAlumni.get(1), adrianoEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), adrianoEnrollment.getCompletionDate().plusMonths(12), "EMPREGADO", "Startup Lagoa", "Product Designer", "Conseguiu migrar para produto digital.");
        }

        if (biancaEnrollment != null && biancaEnrollment.getCompletionDate() != null) {
            ensureCareerProgression(fullStackAlumni.get(2), biancaEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), biancaEnrollment.getCompletionDate().plusMonths(6), "SEM_RESPOSTA", null, null, "Nao respondeu ao primeiro contato.");
            ensureCareerProgression(fullStackAlumni.get(2), biancaEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), biancaEnrollment.getCompletionDate().plusMonths(13), "DESEMPREGADO", null, null, "Participando de processos seletivos e freelas pontuais.");
        }

        if (caioEnrollment != null && caioEnrollment.getCompletionDate() != null) {
            ensureCareerProgression(fullStackAlumni.get(3), caioEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), caioEnrollment.getCompletionDate().plusMonths(6), "SEM_RESPOSTA", null, null, "Contato inicial sem retorno.");
        }

        if (queziaEnrollment != null && queziaEnrollment.getCompletionDate() != null) {
            ensureCareerProgression(dataAlumni.get(0), queziaEnrollment, classes.get("DADOS-MACEIO"), programs.get("DADOS"), queziaEnrollment.getCompletionDate().plusMonths(6), "EMPREGADO", "Data Norte", "Analista de Dados Jr.", "Contratada apos concluir a trilha de analytics.");
        }

        if (rafaelEnrollment != null && rafaelEnrollment.getCompletionDate() != null) {
            ensureCareerProgression(dataAlumni.get(1), rafaelEnrollment, classes.get("DADOS-MACEIO"), programs.get("DADOS"), rafaelEnrollment.getCompletionDate().plusMonths(3), "SEM_RESPOSTA", null, null, "Primeiro contato realizado antes do checkpoint de 6 meses.");
        }

        CareerAutomationSettingsModel settings = careerAutomationSettingsRepository.findTopByOrderByIdAsc().orElseGet(CareerAutomationSettingsModel::new);
        settings.setEnabled(true);
        settings.setProgram(null);
        settings.setClassModel(null);
        settings.setSubject("Acompanhamento de carreira - BRISA");
        settings.setMessage("Queremos saber como esta sua trajetoria profissional apos a conclusao do programa. Seu retorno ajuda no acompanhamento dos egressos.");
        settings.setCheckpointsCsv("6,12,18,24");
        settings.setLastTestAt(LocalDateTime.now().minusDays(2));
        settings = careerAutomationSettingsRepository.save(settings);

        if (yasminEnrollment != null && yasminEnrollment.getCompletionDate() != null) {
            ensureCareerDispatch(fullStackAlumni.get(0), yasminEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), 18, yasminEnrollment.getCompletionDate().plusMonths(18), fullStackAlumni.get(0).getEmail(), settings.getSubject() + " - 18 meses", "SENT", 1, null);
        }
        if (adrianoEnrollment != null && adrianoEnrollment.getCompletionDate() != null) {
            ensureCareerDispatch(fullStackAlumni.get(1), adrianoEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), 18, adrianoEnrollment.getCompletionDate().plusMonths(18), fullStackAlumni.get(1).getEmail(), settings.getSubject() + " - 18 meses", "FAILED", 2, "Servidor de e-mail indisponivel na tentativa anterior.");
        }
        if (biancaEnrollment != null && biancaEnrollment.getCompletionDate() != null) {
            ensureCareerDispatch(fullStackAlumni.get(2), biancaEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), 12, biancaEnrollment.getCompletionDate().plusMonths(12), fullStackAlumni.get(2).getEmail(), settings.getSubject() + " - 12 meses", "SENT", 1, null);
        }
        if (caioEnrollment != null && caioEnrollment.getCompletionDate() != null) {
            ensureCareerDispatch(fullStackAlumni.get(3), caioEnrollment, classes.get("FS-ARAPIRACA"), programs.get("FULLSTACK"), 12, caioEnrollment.getCompletionDate().plusMonths(12), fullStackAlumni.get(3).getEmail(), settings.getSubject() + " - 12 meses", "SENT", 1, null);
        }
        if (queziaEnrollment != null && queziaEnrollment.getCompletionDate() != null) {
            ensureCareerDispatch(dataAlumni.get(0), queziaEnrollment, classes.get("DADOS-MACEIO"), programs.get("DADOS"), 6, queziaEnrollment.getCompletionDate().plusMonths(6), dataAlumni.get(0).getEmail(), settings.getSubject() + " - 6 meses", "SENT", 1, null);
        }
    }

    private void seedUfgPresentationData(
            Map<String, ProgramModel> programs,
            Map<String, ClassModel> classes,
            Map<String, StageModel> stages,
            Map<String, AcademicRoleModel> academicRoles,
            Map<String, KnowledgeAreaModel> knowledgeAreas,
            Map<String, InstitutionModel> institutions,
            Map<String, PeopleModel> staff,
            LocalDate today
    ) {
        ProgramModel program = programs.get("UFG");
        ClassModel classModel = classes.get("UFG-GOIANIA");
        if (program == null || classModel == null) {
            return;
        }

        StageModel selectionStage = stages.get("UFG-GOIANIA-SELECAO");
        StageModel levelingStage = stages.get("UFG-GOIANIA-NIVELAMENTO");
        StageModel immersionStage = stages.get("UFG-GOIANIA-IMERSAO");
        AcademicRoleModel studentRole = academicRoles.get("ALUNO");

        List<CourseModel> courses = List.of(
                ensureCourse("Curso Introdutorio", "Ambientacao na plataforma de EaD da BRISA.", 10, knowledgeAreas.get("Produto"), knowledgeAreas.get("Soft Skills")),
                ensureCourse("Logica de Programacao", "Fundamentos de logica com Python.", 30, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Soft Skills")),
                ensureCourse("Programacao Python", "Programacao aplicada e automacoes.", 40, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Dados")),
                ensureCourse("Organizacao de Computadores", "Arquitetura, memoria e sistemas computacionais.", 30, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Cloud")),
                ensureCourse("Banco de Dados", "Modelagem, SQL e consultas.", 30, knowledgeAreas.get("Dados"), knowledgeAreas.get("Programacao")),
                ensureCourse("Empreendedorismo e Gerencia de Projetos", "Produto, gestao e planejamento de entregas.", 30, knowledgeAreas.get("Gestao"), knowledgeAreas.get("Produto")),
                ensureCourse("Desenvolvimento de Aplicativos Mobile", "Fundamentos para aplicacoes moveis.", 30, knowledgeAreas.get("Programacao"), knowledgeAreas.get("Produto")),
                ensureCourse("Business Intelligence", "Indicadores e visualizacao de dados.", 30, knowledgeAreas.get("Dados"), knowledgeAreas.get("Produto")),
                ensureCourse("Big Data", "Introducao a processamento de grandes volumes de dados.", 30, knowledgeAreas.get("Dados"), knowledgeAreas.get("Cloud")),
                ensureCourse("Introducao a Inteligencia Artificial", "Conceitos basicos de IA e aprendizado de maquina.", 30, knowledgeAreas.get("Dados"), knowledgeAreas.get("Programacao")),
                ensureCourse("Internet das Coisas", "Dispositivos conectados e aplicacoes IoT.", 30, knowledgeAreas.get("Cloud"), knowledgeAreas.get("Programacao")),
                ensureCourse("Introducao a Engenharia de Requisitos", "Levantamento, escrita e validacao de requisitos.", 30, knowledgeAreas.get("Produto"), knowledgeAreas.get("Gestao")),
                ensureCourse("Introducao ao Treinamento de IA", "Dados, treino e avaliacao de modelos.", 30, knowledgeAreas.get("Dados"), knowledgeAreas.get("Programacao")),
                ensureCourse("Introducao a UI e UX", "Pesquisa, interface e experiencia do usuario.", 30, knowledgeAreas.get("Design"), knowledgeAreas.get("Produto"))
        );

        for (int index = 0; index < courses.size(); index++) {
            assignCourse(classModel, courses.get(index), index < 6);
        }

        ensureCourseEvaluation(courses.get(1), "UFG-LOG-01", "FIXACAO", "Questoes de Fixacao - Logica", 5.0, 10.0, LocalDate.of(2025, 9, 10));
        ensureCourseEvaluation(courses.get(2), "UFG-PY-01", "FIXACAO", "Questoes de Fixacao - Python", 5.0, 10.0, LocalDate.of(2025, 9, 17));
        ensureCourseEvaluation(courses.get(4), "UFG-BD-01", "FIXACAO", "Questoes de Fixacao - Banco de Dados", 5.0, 10.0, LocalDate.of(2025, 9, 24));
        ensureCourseEvaluation(courses.get(5), "UFG-EMP-01", "FIXACAO", "Entrega de Empreendedorismo", 5.0, 10.0, LocalDate.of(2025, 10, 1));

        String[][] studentRows = {
                {"Andriel De Figueredo Furtado", "andriel@discente.ufg.br", "Ampla concorrencia", "0", "0"},
                {"Arthur Trindade Da Silva", "arthur.trindade@discente.ufg.br", "Negros e pardos", "3", "3"},
                {"Aureo Bento Xavier Junior", "aureojrster@gmail.com", "Ampla concorrencia", "2", "2"},
                {"Bruna Borges Da Silva", "bruborges12@gmail.com", "Mulheres", "5", "5"},
                {"Delvo Resende", "delvoresende@ufg.br", "45+", "14", "6"},
                {"Eduarda Lima Claudio Reges Campos", "dudalreges@gmail.com", "Ampla concorrencia", "6", "6"},
                {"Fabio Sodre Rocha", "fabiosodremat@gmail.com", "Ampla concorrencia", "3", "3"},
                {"Fernando Cardoso Houara Brettas", "fernandochbrettas@gmail.com", "Ampla concorrencia", "6", "6"},
                {"Flavia Rosado Lima", "flaviarosadolima@gmail.com", "Mulheres", "2", "2"},
                {"Francieli Moreira De Carvalho", "francieli.mcarvalho@gmail.com", "Mulheres", "2", "1"},
                {"Gabriel Marques Laer Da Silva", "gabriel.laer@discente.ufg.br", "Ampla concorrencia", "14", "6"},
                {"Gabriel Queiroz Rodrigues", "gabriel.qr.10@gmail.com", "Negros e pardos", "6", "6"},
                {"Gabriel Silva Taveira", "gabrieltaveira@discente.ufg.br", "Ampla concorrencia", "14", "6"},
                {"Gilvan Jose Meireles", "gil-van-jose@hotmail.com", "45+", "14", "6"},
                {"Giovanna Lyssa Rodrigues Borges Teles", "giovannalyssa@discente.ufg.br", "Mulheres", "0", "0"},
                {"Guilherme Goulart Caldas Araujo", "guilherme94barcelona@gmail.com", "Ampla concorrencia", "5", "4"},
                {"Guilherme Henrique Candido De Moraes", "guilhermehenriqueif@gmail.com", "Ampla concorrencia", "4", "4"},
                {"Guilherme Silva Virgilli", "gsvirgilli@gmail.com", "Ampla concorrencia", "1", "1"},
                {"Gustavo Fagundes Vicente", "brttplobbr@gmail.com", "PCDs", "9", "6"},
                {"Hailton David Lemos", "hailton.david@gmail.com", "45+", "14", "6"},
                {"Helder Dos Santos Lima", "heldersantos@discente.ufg.br", "Negros e pardos", "9", "4"},
                {"Isabela Cristina Gomes Da Fonseca", "isabela.cristina@discente.ufg.br", "Mulheres", "3", "1"},
                {"Isabela Jung Cardoso", "jung2@discente.ufg.br", "Mulheres", "4", "4"},
                {"Itallo Junior Chaves Dos Santos", "itallosantos@discente.ufg.br", "Negros e pardos", "0", "0"},
                {"Italo Marques Rodrigues Silva", "italomarques1235@gmail.com", "Negros e pardos", "3", "3"},
                {"Joao Pedro Campos Constantino", "pedrocns@discente.ufg.br", "Negros e pardos", "14", "6"},
                {"Joao Pedro Ribeiro Barbosa", "joao1.barbosa@outlook.com", "Ampla concorrencia", "1", "1"},
                {"Joao Vitor Da Costa Almeida", "jhovitor98@discente.ufg.br", "Negros e pardos", "1", "1"},
                {"Joao Vitor Gomes De Oliveira", "vecctor.vitor15@gmail.com", "Negros e pardos", "14", "6"},
                {"Jorge Lucas Pimmel Pacheco", "jorgelucas011@gmail.com", "Ampla concorrencia", "10", "6"},
                {"Joyce Beatriz Ferreira Da Costa Silva", "joycesilva@discente.ufg.br", "Mulheres", "1", "1"},
                {"Kathleen Caroline Barbosa De Oliveira", "kathleencaroline357@gmail.com", "Mulheres", "7", "6"},
                {"Lara Souza Ribeiro Vargas E Aragao", "alloymemory@gmail.com", "Mulheres", "12", "6"},
                {"Leidiane Beatriz Passos Rodrigues", "leidianebeatrizpassosrodrigues@gmail.com", "Mulheres", "4", "4"},
                {"Lucio Flavio De Paula Couto", "luciosistemainf@hotmail.com", "Ampla concorrencia", "4", "3"},
                {"Mariana Goncalves Landi", "mariana26landii@gmail.com", "Mulheres", "14", "6"},
                {"Mateus Rodrigues Soares", "mateusmk04@gmail.com", "Ampla concorrencia", "1", "1"},
                {"Mirmila Socrates Do Nascimento", "mirmilasocrates@discente.ufg.br", "Mulheres", "7", "5"},
                {"Natalie Almeida Coelho", "natalie.coelho@discente.ufg.br", "PCDs", "9", "6"},
                {"Pedro Carlos Da Conceicao Arantes", "pedro.carlos@discente.ufg.br", "Ampla concorrencia", "7", "6"},
                {"Rafael Augusto Da Silva Januario", "rafaasjgames@gmail.com", "Ampla concorrencia", "3", "3"},
                {"Rafael Barbosa Da Silva", "rafaelbrbsilva@gmail.com", "Negros e pardos", "1", "1"},
                {"Rafael Bueno Pires", "rafaelbuenoh@discente.ufg.br", "Ampla concorrencia", "0", "0"},
                {"Ricardo Da Silva Santos", "ricardo.santos@ifg.edu.br", "Ampla concorrencia", "14", "6"},
                {"Talison Amorim Do Nascimento", "amorimtalison9@gmail.com", "PCDs", "14", "6"},
                {"Ubiratan Alves Paniago Filho", "ubiratan.filho@ufg.br", "Ampla concorrencia", "2", "2"},
                {"Vinicius Amorim Da Silva", "amorimm.viniciuss@gmail.com", "Ampla concorrencia", "14", "6"},
                {"Vitor Vinicius Gomes Cerqueira", "eng.vitorvg@gmail.com", "Ampla concorrencia", "1", "1"},
                {"Webse Da Mota Costa", "websecosta@gmail.com", "Negros e pardos", "0", "0"},
                {"Yasmin Souza De Castro", "yasminsdcastro@gmail.com", "Mulheres", "10", "6"}
        };

        List<PeopleModel> approvedStudents = new ArrayList<>();
        ExamModel levelingExam = ensureExam("Prova de Nivelamento - UFG 2025.2", classModel, LocalDate.of(2025, 11, 4));
        List<ExamQuestionModel> questions = new ArrayList<>();
        for (int number = 1; number <= 40; number++) {
            KnowledgeAreaModel subject = number <= 16 ? knowledgeAreas.get("Programacao") : number <= 28 ? knowledgeAreas.get("Dados") : knowledgeAreas.get("Produto");
            questions.add(ensureExamQuestion(levelingExam, number, subject, 2.0));
        }

        for (int index = 0; index < studentRows.length; index++) {
            String[] row = studentRows[index];
            int completedCourses = Integer.parseInt(row[3]);
            int requiredCourses = Integer.parseInt(row[4]);
            String gender = row[2].equals("Mulheres") || row[0].contains("Bruna") || row[0].contains("Isabela")
                    || row[0].contains("Joyce") || row[0].contains("Kathleen") || row[0].contains("Lara")
                    || row[0].contains("Mariana") || row[0].contains("Natalie") || row[0].contains("Yasmin")
                    ? "Feminino" : "Masculino";
            String cpf = String.format(Locale.ROOT, "%011d", 91000000000L + index + 1);
            PeopleModel student = ensurePerson(
                    cpf,
                    row[0],
                    row[1],
                    index % 5 == 0 ? "Tecnico em area de exatas" : "Graduacao",
                    "Endereco real protegido - planilha UFG " + (index + 1),
                    null,
                    index % 4 == 0 ? "Aparecida de Goiania" : "Goiania",
                    "GO",
                    gender,
                    row[2],
                    "6298000" + String.format(Locale.ROOT, "%04d", index + 1),
                    LocalDate.of(1980 + (index % 24), (index % 12) + 1, (index % 27) + 1),
                    institutions.get("UFG").getName(),
                    index % 3 == 0 ? "Engenharia de Software" : index % 3 == 1 ? "Ciencia da Computacao" : "Analise e Desenvolvimento de Sistemas",
                    index % 4 == 0 ? "Concluido" : "Cursando",
                    index % 4 == 0 ? LocalDate.of(2024, 12, 15) : null
            );
            approvedStudents.add(student);

            createStageCandidate(selectionStage, student, StageStatus.APROVADO, "Selecionado a partir da planilha de candidatos UFG.");
            createStageCandidate(levelingStage, student, StageStatus.APROVADO, "Resultado consolidado no relatorio semanal UFG.");
            createStageCandidate(immersionStage, student, StageStatus.APROVADO, "Aprovado para imersao: " + row[2]);

            boolean concludedImmersion = completedCourses >= 6 && !classModel.getImmersionEndDate().isAfter(today);
            createEnrollment(
                    student,
                    classModel,
                    studentRole,
                    concludedImmersion ? "CONCLUIDO" : "ATIVO",
                    classModel.getLevelingStartDate(),
                    concludedImmersion ? classModel.getImmersionEndDate() : null,
                    Math.min(100.0, 72.0 + completedCourses + (index % 6)),
                    Math.min(100.0, 70.0 + (requiredCourses * 4.0) + (index % 5))
            );

            for (int courseIndex = 0; courseIndex < courses.size(); courseIndex++) {
                double percentage;
                String status;
                LocalDate completionDate = null;
                if (courseIndex < completedCourses) {
                    percentage = 100.0;
                    status = "Concluido";
                    completionDate = LocalDate.of(2025, 9, 1).plusDays(courseIndex * 3L + (index % 5));
                } else if (courseIndex < Math.max(requiredCourses, completedCourses + 1)) {
                    percentage = 35.0 + ((index + courseIndex) % 6) * 10.0;
                    status = "Em andamento";
                } else {
                    percentage = 0.0;
                    status = "Nao iniciado";
                }
                createCourseProgression(courses.get(courseIndex), student, completionDate, percentage, status, LocalDate.of(2025, 11, 3).minusDays(index % 7));
            }

            double score = Math.min(80.0, 48.0 + (index % 12) * 2.0 + Math.min(requiredCourses, 6));
            ensureExamResult(levelingExam, student, score, 39 + (index % 24));
            int correctLimit = Math.max(1, (int) Math.round(score / 2.0));
            for (ExamQuestionModel question : questions) {
                boolean correct = question.getQuestionNumber() <= correctLimit;
                ensureExamAnswer(student, question, correct, correct ? "Acerto" : "Erro", correct ? 2.0 : 0.0);
            }
        }

        String[][] projectRows = {
                {"GoLeadger", "Agente de BlockChain", "Agentes de IA para plataforma de orquestracao de blockchain."},
                {"Oficina de Ensino", "Gerador de Questoes", "Motor gerador de questoes para apoio didatico."},
                {"Clarion Lex", "Agente Juridico Trabalhista", "Agente inteligente para producao de pecas trabalhistas."},
                {"Apoema Tecnologia e Inovacao", "IA para Ecossistema de Inovacao", "Interface conversacional para o ecossistema de inovacao."},
                {"BRISA", "Prova Online", "Aplicacao de provas online com recursos de seguranca."},
                {"Pingo Solucoes Tecnologicas", "Mamae Pingo", "Promocao da parentalidade na primeira infancia."},
                {"SECTI", "Sukatech", "Pontos e cashback para descarte de lixo eletronico."},
                {"BP Company Sistemas", "Processos com IA", "Automatizacao de acoes de processo com agentes de IA."},
                {"ERP para Clinica Psico", "ERP para Clinica Psico", "Solucao operacional para startup de saude."},
                {"Truth DAO", "Extrator de Noticias", "Extracao de noticias de midias digitais."},
                {"LABIIH", "Prova Segura", "Tecnologia para integridade de vestigios criminais papiloscopicos."}
        };

        List<PeopleModel> leaders = new ArrayList<>(staff.values());
        for (int projectIndex = 0; projectIndex < projectRows.length; projectIndex++) {
            PeopleModel leader = leaders.get(projectIndex % leaders.size());
            ProjectGroupModel group = ensureProjectGroup(
                    projectRows[projectIndex][1],
                    classModel,
                    projectRows[projectIndex][2],
                    institutions.get("UFG"),
                    projectRows[projectIndex][0],
                    leader,
                    "https://github.com/brisa/ufg-" + normalizeSlug(projectRows[projectIndex][1]).replace(".", "-"),
                    DayOfWeek.of((projectIndex % 5) + 1),
                    LocalDate.of(2025, 11, 26).plusDays(projectIndex)
            );

            int start = projectIndex * 5;
            int end = Math.min(start + 5, approvedStudents.size());
            for (int studentIndex = start; studentIndex < end; studentIndex++) {
                addGroupMember(group, approvedStudents.get(studentIndex));
            }
            ensureMeeting(group, LocalDate.of(2025, 12, 3).plusWeeks(projectIndex % 4), "COMPLETED");
            ensureMeeting(group, LocalDate.of(2026, 2, 18), "COMPLETED");
            ensureMeeting(group, LocalDate.of(2026, 5, 20), "SCHEDULED");
        }
    }

    private void seedSystemLogs(LocalDate today) {
        UserModel admin = userRepository.getByLogin("admin").orElse(null);
        createSystemLog(LogAction.USER_LOGIN, "Administrador acessou o sistema de homologacao.", "Auth", "admin", admin, "127.0.0.1");
        createSystemLog(LogAction.PROGRAM_CREATE, "Programa BRISA One - IA Aplicada registrado na base mock.", "Program", "BRISA-IA-2026", admin, "127.0.0.1");
        createSystemLog(LogAction.CLASS_CREATE, "Turma UFAL 2026.1 preparada para demonstracao.", "Class", "UFAL 2026.1", admin, "127.0.0.1");
        createSystemLog(LogAction.PEOPLE_IMPORT, "Participantes de exemplo importados para navegacao do sistema.", "People", "mock-batch", admin, "127.0.0.1");
        createSystemLog(LogAction.PEOPLE_IMPORT, "Planilhas UFG 2025.2 consolidadas no seed de apresentacao.", "People", "UFG 2025.2", admin, "127.0.0.1");
        createSystemLog(LogAction.ENROLLMENT_IMPORT, "Matriculas de exemplo disponibilizadas para testes visuais.", "Enrollment", "mock-enrollments", admin, "127.0.0.1");
        createSystemLog(LogAction.ADVISOR_CREATE, "Equipe academica mockada com orientadores e gestores.", "Advisor", "mock-team", admin, "127.0.0.1");
        createSystemLog(LogAction.SYSTEM_INFO, "Automacao de carreira mockada com historico de disparos.", "CareerAutomation", "career-automation-base", admin, "127.0.0.1");
        createSystemLog(LogAction.SYSTEM_INFO, "Configuracao da automacao de carreira atualizada", "CareerAutomation", "career-automation-update", admin, "127.0.0.1", "{\"source\":\"mock-data-seeder\",\"operation\":\"UPDATE\",\"enabled\":true,\"checkpoints\":[6,12,18,24]}");
        createSystemLog(LogAction.SYSTEM_INFO, "E-mail de teste da automacao de carreira enviado", "CareerAutomation", "career-automation-test", admin, "127.0.0.1", "{\"source\":\"mock-data-seeder\",\"operation\":\"TEST\",\"recipientEmail\":\"admin@brisa.com\",\"checkpoints\":[6,12,18,24]}");
        createSystemLog(LogAction.SYSTEM_INFO, "Disparo automatico da carreira executado com 4 envio(s) e 1 falha(s)", "CareerAutomation", "career-automation-send", admin, "127.0.0.1", "{\"source\":\"mock-data-seeder\",\"operation\":\"SEND\",\"sentCount\":4,\"failedCount\":1,\"checkpoints\":[6,12,18,24]}");
        createSystemLog(LogAction.SYSTEM_WARNING, "Falha ao enviar parte dos acompanhamentos automaticos da carreira", "CareerAutomation", "career-automation-send-failed", admin, "127.0.0.1", "{\"source\":\"mock-data-seeder\",\"operation\":\"SEND_FAILED\",\"sentCount\":4,\"failedCount\":1,\"checkpoints\":[6,12,18,24],\"errorMessage\":\"Uma tentativa falhou por indisponibilidade temporaria do provedor.\"}");
    }

    private AcademicRoleModel ensureAcademicRole(String name, String description) {
        return academicRoleRepository.findByNameIgnoreCase(name)
                .map(existing -> {
                    existing.setDescription(description);
                    return academicRoleRepository.save(existing);
                })
                .orElseGet(() -> academicRoleRepository.save(new AcademicRoleModel(null, name, description)));
    }

    private InstitutionRoleModel ensureInstitutionRole(String name) {
        return institutionRoleRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> institutionRoleRepository.save(new InstitutionRoleModel(null, name)));
    }

    private KnowledgeAreaModel ensureKnowledgeArea(String name) {
        return knowledgeAreaRepository.findByNameIgnoreCase(name)
                .orElseGet(() -> knowledgeAreaRepository.save(new KnowledgeAreaModel(null, name)));
    }

    private InstitutionModel ensureInstitution(String code, String name, String acronym, String state, String coordinator) {
        InstitutionModel institution = institutionRepository.findByCodeIgnoreCase(code)
                .orElseGet(InstitutionModel::new);
        institution.setCode(code);
        institution.setName(name);
        institution.setAcronym(acronym);
        institution.setState(state);
        institution.setLocalCoordinatorName(coordinator);
        return institutionRepository.save(institution);
    }

    private ProgramModel ensureProgram(
            String code,
            String name,
            String contractNumber,
            String executorName,
            String fundingEntity,
            String generalCoordinator,
            BigDecimal value,
            LocalDate startDate,
            LocalDate endDate,
            String targetAudience,
            String levelingModality,
            String levelingDuration,
            String immersionDuration,
            Integer immersionWorkloadHours,
            String quotaCriteria,
            String evaluationCriteria,
            String executor,
            String location,
            String supportEmail,
            String officialWebsite,
            String mainLocality,
            String observations,
            String partnerNames
    ) {
        ProgramModel program = programRepository.findByCodeIgnoreCase(code).orElseGet(ProgramModel::new);
        program.setCode(code);
        program.setName(name);
        program.setContractNumber(contractNumber);
        program.setExecutorName(executorName);
        program.setFundingEntity(fundingEntity);
        program.setGeneralCoordinator(generalCoordinator);
        program.setProgramValue(value);
        program.setStartDate(startDate);
        program.setEndDate(endDate);
        program.setTargetAudience(targetAudience);
        program.setLevelingModality(levelingModality);
        program.setLevelingDuration(levelingDuration);
        program.setImmersionDuration(immersionDuration);
        program.setImmersionWorkloadHours(immersionWorkloadHours);
        program.setQuotaCriteria(quotaCriteria);
        program.setEvaluationCriteria(evaluationCriteria);
        program.setExecutor(executor);
        program.setLocation(location);
        program.setSupportEmail(supportEmail);
        program.setOfficialWebsite(officialWebsite);
        program.setMainLocality(mainLocality);
        program.setObservations(observations);
        program.setPartnerNames(partnerNames);
        return programRepository.save(program);
    }

    private void ensureProgramInstitution(ProgramModel program, InstitutionModel institution, InstitutionRoleModel role) {
        if (program == null || institution == null) {
            return;
        }

        if (programInstitutionRepository.existsByProgramIdAndInstitutionId(program.getId(), institution.getId())) {
            return;
        }

        ProgramInstitutionModel relation = new ProgramInstitutionModel();
        relation.setProgram(program);
        relation.setInstitution(institution);
        relation.setRole(role);
        programInstitutionRepository.save(relation);
    }

    private ClassModel ensureClass(
            String code,
            ProgramModel program,
            InstitutionModel location,
            String locality,
            Integer qtdVagas,
            Integer selectionCapacity,
            Integer levelingCapacity,
            Integer immersionCapacity,
            Integer immersionTeamSize,
            LocalDate startDate,
            LocalDate endDate,
            LocalDate publicationDate,
            LocalDate applicationStartDate,
            LocalDate applicationEndDate,
            LocalDate levelingSelectionAnnouncementDate,
            LocalDate levelingStartDate,
            LocalDate levelingEndDate,
            LocalDate levelingFinalExamDate,
            LocalDate immersionSelectionAnnouncementDate,
            LocalDate immersionStartDate,
            LocalDate immersionEndDate,
            LocalDate partialEvaluationDate,
            LocalDate finalEvaluationDate,
            LocalDate certificateIssueDate
    ) {
        ClassModel classModel = classRepository.findByCodeIgnoreCase(code).orElseGet(ClassModel::new);
        classModel.setCode(code);
        classModel.setProgram(program);
        classModel.setLocation(location);
        classModel.setLocality(locality);
        classModel.setQtdVagas(qtdVagas);
        classModel.setDefaultSelectionCapacity(selectionCapacity);
        classModel.setDefaultLevelingCapacity(levelingCapacity);
        classModel.setDefaultImmersionCapacity(immersionCapacity);
        classModel.setImmersionTeamSize(immersionTeamSize);
        classModel.setStartDate(startDate);
        classModel.setEndDate(endDate);
        classModel.setPublicationDate(publicationDate);
        classModel.setApplicationStartDate(applicationStartDate);
        classModel.setApplicationEndDate(applicationEndDate);
        classModel.setLevelingSelectionAnnouncementDate(levelingSelectionAnnouncementDate);
        classModel.setLevelingStartDate(levelingStartDate);
        classModel.setLevelingEndDate(levelingEndDate);
        classModel.setLevelingFinalExamDate(levelingFinalExamDate);
        classModel.setImmersionSelectionAnnouncementDate(immersionSelectionAnnouncementDate);
        classModel.setImmersionStartDate(immersionStartDate);
        classModel.setImmersionEndDate(immersionEndDate);
        classModel.setPartialEvaluationDate(partialEvaluationDate);
        classModel.setFinalEvaluationDate(finalEvaluationDate);
        classModel.setCertificateIssueDate(certificateIssueDate);
        return classRepository.save(classModel);
    }

    private StageModel ensureStage(ClassModel classModel, String name, String description) {
        return stageRepository.findByNameAndClassModelId(name, classModel.getId())
                .map(existing -> {
                    existing.setDescription(description);
                    return stageRepository.save(existing);
                })
                .orElseGet(() -> {
                    StageModel stage = new StageModel();
                    stage.setClassModel(classModel);
                    stage.setName(name);
                    stage.setDescription(description);
                    return stageRepository.save(stage);
                });
    }

    private void ensureAddendum(ProgramModel program, Integer number, LocalDate startDate, LocalDate endDate, BigDecimal value) {
        boolean exists = programAddendumRepository.findByProgramIdOrderByAddendumNumberAsc(program.getId()).stream()
                .anyMatch(item -> Objects.equals(item.getAddendumNumber(), number));
        if (exists) {
            return;
        }

        ProgramAddendumModel addendum = new ProgramAddendumModel();
        addendum.setProgram(program);
        addendum.setAddendumNumber(number);
        addendum.setStartDate(startDate);
        addendum.setEndDate(endDate);
        addendum.setValue(value);
        programAddendumRepository.save(addendum);
    }

    private AdvisorModel ensureAdvisor(String cpf, String name, AdvisorRoleType roleType, String email, String formation, LocalDate birthDate) {
        AdvisorModel advisor = advisorRepository.findByCpf(cpf).orElseGet(AdvisorModel::new);
        advisor.setCpf(cpf);
        advisor.setName(name);
        advisor.setRoleType(roleType);
        advisor.setEmail(email);
        advisor.setFormation(formation);
        advisor.setBirthDate(birthDate);
        advisor.setActive(Boolean.TRUE);
        return advisorRepository.save(advisor);
    }

    private PeopleModel ensurePerson(
            String cpf,
            String name,
            String email,
            String educationLevel,
            String address,
            String addressExtra,
            String city,
            String state,
            String gender,
            String quotaCategory,
            String phone,
            LocalDate birthDate,
            String institutionName,
            String courseName,
            String educationStatus,
            LocalDate completionDate
    ) {
        PeopleModel people = peopleRepository.findByEmailIgnoreCase(email)
                .or(() -> peopleRepository.findByCpf(cpf))
                .orElseGet(PeopleModel::new);
        people.setCpf(cpf);
        people.setName(name);
        people.setEmail(email);
        people.setEducationLevel(educationLevel);
        people.setAddress(address);
        people.setAddressExtra(addressExtra);
        people.setCity(city);
        people.setState(state);
        people.setGender(gender);
        people.setQuotaCategory(quotaCategory);
        people.setPhone(phone);
        people.setLinkedin("https://linkedin.com/in/" + email.substring(0, email.indexOf('@')));
        people.setZipCode("57000-" + cpf.substring(Math.max(0, cpf.length() - 3)));
        people.setBirthDate(birthDate);
        people.setInstitutionName(institutionName);
        people.setCourseName(courseName);
        people.setEducationStatus(educationStatus);
        people.setEducationCompletionDate(completionDate);
        people.setSoftDeleted(Boolean.FALSE);
        return peopleRepository.save(people);
    }

    private String buildStudentEmail(String name, String prefix) {
        return normalizeSlug(name) + "@" + prefix + ".mock";
    }

    private String normalizeSlug(String value) {
        return value.toLowerCase(Locale.ROOT).replace(" ", ".");
    }

    private void createEnrollment(
            PeopleModel people,
            ClassModel classModel,
            AcademicRoleModel role,
            String status,
            LocalDate enrollmentDate,
            LocalDate completionDate,
            Double grade,
            Double frequency
    ) {
        EnrollmentModel enrollment = enrollmentRepository.findByPeopleIdAndClassModelIdAndAcademicRoleId(people.getId(), classModel.getId(), role.getId())
                .orElseGet(EnrollmentModel::new);
        enrollment.setPeople(people);
        enrollment.setClassModel(classModel);
        enrollment.setAcademicRole(role);
        enrollment.setStatus(status);
        enrollment.setEnrollmentDate(enrollmentDate);
        enrollment.setCompletionDate(completionDate);
        enrollment.setGrade(grade);
        enrollment.setFrequency(frequency);
        enrollmentRepository.save(enrollment);
    }

    private void createStageCandidate(StageModel stage, PeopleModel people, StageStatus status, String notes) {
        StageCandidateModel candidate = stageCandidateRepository.findByStageIdAndPeopleId(stage.getId(), people.getId())
                .orElseGet(StageCandidateModel::new);
        candidate.setStage(stage);
        candidate.setPeople(people);
        candidate.setStatus(status);
        candidate.setNotes(notes);
        stageCandidateRepository.save(candidate);
    }

    private CourseModel ensureCourse(String name, String description, double workload, KnowledgeAreaModel area, KnowledgeAreaModel subArea) {
        CourseModel course = courseRepository.findByNameIgnoreCase(name).orElseGet(CourseModel::new);
        course.setName(name);
        course.setDescription(description);
        course.setWorkload(workload);
        course.setKnowledgeArea(area);
        course.setSubArea(subArea);
        return courseRepository.save(course);
    }

    private void assignCourse(ClassModel classModel, CourseModel course, boolean required) {
        CourseAssignmentModel assignment = courseAssignmentRepository.findByCourseIdAndClassId(course.getId(), classModel.getId());
        if (assignment == null) {
            assignment = new CourseAssignmentModel();
            assignment.setClassModel(classModel);
            assignment.setCourse(course);
        }
        assignment.setRequired(required);
        courseAssignmentRepository.save(assignment);
    }

    private void ensureCourseEvaluation(CourseModel course, String code, String type, String name, Double minimumScore, Double maximumScore, LocalDate eventDate) {
        if (courseEvaluationRepository.findByCourseIdAndCodeIgnoreCase(course.getId(), code).isPresent()) {
            return;
        }

        CourseEvaluationModel evaluation = new CourseEvaluationModel();
        evaluation.setCourse(course);
        evaluation.setCode(code);
        evaluation.setType(type);
        evaluation.setName(name);
        evaluation.setMinimumScore(minimumScore);
        evaluation.setMaximumScore(maximumScore);
        evaluation.setEventDate(eventDate);
        courseEvaluationRepository.save(evaluation);
    }

    private void createCourseProgression(
            CourseModel course,
            PeopleModel people,
            LocalDate date,
            double completionPercentage,
            String status,
            LocalDate lastAccess
    ) {
        CourseProgressionModel progression = courseProgressionRepository.findByCourseIdAndPeopleId(course.getId(), people.getId())
                .orElseGet(CourseProgressionModel::new);
        progression.setCourse(course);
        progression.setPeople(people);
        progression.setDate(date);
        progression.setCompletionPercentage(completionPercentage);
        progression.setStatus(status);
        progression.setLastAccess(lastAccess);
        courseProgressionRepository.save(progression);
    }

    private ExamModel ensureExam(String name, ClassModel classModel, LocalDate examDate) {
        ExamModel exam = examRepository.findByClassModelIdAndNameIgnoreCase(classModel.getId(), name).orElseGet(ExamModel::new);
        exam.setName(name);
        exam.setClassModel(classModel);
        exam.setExamDate(examDate);
        return examRepository.save(exam);
    }

    private ExamQuestionModel ensureExamQuestion(ExamModel exam, Integer questionNumber, KnowledgeAreaModel subject, Double points) {
        ExamQuestionModel question = examQuestionRepository.findByExamIdAndQuestionNumber(exam.getId(), questionNumber).orElseGet(ExamQuestionModel::new);
        question.setExam(exam);
        question.setQuestionNumber(questionNumber);
        question.setSubject(subject);
        question.setPoints(points);
        return examQuestionRepository.save(question);
    }

    private void ensureExamResult(ExamModel exam, PeopleModel people, Double totalScore, Integer durationMinutes) {
        ExamResultModel result = examResultRepository.findByExamIdAndPeopleId(exam.getId(), people.getId())
                .orElseGet(ExamResultModel::new);
        result.setExam(exam);
        result.setPeople(people);
        result.setTotalScore(totalScore);
        result.setDurationMinutes(durationMinutes);
        examResultRepository.save(result);
    }

    private void ensureExamAnswer(PeopleModel people, ExamQuestionModel question, Boolean isCorrect, String answerText, Double pointsEarned) {
        boolean exists = examAnswerRepository.findAllByExamIdWithRelations(question.getExam().getId()).stream()
                .anyMatch(item -> Objects.equals(item.getPeople().getId(), people.getId())
                        && Objects.equals(item.getExamQuestion().getId(), question.getId()));
        if (exists) {
            return;
        }

        ExamAnswerModel answer = new ExamAnswerModel();
        answer.setPeople(people);
        answer.setExamQuestion(question);
        answer.setIsCorrect(isCorrect);
        answer.setAnswerText(answerText);
        answer.setPointsEarned(pointsEarned);
        examAnswerRepository.save(answer);
    }

    private ProjectGroupModel ensureProjectGroup(
            String theme,
            ClassModel classModel,
            String description,
            InstitutionModel projectCompany,
            String sponsorCompany,
            PeopleModel leader,
            String repositoryLink,
            DayOfWeek meetingDay,
            LocalDate firstMeetingDate
    ) {
        ProjectGroupModel existing = projectGroupRepository.findAll().stream()
                .filter(item -> item.getClassModel() != null && Objects.equals(item.getClassModel().getId(), classModel.getId()))
                .filter(item -> theme.equalsIgnoreCase(item.getProjectTheme()))
                .findFirst()
                .orElse(null);

        ProjectGroupModel group = existing == null ? new ProjectGroupModel() : existing;
        group.setProjectTheme(theme);
        group.setClassModel(classModel);
        group.setDescription(description);
        group.setProjectCompany(projectCompany);
        group.setSponsorCompany(sponsorCompany);
        group.setLeader(leader);
        group.setRepositoryLink(repositoryLink);
        group.setWeeklyMeetingDay(meetingDay);
        group.setFirstMeetingDate(firstMeetingDate);
        return projectGroupRepository.save(group);
    }

    private void addGroupMember(ProjectGroupModel group, PeopleModel people) {
        boolean exists = peopleProjectGroupRepository.findAll().stream()
                .anyMatch(item -> Objects.equals(item.getProjectGroup().getId(), group.getId())
                        && Objects.equals(item.getPeople().getId(), people.getId()));
        if (exists) {
            return;
        }

        PeopleProjectGroupModel member = new PeopleProjectGroupModel();
        member.setProjectGroup(group);
        member.setPeople(people);
        peopleProjectGroupRepository.save(member);
    }

    private void ensureMeeting(ProjectGroupModel group, LocalDate date, String status) {
        boolean exists = projectGroupMeetingRepository.findByProjectGroupIdOrderByMeetingDateAsc(group.getId()).stream()
                .anyMatch(item -> Objects.equals(item.getMeetingDate(), date));
        if (exists) {
            return;
        }

        ProjectGroupMeetingModel meeting = new ProjectGroupMeetingModel();
        meeting.setProjectGroup(group);
        meeting.setMeetingDate(date);
        meeting.setStatus(status);
        projectGroupMeetingRepository.save(meeting);
    }

    private void ensureCareerProgression(
            PeopleModel people,
            EnrollmentModel enrollment,
            ClassModel classModel,
            ProgramModel program,
            LocalDate surveyDate,
            String status,
            String company,
            String position,
            String notes
    ) {
        boolean exists = careerProgressionRepository.findAllWithRelations(people.getId(), classModel.getId(), program.getId()).stream()
                .anyMatch(item -> enrollment != null
                        && item.getEnrollment() != null
                        && Objects.equals(item.getEnrollment().getId(), enrollment.getId())
                        && Objects.equals(item.getSurveyDate(), surveyDate));
        if (exists) {
            return;
        }

        CareerProgressionModel progression = new CareerProgressionModel();
        progression.setPeople(people);
        progression.setEnrollment(enrollment);
        progression.setClassModel(classModel);
        progression.setProgram(program);
        progression.setSurveyDate(surveyDate);
        progression.setStatus(status);
        progression.setCompany(company);
        progression.setPosition(position);
        progression.setNotes(notes);
        careerProgressionRepository.save(progression);
    }

    private void ensureCareerDispatch(
            PeopleModel people,
            EnrollmentModel enrollment,
            ClassModel classModel,
            ProgramModel program,
            Integer checkpointMonths,
            LocalDate dueDate,
            String recipientEmail,
            String subjectSnapshot,
            String status,
            Integer attemptCount,
            String lastError
    ) {
        CareerAutomationDispatchModel dispatch = careerAutomationDispatchRepository.findByEnrollment_IdAndCheckpointMonths(enrollment.getId(), checkpointMonths)
                .orElseGet(CareerAutomationDispatchModel::new);
        dispatch.setPeople(people);
        dispatch.setEnrollment(enrollment);
        dispatch.setClassModel(classModel);
        dispatch.setProgram(program);
        dispatch.setCheckpointMonths(checkpointMonths);
        dispatch.setDueDate(dueDate);
        dispatch.setRecipientEmail(recipientEmail);
        dispatch.setSubjectSnapshot(subjectSnapshot);
        dispatch.setStatus(status);
        dispatch.setAttemptCount(attemptCount);
        dispatch.setLastAttemptAt(LocalDateTime.now().minusDays(attemptCount));
        dispatch.setSentAt("SENT".equalsIgnoreCase(status) ? LocalDateTime.now().minusDays(2) : null);
        dispatch.setLastError(lastError);
        careerAutomationDispatchRepository.save(dispatch);
    }

    private void createSystemLog(
            LogAction action,
            String description,
            String entityType,
            String entityId,
            UserModel user,
            String ipAddress
    ) {
        createSystemLog(action, description, entityType, entityId, user, ipAddress, "{\"source\":\"mock-data-seeder\"}");
    }

    private void createSystemLog(
            LogAction action,
            String description,
            String entityType,
            String entityId,
            UserModel user,
            String ipAddress,
            String details
    ) {
        boolean exists = systemLogRepository.findAll().stream()
                .anyMatch(log -> log.getAction() == action
                        && Objects.equals(log.getDescription(), description)
                        && Objects.equals(log.getEntityType(), entityType)
                        && Objects.equals(log.getEntityId(), entityId));
        if (exists) {
            return;
        }

        SystemLogModel log = new SystemLogModel();
        log.setAction(action);
        log.setDescription(description);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setUser(user);
        log.setIpAddress(ipAddress);
        log.setUserAgent("MockDataSeeder/1.0");
        log.setDetails(details);
        systemLogRepository.save(log);
    }
}
