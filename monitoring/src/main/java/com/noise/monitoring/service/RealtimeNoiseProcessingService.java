package com.noise.monitoring.service;

import com.noise.monitoring.dto.NoiseRequest;

import com.noise.monitoring.model.AlarmLog;
import com.noise.monitoring.model.SensorReading;

import com.noise.monitoring.repository
        .AlarmLogRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RealtimeNoiseProcessingService {

    @Autowired
    private NoiseClassificationService
            classificationService;

    @Autowired
    private LearningEngineService
            learningEngineService;

    @Autowired
    private MLPredictionService
            mlPredictionService;

    @Autowired
    private AutoRetrainService
            autoRetrainService;

    @Autowired
    private AlarmLogRepository
            alarmRepository;

    // =====================================================
    // REALTIME PROCESSING PIPELINE
    // =====================================================

    public void processRealtimeNoise(
            NoiseRequest request
    ) {

        long startTime =
                System.currentTimeMillis();

        try {

            System.out.println(
                    "\n================================="
            );

            System.out.println(
                    "REALTIME PIPELINE STARTED"
            );

            /*
             =========================================
             STEP 1
             VALIDATION
             =========================================
            */

            if (request == null) {

                System.out.println(
                        "REQUEST NULL"
                );

                return;
            }

            if (
                    request.getClassroomId() == null
            ) {

                System.out.println(
                        "CLASSROOM ID NULL"
                );

                return;
            }

            /*
             =========================================
             STEP 2
             RULE BASED CLASSIFICATION
             =========================================
            */

            String trainingLabel =
                    classificationService
                    .classifyNoise(
                            request.getDbLevel(),
                            request.getDominantFrequency(),
                            request.getVariance(),
                            request.getSpikeCount()
                    );

            if (
                    trainingLabel == null
                    ||
                    trainingLabel.isBlank()
            ) {

                trainingLabel = "UNKNOWN";
            }

            System.out.println(
                    "RULE LABEL : "
                    + trainingLabel
            );

            /*
             =========================================
             STEP 3
             MACHINE LEARNING PREDICTION
             =========================================
            */

            MLPredictionService.PredictionResult
                    predictionResult =
                    mlPredictionService
                    .predictNoise(
                            request
                    );

            String mlPrediction =
                    predictionResult
                    .getPrediction();

            double confidence =
                    predictionResult
                    .getConfidence();

            if (
                    mlPrediction == null
                    ||
                    mlPrediction.isBlank()
            ) {

                mlPrediction = "UNKNOWN";
            }

            System.out.println(
                    "ML RESULT : "
                    + mlPrediction
            );

            System.out.println(
                    "ML CONFIDENCE : "
                    + confidence
            );

            /*
             =========================================
             STEP 4
             MQTT INFO
             =========================================
            */

            String mqttSource =
                    "ESP32 MQTT CLIENT";

            String mqttTopic =
                    "classroom/noise";

            /*
             =========================================
             STEP 5
             SAVE + LEARNING ENGINE
             =========================================
            */

            SensorReading savedReading =
                    learningEngineService
                    .processNewReading(

                            request,

                            trainingLabel,

                            mlPrediction,

                            confidence,

                            mqttSource,

                            mqttTopic
                    );

            if (savedReading == null) {

                System.out.println(
                        "DATABASE SAVE FAILED"
                );

                return;
            }

            /*
             =========================================
             DATABASE SUCCESS
             =========================================
            */

            System.out.println(
                    "DATABASE SAVE SUCCESS"
            );

            System.out.println(
                    "READING ID : "
                    + savedReading.getId()
            );

            System.out.println(
                    "MODE : "
                    + savedReading.getModeAtTime()
            );

            System.out.println(
                    "THRESHOLD : "
                    + savedReading.getThresholdAtTime()
            );

            /*
             =========================================
             STEP 6
             ALARM CHECK
             =========================================
            */

            if (
                    savedReading.getThresholdAtTime()
                    != null
                    &&
                    request.getDbLevel()
                    >
                    savedReading.getThresholdAtTime()
            ) {

                /*
                 =====================================
                 CREATE ALARM LOG
                 =====================================
                */

                AlarmLog alarm =
                        new AlarmLog();

                alarm.setClassroomId(
                        request.getClassroomId()
                );

                alarm.setReadingId(
                        savedReading.getId()
                );

                alarm.setActualDb(
                        request.getDbLevel()
                );

                alarm.setTriggeredThreshold(
                        savedReading
                        .getThresholdAtTime()
                );

                alarm.setModeAtTime(
                        savedReading
                        .getModeAtTime()
                );

                alarm.setTriggeredAt(
                        LocalDateTime.now()
                );

                /*
                 =====================================
                 SAVE ALARM
                 =====================================
                */

                alarmRepository.save(
                        alarm
                );

                System.out.println(
                        "ALARM TRIGGERED"
                );

                System.out.println(
                        "ALARM LOG SAVED"
                );
            }

            else {

                System.out.println(
                        "NO ALARM"
                );
            }

            /*
             =========================================
             STEP 7
             AUTO RETRAIN CHECK
             =========================================
            */

            autoRetrainService
                    .checkAutoRetrain();

            /*
             =========================================
             STEP 8
             FINAL LOG
             =========================================
            */

            long endTime =
                    System.currentTimeMillis();

            long processTime =
                    endTime - startTime;

            System.out.println(
                    "================================="
            );

            System.out.println(
                    "REALTIME PIPELINE FINISHED"
            );

            System.out.println(
                    "=================================\n"
            );

            System.out.println(
                    "PROCESS TIME : "
                    + processTime
                    + " ms"
            );

            System.out.println(
                    "REALTIME PROCESSING SUCCESS"
            );

            System.out.println(
                    "=================================\n"
            );

        }

        catch (Exception e) {

            System.out.println(
                    "REALTIME PIPELINE ERROR"
            );

            e.printStackTrace();
        }
    }
}