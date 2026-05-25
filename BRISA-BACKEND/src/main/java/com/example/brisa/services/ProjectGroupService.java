package com.example.brisa.services;

import com.example.brisa.dtos.ProjectGroupCreateRequestDTO;
import com.example.brisa.dtos.ProjectGroupDetailResponseDTO;
import com.example.brisa.dtos.ProjectGroupResponseDTO;
import com.example.brisa.enums.AdvisorRoleType;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.AdvisorModel;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.EnrollmentModel;
import com.example.brisa.models.InstitutionModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.PeopleProjectGroupModel;
import com.example.brisa.models.ProjectGroupMeetingModel;
import com.example.brisa.models.ProjectGroupModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.models.roles.AcademicRoleModel;
import com.example.brisa.repositories.AcademicRoleRepository;
import com.example.brisa.repositories.AdvisorRepository;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.EnrollmentRepository;
import com.example.brisa.repositories.InstitutionRepository;
import com.example.brisa.repositories.PeopleProjectGroupRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.ProjectGroupMeetingRepository;
import com.example.brisa.repositories.ProjectGroupRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectGroupService {

    private final ProjectGroupRepository projectGroupRepository;
    private final ProjectGroupMeetingRepository projectGroupMeetingRepository;
    private final ClassRepository classRepository;
    private final PeopleRepository peopleRepository;
    private final InstitutionRepository institutionRepository;
    private final StageCandidateRepository stageCandidateRepository;
    private final PeopleProjectGroupRepository peopleProjectGroupRepository;
    private final AdvisorRepository advisorRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AcademicRoleRepository academicRoleRepository;

    @Transactional
    public ProjectGroupResponseDTO createProjectGroup(Long classId, ProjectGroupCreateRequestDTO requestDTO) {
        // Validar turma
        ClassModel classModel = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));

        PeopleModel leader = resolveLeader(requestDTO.getLeaderId(), classModel);

        // Validar empresa/instituição parceira (obrigatória)
        if (requestDTO.getProjectCompanyId() == null) {
            throw new ValidationException(java.util.List.of("Empresa/Instituição parceira é obrigatória"));
        }
        InstitutionModel company = institutionRepository.findById(requestDTO.getProjectCompanyId())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa/Instituição não encontrada"));
        validateProgramPartnerInstitution(classModel, company);

        // sponsorCompany agora é derivada da empresa/instituição parceira selecionada
        String sponsorCompany = company.getName();

        // Validar alunos (devem estar na etapa de imersão)
        List<PeopleModel> members = new java.util.ArrayList<>();
        if (requestDTO.getMemberIds() != null && !requestDTO.getMemberIds().isEmpty()) {
            // Buscar candidates da etapa de imersão desta classe
            List<StageCandidateModel> immersionCandidates = stageCandidateRepository.findByClassIdWithPeople(classId).stream()
                    .filter(this::isImmersionCandidate)
                    .collect(Collectors.toList());

            Set<Long> immersionPeopleIds = immersionCandidates.stream()
                    .map(sc -> sc.getPeople().getId())
                    .collect(Collectors.toSet());

            for (Long memberId : requestDTO.getMemberIds()) {
                if (!immersionPeopleIds.contains(memberId)) {
                    throw new ValidationException(java.util.List.of("Aluno " + memberId + " não está na etapa de imersão"));
                }
                PeopleModel member = peopleRepository.findById(memberId)
                        .orElseThrow(() -> new ResourceNotFoundException("Aluno não encontrado"));
                members.add(member);
            }
        }

        // Validar datas
        LocalDate now = LocalDate.now();
        if (requestDTO.getFirstMeetingDate().isBefore(now)) {
            throw new ValidationException(java.util.List.of("Data da primeira reunião não pode ser anterior a hoje"));
        }

        // Criar grupo
        ProjectGroupModel projectGroup = new ProjectGroupModel();
        projectGroup.setProjectTheme(requestDTO.getProjectTheme());
        projectGroup.setDescription(requestDTO.getDescription());
        projectGroup.setProjectCompany(company);
        projectGroup.setSponsorCompany(sponsorCompany);
        projectGroup.setLeader(leader);
        projectGroup.setClassModel(classModel);
        projectGroup.setRepositoryLink(requestDTO.getRepositoryLink());
        projectGroup.setWeeklyMeetingDay(requestDTO.getWeeklyMeetingDay());
        projectGroup.setFirstMeetingDate(requestDTO.getFirstMeetingDate());

        ProjectGroupModel savedGroup = projectGroupRepository.save(projectGroup);

        // Adicionar membros
        for (PeopleModel member : members) {
            PeopleProjectGroupModel membership = new PeopleProjectGroupModel();
            membership.setPeople(member);
            membership.setProjectGroup(savedGroup);
            peopleProjectGroupRepository.save(membership);
        }

        // Gerar reuniões semanais
        generateWeeklyMeetings(savedGroup);

        return mapToResponseDTO(savedGroup);
    }

    @Transactional
    public void generateWeeklyMeetings(ProjectGroupModel projectGroup) {
        LocalDate startDate = projectGroup.getFirstMeetingDate();
        LocalDate endDate = projectGroup.getClassModel().getEndDate();
        DayOfWeek targetDay = projectGroup.getWeeklyMeetingDay();

        if (endDate == null) {
            throw new ValidationException(java.util.List.of("Turma deve ter data de término definida"));
        }

        // Gerar reuniões para cada semana até o fim da turma
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            if (currentDate.getDayOfWeek().equals(targetDay)) {
                ProjectGroupMeetingModel meeting = new ProjectGroupMeetingModel();
                meeting.setProjectGroup(projectGroup);
                meeting.setMeetingDate(currentDate);
                meeting.setStatus("SCHEDULED");
                projectGroupMeetingRepository.save(meeting);
            }
            currentDate = currentDate.plusDays(1);
        }
    }

    private PeopleModel resolveLeader(Long leaderId, ClassModel classModel) {
        if (leaderId == null) {
            throw new ValidationException(java.util.List.of("Selecione um orientador"));
        }

        AdvisorModel advisor = advisorRepository.findById(leaderId).orElse(null);
        PeopleModel leader = advisor != null
                ? resolvePeopleFromAdvisor(advisor)
                : peopleRepository.findById(leaderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Orientador não encontrado"));

        ensureLeaderEnrollment(leader, classModel, advisor != null ? advisor.getRoleType() : AdvisorRoleType.ORIENTADOR);
        return leader;
    }

    private PeopleModel resolvePeopleFromAdvisor(AdvisorModel advisor) {
        String cpf = normalizeCpf(advisor.getCpf());
        PeopleModel person = null;

        if (!cpf.isBlank()) {
            person = peopleRepository.findByCpf(cpf).orElse(null);
        }

        String email = normalizeEmail(advisor.getEmail(), cpf);
        if (person == null && !email.isBlank()) {
            person = peopleRepository.findByEmailIgnoreCase(email).orElse(null);
        }

        if (person == null) {
            person = new PeopleModel();
            person.setCpf(cpf);
            person.setEmail(email);
        }

        person.setName(advisor.getName());
        person.setCpf(cpf);
        person.setEmail(email);
        person.setEducationLevel(defaultIfBlank(advisor.getFormation(), person.getEducationLevel()));
        if (advisor.getBirthDate() != null) {
            person.setBirthDate(advisor.getBirthDate());
        }
        if (Boolean.TRUE.equals(person.getSoftDeleted())) {
            person.setSoftDeleted(false);
        }

        return peopleRepository.save(person);
    }

    private void ensureLeaderEnrollment(PeopleModel leader, ClassModel classModel, AdvisorRoleType roleType) {
        if (leader == null || classModel == null) {
            return;
        }

        String roleName = roleType == AdvisorRoleType.GESTOR ? "GESTOR" : "ORIENTADOR";
        AcademicRoleModel role = academicRoleRepository.findByName(roleName)
                .orElseGet(() -> academicRoleRepository.save(new AcademicRoleModel(roleName, roleName.equals("GESTOR") ? "Gestor" : "Orientador")));

        enrollmentRepository.findByPeopleIdAndClassModelIdAndAcademicRoleId(leader.getId(), classModel.getId(), role.getId())
                .orElseGet(() -> {
                    EnrollmentModel enrollment = new EnrollmentModel();
                    enrollment.setPeople(leader);
                    enrollment.setClassModel(classModel);
                    enrollment.setAcademicRole(role);
                    enrollment.setEnrollmentDate(LocalDate.now());
                    enrollment.setStatus("ATIVA");
                    return enrollmentRepository.save(enrollment);
                });
    }

    private String normalizeCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    private String normalizeEmail(String email, String cpf) {
        String trimmed = email == null ? "" : email.trim().toLowerCase();
        if (trimmed.contains("@")) {
            return trimmed;
        }
        return "academico-" + (cpf == null || cpf.isBlank() ? "sem-cpf" : cpf) + "@brisa.local";
    }

    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isBlank() ? fallback : value.trim();
    }

    private boolean isImmersionCandidate(StageCandidateModel candidate) {
        return candidate != null
                && candidate.getStage() != null
                && normalizeStageName(candidate.getStage().getName()).contains("imers");
    }

    private String normalizeStageName(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
    }

    @Transactional(readOnly = true)
    public List<ProjectGroupResponseDTO> getGroupsByClass(Long classId) {
        List<ProjectGroupModel> groups = projectGroupRepository.findAll().stream()
                .filter(g -> g.getClassModel().getId().equals(classId))
                .collect(Collectors.toList());

        return groups.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProjectGroupDetailResponseDTO getGroupDetail(Long groupId) {
        ProjectGroupModel group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        return mapToDetailResponseDTO(group);
    }

    @Transactional
    public ProjectGroupResponseDTO updateProjectGroup(Long groupId, ProjectGroupCreateRequestDTO requestDTO) {
        ProjectGroupModel group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        // Atualizar campos básicos
        group.setProjectTheme(requestDTO.getProjectTheme());
        group.setDescription(requestDTO.getDescription());
        group.setRepositoryLink(requestDTO.getRepositoryLink());

        // Atualizar empresa/instituição
        if (requestDTO.getProjectCompanyId() != null) {
            InstitutionModel company = institutionRepository.findById(requestDTO.getProjectCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa/Instituição não encontrada"));
            validateProgramPartnerInstitution(group.getClassModel(), company);
            group.setProjectCompany(company);
            group.setSponsorCompany(company.getName());
        }

        // Atualizar orientador
        if (requestDTO.getLeaderId() != null) {
            PeopleModel leader = resolveLeader(requestDTO.getLeaderId(), group.getClassModel());
            group.setLeader(leader);
        }

        ProjectGroupModel updatedGroup = projectGroupRepository.save(group);
        return mapToResponseDTO(updatedGroup);
    }

    @Transactional
    public void deleteProjectGroup(Long groupId) {
        ProjectGroupModel group = projectGroupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo não encontrado"));

        // Deletar reuniões
        projectGroupMeetingRepository.deleteByProjectGroupId(groupId);

        // Deletar membros
        peopleProjectGroupRepository.deleteByProjectGroupId(groupId);

        // Deletar grupo
        projectGroupRepository.delete(group);
    }

    private ProjectGroupResponseDTO mapToResponseDTO(ProjectGroupModel group) {
        ProjectGroupResponseDTO dto = new ProjectGroupResponseDTO();
        dto.setId(group.getId());
        dto.setProjectTheme(group.getProjectTheme());
        dto.setDescription(group.getDescription());
        dto.setProjectCompanyName(group.getProjectCompany() != null ? group.getProjectCompany().getName() : null);
        dto.setProjectCompanyId(group.getProjectCompany() != null ? group.getProjectCompany().getId() : null);
        dto.setSponsorCompany(group.getSponsorCompany());
        dto.setLeaderName(group.getLeader() != null ? group.getLeader().getName() : null);
        dto.setLeaderId(group.getLeader() != null ? group.getLeader().getId() : null);
        dto.setMemberCount(group.getMembers() != null ? group.getMembers().size() : 0);
        dto.setWeeklyMeetingDay(group.getWeeklyMeetingDay());
        dto.setFirstMeetingDate(group.getFirstMeetingDate());
        dto.setRepositoryLink(group.getRepositoryLink());

        // Contar reuniões futuras
        LocalDate now = LocalDate.now();
        long upcomingCount = group.getMeetings().stream()
                .filter(m -> m.getMeetingDate().isAfter(now) && "SCHEDULED".equals(m.getStatus()))
                .count();
        dto.setUpcomingMeetingsCount((int) upcomingCount);

        return dto;
    }

    private ProjectGroupDetailResponseDTO mapToDetailResponseDTO(ProjectGroupModel group) {
        ProjectGroupDetailResponseDTO dto = new ProjectGroupDetailResponseDTO();
        dto.setId(group.getId());
        dto.setProjectTheme(group.getProjectTheme());
        dto.setDescription(group.getDescription());
        dto.setProjectCompanyName(group.getProjectCompany() != null ? group.getProjectCompany().getName() : null);
        dto.setProjectCompanyId(group.getProjectCompany() != null ? group.getProjectCompany().getId() : null);
        dto.setSponsorCompany(group.getSponsorCompany());
        dto.setLeaderName(group.getLeader() != null ? group.getLeader().getName() : null);
        dto.setLeaderId(group.getLeader() != null ? group.getLeader().getId() : null);
        dto.setWeeklyMeetingDay(group.getWeeklyMeetingDay());
        dto.setFirstMeetingDate(group.getFirstMeetingDate());
        dto.setRepositoryLink(group.getRepositoryLink());

        // Mapear membros
        List<ProjectGroupDetailResponseDTO.GroupMemberDTO> memberDTOs = group.getMembers().stream()
                .map(m -> new ProjectGroupDetailResponseDTO.GroupMemberDTO(
                        m.getPeople().getId(),
                        m.getPeople().getName(),
                        m.getPeople().getEmail()
                ))
                .collect(Collectors.toList());
        dto.setMembers(memberDTOs);

        // Mapear reuniões
        List<ProjectGroupDetailResponseDTO.GroupMeetingDTO> meetingDTOs = group.getMeetings().stream()
                .map(m -> new ProjectGroupDetailResponseDTO.GroupMeetingDTO(
                        m.getId(),
                        m.getMeetingDate(),
                        m.getStatus()
                ))
                .collect(Collectors.toList());
        dto.setMeetings(meetingDTOs);

        return dto;
    }

    private void validateProgramPartnerInstitution(ClassModel classModel, InstitutionModel institution) {
        if (classModel == null || classModel.getProgram() == null) {
            throw new ValidationException(java.util.List.of("Turma sem programa vinculado para validar empresa/instituição parceira."));
        }

        Set<Long> partnerIds = classModel.getProgram().getProgramInstitutions().stream()
                .map(membership -> membership.getInstitution())
                .filter(Objects::nonNull)
                .map(InstitutionModel::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (partnerIds.isEmpty()) {
            throw new ValidationException(java.util.List.of("O programa da turma não possui empresas/instituições parceiras cadastradas."));
        }

        if (institution == null || institution.getId() == null || !partnerIds.contains(institution.getId())) {
            throw new ValidationException(java.util.List.of("A empresa/instituição selecionada não está cadastrada como parceira deste programa."));
        }
    }
}
