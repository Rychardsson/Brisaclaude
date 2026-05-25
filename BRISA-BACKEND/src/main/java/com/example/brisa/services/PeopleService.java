package com.example.brisa.services;

import com.example.brisa.dtos.people.PeopleCreateLinkRequestDTO;
import com.example.brisa.dtos.people.PeopleImportDTO;
import com.example.brisa.dtos.people.PeopleImportResponseDTO;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.repositories.PeopleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PeopleService {
    
    private final PeopleRepository peopleRepository;
    private final PeopleIntegrationService peopleIntegrationService;
    private final ExcelImportHelper excelImportHelper;
    
    private static final int BATCH_SIZE = 500; // Processar em lotes de 500 registros

    public List<PeopleModel> findAll() {
        return peopleRepository.findAllActiveOrderByNameAsc();
    }

    public PeopleModel findById(Long id) {
        return peopleRepository.findActiveById(id)
                .orElseThrow(() -> new com.example.brisa.exceptions.ResourceNotFoundException("Pessoa não encontrada com id: " + id));
    }

    @Transactional
    public PeopleModel create(PeopleModel people) {
        if (people.getSoftDeleted() == null) {
            people.setSoftDeleted(false);
        }
        return peopleRepository.save(people);
    }

    @Transactional
    public PeopleModel update(Long id, PeopleModel peopleDetails) {
        PeopleModel people = findById(id);
        
        people.setName(peopleDetails.getName());
        people.setEmail(peopleDetails.getEmail());
        people.setCpf(peopleDetails.getCpf());
        people.setEducationLevel(peopleDetails.getEducationLevel());
        people.setAddress(peopleDetails.getAddress());
        people.setAddressExtra(peopleDetails.getAddressExtra());
        people.setCity(peopleDetails.getCity());
        people.setState(peopleDetails.getState());
        people.setGender(peopleDetails.getGender());
        people.setQuotaCategory(peopleDetails.getQuotaCategory());
        people.setPhone(peopleDetails.getPhone());
        people.setLinkedin(peopleDetails.getLinkedin());
        people.setZipCode(peopleDetails.getZipCode());
        people.setBirthDate(peopleDetails.getBirthDate());
        people.setInstitutionName(peopleDetails.getInstitutionName());
        people.setCourseName(peopleDetails.getCourseName());
        people.setEducationStatus(peopleDetails.getEducationStatus());
        people.setEducationCompletionDate(peopleDetails.getEducationCompletionDate());
        
        return peopleRepository.save(people);
    }

    @Transactional
    public void delete(Long id) {
        PeopleModel people = findById(id);
        people.setSoftDeleted(true);
        peopleRepository.save(people);
    }
    
    @Transactional
    public PeopleImportResponseDTO importPeopleFromExcel(MultipartFile file) throws IOException {
        return importPeopleFromExcel(file, null, null, null, null);
    }

    @Transactional
    public PeopleImportResponseDTO importPeopleFromExcel(
            MultipartFile file,
            Long programaId,
            Long turmaId,
            Long etapaId,
            String statusInicial
    ) throws IOException {
        List<PeopleImportDTO> peopleList = parseExcelFile(file);
        peopleList.forEach(person -> {
            if (person.getProgramaId() == null) person.setProgramaId(programaId);
            if (person.getTurmaId() == null) person.setTurmaId(turmaId);
            if (person.getEtapaId() == null) person.setEtapaId(etapaId);
            if (isBlank(person.getStatusInicial())) person.setStatusInicial(statusInicial);
        });
        return importPeopleInBatches(peopleList);
    }
    
    private List<PeopleImportDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<PeopleImportDTO> peopleList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                return peopleList;
            }

            java.util.Map<String, Integer> headers = excelImportHelper.mapHeaders(sheet.getRow(0));
            Integer idxName = excelImportHelper.findColumn(headers, List.of("nome do aluno", "nome completo", "nome", "name"), 0);
            Integer idxBirthDate = excelImportHelper.findColumn(headers, List.of("data de nascimento", "nascimento", "birth date", "birthdate"), 1);
            Integer idxGender = excelImportHelper.findColumn(headers, List.of("genero", "gênero", "gender"), 2);
            Integer idxQuota = excelImportHelper.findColumn(headers, List.of("cota", "quota"), 3);
            Integer idxCpf = excelImportHelper.findColumn(headers, List.of("cpf"), 4);
            Integer idxEmail = excelImportHelper.findColumn(headers, List.of("e-mail", "email"), 5);
            Integer idxPhone = excelImportHelper.findColumn(headers, List.of("telefone", "phone"), 6);
            Integer idxState = excelImportHelper.findColumn(headers, List.of("estado de residencia", "estado de residência", "estado", "uf", "state"), 7);
            Integer idxCity = excelImportHelper.findColumn(headers, List.of("cidade de residencia", "cidade de residência", "cidade", "city"), 8);
            Integer idxEducationLevel = excelImportHelper.findColumn(headers, List.of("tipo de formacao", "tipo de formação", "formacao", "formação", "educationlevel"), 9);
            Integer idxInstitution = excelImportHelper.findColumn(headers, List.of("instituicao", "instituição", "institution"), 10);
            Integer idxCourse = excelImportHelper.findColumn(headers, List.of("curso", "course"), 11);
            Integer idxEducationStatus = excelImportHelper.findColumn(headers, List.of("status da formacao", "status da formação", "educationstatus"), 12);
            Integer idxCompletionDate = excelImportHelper.findColumn(headers, List.of("data de conclusao", "data de conclusão", "completiondate"), 13);
            Integer idxProgram = excelImportHelper.findColumn(headers, List.of("programa id", "programaid", "programa"), 14);
            Integer idxClass = excelImportHelper.findColumn(headers, List.of("turma id", "turmaid", "turma"), 15);
            Integer idxStage = excelImportHelper.findColumn(headers, List.of("etapa inicial id", "etapa id", "etapa inicial", "etapa"), 16);
            Integer idxInitialStatus = excelImportHelper.findColumn(headers, List.of("status inicial", "status"), 17);
            
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (excelImportHelper.isRowEmpty(row)) continue;
                
                PeopleImportDTO person = new PeopleImportDTO();
                person.setName(excelImportHelper.getString(row, idxName));
                
                // Pega a data pelo Helper. Se falhar, tentamos o método manual da própria classe.
                LocalDate birthDate = excelImportHelper.getDate(row, idxBirthDate);
                if (birthDate == null && idxBirthDate != null) {
                     birthDate = parseDateFromCell(row.getCell(idxBirthDate));
                }
                person.setBirthDate(birthDate);

                person.setGender(excelImportHelper.getString(row, idxGender));
                person.setQuotaCategory(excelImportHelper.getString(row, idxQuota));
                person.setCpf(onlyDigits(excelImportHelper.getString(row, idxCpf)));
                person.setEmail(normalizeEmail(excelImportHelper.getString(row, idxEmail)));
                person.setPhone(excelImportHelper.getString(row, idxPhone));
                person.setState(excelImportHelper.getString(row, idxState));
                person.setCity(excelImportHelper.getString(row, idxCity));
                person.setEducationLevel(excelImportHelper.getString(row, idxEducationLevel));
                person.setInstitutionName(excelImportHelper.getString(row, idxInstitution));
                person.setCourseName(excelImportHelper.getString(row, idxCourse));
                person.setEducationStatus(excelImportHelper.getString(row, idxEducationStatus));
                
                LocalDate completionDate = excelImportHelper.getDate(row, idxCompletionDate);
                if (completionDate == null && idxCompletionDate != null) {
                     completionDate = parseDateFromCell(row.getCell(idxCompletionDate));
                }
                person.setEducationCompletionDate(completionDate);

                person.setProgramaId(parseLong(excelImportHelper.getString(row, idxProgram)));
                person.setTurmaId(parseLong(excelImportHelper.getString(row, idxClass)));
                person.setEtapaId(parseLong(excelImportHelper.getString(row, idxStage)));
                person.setStatusInicial(excelImportHelper.getString(row, idxInitialStatus));

                if (!isBlank(person.getName()) && !isBlank(person.getEmail())) {
                    peopleList.add(person);
                }   
            }
        }
        
        return peopleList;
    }
    
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> null;
        };
    }
    
    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) return null;
        
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        
        return null;
    }

    private LocalDate parseDateFromCell(Cell cell) {
        LocalDate date = getCellValueAsDate(cell);
        if (date != null) return date;

        String raw = getCellValueAsString(cell);
        if (raw == null || raw.trim().isEmpty()) return null;

        String value = raw.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(value, formatter);
            } catch (Exception ignored) {
            }
        }

        return null;
    }
    
    @Transactional
    public PeopleImportResponseDTO importPeopleInBatches(List<PeopleImportDTO> peopleList) {
        List<String> duplicatePersons = new ArrayList<>();
        List<PeopleModel> peopleToInsert = new ArrayList<>();
        int successfullyInserted = 0;
        int alreadyExists = 0;
        
        for (PeopleImportDTO personDTO : peopleList) {
            if (personDTO.getTurmaId() != null && personDTO.getEtapaId() != null) {
                try {
                    var response = peopleIntegrationService.createOrLink(toCreateLinkRequest(personDTO));
                    if (response.pessoaCriada() || response.vinculoCriado()) {
                        successfullyInserted++;
                    } else {
                        alreadyExists++;
                        duplicatePersons.add(personDTO.getName() + " (Já vinculado)");
                    }
                } catch (ValidationException ex) {
                    // CORREÇÃO: Erros de validação agora são relatados corretamente sem incrementar "alreadyExists"
                    duplicatePersons.add(personDTO.getName() + " (Erro: " + String.join(", ", ex.getErrors()) + ")");
                }
                continue;
            }

            PeopleModel existing = findExisting(personDTO);
            if (existing != null) {
                mergePerson(existing, personDTO);
                peopleRepository.save(existing);
                alreadyExists++;
                duplicatePersons.add(personDTO.getName() + " (Atualizado sem vínculo de turma)");
            } else {
                peopleToInsert.add(toPeopleModel(personDTO));

                if (peopleToInsert.size() >= BATCH_SIZE) {
                    peopleRepository.saveAll(peopleToInsert);
                    successfullyInserted += peopleToInsert.size();
                    peopleToInsert.clear();
                }
            }
        }
        
        // Salva o restante que não completou um lote
        if (!peopleToInsert.isEmpty()) {
            peopleRepository.saveAll(peopleToInsert);
            successfullyInserted += peopleToInsert.size();
        }
        
        // Monta a resposta com estatísticas da importação
        PeopleImportResponseDTO response = new PeopleImportResponseDTO();
        response.setTotalProcessed(peopleList.size());
        response.setSuccessfullyInserted(successfullyInserted);
        response.setAlreadyExists(alreadyExists);
        response.setDuplicatePersons(duplicatePersons);
        
        return response;
    }

    private PeopleCreateLinkRequestDTO toCreateLinkRequest(PeopleImportDTO personDTO) {
        PeopleCreateLinkRequestDTO request = new PeopleCreateLinkRequestDTO();
        request.setProgramaId(personDTO.getProgramaId());
        request.setTurmaId(personDTO.getTurmaId());
        request.setEtapaId(personDTO.getEtapaId());
        request.setStatusInicial(defaultIfBlank(personDTO.getStatusInicial(), "Inscrito"));
        request.setNome(personDTO.getName());
        request.setDataNascimento(personDTO.getBirthDate());
        request.setGenero(personDTO.getGender());
        request.setCpf(personDTO.getCpf());
        request.setEmail(personDTO.getEmail());
        request.setTelefone(personDTO.getPhone());
        request.setLinkedin(personDTO.getLinkedin());
        request.setEndereco(personDTO.getAddress());
        request.setEstado(personDTO.getState());
        request.setCidade(personDTO.getCity());
        request.setCep(personDTO.getZipCode());
        request.setComplementoEndereco(personDTO.getAddressExtra());
        request.setInstituicao(personDTO.getInstitutionName());
        request.setCurso(personDTO.getCourseName());
        request.setDataConclusao(personDTO.getEducationCompletionDate());

        // --- HIGIENIZAÇÃO DOS DADOS PARA BATER COM OS ENUMS DO SISTEMA ---
        
        // Trata Status Formação
        String statusFormacao = defaultIfBlank(personDTO.getEducationStatus(), "");
        if (statusFormacao.toLowerCase().contains("conclu")) {
            request.setStatusFormacao("Concluido"); // Removendo o acento para bater com STATUS_FORMACAO_OPTIONS
        } else {
            request.setStatusFormacao(statusFormacao);
        }

        // Trata Tipo de Formação
        String tipoFormacao = defaultIfBlank(personDTO.getEducationLevel(), "");
        if (tipoFormacao.toLowerCase().contains("tic") || tipoFormacao.toLowerCase().contains("computa")) {
            request.setTipoFormacao("Computacao ou cursos relacionados a TIC");
        } else {
            request.setTipoFormacao(tipoFormacao);
        }

        // Trata Cota
        String cota = defaultIfBlank(personDTO.getQuotaCategory(), "");
        if (cota.equalsIgnoreCase("Cota Racial")) {
            request.setCota("Negro/Pardo");
        } else if (cota.equalsIgnoreCase("Escola Pública")) {
            request.setCota("Ampla Concorrencia"); // "Escola Pública" não existe no Enum, caindo para Ampla Concorrência (pode ser ajustado)
        } else {
            request.setCota(cota);
        }

        return request;
    }

    private PeopleModel toPeopleModel(PeopleImportDTO personDTO) {
        PeopleModel person = new PeopleModel();
        mergePerson(person, personDTO);
        if (person.getSoftDeleted() == null) {
            person.setSoftDeleted(false);
        }
        return person;
    }

    private void mergePerson(PeopleModel person, PeopleImportDTO personDTO) {
        person.setName(defaultIfBlank(personDTO.getName(), person.getName()));
        person.setEmail(defaultIfBlank(personDTO.getEmail(), person.getEmail()));
        person.setCpf(defaultIfBlank(personDTO.getCpf(), person.getCpf()));
        person.setEducationLevel(defaultIfBlank(personDTO.getEducationLevel(), person.getEducationLevel()));
        person.setAddress(defaultIfBlank(personDTO.getAddress(), person.getAddress()));
        person.setAddressExtra(defaultIfBlank(personDTO.getAddressExtra(), person.getAddressExtra()));
        person.setState(defaultIfBlank(personDTO.getState(), person.getState()));
        person.setCity(defaultIfBlank(personDTO.getCity(), person.getCity()));
        person.setGender(defaultIfBlank(personDTO.getGender(), person.getGender()));
        person.setQuotaCategory(defaultIfBlank(personDTO.getQuotaCategory(), person.getQuotaCategory()));
        person.setPhone(defaultIfBlank(personDTO.getPhone(), person.getPhone()));
        person.setLinkedin(defaultIfBlank(personDTO.getLinkedin(), person.getLinkedin()));
        person.setZipCode(defaultIfBlank(personDTO.getZipCode(), person.getZipCode()));
        person.setInstitutionName(defaultIfBlank(personDTO.getInstitutionName(), person.getInstitutionName()));
        person.setCourseName(defaultIfBlank(personDTO.getCourseName(), person.getCourseName()));
        person.setEducationStatus(defaultIfBlank(personDTO.getEducationStatus(), person.getEducationStatus()));
        if (personDTO.getBirthDate() != null) person.setBirthDate(personDTO.getBirthDate());
        if (personDTO.getEducationCompletionDate() != null) person.setEducationCompletionDate(personDTO.getEducationCompletionDate());
        if (Boolean.TRUE.equals(person.getSoftDeleted())) person.setSoftDeleted(false);
    }

    private PeopleModel findExisting(PeopleImportDTO personDTO) {
        if (!isBlank(personDTO.getCpf())) {
            var byCpf = peopleRepository.findByCpf(personDTO.getCpf());
            if (byCpf.isPresent()) return byCpf.get();
        }
        if (!isBlank(personDTO.getEmail())) {
            var byEmail = peopleRepository.findByEmail(personDTO.getEmail());
            if (byEmail.isPresent()) return byEmail.get();
        }
        return null;
    }

    private String normalizeEmail(String value) {
        return isBlank(value) ? null : value.trim().toLowerCase();
    }

    private String onlyDigits(String value) {
        if (value == null) return null;
        String digits = value.replaceAll("\\D+", "");
        return digits.isBlank() ? null : digits;
    }

    private Long parseLong(String value) {
        if (isBlank(value)) return null;
        try {
            return Long.parseLong(value.replaceAll("\\D+", ""));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String defaultIfBlank(String value, String fallback) {
        return isBlank(value) ? fallback : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}