package com.noise.monitoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.noise.monitoring.model.SensorReading;
import java.util.List;

public interface SensorReadingRepository 
        extends JpaRepository<SensorReading, Long> {

    List<SensorReading> findByClassroomId(Long classroomId);

    List<SensorReading> 
        findTop10ByClassroomIdOrderByRecordedAtDesc(Long classroomId);

    List<SensorReading> 
        findTop100ByClassroomIdOrderByRecordedAtDesc(Long classroomId);

    List<SensorReading> 
        findTop50ByClassroomIdOrderByRecordedAtDesc(Long classroomId);
    
    List<SensorReading> 
        findByClassroomIdOrderByRecordedAtAsc(Long classroomId);

    List<SensorReading>
        findTop200ByClassroomIdOrderByRecordedAtDesc(Long classroomId);

    long countByClassroomId(Long classroomId);
}