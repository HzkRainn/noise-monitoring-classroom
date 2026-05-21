package com.noise.monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_modes")
public class SystemMode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long classroomId;

    @Enumerated(EnumType.STRING)
    private ModeType mode;

    private double meanDb;
    private double stdDev;
    private double thresholdValue;

    private LocalDateTime calculatedAt;

    public enum ModeType {
        DEFAULT,
        DISCUSSION,
        FOCUSED,
        CHAOTIC,
        HUMAN_ACTIVITY,
        MACHINE_NOISE,
        EXAM_MODE
    }

    // ===== GETTER & SETTER =====

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getClassroomId() { return classroomId; }

    public void setClassroomId(Long classroomId) { this.classroomId = classroomId; }

    public ModeType getMode() { return mode; }

    public void setMode(ModeType mode) { this.mode = mode; }

    public double getMeanDb() { return meanDb; }

    public void setMeanDb(double meanDb) { this.meanDb = meanDb; }

    public double getStdDev() { return stdDev; }

    public void setStdDev(double stdDev) { this.stdDev = stdDev; }

    public double getThresholdValue() { return thresholdValue; }

    public void setThresholdValue(double thresholdValue) { this.thresholdValue = thresholdValue; }

    public LocalDateTime getCalculatedAt() { return calculatedAt; }

    public void setCalculatedAt(LocalDateTime calculatedAt) { this.calculatedAt = calculatedAt; }
}