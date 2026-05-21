package com.noise.monitoring.repository;

import com.noise.monitoring.model.SystemMode;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemModeRepository
        extends JpaRepository<SystemMode, Long> {

    Optional<SystemMode>
    findTopByClassroomIdOrderByCalculatedAtDesc(
            Long classroomId
    );
}