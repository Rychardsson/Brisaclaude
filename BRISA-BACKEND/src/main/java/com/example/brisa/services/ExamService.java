package com.example.brisa.services;

import com.example.brisa.dtos.exam.ApprovedRankingRequestDTO;
import com.example.brisa.dtos.exam.ApprovedRankingResponseDTO;
import com.example.brisa.dtos.exam.ExamImportResponseDTO;
import com.example.brisa.dtos.exam.ExamRespondentProfileDTO;
import com.example.brisa.dtos.exam.ExamSummaryDTO;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.KnowledgeAreaModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.course.CourseProgressionModel;
import com.example.brisa.models.exam.ExamAnswerModel;
import com.example.brisa.models.exam.ExamModel;
import com.example.brisa.models.exam.ExamQuestionModel;
import com.example.brisa.models.exam.ExamResultModel;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.CourseProgressionRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.ExamAnswerRepository;
import com.example.brisa.repositories.ExamQuestionRepository;
import com.example.brisa.repositories.ExamRepository;
import com.example.brisa.repositories.ExamResultRepository;
import com.example.brisa.repositories.KnowledgeAreaRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {

    private static final Pattern QUESTION_PATTERN = Pattern.compile("^(?:q|questao|question)(\\d+)$");
    private static final Pattern SUBJECT_PATTERN = Pattern.compile("^(?:q|questao|question)(\\d+)(?:assunto|area|tema|subject)$");
    private static final Pattern SUBJECT_PREFIX_PATTERN = Pattern.compile("^(?:assunto|area|tema|subject)(\\d+)$");

    private final ClassRepository classRepository;
    private final ExamRepository examRepository;
    private final ExamQuestionRepository examQuestionRepository;
    private final ExamAnswerRepository examAnswerRepository;
    private final ExamResultRepository examResultRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final PeopleRepository peopleRepository;
    private final CourseProgressionRepository courseProgressionRepository;
    private final KnowledgeAreaRepository knowledgeAreaRepository;
    private final ExcelImportHelper excelImportHelper;

    @Transactional
    public ExamImportResponseDTO importResultsFromExcel(Long classId, MultipartFile file) throws IOException {
        ClassModel classModel = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma nao encontrada."));

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new ResourceNotFoundException("Planilha de prova vazia.");
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headers = excelImportHelper.mapHeaders(headerRow);
            Integer cpfIndex = excelImportHelper.findColumn(headers, List.of("cpf", "cpfaluno"), null);
            Integer nameIndex = excelImportHelper.findColumn(headers, List.of("nome", "nomecompleto", "name"), null);
            Integer totalScoreIndex = excelImportHelper.findColumn(headers, List.of("notafinal", "nota", "totalscore", "score"), null);
            Integer durationIndex = excelImportHelper.findColumn(headers, List.of("tempodeconclusao", "duracao", "durationminutes", "duration"), null);
            Integer examDateIndex = excelImportHelper.findColumn(headers, List.of("dataprova", "examdate"), null);
            Integer examNameIndex = excelImportHelper.findColumn(headers, List.of("codprova", "nomeprova", "examcode", "examname"), null);

            Map<Integer, Integer> questionColumns = new LinkedHashMap<>();
            Map<Integer, Integer> questionSubjectColumns = new LinkedHashMap<>();
            for (Map.Entry<String, Integer> entry : headers.entrySet()) {
                Integer questionNumber = extractQuestionNumber(entry.getKey());
                if (questionNumber != null) {
                    questionColumns.putIfAbsent(questionNumber, entry.getValue());
                    continue;
                }

                Integer subjectQuestionNumber = extractSubjectQuestionNumber(entry.getKey());
                if (subjectQuestionNumber != null) {
                    questionSubjectColumns.putIfAbsent(subjectQuestionNumber, entry.getValue());
                }
            }

            Row firstDataRow = findFirstDataRow(sheet);
            String examName = firstDataRow != null && examNameIndex != null
                    ? excelImportHelper.getString(firstDataRow, examNameIndex)
                    : null;
            if (examName == null || examName.isBlank()) {
                examName = "Prova Final - " + (classModel.getCode() == null ? "Turma" : classModel.getCode());
            }

            LocalDate examDate = firstDataRow != null && examDateIndex != null
                    ? excelImportHelper.getDate(firstDataRow, examDateIndex)
                    : null;
            if (examDate == null) {
                examDate = classModel.getLevelingFinalExamDate();
            }
            if (examDate == null) {
                examDate = LocalDate.now();
            }

            ExamModel exam = examRepository.findByClassModelIdAndNameIgnoreCase(classId, examName)
                    .orElseGet(ExamModel::new);
            exam.setClassModel(classModel);
            exam.setName(examName);
            exam.setExamDate(examDate);
            exam = examRepository.save(exam);

            Map<Integer, ExamQuestionModel> questionsByNumber = examQuestionRepository.findByExamIdOrderByQuestionNumberAsc(exam.getId()).stream()
                    .collect(Collectors.toMap(ExamQuestionModel::getQuestionNumber, question -> question, (left, right) -> left, LinkedHashMap::new));

            for (Integer questionNumber : questionColumns.keySet()) {
                ExamQuestionModel question = questionsByNumber.get(questionNumber);
                if (question == null) {
                    question = new ExamQuestionModel();
                    question.setExam(exam);
                    question.setQuestionNumber(questionNumber);
                }

                Integer subjectIndex = questionSubjectColumns.get(questionNumber);
                if (subjectIndex != null && firstDataRow != null) {
                    String subjectName = excelImportHelper.getString(firstDataRow, subjectIndex);
                    if (subjectName != null && !subjectName.isBlank()) {
                        question.setSubject(getOrCreateKnowledgeArea(subjectName));
                    }
                }

                questionsByNumber.put(questionNumber, examQuestionRepository.save(question));
            }

            Map<String, PeopleModel> peopleByCpf = buildPeopleByCpf(classId);
            Map<String, PeopleModel> peopleByName = buildPeopleByName(classId);
            List<String> warnings = new ArrayList<>();
            int participantsProcessed = 0;

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (excelImportHelper.isRowEmpty(row)) {
                    continue;
                }

                String cpf = cpfIndex == null ? null : excelImportHelper.getString(row, cpfIndex);
                String name = nameIndex == null ? null : excelImportHelper.getString(row, nameIndex);
                PeopleModel people = resolvePeople(peopleByCpf, peopleByName, cpf, name);
                if (people == null) {
                    warnings.add("Linha " + (rowIndex + 1) + ": participante nao encontrado.");
                    continue;
                }

                participantsProcessed++;
                ExamResultModel result = examResultRepository.findByExamIdAndPeopleId(exam.getId(), people.getId())
                        .orElseGet(ExamResultModel::new);
                result.setExam(exam);
                result.setPeople(people);

                Double totalScore = totalScoreIndex == null ? null : excelImportHelper.getDouble(row, totalScoreIndex);
                Integer durationMinutes = durationIndex == null ? null : excelImportHelper.getInteger(row, durationIndex);
                double computedScore = 0.0;
                boolean hasQuestionScore = false;

                examAnswerRepository.deleteByPeopleIdAndExamId(people.getId(), exam.getId());

                for (Map.Entry<Integer, Integer> questionEntry : questionColumns.entrySet()) {
                    Integer questionNumber = questionEntry.getKey();
                    Integer columnIndex = questionEntry.getValue();
                    QuestionValue questionValue = parseQuestionValue(row, columnIndex);
                    if (!questionValue.hasAnyValue()) {
                        continue;
                    }

                    ExamQuestionModel question = questionsByNumber.get(questionNumber);
                    Integer subjectIndex = questionSubjectColumns.get(questionNumber);
                    if (subjectIndex != null && question.getSubject() == null) {
                        String subjectName = excelImportHelper.getString(row, subjectIndex);
                        if (subjectName != null && !subjectName.isBlank()) {
                            question.setSubject(getOrCreateKnowledgeArea(subjectName));
                            question = examQuestionRepository.save(question);
                            questionsByNumber.put(questionNumber, question);
                        }
                    }

                    ExamAnswerModel answer = new ExamAnswerModel();
                    answer.setPeople(people);
                    answer.setExamQuestion(question);
                    answer.setIsCorrect(questionValue.correct());
                    answer.setAnswerText(questionValue.rawValue());
                    answer.setPointsEarned(questionValue.pointsEarned());
                    examAnswerRepository.save(answer);

                    if (questionValue.pointsEarned() != null) {
                        computedScore += questionValue.pointsEarned();
                        hasQuestionScore = true;
                    } else if (Boolean.TRUE.equals(questionValue.correct())) {
                        computedScore += 1.0;
                        hasQuestionScore = true;
                    }
                }

                if (totalScore == null && hasQuestionScore) {
                    totalScore = computedScore;
                }

                result.setTotalScore(totalScore);
                result.setDurationMinutes(durationMinutes);
                examResultRepository.save(result);
            }

            return new ExamImportResponseDTO(
                    exam.getId(),
                    exam.getName(),
                    participantsProcessed,
                    questionColumns.size(),
                    warnings
            );
        }
    }

    @Transactional(readOnly = true)
    public ExamSummaryDTO getLatestSummary(Long classId) {
        ExamModel exam = examRepository.findFirstByClassModelIdOrderByExamDateDescIdDesc(classId)
                .orElse(null);
        if (exam == null) {
            return emptySummary();
        }

        List<ExamResultModel> results = examResultRepository.findByExamIdWithRelations(exam.getId());
        List<ExamAnswerModel> answers = examAnswerRepository.findAllByExamIdWithRelations(exam.getId());

        int totalParticipants = results.size();
        double highestScore = results.stream()
                .map(ExamResultModel::getTotalScore)
                .filter(Objects::nonNull)
                .max(Double::compareTo)
                .orElse(0.0);
        double averageScore = results.stream()
                .map(ExamResultModel::getTotalScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        List<ExamSummaryDTO.ScoreBucketDTO> distribution = buildScoreDistribution(results, highestScore);
        List<ExamSummaryDTO.QuestionPerformanceDTO> questionPerformance = buildQuestionPerformance(answers);

        return new ExamSummaryDTO(
                exam.getName(),
                exam.getExamDate() == null ? "-" : exam.getExamDate().toString(),
                questionPerformance.size(),
                totalParticipants,
                results.stream().filter(result -> isFemale(result.getPeople())).count(),
                results.stream().filter(result -> isMale(result.getPeople())).count(),
                results.stream().filter(result -> result.getTotalScore() != null && result.getTotalScore() <= 0.0).count(),
                results.stream().filter(result -> result.getTotalScore() != null && Double.compare(result.getTotalScore(), highestScore) == 0).count(),
                highestScore,
                averageScore,
                distribution,
                questionPerformance.stream().sorted(Comparator.comparing(ExamSummaryDTO.QuestionPerformanceDTO::successRate).reversed()).limit(15).toList(),
                questionPerformance.stream().sorted(Comparator.comparing(ExamSummaryDTO.QuestionPerformanceDTO::successRate)).limit(15).toList()
        );
    }

    @Transactional(readOnly = true)
    public ExamRespondentProfileDTO getLatestRespondentProfile(Long classId) {
        ExamModel exam = examRepository.findFirstByClassModelIdOrderByExamDateDescIdDesc(classId)
                .orElse(null);
        if (exam == null) {
            return new ExamRespondentProfileDTO(List.of(), List.of(), List.of(), List.of());
        }

        List<ExamResultModel> results = examResultRepository.findByExamIdWithRelations(exam.getId());
        return new ExamRespondentProfileDTO(
                buildCountMetrics(results, result -> safeLabel(result.getPeople() != null ? result.getPeople().getCourseName() : null, "Nao informado")),
                buildAverageMetrics(results, result -> safeLabel(result.getPeople() != null ? result.getPeople().getCourseName() : null, "Nao informado")),
                buildAverageMetrics(results, result -> safeLabel(normalizeQuotaLabel(result.getPeople() != null ? result.getPeople().getQuotaCategory() : null), "Nao informado")),
                buildAverageMetrics(results, result -> safeLabel(formatCity(result.getPeople()), "Nao informado"))
        );
    }

    @Transactional(readOnly = true)
    public ApprovedRankingResponseDTO calculateApprovedRanking(Long classId, ApprovedRankingRequestDTO request) {
        ExamModel exam = examRepository.findFirstByClassModelIdOrderByExamDateDescIdDesc(classId)
                .orElse(null);
        if (exam == null) {
            return new ApprovedRankingResponseDTO(0, 0, List.of(), List.of(), List.of());
        }

        List<ExamResultModel> results = examResultRepository.findByExamIdWithRelations(exam.getId());
        Map<Long, Long> completedCoursesByPeopleId = courseProgressionRepository.findByClassId(classId).stream()
                .filter(progression -> progression.getPeople() != null && progression.getPeople().getId() != null)
                .filter(progression -> isCompletedStatus(progression.getStatus()))
                .collect(Collectors.groupingBy(progression -> progression.getPeople().getId(), Collectors.counting()));

        List<CandidateRankingData> rankedCandidates = results.stream()
                .map(result -> buildRankingData(result, completedCoursesByPeopleId.getOrDefault(result.getPeople().getId(), 0L), request))
                .sorted(Comparator
                        .comparing(CandidateRankingData::totalPoints).reversed()
                        .thenComparing(CandidateRankingData::examScore).reversed()
                        .thenComparing(CandidateRankingData::completedCourses).reversed()
                        .thenComparing(CandidateRankingData::age, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(CandidateRankingData::name, String.CASE_INSENSITIVE_ORDER))
                .toList();

        List<CandidateRankingData> approvedData = new ArrayList<>();
        Set<Long> selectedPeopleIds = new java.util.HashSet<>();
        allocateQuotaSeats(approvedData, selectedPeopleIds, rankedCandidates, request.getPcdSeats(), this::isPcd);
        allocateQuotaSeats(approvedData, selectedPeopleIds, rankedCandidates, request.getNegroPardoSeats(), this::isNegroPardo);
        allocateQuotaSeats(approvedData, selectedPeopleIds, rankedCandidates, request.getMulheresSeats(), this::isWoman);
        allocateQuotaSeats(approvedData, selectedPeopleIds, rankedCandidates, request.getAge45Seats(), this::isAge45Plus);

        int totalVacancies = resolveTotalVacancies(request);
        int remainingSeats = Math.max(totalVacancies - approvedData.size(), 0);
        allocateQuotaSeats(approvedData, selectedPeopleIds, rankedCandidates, remainingSeats, candidate -> true);

        List<CandidateRankingData> remaining = rankedCandidates.stream()
                .filter(candidate -> !selectedPeopleIds.contains(candidate.peopleId()))
                .toList();
        int waitlistSize = Math.min(totalVacancies, remaining.size());

        List<ApprovedRankingResponseDTO.ApprovedCandidateDTO> approved = toRankingDtos(approvedData, "Aprovado");
        List<ApprovedRankingResponseDTO.ApprovedCandidateDTO> waitlist = toRankingDtos(remaining.subList(0, waitlistSize), "Lista de espera");
        List<ApprovedRankingResponseDTO.ApprovedCandidateDTO> rejected = toRankingDtos(remaining.subList(waitlistSize, remaining.size()), "Nao classificado");

        return new ApprovedRankingResponseDTO(
                totalVacancies,
                rankedCandidates.size(),
                approved,
                waitlist,
                rejected
        );
    }

    private ExamSummaryDTO emptySummary() {
        return new ExamSummaryDTO("-", "-", 0, 0, 0, 0, 0, 0, 0.0, 0.0, List.of(), List.of(), List.of());
    }

    private Row findFirstDataRow(Sheet sheet) {
        for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (!excelImportHelper.isRowEmpty(row)) {
                return row;
            }
        }
        return null;
    }

    private Integer extractQuestionNumber(String normalizedHeader) {
        Matcher matcher = QUESTION_PATTERN.matcher(normalizedHeader);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return null;
    }

    private Integer extractSubjectQuestionNumber(String normalizedHeader) {
        Matcher suffixMatcher = SUBJECT_PATTERN.matcher(normalizedHeader);
        if (suffixMatcher.matches()) {
            return Integer.parseInt(suffixMatcher.group(1));
        }
        Matcher prefixMatcher = SUBJECT_PREFIX_PATTERN.matcher(normalizedHeader);
        if (prefixMatcher.matches()) {
            return Integer.parseInt(prefixMatcher.group(1));
        }
        return null;
    }

    private Map<String, PeopleModel> buildPeopleByCpf(Long classId) {
        Map<String, PeopleModel> peopleByCpf = new LinkedHashMap<>();

        for (EnrollmentModel enrollment : enrollmentRepository.findByClassIdWithRelations(classId)) {
            PeopleModel people = enrollment.getPeople();
            if (people != null && people.getCpf() != null) {
                peopleByCpf.putIfAbsent(normalizeDocument(people.getCpf()), people);
            }
        }

        for (StageCandidateModel candidate : stageCandidateRepository.findByClassIdWithPeople(classId)) {
            PeopleModel people = candidate.getPeople();
            if (people != null && people.getCpf() != null) {
                peopleByCpf.putIfAbsent(normalizeDocument(people.getCpf()), people);
            }
        }

        for (PeopleModel people : peopleRepository.findAll()) {
            if (people.getCpf() != null) {
                peopleByCpf.putIfAbsent(normalizeDocument(people.getCpf()), people);
            }
        }

        return peopleByCpf;
    }

    private Map<String, PeopleModel> buildPeopleByName(Long classId) {
        Map<String, PeopleModel> peopleByName = new LinkedHashMap<>();

        for (EnrollmentModel enrollment : enrollmentRepository.findByClassIdWithRelations(classId)) {
            PeopleModel people = enrollment.getPeople();
            if (people != null && people.getName() != null) {
                peopleByName.putIfAbsent(normalizeText(people.getName()), people);
            }
        }

        for (StageCandidateModel candidate : stageCandidateRepository.findByClassIdWithPeople(classId)) {
            PeopleModel people = candidate.getPeople();
            if (people != null && people.getName() != null) {
                peopleByName.putIfAbsent(normalizeText(people.getName()), people);
            }
        }

        return peopleByName;
    }

    private PeopleModel resolvePeople(Map<String, PeopleModel> peopleByCpf, Map<String, PeopleModel> peopleByName, String cpf, String name) {
        String normalizedCpf = normalizeDocument(cpf);
        if (!normalizedCpf.isBlank() && peopleByCpf.containsKey(normalizedCpf)) {
            return peopleByCpf.get(normalizedCpf);
        }

        String normalizedName = normalizeText(name);
        if (!normalizedName.isBlank()) {
            return peopleByName.get(normalizedName);
        }

        return null;
    }

    private QuestionValue parseQuestionValue(Row row, Integer columnIndex) {
        String rawValue = excelImportHelper.getString(row, columnIndex);
        Double numericValue = excelImportHelper.getDouble(row, columnIndex);

        if (numericValue != null) {
            return new QuestionValue(numericValue > 0.0, numericValue, rawValue);
        }
        if (rawValue == null || rawValue.isBlank()) {
            return QuestionValue.empty();
        }

        String normalized = normalizeText(rawValue);
        if (normalized.equals("certo") || normalized.equals("correto") || normalized.equals("acerto") || normalized.equals("sim") || normalized.equals("true") || normalized.equals("x")) {
            return new QuestionValue(true, 1.0, rawValue);
        }
        if (normalized.equals("errado") || normalized.equals("incorreto") || normalized.equals("nao") || normalized.equals("false")) {
            return new QuestionValue(false, 0.0, rawValue);
        }

        return new QuestionValue(null, null, rawValue);
    }

    private KnowledgeAreaModel getOrCreateKnowledgeArea(String name) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        return knowledgeAreaRepository.findByNameIgnoreCase(trimmed)
                .orElseGet(() -> knowledgeAreaRepository.save(new KnowledgeAreaModel(null, trimmed)));
    }

    private List<ExamSummaryDTO.ScoreBucketDTO> buildScoreDistribution(List<ExamResultModel> results, double highestScore) {
        if (results.isEmpty()) {
            return List.of();
        }

        double maxScore = highestScore <= 0.0 ? 100.0 : highestScore;
        double bucketSize = Math.max(1.0, Math.ceil(maxScore / 5.0));
        List<ExamSummaryDTO.ScoreBucketDTO> buckets = new ArrayList<>();
        for (int bucket = 0; bucket < 5; bucket++) {
            double start = bucket * bucketSize;
            double end = bucket == 4 ? maxScore : Math.min(maxScore, ((bucket + 1) * bucketSize) - 0.01);
            long count = results.stream()
                    .filter(result -> result.getTotalScore() != null)
                    .filter(result -> result.getTotalScore() >= start && result.getTotalScore() <= end)
                    .count();
            buckets.add(new ExamSummaryDTO.ScoreBucketDTO(formatBucketLabel(start, end), count));
        }
        return buckets;
    }

    private List<ExamSummaryDTO.QuestionPerformanceDTO> buildQuestionPerformance(List<ExamAnswerModel> answers) {
        Map<Integer, List<ExamAnswerModel>> answersByQuestion = answers.stream()
                .filter(answer -> answer.getExamQuestion() != null && answer.getExamQuestion().getQuestionNumber() != null)
                .collect(Collectors.groupingBy(answer -> answer.getExamQuestion().getQuestionNumber()));

        return answersByQuestion.entrySet().stream()
                .map(entry -> {
                    List<ExamAnswerModel> questionAnswers = entry.getValue();
                    long totalAnswers = questionAnswers.size();
                    long correctAnswers = questionAnswers.stream()
                            .filter(answer -> Boolean.TRUE.equals(answer.getIsCorrect()) || (answer.getPointsEarned() != null && answer.getPointsEarned() > 0.0))
                            .count();
                    String subject = questionAnswers.stream()
                            .map(answer -> answer.getExamQuestion().getSubject())
                            .filter(Objects::nonNull)
                            .map(KnowledgeAreaModel::getName)
                            .filter(Objects::nonNull)
                            .findFirst()
                            .orElse("-");
                    double successRate = totalAnswers == 0 ? 0.0 : (correctAnswers * 100.0) / totalAnswers;
                    return new ExamSummaryDTO.QuestionPerformanceDTO(entry.getKey(), subject, correctAnswers, totalAnswers, successRate);
                })
                .toList();
    }

    private List<ExamRespondentProfileDTO.MetricDTO> buildCountMetrics(
            List<ExamResultModel> results,
            java.util.function.Function<ExamResultModel, String> groupResolver
    ) {
        if (results.isEmpty()) {
            return List.of();
        }

        Map<String, Long> counts = results.stream()
                .collect(Collectors.groupingBy(groupResolver, LinkedHashMap::new, Collectors.counting()));

        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> new ExamRespondentProfileDTO.MetricDTO(
                        entry.getKey(),
                        (entry.getValue() * 100.0) / results.size(),
                        entry.getValue()
                ))
                .toList();
    }

    private List<ExamRespondentProfileDTO.MetricDTO> buildAverageMetrics(
            List<ExamResultModel> results,
            java.util.function.Function<ExamResultModel, String> groupResolver
    ) {
        if (results.isEmpty()) {
            return List.of();
        }

        Map<String, List<ExamResultModel>> grouped = results.stream()
                .collect(Collectors.groupingBy(groupResolver, LinkedHashMap::new, Collectors.toList()));

        return grouped.entrySet().stream()
                .map(entry -> {
                    double average = entry.getValue().stream()
                            .map(ExamResultModel::getTotalScore)
                            .filter(Objects::nonNull)
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                    return new ExamRespondentProfileDTO.MetricDTO(entry.getKey(), average, entry.getValue().size());
                })
                .sorted(Comparator.comparing(ExamRespondentProfileDTO.MetricDTO::value).reversed())
                .limit(10)
                .toList();
    }

    private CandidateRankingData buildRankingData(ExamResultModel result, long completedCourses, ApprovedRankingRequestDTO request) {
        PeopleModel people = result.getPeople();
        int cityBonus = calculateCityBonus(people, request);
        Integer age = calculateAge(people);
        double examScore = result.getTotalScore() == null ? 0.0 : result.getTotalScore();
        double totalPoints = examScore + (completedCourses * request.getPointsPerCompletedCourse()) + cityBonus;

        return new CandidateRankingData(
                people.getId(),
                safeLabel(people.getName(), "Participante"),
                people.getCpf(),
                normalizeQuotaLabel(people.getQuotaCategory()),
                formatCity(people),
                age,
                examScore,
                completedCourses,
                cityBonus,
                totalPoints,
                safeLabel(people.getGender(), "-")
        );
    }

    private void allocateQuotaSeats(
            List<CandidateRankingData> approved,
            Set<Long> selectedPeopleIds,
            List<CandidateRankingData> rankedCandidates,
            Integer requestedSeats,
            Predicate<CandidateRankingData> predicate
    ) {
        int seats = requestedSeats == null ? 0 : requestedSeats;
        if (seats <= 0) {
            return;
        }

        for (CandidateRankingData candidate : rankedCandidates) {
            if (seats <= 0) {
                return;
            }
            if (selectedPeopleIds.contains(candidate.peopleId()) || !predicate.test(candidate)) {
                continue;
            }
            approved.add(candidate);
            selectedPeopleIds.add(candidate.peopleId());
            seats--;
        }
    }

    private List<ApprovedRankingResponseDTO.ApprovedCandidateDTO> toRankingDtos(List<CandidateRankingData> candidates, String decision) {
        List<ApprovedRankingResponseDTO.ApprovedCandidateDTO> response = new ArrayList<>();
        for (int index = 0; index < candidates.size(); index++) {
            CandidateRankingData candidate = candidates.get(index);
            response.add(new ApprovedRankingResponseDTO.ApprovedCandidateDTO(
                    candidate.peopleId(),
                    candidate.name(),
                    candidate.cpf(),
                    candidate.quota(),
                    candidate.city(),
                    candidate.age(),
                    candidate.examScore(),
                    candidate.completedCourses(),
                    candidate.cityBonus(),
                    candidate.totalPoints(),
                    index + 1,
                    decision
            ));
        }
        return response;
    }

    private int resolveTotalVacancies(ApprovedRankingRequestDTO request) {
        int derived = safeInt(request.getAmplaConcorrenciaSeats())
                + safeInt(request.getPcdSeats())
                + safeInt(request.getNegroPardoSeats())
                + safeInt(request.getMulheresSeats())
                + safeInt(request.getAge45Seats());
        if (request.getTotalVacancies() != null && request.getTotalVacancies() > 0) {
            return request.getTotalVacancies();
        }
        return derived > 0 ? derived : 1;
    }

    private int calculateCityBonus(PeopleModel people, ApprovedRankingRequestDTO request) {
        String city = normalizeText(people == null ? null : people.getCity());
        if (city.isBlank()) {
            return 0;
        }
        return request.getPriorityCities().stream()
                .map(this::normalizeText)
                .anyMatch(city::equals)
                ? safeInt(request.getBonusPerPriorityCity())
                : 0;
    }

    private Integer calculateAge(PeopleModel people) {
        if (people == null || people.getBirthDate() == null) {
            return null;
        }
        return Period.between(people.getBirthDate(), LocalDate.now()).getYears();
    }

    private boolean isCompletedStatus(String status) {
        String normalized = normalizeText(status);
        return normalized.contains("concluido") || normalized.contains("concluido") || normalized.contains("finalizado");
    }

    private boolean isFemale(PeopleModel people) {
        return normalizeText(people == null ? null : people.getGender()).startsWith("f");
    }

    private boolean isMale(PeopleModel people) {
        return normalizeText(people == null ? null : people.getGender()).startsWith("m");
    }

    private boolean isPcd(CandidateRankingData candidate) {
        String normalized = normalizeText(candidate.quota());
        return normalized.contains("pcd") || normalized.contains("neuro");
    }

    private boolean isNegroPardo(CandidateRankingData candidate) {
        String normalized = normalizeText(candidate.quota());
        return normalized.contains("negro") || normalized.contains("pardo");
    }

    private boolean isWoman(CandidateRankingData candidate) {
        String normalizedQuota = normalizeText(candidate.quota());
        String normalizedGender = normalizeText(candidate.gender());
        return normalizedQuota.contains("mulher") || normalizedGender.startsWith("f");
    }

    private boolean isAge45Plus(CandidateRankingData candidate) {
        return candidate.age() != null && candidate.age() >= 45;
    }

    private String formatBucketLabel(double start, double end) {
        int startValue = (int) Math.floor(start);
        int endValue = (int) Math.ceil(end);
        return startValue + "-" + endValue;
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

    private String safeLabel(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private String formatCity(PeopleModel people) {
        if (people == null) {
            return "-";
        }
        String city = safeLabel(people.getCity(), "");
        String state = safeLabel(people.getState(), "");
        if (!city.isBlank() && !state.isBlank()) {
            return city + " - " + state;
        }
        if (!city.isBlank()) {
            return city;
        }
        if (!state.isBlank()) {
            return state;
        }
        return "-";
    }

    private String normalizeQuotaLabel(String value) {
        String normalized = normalizeText(value);
        if (normalized.isBlank()) {
            return "-";
        }
        if (normalized.contains("ampla")) {
            return "Ampla concorrencia";
        }
        if (normalized.contains("pcd") || normalized.contains("neuro")) {
            return "PCD/Neurodivergente";
        }
        if (normalized.contains("negro") || normalized.contains("pardo")) {
            return "Negro/Pardo";
        }
        if (normalized.contains("mulher")) {
            return "Mulheres";
        }
        if (normalized.contains("45")) {
            return "45+";
        }
        return value == null ? "-" : value;
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }

    private record QuestionValue(Boolean correct, Double pointsEarned, String rawValue) {
        static QuestionValue empty() {
            return new QuestionValue(null, null, null);
        }

        boolean hasAnyValue() {
            return correct != null || pointsEarned != null || (rawValue != null && !rawValue.isBlank());
        }
    }

    private record CandidateRankingData(
            Long peopleId,
            String name,
            String cpf,
            String quota,
            String city,
            Integer age,
            double examScore,
            long completedCourses,
            int cityBonus,
            double totalPoints,
            String gender
    ) {
    }
}
