package com.activitytracker.repository;

import com.activitytracker.entity.ActivityEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ActivityEntryRepository extends JpaRepository<ActivityEntry, Long> {

    Page<ActivityEntry> findByUserIdAndActivityTypeId(Long userId, Long activityTypeId, Pageable pageable);

    List<ActivityEntry> findByUserIdAndActivityTypeIdAndEntryDateBetweenOrderByEntryDateAsc(
            Long userId, Long activityTypeId, LocalDate from, LocalDate to);

    List<ActivityEntry> findByUserIdAndActivityTypeIdOrderByEntryDateDesc(
            Long userId, Long activityTypeId);

    long countByUserIdAndActivityTypeId(Long userId, Long activityTypeId);

    @Query("SELECT COALESCE(COUNT(e), 0) FROM ActivityEntry e WHERE e.user.id = :userId AND e.activityType.id = :typeId AND e.entryDate = :date")
    long countByUserIdAndActivityTypeIdAndEntryDate(
            @Param("userId") Long userId,
            @Param("typeId") Long typeId,
            @Param("date") LocalDate date);
}
