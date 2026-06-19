package com.activitytracker.repository;

import com.activitytracker.entity.ActivityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ActivityTypeRepository extends JpaRepository<ActivityType, Long> {
    Page<ActivityType> findByUserId(Long userId, Pageable pageable);
    List<ActivityType> findByUserId(Long userId);
    Optional<ActivityType> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
