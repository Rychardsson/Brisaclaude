package com.example.brisa.repositories;

import com.example.brisa.models.ProjectGroupMeetingModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ProjectGroupMeetingRepository extends JpaRepository<ProjectGroupMeetingModel, Long> {
    List<ProjectGroupMeetingModel> findByProjectGroupIdOrderByMeetingDateAsc(Long projectGroupId);

    List<ProjectGroupMeetingModel> findByProjectGroupIdAndMeetingDateBetween(Long projectGroupId, LocalDate startDate, LocalDate endDate);

    void deleteByProjectGroupId(Long projectGroupId);
}
