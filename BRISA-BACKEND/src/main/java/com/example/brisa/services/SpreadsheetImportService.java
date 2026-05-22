package com.example.brisa.services;

import com.example.brisa.dtos.imports.SpreadsheetImportResponseDTO;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.InstitutionModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.ProgramInstitutionModel;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.models.StageModel;
import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.InstitutionRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.ProgramRepository;
import com.example.brisa.repositories.ProgramInstitutionRepository;
import com.example.brisa.repositories.StageRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SpreadsheetImportService {

    private final ProgramRepository programRepository;
    private final ClassRepository classRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final InstitutionRepository institutionRepository;
    private final PeopleRepository peopleRepository;
    private final AcademicRoleRepository academicRoleRepository;
    private final ProgramInstitutionRepository programInstitutionRepository;
    private final StageRepository stageRepository;
    private final ExcelImportHelper excelImportHelper;

    @Transactional
    public SpreadsheetImportResponseDTO importProgramsFromExcel(MultipartFile file) throws IOException {
        SpreadsheetImportResponseDTO response = new SpreadsheetImportResponseDTO();
        List<String> duplicateItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));

            Integer idxCode = excelImportHelper.findColumn(headers, List.of("codigo", "cÃ³digo", "code", "cod_programa"), 0);
            Integer idxName = excelImportHelper.findColumn(headers, List.of("nome", "nome programa", "programa", "name"), 1);
            Integer idxContract = excelImportHelper.findColumn(headers, List.of("numero contrato", "nÃºmero contrato", "contrato", "contract number", "contractnumber"), 2);
            Integer idxStartDate = excelImportHelper.findColumn(headers, List.of("data inicio", "data inÃ­cio", "inicio", "start date", "startdate"), 3);
            Integer idxEndDate = excelImportHelper.findColumn(headers, List.of("data fim", "data tÃ©rmino", "data termino", "fim", "end date", "enddate"), 4);
            Integer idxTargetAudience = excelImportHelper.findColumn(headers, List.of("publico alvo", "pÃºblico alvo", "target audience", "targetaudience"), null);
            Integer idxLevelingModality = excelImportHelper.findColumn(headers, List.of("modalidade nivelamento", "leveling modality", "levelingmodality"), null);
            Integer idxLevelingDuration = excelImportHelper.findColumn(headers, List.of("duracao nivelamento", "duraÃ§Ã£o nivelamento", "leveling duration", "levelingduration"), null);
            Integer idxImmersionDuration = excelImportHelper.findColumn(headers, List.of("duracao imersao", "duraÃ§Ã£o imersÃ£o", "immersion duration", "immersionduration"), null);
            Integer idxImmersionWorkload = excelImportHelper.findColumn(headers, List.of("carga horaria imersao", "carga horÃ¡ria imersÃ£o", "immersion workload hours", "immersionworkloadhours"), null);
            Integer idxQuotaCriteria = excelImportHelper.findColumn(headers, List.of("criterios de cota", "critÃ©rios de cota", "quota criteria", "quotacriteria", "cotas"), null);
            Integer idxEvaluationCriteria = excelImportHelper.findColumn(headers, List.of("criterios de avaliacao", "critÃ©rios de avaliaÃ§Ã£o", "evaluation criteria", "evaluationcriteria"), null);
            Integer idxInstitutionName = excelImportHelper.findColumn(headers, List.of("instituicao", "instituiÃ§Ã£o", "parceiro", "partner", "institution"), null);
            Integer idxInstitutionCode = excelImportHelper.findColumn(headers, List.of("codigo instituicao", "cÃ³digo instituiÃ§Ã£o", "institution code", "institutioncode", "sigla instituicao", "sigla instituiÃ§Ã£o"), null);

            Integer idxExecutorName = excelImportHelper.findColumn(headers, List.of("executora", "executor", "executor name", "executorname"), null);
            Integer idxFundingEntity = excelImportHelper.findColumn(headers, List.of("entidade fomento", "entidade de fomento", "funding entity", "fundingentity"), null);
            Integer idxGeneralCoordinator = excelImportHelper.findColumn(headers, List.of("coordenador geral", "general coordinator", "generalcoordinator"), null);
            Integer idxProgramValue = excelImportHelper.findColumn(headers, List.of("valor programa", "valor do programa", "program value", "programvalue"), null);
            Integer idxSupportEmail = excelImportHelper.findColumn(headers, List.of("email suporte", "e-mail suporte", "support email", "supportemail"), null);
            Integer idxOfficialWebsite = excelImportHelper.findColumn(headers, List.of("site oficial", "official website", "officialwebsite"), null);
            Integer idxMainLocality = excelImportHelper.findColumn(headers, List.of("localidade", "main locality", "mainlocality"), null);
            Integer idxObservations = excelImportHelper.findColumn(headers, List.of("observacoes", "observaÃƒÂ§ÃƒÂµes", "observation", "observations"), null);
            Integer idxPartnerNames = excelImportHelper.findColumn(headers, List.of("parceiros", "partner names", "partnernames"), null);

            Integer idxFrequency = excelImportHelper.findColumn(headers, List.of("frequencia", "frequÃƒÂªncia", "frequency"), 9);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (excelImportHelper.isRowEmpty(row)) {
                    continue;
                }

                response.setTotalProcessed(response.getTotalProcessed() + 1);

                String code = excelImportHelper.getString(row, idxCode);
                String name = excelImportHelper.getString(row, idxName);
                String trackingKey = buildTrackingKey("program", code, name);

                if (trackingKey == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": informe pelo menos cÃ³digo ou nome do programa.");
                    continue;
                }

                if (!seenKeys.add(trackingKey)) {
                    duplicateItems.add(defaultIfBlank(name, code, "Linha " + (rowIndex + 1)));
                    response.setAlreadyExists(response.getAlreadyExists() + 1);
                    continue;
                }

                ProgramModel program = findProgram(code, name).orElseGet(ProgramModel::new);
                boolean isNew = program.getId() == null;

                if (name == null && isNew) {
                    errors.add("Linha " + (rowIndex + 1) + ": o nome do programa Ã© obrigatÃ³rio para novos cadastros.");
                    continue;
                }

                if (code != null) program.setCode(code);
                if (name != null) program.setName(name);

                String contractNumber = excelImportHelper.getString(row, idxContract);
                LocalDate startDate = excelImportHelper.getDate(row, idxStartDate);
                LocalDate endDate = excelImportHelper.getDate(row, idxEndDate);
                String targetAudience = excelImportHelper.getString(row, idxTargetAudience);
                String executorName = excelImportHelper.getString(row, idxExecutorName);
                String fundingEntity = excelImportHelper.getString(row, idxFundingEntity);
                String generalCoordinator = excelImportHelper.getString(row, idxGeneralCoordinator);
                Double programValue = excelImportHelper.getDouble(row, idxProgramValue);
                String levelingModality = excelImportHelper.getString(row, idxLevelingModality);
                String levelingDuration = excelImportHelper.getString(row, idxLevelingDuration);
                String immersionDuration = excelImportHelper.getString(row, idxImmersionDuration);
                Integer immersionWorkload = excelImportHelper.getInteger(row, idxImmersionWorkload);
                String quotaCriteria = excelImportHelper.getString(row, idxQuotaCriteria);
                String evaluationCriteria = excelImportHelper.getString(row, idxEvaluationCriteria);
                String supportEmail = excelImportHelper.getString(row, idxSupportEmail);
                String officialWebsite = excelImportHelper.getString(row, idxOfficialWebsite);
                String mainLocality = excelImportHelper.getString(row, idxMainLocality);
                String observations = excelImportHelper.getString(row, idxObservations);
                String partnerNames = excelImportHelper.getString(row, idxPartnerNames);

                if (contractNumber != null) program.setContractNumber(contractNumber);
                if (startDate != null) program.setStartDate(startDate);
                if (endDate != null) program.setEndDate(endDate);
                if (targetAudience != null) program.setTargetAudience(targetAudience);
                if (executorName != null) program.setExecutorName(executorName);
                if (fundingEntity != null) program.setFundingEntity(fundingEntity);
                if (generalCoordinator != null) program.setGeneralCoordinator(generalCoordinator);
                if (programValue != null) program.setProgramValue(BigDecimal.valueOf(programValue));
                if (levelingModality != null) program.setLevelingModality(levelingModality);
                if (levelingDuration != null) program.setLevelingDuration(levelingDuration);
                if (immersionDuration != null) program.setImmersionDuration(immersionDuration);
                if (immersionWorkload != null) program.setImmersionWorkloadHours(immersionWorkload);
                if (quotaCriteria != null) program.setQuotaCriteria(quotaCriteria);
                if (evaluationCriteria != null) program.setEvaluationCriteria(evaluationCriteria);
                if (supportEmail != null) program.setSupportEmail(supportEmail);
                if (officialWebsite != null) program.setOfficialWebsite(officialWebsite);
                if (mainLocality != null) program.setMainLocality(mainLocality);
                if (observations != null) program.setObservations(observations);
                if (partnerNames != null) program.setPartnerNames(partnerNames);

                ProgramModel savedProgram = programRepository.save(program);

                String institutionCode = excelImportHelper.getString(row, idxInstitutionCode);
                String institutionName = excelImportHelper.getString(row, idxInstitutionName);
                if (institutionCode != null || institutionName != null) {
                    ResolvedEntity<InstitutionModel> resolvedInstitution = resolveInstitution(institutionCode, institutionName, null);
                    if (resolvedInstitution.created()) {
                        response.setRelatedRecordsCreated(response.getRelatedRecordsCreated() + 1);
                    }
                    attachInstitutionToProgram(savedProgram, resolvedInstitution.entity());
                }

                if (isNew) {
                    response.setSuccessfullyInserted(response.getSuccessfullyInserted() + 1);
                } else {
                    response.setUpdated(response.getUpdated() + 1);
                }
            }
        }

        response.setDuplicateItems(duplicateItems);
        response.setErrors(errors);
        return response;
    }

    @Transactional
    public SpreadsheetImportResponseDTO importClassesFromExcel(MultipartFile file) throws IOException {
        SpreadsheetImportResponseDTO response = new SpreadsheetImportResponseDTO();
        List<String> duplicateItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));

            Integer idxCode = excelImportHelper.findColumn(headers, List.of("codigo", "cÃ³digo", "code", "cod_turma", "turma"), 0);
            Integer idxProgramCode = excelImportHelper.findColumn(headers, List.of("codigo programa", "cÃ³digo programa", "program code", "programcode", "cod_programa"), 1);
            Integer idxProgramName = excelImportHelper.findColumn(headers, List.of("programa", "nome programa", "program name", "programname"), 2);
            Integer idxLocality = excelImportHelper.findColumn(headers, List.of("localidade", "cidade", "polo", "locality"), 3);
            Integer idxInstitutionCode = excelImportHelper.findColumn(headers, List.of("codigo instituicao", "cÃ³digo instituiÃ§Ã£o", "institution code", "institutioncode"), 4);
            Integer idxInstitutionName = excelImportHelper.findColumn(headers, List.of("instituicao", "instituiÃ§Ã£o", "institution", "location"), 5);
            Integer idxStartDate = excelImportHelper.findColumn(headers, List.of("data inicio", "data inÃ­cio", "start date", "startdate"), 6);
            Integer idxEndDate = excelImportHelper.findColumn(headers, List.of("data fim", "data tÃ©rmino", "data termino", "end date", "enddate"), 7);
            Integer idxQtdVagas = excelImportHelper.findColumn(headers, List.of("quantidade vagas", "qtd vagas", "vagas", "qtdvagas"), null);
            Integer idxPublicationDate = excelImportHelper.findColumn(headers, List.of("data publicacao", "data publicaÃ§Ã£o", "publication date", "publicationdate"), null);
            Integer idxApplicationStartDate = excelImportHelper.findColumn(headers, List.of("inicio inscricoes", "inÃ­cio inscriÃ§Ãµes", "application start date", "applicationstartdate"), null);
            Integer idxApplicationEndDate = excelImportHelper.findColumn(headers, List.of("fim inscricoes", "fim inscriÃ§Ãµes", "application end date", "applicationenddate"), null);
            Integer idxLevelingSelectionAnnouncementDate = excelImportHelper.findColumn(headers, List.of("resultado nivelamento", "leveling selection announcement date", "levelingselectionannouncementdate"), null);
            Integer idxLevelingConfirmationStartDate = excelImportHelper.findColumn(headers, List.of("inicio confirmacao nivelamento", "inÃ­cio confirmaÃ§Ã£o nivelamento", "leveling confirmation start date", "levelingconfirmationstartdate"), null);
            Integer idxLevelingConfirmationEndDate = excelImportHelper.findColumn(headers, List.of("fim confirmacao nivelamento", "fim confirmaÃ§Ã£o nivelamento", "leveling confirmation end date", "levelingconfirmationenddate"), null);
            Integer idxLevelingStartDate = excelImportHelper.findColumn(headers, List.of("inicio nivelamento", "inÃ­cio nivelamento", "leveling start date", "levelingstartdate"), null);
            Integer idxLevelingEndDate = excelImportHelper.findColumn(headers, List.of("fim nivelamento", "leveling end date", "levelingenddate"), null);
            Integer idxLevelingFinalExamDate = excelImportHelper.findColumn(headers, List.of("prova final nivelamento", "leveling final exam date", "levelingfinalexamdate"), null);
            Integer idxImmersionSelectionAnnouncementDate = excelImportHelper.findColumn(headers, List.of("resultado imersao", "resultado imersÃ£o", "immersion selection announcement date", "immersionselectionannouncementdate"), null);
            Integer idxImmersionConfirmationStartDate = excelImportHelper.findColumn(headers, List.of("inicio confirmacao imersao", "inÃ­cio confirmaÃ§Ã£o imersÃ£o", "immersion confirmation start date", "immersionconfirmationstartdate"), null);
            Integer idxImmersionConfirmationEndDate = excelImportHelper.findColumn(headers, List.of("fim confirmacao imersao", "fim confirmaÃ§Ã£o imersÃ£o", "immersion confirmation end date", "immersionconfirmationenddate"), null);
            Integer idxImmersionStartDate = excelImportHelper.findColumn(headers, List.of("inicio imersao", "inÃ­cio imersÃ£o", "immersion start date", "immersionstartdate"), null);
            Integer idxImmersionEndDate = excelImportHelper.findColumn(headers, List.of("fim imersao", "fim imersÃ£o", "immersion end date", "immersionenddate"), null);
            Integer idxPartialEvaluationDate = excelImportHelper.findColumn(headers, List.of("avaliacao parcial", "avaliaÃ§Ã£o parcial", "partial evaluation date", "partialevaluationdate"), null);
            Integer idxFinalEvaluationDate = excelImportHelper.findColumn(headers, List.of("avaliacao final", "avaliaÃ§Ã£o final", "final evaluation date", "finalevaluationdate"), null);
            Integer idxCertificateIssueDate = excelImportHelper.findColumn(headers, List.of("emissao certificado", "emissÃ£o certificado", "certificate issue date", "certificateissuedate"), null);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (excelImportHelper.isRowEmpty(row)) {
                    continue;
                }

                response.setTotalProcessed(response.getTotalProcessed() + 1);

                String code = excelImportHelper.getString(row, idxCode);
                if (code == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": o cÃ³digo da turma Ã© obrigatÃ³rio.");
                    continue;
                }

                String trackingKey = "class:" + normalize(code);
                if (!seenKeys.add(trackingKey)) {
                    duplicateItems.add(code);
                    response.setAlreadyExists(response.getAlreadyExists() + 1);
                    continue;
                }

                String programCode = excelImportHelper.getString(row, idxProgramCode);
                String programName = excelImportHelper.getString(row, idxProgramName);
                ProgramModel program = findProgram(programCode, programName).orElse(null);
                if (program == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": programa nÃ£o encontrado para a turma '" + code + "'.");
                    continue;
                }

                ClassModel classModel = classRepository.findByCodeIgnoreCase(code).orElseGet(ClassModel::new);
                boolean isNew = classModel.getId() == null;

                classModel.setCode(code);
                classModel.setProgram(program);

                String locality = excelImportHelper.getString(row, idxLocality);
                if (locality != null) classModel.setLocality(locality);

                String institutionCode = excelImportHelper.getString(row, idxInstitutionCode);
                String institutionName = excelImportHelper.getString(row, idxInstitutionName);
                if (institutionCode != null || institutionName != null) {
                    ResolvedEntity<InstitutionModel> resolvedInstitution = resolveInstitution(institutionCode, institutionName, locality);
                    classModel.setLocation(resolvedInstitution.entity());
                    if (resolvedInstitution.created()) {
                        response.setRelatedRecordsCreated(response.getRelatedRecordsCreated() + 1);
                    }
                }

                LocalDate startDate = firstNonNull(
                        excelImportHelper.getDate(row, idxStartDate),
                        excelImportHelper.getDate(row, idxApplicationStartDate),
                        excelImportHelper.getDate(row, idxLevelingStartDate),
                        excelImportHelper.getDate(row, idxImmersionStartDate)
                );
                LocalDate endDate = firstNonNull(
                        excelImportHelper.getDate(row, idxEndDate),
                        excelImportHelper.getDate(row, idxCertificateIssueDate),
                        excelImportHelper.getDate(row, idxFinalEvaluationDate),
                        excelImportHelper.getDate(row, idxImmersionEndDate),
                        excelImportHelper.getDate(row, idxLevelingEndDate)
                );

                if (startDate != null) classModel.setStartDate(startDate);
                if (endDate != null) classModel.setEndDate(endDate);

                Integer qtdVagas = excelImportHelper.getInteger(row, idxQtdVagas);
                if (qtdVagas != null) classModel.setQtdVagas(qtdVagas);

                setIfPresent(excelImportHelper.getDate(row, idxPublicationDate), classModel::setPublicationDate);
                setIfPresent(excelImportHelper.getDate(row, idxApplicationStartDate), classModel::setApplicationStartDate);
                setIfPresent(excelImportHelper.getDate(row, idxApplicationEndDate), classModel::setApplicationEndDate);
                setIfPresent(excelImportHelper.getDate(row, idxLevelingSelectionAnnouncementDate), classModel::setLevelingSelectionAnnouncementDate);
                setIfPresent(excelImportHelper.getDate(row, idxLevelingConfirmationStartDate), classModel::setLevelingConfirmationStartDate);
                setIfPresent(excelImportHelper.getDate(row, idxLevelingConfirmationEndDate), classModel::setLevelingConfirmationEndDate);
                setIfPresent(excelImportHelper.getDate(row, idxLevelingStartDate), classModel::setLevelingStartDate);
                setIfPresent(excelImportHelper.getDate(row, idxLevelingEndDate), classModel::setLevelingEndDate);
                setIfPresent(excelImportHelper.getDate(row, idxLevelingFinalExamDate), classModel::setLevelingFinalExamDate);
                setIfPresent(excelImportHelper.getDate(row, idxImmersionSelectionAnnouncementDate), classModel::setImmersionSelectionAnnouncementDate);
                setIfPresent(excelImportHelper.getDate(row, idxImmersionConfirmationStartDate), classModel::setImmersionConfirmationStartDate);
                setIfPresent(excelImportHelper.getDate(row, idxImmersionConfirmationEndDate), classModel::setImmersionConfirmationEndDate);
                setIfPresent(excelImportHelper.getDate(row, idxImmersionStartDate), classModel::setImmersionStartDate);
                setIfPresent(excelImportHelper.getDate(row, idxImmersionEndDate), classModel::setImmersionEndDate);
                setIfPresent(excelImportHelper.getDate(row, idxPartialEvaluationDate), classModel::setPartialEvaluationDate);
                setIfPresent(excelImportHelper.getDate(row, idxFinalEvaluationDate), classModel::setFinalEvaluationDate);
                setIfPresent(excelImportHelper.getDate(row, idxCertificateIssueDate), classModel::setCertificateIssueDate);

                ClassModel savedClass = classRepository.save(classModel);
                ensureDefaultStages(savedClass);

                if (isNew) {
                    response.setSuccessfullyInserted(response.getSuccessfullyInserted() + 1);
                } else {
                    response.setUpdated(response.getUpdated() + 1);
                }
            }
        }

        response.setDuplicateItems(duplicateItems);
        response.setErrors(errors);
        return response;
    }

    @Transactional
    public SpreadsheetImportResponseDTO importInstitutionsFromExcel(MultipartFile file) throws IOException {
        SpreadsheetImportResponseDTO response = new SpreadsheetImportResponseDTO();
        List<String> duplicateItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));

            Integer idxCode = excelImportHelper.findColumn(headers, List.of("codigo", "cÃ³digo", "code"), 0);
            Integer idxName = excelImportHelper.findColumn(headers, List.of("nome", "instituicao", "instituiÃ§Ã£o", "name"), 1);
            Integer idxAcronym = excelImportHelper.findColumn(headers, List.of("sigla", "acronym"), 2);
            Integer idxState = excelImportHelper.findColumn(headers, List.of("uf", "estado", "state"), 3);
            Integer idxCoordinator = excelImportHelper.findColumn(headers, List.of("coordenador local", "local coordinator", "localcoordinatorname"), 4);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (excelImportHelper.isRowEmpty(row)) {
                    continue;
                }

                response.setTotalProcessed(response.getTotalProcessed() + 1);

                String code = excelImportHelper.getString(row, idxCode);
                String name = excelImportHelper.getString(row, idxName);
                String trackingKey = buildTrackingKey("institution", code, name);

                if (trackingKey == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": informe cÃ³digo ou nome da instituiÃ§Ã£o.");
                    continue;
                }

                if (!seenKeys.add(trackingKey)) {
                    duplicateItems.add(defaultIfBlank(name, code, "Linha " + (rowIndex + 1)));
                    response.setAlreadyExists(response.getAlreadyExists() + 1);
                    continue;
                }

                InstitutionModel institution = findInstitution(code, name).orElseGet(InstitutionModel::new);
                boolean isNew = institution.getId() == null;

                if (name == null && isNew) {
                    errors.add("Linha " + (rowIndex + 1) + ": o nome da instituiÃ§Ã£o Ã© obrigatÃ³rio para novos cadastros.");
                    continue;
                }

                if (code != null) institution.setCode(code);
                if (name != null) institution.setName(name);

                String acronym = excelImportHelper.getString(row, idxAcronym);
                String state = excelImportHelper.getString(row, idxState);
                String coordinator = excelImportHelper.getString(row, idxCoordinator);

                if (acronym != null) institution.setAcronym(acronym);
                if (state != null) institution.setState(state);
                if (coordinator != null) institution.setLocalCoordinatorName(coordinator);

                institutionRepository.save(institution);

                if (isNew) {
                    response.setSuccessfullyInserted(response.getSuccessfullyInserted() + 1);
                } else {
                    response.setUpdated(response.getUpdated() + 1);
                }
            }
        }

        response.setDuplicateItems(duplicateItems);
        response.setErrors(errors);
        return response;
    }

    @Transactional
    public SpreadsheetImportResponseDTO importEnrollmentsFromExcel(MultipartFile file) throws IOException {
        SpreadsheetImportResponseDTO response = new SpreadsheetImportResponseDTO();
        List<String> duplicateItems = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        Set<String> seenKeys = new HashSet<>();

        try (InputStream inputStream = file.getInputStream(); Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));

            Integer idxName = excelImportHelper.findColumn(headers, List.of("nome", "name"), 0);
            Integer idxEmail = excelImportHelper.findColumn(headers, List.of("email", "e-mail"), 1);
            Integer idxCpf = excelImportHelper.findColumn(headers, List.of("cpf"), 2);
            Integer idxClassCode = excelImportHelper.findColumn(headers, List.of("codigo turma", "cÃ³digo turma", "turma", "class code", "classcode", "cod_turma"), 3);
            Integer idxRole = excelImportHelper.findColumn(headers, List.of("papel", "papel academico", "papel acadÃªmico", "role", "academic role", "academicrole"), 4);
            Integer idxEnrollmentDate = excelImportHelper.findColumn(headers, List.of("data matricula", "data matrÃ­cula", "enrollment date", "enrollmentdate"), 5);
            Integer idxStatus = excelImportHelper.findColumn(headers, List.of("status"), 6);
            Integer idxCompletionDate = excelImportHelper.findColumn(headers, List.of("data conclusao", "data conclusÃ£o", "completion date", "completiondate"), 7);
            Integer idxGrade = excelImportHelper.findColumn(headers, List.of("nota", "grade"), 8);
            Integer idxFrequency = excelImportHelper.findColumn(headers, List.of("frequencia", "frequência", "frequency"), 9);

            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (excelImportHelper.isRowEmpty(row)) {
                    continue;
                }

                response.setTotalProcessed(response.getTotalProcessed() + 1);

                String name = excelImportHelper.getString(row, idxName);
                String email = excelImportHelper.getString(row, idxEmail);
                String cpf = onlyDigits(excelImportHelper.getString(row, idxCpf));
                String classCode = excelImportHelper.getString(row, idxClassCode);
                String roleName = normalizeAcademicRoleName(excelImportHelper.getString(row, idxRole));

                if (classCode == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": o cÃ³digo da turma Ã© obrigatÃ³rio.");
                    continue;
                }

                if (email == null && cpf == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": informe e-mail ou CPF para localizar a pessoa.");
                    continue;
                }

                String trackingKey = "enrollment:" + normalize(defaultIfBlank(email, cpf, name)) + "|" + normalize(classCode) + "|" + normalize(roleName);
                if (!seenKeys.add(trackingKey)) {
                    duplicateItems.add(defaultIfBlank(name, email, cpf, classCode));
                    response.setAlreadyExists(response.getAlreadyExists() + 1);
                    continue;
                }

                ClassModel classModel = classRepository.findByCodeIgnoreCase(classCode).orElse(null);
                if (classModel == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": turma nÃ£o encontrada com cÃ³digo '" + classCode + "'.");
                    continue;
                }

                ResolvedEntity<PeopleModel> resolvedPerson = resolvePerson(name, email, cpf);
                if (resolvedPerson.entity() == null) {
                    errors.add("Linha " + (rowIndex + 1) + ": nÃ£o foi possÃ­vel localizar ou criar a pessoa da matrÃ­cula.");
                    continue;
                }
                if (resolvedPerson.created()) {
                    response.setRelatedRecordsCreated(response.getRelatedRecordsCreated() + 1);
                }

                ResolvedEntity<AcademicRoleModel> resolvedRole = resolveAcademicRole(roleName);
                if (resolvedRole.created()) {
                    response.setRelatedRecordsCreated(response.getRelatedRecordsCreated() + 1);
                }

                EnrollmentModel enrollment = enrollmentRepository
                        .findByPeopleIdAndClassModelIdAndAcademicRoleId(
                                resolvedPerson.entity().getId(),
                                classModel.getId(),
                                resolvedRole.entity().getId()
                        )
                        .orElseGet(EnrollmentModel::new);

                boolean isNew = enrollment.getId() == null;
                enrollment.setPeople(resolvedPerson.entity());
                enrollment.setClassModel(classModel);
                enrollment.setAcademicRole(resolvedRole.entity());

                LocalDate enrollmentDate = excelImportHelper.getDate(row, idxEnrollmentDate);
                LocalDate completionDate = excelImportHelper.getDate(row, idxCompletionDate);
                String status = normalizeEnrollmentStatus(excelImportHelper.getString(row, idxStatus));
                Double grade = excelImportHelper.getDouble(row, idxGrade);
                Double frequency = excelImportHelper.getDouble(row, idxFrequency);

                if (enrollmentDate != null) {
                    enrollment.setEnrollmentDate(enrollmentDate);
                } else if (isNew && enrollment.getEnrollmentDate() == null) {
                    enrollment.setEnrollmentDate(LocalDate.now());
                }
                if (completionDate != null) enrollment.setCompletionDate(completionDate);
                if (status != null) enrollment.setStatus(status);
                if (grade != null) enrollment.setGrade(grade);
                if (frequency != null) enrollment.setFrequency(frequency);

                enrollmentRepository.save(enrollment);

                if (isNew) {
                    response.setSuccessfullyInserted(response.getSuccessfullyInserted() + 1);
                } else {
                    response.setUpdated(response.getUpdated() + 1);
                }
            }
        }

        response.setDuplicateItems(duplicateItems);
        response.setErrors(errors);
        return response;
    }

    private Optional<ProgramModel> findProgram(String code, String name) {
        if (code != null) {
            Optional<ProgramModel> byCode = programRepository.findByCodeIgnoreCase(code);
            if (byCode.isPresent()) {
                return byCode;
            }
        }

        if (name != null) {
            return programRepository.findByNameIgnoreCase(name);
        }

        return Optional.empty();
    }

    private Optional<InstitutionModel> findInstitution(String code, String name) {
        if (code != null) {
            Optional<InstitutionModel> byCode = institutionRepository.findByCodeIgnoreCase(code);
            if (byCode.isPresent()) {
                return byCode;
            }
        }

        if (name != null) {
            return institutionRepository.findByNameIgnoreCase(name);
        }

        return Optional.empty();
    }

    private ResolvedEntity<InstitutionModel> resolveInstitution(String code, String name, String locality) {
        Optional<InstitutionModel> existing = findInstitution(code, name);
        if (existing.isPresent()) {
            InstitutionModel institution = existing.get();
            if (institution.getName() == null && name != null) institution.setName(name);
            if (institution.getCode() == null && code != null) institution.setCode(code);
            if (institution.getAcronym() == null && name != null) institution.setAcronym(inferAcronym(name));
            if (institution.getState() == null && locality != null) institution.setState(inferState(locality));
            return new ResolvedEntity<>(institutionRepository.save(institution), false);
        }

        InstitutionModel institution = new InstitutionModel();
        institution.setCode(code);
        institution.setName(defaultIfBlank(name, code));
        institution.setAcronym(inferAcronym(defaultIfBlank(name, code)));
        institution.setState(inferState(locality));
        return new ResolvedEntity<>(institutionRepository.save(institution), true);
    }

    private void attachInstitutionToProgram(ProgramModel program, InstitutionModel institution) {
        if (program == null || institution == null) {
            return;
        }

        if (programInstitutionRepository.existsByProgramIdAndInstitutionId(program.getId(), institution.getId())) {
            return;
        }

        ProgramInstitutionModel membership = new ProgramInstitutionModel();
        membership.setProgram(program);
        membership.setInstitution(institution);
        programInstitutionRepository.save(membership);
    }

    private void ensureDefaultStages(ClassModel classModel) {
        for (String stageName : List.of("SELECAO", "NIVELAMENTO", "IMERSAO")) {
            if (stageRepository.existsByNameAndClassModelId(stageName, classModel.getId())) {
                continue;
            }
            StageModel stage = new StageModel();
            stage.setName(stageName);
            stage.setDescription(stageName);
            stage.setClassModel(classModel);
            stageRepository.save(stage);
        }
    }

    private ResolvedEntity<PeopleModel> resolvePerson(String name, String email, String cpf) {
        if (email != null) {
            Optional<PeopleModel> byEmail = peopleRepository.findByEmailIgnoreCase(email);
            if (byEmail.isPresent()) {
                PeopleModel person = byEmail.get();
                if (person.getName() == null && name != null) person.setName(name);
                if (person.getCpf() == null && cpf != null) person.setCpf(cpf);
                return new ResolvedEntity<>(peopleRepository.save(person), false);
            }
        }

        if (cpf != null) {
            Optional<PeopleModel> byCpf = peopleRepository.findByCpf(cpf);
            if (byCpf.isPresent()) {
                PeopleModel person = byCpf.get();
                if (person.getName() == null && name != null) person.setName(name);
                if (person.getEmail() == null && email != null) person.setEmail(email);
                return new ResolvedEntity<>(peopleRepository.save(person), false);
            }
        }

        if (name == null) {
            return new ResolvedEntity<>(null, false);
        }

        PeopleModel person = new PeopleModel();
        person.setName(name);
        person.setEmail(email);
        person.setCpf(cpf);
        return new ResolvedEntity<>(peopleRepository.save(person), true);
    }

    private ResolvedEntity<AcademicRoleModel> resolveAcademicRole(String roleName) {
        String normalizedRole = defaultIfBlank(roleName, "ALUNO");
        Optional<AcademicRoleModel> existing = academicRoleRepository.findByNameIgnoreCase(normalizedRole);
        if (existing.isPresent()) {
            return new ResolvedEntity<>(existing.get(), false);
        }

        AcademicRoleModel role = new AcademicRoleModel();
        role.setName(normalizedRole);
        role.setDescription(toTitleCase(normalizedRole));
        return new ResolvedEntity<>(academicRoleRepository.save(role), true);
    }

    private String buildTrackingKey(String prefix, String code, String name) {
        if (code != null) {
            return prefix + ":code:" + normalize(code);
        }
        if (name != null) {
            return prefix + ":name:" + normalize(name);
        }
        return null;
    }

    private String normalizeAcademicRoleName(String value) {
        return defaultIfBlank(normalizeUpper(value), "ALUNO");
    }

    private String normalizeEnrollmentStatus(String value) {
        if (value == null) {
            return null;
        }

        String normalized = normalize(value);
        if (normalized.contains("andamento") || normalized.contains("ativo") || normalized.contains("matric")) {
            return "Em andamento";
        }
        if (normalized.contains("realizado") || normalized.contains("conclu") || normalized.contains("final")) {
            return "Realizado";
        }
        if (normalized.contains("naorealizado") || normalized.contains("cancel") || normalized.contains("deslig") || normalized.contains("inativo")) {
            return "Nao-realizado";
        }
        return value.trim();
    }

    private String inferAcronym(String name) {
        if (name == null) {
            return null;
        }

        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1 && name.length() <= 8) {
            return name.trim().toUpperCase(Locale.ROOT);
        }

        StringBuilder acronym = new StringBuilder();
        for (String part : parts) {
            if (!part.isBlank()) {
                acronym.append(Character.toUpperCase(part.charAt(0)));
            }
        }
        return acronym.toString();
    }

    private String inferState(String locality) {
        if (locality == null) {
            return null;
        }

        String[] parts = locality.split("-");
        if (parts.length < 2) {
            return null;
        }

        String state = parts[parts.length - 1].trim();
        return state.length() <= 2 ? state.toUpperCase(Locale.ROOT) : state;
    }

    private String onlyDigits(String value) {
        if (value == null) {
            return null;
        }
        String digits = value.replaceAll("\\D+", "");
        return digits.isBlank() ? null : digits;
    }

    private String normalizeUpper(String value) {
        if (value == null) {
            return null;
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toUpperCase(Locale.ROOT);
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String toTitleCase(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String lower = value.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    @SafeVarargs
    private <T> T firstNonNull(T... values) {
        for (T value : values) {
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String defaultIfBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private <T> void setIfPresent(T value, java.util.function.Consumer<T> consumer) {
        if (value != null) {
            consumer.accept(value);
        }
    }

    private record ResolvedEntity<T>(T entity, boolean created) {
    }
}

