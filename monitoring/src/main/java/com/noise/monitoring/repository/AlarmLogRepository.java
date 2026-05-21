package com.noise.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.noise.monitoring.model.AlarmLog;
import java.util.List;

public interface AlarmLogRepository extends JpaRepository<AlarmLog, Long> {

    List<AlarmLog> findByClassroomId(Long classroomId);

    List<AlarmLog>
        findTop100ByClassroomIdOrderByTriggeredAtDesc(Long classroomId);

    long countByClassroomId(Long classroomId);
}