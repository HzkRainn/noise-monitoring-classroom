package com.noise.monitoring.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "alarm_logs")
public class AlarmLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     =========================================
     BASIC INFO
     =========================================
    */

    private Long classroomId;

    private Long readingId;

    /*
     =========================================
     ALARM SNAPSHOT
     =========================================
    */

    private Double triggeredThreshold;

    private Double actualDb;

    @Enumerated(EnumType.STRING)
    private SystemMode.ModeType modeAtTime;

    /*
     =========================================
     TIME
     =========================================
    */

    private LocalDateTime triggeredAt;

    // =========================================
    // GETTER SETTER
    // =========================================

    public Long getId() {

        return id;
    }

    public Long getClassroomId() {

        return classroomId;
    }

    public void setClassroomId(
            Long classroomId
    ) {

        this.classroomId = classroomId;
    }

    public Long getReadingId() {

        return readingId;
    }

    public void setReadingId(
            Long readingId
    ) {

        this.readingId = readingId;
    }

    public Double getTriggeredThreshold() {

        return triggeredThreshold;
    }

    public void setTriggeredThreshold(
            Double triggeredThreshold
    ) {

        this.triggeredThreshold =
                triggeredThreshold;
    }

    public Double getActualDb() {

        return actualDb;
    }

    public void setActualDb(
            Double actualDb
    ) {

        this.actualDb = actualDb;
    }

    public SystemMode.ModeType getModeAtTime() {

        return modeAtTime;
    }

    public void setModeAtTime(
            SystemMode.ModeType modeAtTime
    ) {

        this.modeAtTime = modeAtTime;
    }

    public LocalDateTime getTriggeredAt() {

        return triggeredAt;
    }

    public void setTriggeredAt(
            LocalDateTime triggeredAt
    ) {

        this.triggeredAt = triggeredAt;
    }
}