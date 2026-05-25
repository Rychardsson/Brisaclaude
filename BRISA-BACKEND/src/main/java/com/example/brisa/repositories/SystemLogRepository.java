package com.example.brisa.repositories;

import com.example.brisa.enums.LogAction;
import com.example.brisa.models.SystemLogModel;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemLogRepository extends JpaRepository<SystemLogModel, Long>, JpaSpecificationExecutor<SystemLogModel> {

    Page<SystemLogModel> findByAction(LogAction action, Pageable pageable);

    Page<SystemLogModel> findByUserId(UUID userId, Pageable pageable);

    Page<SystemLogModel> findByEntityType(String entityType, Pageable pageable);

    Page<SystemLogModel> findByEntityTypeAndEntityId(String entityType, String entityId, Pageable pageable);

    Page<SystemLogModel> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT l FROM SystemLogModel l WHERE l.user.id = :userId AND l.createdAt BETWEEN :startDate AND :endDate")
    Page<SystemLogModel> findByUserIdAndDateRange(
            @Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );

    long countByAction(LogAction action);

    long countByUserId(UUID userId);

    List<SystemLogModel> findTop10ByOrderByCreatedAtDesc();

    @Query("SELECT l.action, COUNT(l) FROM SystemLogModel l GROUP BY l.action")
    List<Object[]> countByActionGrouped();
}
