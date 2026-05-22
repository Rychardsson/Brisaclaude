package com.example.brisa.services;

import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.models.ProgramModel;
import com.example.brisa.repositories.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProgramService {
    
    private final ProgramRepository programRepository;

    public List<ProgramModel> findAll() {
        return programRepository.findAll();
    }

    public ProgramModel findById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Programa não encontrado com id: " + id));
    }

    @Transactional
    public ProgramModel create(ProgramModel program) {
        return programRepository.save(program);
    }

    @Transactional
    public ProgramModel update(Long id, ProgramModel programDetails) {
        ProgramModel program = findById(id);
        
        program.setCode(programDetails.getCode());
        program.setName(programDetails.getName());
        program.setContractNumber(programDetails.getContractNumber());
        program.setExecutorName(programDetails.getExecutorName());
        program.setFundingEntity(programDetails.getFundingEntity());
        program.setGeneralCoordinator(programDetails.getGeneralCoordinator());
        program.setProgramValue(programDetails.getProgramValue());
        program.setStartDate(programDetails.getStartDate());
        program.setEndDate(programDetails.getEndDate());
        program.setTargetAudience(programDetails.getTargetAudience());
        program.setLevelingModality(programDetails.getLevelingModality());
        program.setLevelingDuration(programDetails.getLevelingDuration());
        program.setImmersionDuration(programDetails.getImmersionDuration());
        program.setImmersionWorkloadHours(programDetails.getImmersionWorkloadHours());
        program.setQuotaCriteria(programDetails.getQuotaCriteria());
        program.setEvaluationCriteria(programDetails.getEvaluationCriteria());
        program.setSupportEmail(programDetails.getSupportEmail());
        program.setOfficialWebsite(programDetails.getOfficialWebsite());
        program.setMainLocality(programDetails.getMainLocality());
        program.setObservations(programDetails.getObservations());
        program.setPartnerNames(programDetails.getPartnerNames());
        
        return programRepository.save(program);
    }

    @Transactional
    public void delete(Long id) {
        ProgramModel program = findById(id);
        programRepository.delete(program);
    }
}
