package com.example.brisa.services;

import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.PeopleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final PeopleRepository peopleRepository;
    private final ClassRepository classRepository;
    private final AcademicRoleRepository academicRoleRepository;
    

    public List<EnrollmentModel> findAll() {
        return enrollmentRepository.findAll();
    }

    public EnrollmentModel findById(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Matrícula não encontrada com id: " + id));
    }

    public List<EnrollmentModel> findByClassId(Long classId) {
        return enrollmentRepository.findByClassModelId(classId);
    }

    public List<EnrollmentModel> findByPeopleId(Long peopleId) {
        return enrollmentRepository.findByPeopleId(peopleId);
    }

    @Transactional
    public EnrollmentModel create(EnrollmentModel enrollment) {
        // Verifica se a pessoa existe
        if (enrollment.getPeople() != null && enrollment.getPeople().getId() != null) {
            PeopleModel people = peopleRepository.findById(enrollment.getPeople().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada"));
            enrollment.setPeople(people);
        }
        
        // Verifica se a turma existe
        if (enrollment.getClassModel() != null && enrollment.getClassModel().getId() != null) {
            ClassModel classModel = classRepository.findById(enrollment.getClassModel().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));
            enrollment.setClassModel(classModel);
        }
        
        // Verifica se o papel acadêmico existe
        if (enrollment.getAcademicRole() != null && enrollment.getAcademicRole().getId() != null) {
            AcademicRoleModel role = academicRoleRepository.findById(enrollment.getAcademicRole().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Papel acadêmico não encontrado"));
            enrollment.setAcademicRole(role);
        }
        
        EnrollmentModel saved = enrollmentRepository.save(enrollment);

        // Backfill removed: não criamos progressões automaticamente ao matricular.
        return saved;
    }

    @Transactional
    public EnrollmentModel update(Long id, EnrollmentModel enrollmentDetails) {
        EnrollmentModel enrollment = findById(id);
        
        enrollment.setEnrollmentDate(enrollmentDetails.getEnrollmentDate());
        enrollment.setStatus(enrollmentDetails.getStatus());
        enrollment.setCompletionDate(enrollmentDetails.getCompletionDate());
        enrollment.setGrade(enrollmentDetails.getGrade());
        enrollment.setFrequency(enrollmentDetails.getFrequency());
        
        if (enrollmentDetails.getAcademicRole() != null && enrollmentDetails.getAcademicRole().getId() != null) {
            AcademicRoleModel role = academicRoleRepository.findById(enrollmentDetails.getAcademicRole().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Papel acadêmico não encontrado"));
            enrollment.setAcademicRole(role);
        }
        
        return enrollmentRepository.save(enrollment);
    }

    @Transactional
    public void delete(Long id) {
        EnrollmentModel enrollment = findById(id);
        enrollmentRepository.delete(enrollment);
    }
}
