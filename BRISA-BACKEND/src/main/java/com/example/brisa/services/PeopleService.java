package com.example.brisa.services;

import com.example.brisa.dtos.people.PeopleImportDTO;
import com.example.brisa.dtos.people.PeopleImportResponseDTO;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.repositories.PeopleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
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
        List<PeopleImportDTO> peopleList = parseExcelFile(file);
        return importPeopleInBatches(peopleList);
    }
    
    private List<PeopleImportDTO> parseExcelFile(MultipartFile file) throws IOException {
        List<PeopleImportDTO> peopleList = new ArrayList<>();
        
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // Começa da linha 1 (pula o cabeçalho na linha 0)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                PeopleImportDTO person = new PeopleImportDTO();
                
                // Coluna A (0): Nome
                person.setName(getCellValueAsString(row.getCell(0)));
                
                // Coluna B (1): Email
                person.setEmail(getCellValueAsString(row.getCell(1)));

                // Coluna C (2): Cpf
                person.setCpf(getCellValueAsString(row.getCell(2)));

                // Coluna D (3): Data de Nascimento
                person.setBirthDate(parseDateFromCell(row.getCell(3)));

                // Coluna F (5): Gênero
                person.setGender(getCellValueAsString(row.getCell(5)));

                // Coluna G (6): CEP
                person.setAddress(getCellValueAsString(row.getCell(6)));

                // Adiciona apenas se tiver nome e email
                if (person.getName() != null && !person.getName().isEmpty() 
                    && person.getEmail() != null && !person.getEmail().isEmpty()) {
                    
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
        int successfullyInserted = 0;
        
        // Extrai todos os emails de uma vez
        List<String> allEmails = peopleList.stream()
                .map(PeopleImportDTO::getEmail)
                .filter(email -> email != null && !email.isEmpty())
                .distinct()
                .toList();
        
        // Busca todos os emails existentes de uma só vez (1 query ao invés de N queries)
        List<PeopleModel> existingPeople = peopleRepository.findAllByEmailIn(allEmails);
        java.util.Set<String> existingEmails = existingPeople.stream()
                .map(PeopleModel::getEmail)
                .collect(java.util.stream.Collectors.toSet());
        
        // Processa em lotes
        List<PeopleModel> peopleToInsert = new ArrayList<>();
        
        for (PeopleImportDTO personDTO : peopleList) {
            // Verifica se já existe usando o Set (muito mais rápido que consultar o banco)
            if (existingEmails.contains(personDTO.getEmail())) {
                duplicatePersons.add(personDTO.getName());
            } else {
                // Prepara a pessoa para inserção
                PeopleModel person = new PeopleModel();
                person.setName(personDTO.getName());
                person.setEmail(personDTO.getEmail());
                person.setCpf(personDTO.getCpf());
                person.setEducationLevel(personDTO.getEducationLevel());
                person.setAddress(personDTO.getAddress());
                person.setCity(personDTO.getCity());
                person.setGender(personDTO.getGender());
                person.setBirthDate(personDTO.getBirthDate());
                
                peopleToInsert.add(person);
                existingEmails.add(personDTO.getEmail()); // Evita duplicatas dentro do próprio lote
                
                // Salva em lotes para otimizar performance
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
        response.setAlreadyExists(duplicatePersons.size());
        response.setDuplicatePersons(duplicatePersons);
        
        return response;
    }
}
