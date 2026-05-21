package com.noise.monitoring.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "classroom_id")
    private Long classroomId;

    @Column(name = "db_level")
    private double dbLevel;

    @Column(name = "dominant_frequency")
    private double dominantFrequency;

    private double variance;

    @Column(name = "spike_count")
    private int spikeCount;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode_at_time")
    private SystemMode.ModeType modeAtTime;

    @Column(name = "threshold_at_time")
    private Double thresholdAtTime;

    // ===============================
    // ML TRAINING LABEL
    // ===============================
    @Column(name = "training_label")
    private String trainingLabel;

    // ===============================
    // GETTER & SETTER
    // ===============================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(Long classroomId) {
        this.classroomId = classroomId;
    }

    public double getDbLevel() {
        return dbLevel;
    }

    public void setDbLevel(double dbLevel) {
        this.dbLevel = dbLevel;
    }

    public double getDominantFrequency() {
        return dominantFrequency;
    }

    public void setDominantFrequency(double dominantFrequency) {
        this.dominantFrequency = dominantFrequency;
    }

    public double getVariance() {
        return variance;
    }

    public void setVariance(double variance) {
        this.variance = variance;
    }

    public int getSpikeCount() {
        return spikeCount;
    }

    public void setSpikeCount(int spikeCount) {
        this.spikeCount = spikeCount;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(LocalDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }

    public SystemMode.ModeType getModeAtTime() {
        return modeAtTime;
    }

    public void setModeAtTime(SystemMode.ModeType modeAtTime) {
        this.modeAtTime = modeAtTime;
    }

    public Double getThresholdAtTime() {
        return thresholdAtTime;
    }

    public void setThresholdAtTime(Double thresholdAtTime) {
        this.thresholdAtTime = thresholdAtTime;
    }

    // ===============================
    // TRAINING LABEL
    // ===============================

    public String getTrainingLabel() {
        return trainingLabel;
    }

    public void setTrainingLabel(String trainingLabel) {
        this.trainingLabel = trainingLabel;
    }
}