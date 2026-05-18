package com.example.brisa.services;

import com.example.brisa.dtos.ProjectGroupCreateRequestDTO;
import com.example.brisa.dtos.ProjectGroupDetailResponseDTO;
import com.example.brisa.dtos.ProjectGroupResponseDTO;
import com.example.brisa.exceptions.ResourceNotFoundException;
import com.example.brisa.exceptions.ValidationException;
import com.example.brisa.models.ClassModel;
import com.example.brisa.models.InstitutionModel;
import com.example.brisa.models.PeopleModel;
import com.example.brisa.models.PeopleProjectGroupModel;
import com.example.brisa.models.ProjectGroupMeetingModel;
import com.example.brisa.models.ProjectGroupModel;
import com.example.brisa.models.StageCandidateModel;
import com.example.brisa.repositories.ClassRepository;
import com.example.brisa.repositories.InstitutionRepository;
import com.example.brisa.repositories.PeopleProjectGroupRepository;
import com.example.brisa.repositories.PeopleRepository;
import com.example.brisa.repositories.ProjectGroupMeetingRepository;
import com.example.brisa.repositories.ProjectGroupRepository;
import com.example.brisa.repositories.StageCandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
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

    @Transactional
    public ProjectGroupResponseDTO createProjectGroup(Long classId, ProjectGroupCreateRequestDTO requestDTO) {
        // Validar turma
        ClassModel classModel = classRepository.findById(classId)
                .orElseThrow(() -> new ResourceNotFoundException("Turma não encontrada"));

        // Validar orientador
        PeopleModel leader = peopleRepository.findById(requestDTO.getLeaderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orientador não encontrado"));

        // Validar empresa/instituição (opcional)
        InstitutionModel company = null;
        if (requestDTO.getProjectCompanyId() != null) {
            company = institutionRepository.findById(requestDTO.getProjectCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Empresa/Instituição não encontrada"));
        }

        // Validar alunos (devem estar na etapa de imersão)
        List<PeopleModel> members = new java.util.ArrayList<>();
        if (requestDTO.getMemberIds() != null && !requestDTO.getMemberIds().isEmpty()) {
            // Buscar candidates da etapa de imersão desta classe
            List<StageCandidateModel> immersionCandidates = stageCandidateRepository.findAll().stream()
                    .filter(sc -> sc.getStage().getClassModel().getId().equals(classId)
                            && "imersao".equalsIgnoreCase(sc.getStage().getName()))
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
            group.setProjectCompany(company);
        }

        // Atualizar orientador
        if (requestDTO.getLeaderId() != null) {
            PeopleModel leader = peopleRepository.findById(requestDTO.getLeaderId())
                    .orElseThrow(() -> new ResourceNotFoundException("Orientador não encontrado"));
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
}
