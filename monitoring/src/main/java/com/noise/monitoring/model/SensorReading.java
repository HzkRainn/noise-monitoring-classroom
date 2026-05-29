package com.noise.monitoring.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================
    // CLASSROOM
    // =========================================

    @Column(name = "classroom_id")
    private Long classroomId;

    // =========================================
    // SENSOR DATA
    // =========================================

    @Column(name = "db_level")
    private double dbLevel;

    @Column(name = "dominant_frequency")
    private double dominantFrequency;

    private double variance;

    @Column(name = "spike_count")
    private int spikeCount;

    // =========================================
    // TIME
    // =========================================

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;

    // =========================================
    // SYSTEM MODE
    // =========================================

    @Enumerated(EnumType.STRING)

    @Column(name = "mode_at_time")

    private SystemMode.ModeType modeAtTime;

    @Column(name = "threshold_at_time")
    private Double thresholdAtTime;

    // =========================================
    // RULE BASED LABEL
    // =========================================

    @Column(name = "training_label")
    private String trainingLabel;

    // =========================================
    // MACHINE LEARNING PREDICTION
    // =========================================

    @Column(name = "ml_prediction")
    private String mlPrediction;

    // =========================================
    // MACHINE LEARNING CONFIDENCE
    // =========================================

    @Column(name = "ml_confidence")
    private Double mlConfidence;

    // =========================================
    // MQTT SOURCE
    // =========================================

    @Column(name = "mqtt_source")
    private String mqttSource;

    @Column(name = "mqtt_topic")
    private String mqttTopic;

    /*
     =========================================
     GETTER & SETTER
     =========================================
    */

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

    public void setDominantFrequency(
            double dominantFrequency
    ) {
        this.dominantFrequency =
                dominantFrequency;
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

    public void setRecordedAt(
            LocalDateTime recordedAt
    ) {
        this.recordedAt = recordedAt;
    }

    public SystemMode.ModeType getModeAtTime() {
        return modeAtTime;
    }

    public void setModeAtTime(
            SystemMode.ModeType modeAtTime
    ) {
        this.modeAtTime = modeAtTime;
    }

    public Double getThresholdAtTime() {
        return thresholdAtTime;
    }

    public void setThresholdAtTime(
            Double thresholdAtTime
    ) {
        this.thresholdAtTime =
                thresholdAtTime;
    }

    public String getTrainingLabel() {
        return trainingLabel;
    }

    public void setTrainingLabel(
            String trainingLabel
    ) {
        this.trainingLabel =
                trainingLabel;
    }

    public String getMlPrediction() {
        return mlPrediction;
    }

    public void setMlPrediction(
            String mlPrediction
    ) {
        this.mlPrediction =
                mlPrediction;
    }

    // =========================================
    // ML CONFIDENCE
    // =========================================

    public Double getMlConfidence() {
        return mlConfidence;
    }

    public void setMlConfidence(
            Double mlConfidence
    ) {
        this.mlConfidence =
                mlConfidence;
    }

    // =========================================
    // MQTT SOURCE
    // =========================================

    public String getMqttSource() {
        return mqttSource;
    }

    public void setMqttSource(
            String mqttSource
    ) {
        this.mqttSource = mqttSource;
    }

    public String getMqttTopic() {
        return mqttTopic;
    }

    public void setMqttTopic(
            String mqttTopic
    ) {
        this.mqttTopic = mqttTopic;
    }
}