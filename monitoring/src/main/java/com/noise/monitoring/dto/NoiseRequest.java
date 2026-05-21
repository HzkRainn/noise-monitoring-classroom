package com.noise.monitoring.dto;

public class NoiseRequest {

    private Long classroomId;

    private double dbLevel;

    private double dominantFrequency;

    private double variance;

    private int spikeCount;

    // =========================
    // GETTER & SETTER
    // =========================

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
}