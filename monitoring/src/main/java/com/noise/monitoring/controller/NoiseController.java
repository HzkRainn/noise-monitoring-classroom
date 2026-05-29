package com.noise.monitoring.controller;

import com.noise.monitoring.dto.NoiseRequest;

import com.noise.monitoring.model.AlarmLog;
import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.model.SystemMode;

import com.noise.monitoring.repository
        .AlarmLogRepository;

import com.noise.monitoring.repository
        .SensorReadingRepository;

import com.noise.monitoring.repository
        .SystemModeRepository;

import com.noise.monitoring.service
        .LearningEngineService;

import com.noise.monitoring.service
        .MLPredictionService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/noise")
@CrossOrigin("*")
public class NoiseController {

    @Autowired
    private SensorReadingRepository
            readingRepository;

    @Autowired
    private AlarmLogRepository
            alarmRepository;

    @Autowired
    private SystemModeRepository
            systemModeRepository;

    @Autowired
    private LearningEngineService
            learningEngineService;

    @Autowired
    private MLPredictionService
            mlPredictionService;

    // =====================================================
    // SUBMIT SENSOR DATA
    // =====================================================

    @PostMapping
    public SensorReading receiveNoiseData(
            @RequestBody NoiseRequest request
    ) {

        System.out.println(
                "\n================================="
        );

        System.out.println(
                "REST API NOISE REQUEST RECEIVED"
        );

        try {

            // =========================================
            // MACHINE LEARNING PREDICTION
            // =========================================

                MLPredictionService.PredictionResult
                        predictionResult =

                        mlPredictionService
                        .predictNoise(request);

                String mlPrediction =
                        predictionResult
                        .getPrediction();

                double confidence =
                        predictionResult
                        .getConfidence();

                System.out.println(
                        "ML PREDICTION : "
                        + mlPrediction
                );

                System.out.println(
                        "ML CONFIDENCE : "
                        + confidence
                );

            // =========================================
            // LEARNING ENGINE
            // =========================================

            SystemMode latestMode =
                    learningEngineService.learn(
                            request.getClassroomId()
                    );

            // =========================================
            // CREATE READING
            // =========================================

            SensorReading reading =
                    new SensorReading();

            reading.setClassroomId(
                    request.getClassroomId()
            );

            reading.setDbLevel(
                    request.getDbLevel()
            );

            reading.setDominantFrequency(
                    request.getDominantFrequency()
            );

            reading.setVariance(
                    request.getVariance()
            );

            reading.setSpikeCount(
                    request.getSpikeCount()
            );

            reading.setRecordedAt(
                    LocalDateTime.now()
            );

            // =========================================
            // SAVE MODE SNAPSHOT
            // =========================================

            if (latestMode != null) {

                reading.setModeAtTime(
                        latestMode.getMode()
                );

                reading.setThresholdAtTime(
                        latestMode.getThresholdValue()
                );
            }

            // =========================================
            // SAVE AI LABEL
            // =========================================

            reading.setTrainingLabel(
                    mlPrediction
            );

            reading.setMlPrediction(
                    mlPrediction
            );

            // =========================================
            // SAVE DATABASE
            // =========================================

            SensorReading savedReading =
                    readingRepository.save(
                            reading
                    );

            System.out.println(
                    "DATABASE SAVE SUCCESS"
            );

            System.out.println(
                    "READING ID : " +
                    savedReading.getId()
            );

            // =========================================
            // ALARM DETECTION
            // =========================================

            if (
                    latestMode != null
                    &&
                    request.getDbLevel()
                    >
                    latestMode.getThresholdValue()
            ) {

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

                alarm.setModeAtTime(
                        latestMode.getMode()
                );

                alarm.setTriggeredAt(
                        LocalDateTime.now()
                );

                alarmRepository.save(alarm);

                System.out.println(
                        "ALARM TRIGGERED"
                );
            }

            else {

                System.out.println(
                        "NO ALARM"
                );
            }

            System.out.println(
                    "=================================\n"
            );

            return savedReading;

        } catch (Exception e) {

            System.out.println(
                    "NOISE PROCESSING ERROR"
            );

            e.printStackTrace();

            throw e;
        }
    }

    // =====================================================
    // GET LATEST READINGS
    // =====================================================

    @GetMapping("/latest/{classroomId}")
    public List<SensorReading> getLatestReadings(
            @PathVariable Long classroomId,
            @RequestParam(defaultValue = "100")
            int limit
    ) {

        if (limit <= 10) {

            return readingRepository
                    .findTop10ByClassroomIdOrderByRecordedAtDesc(
                            classroomId
                    );
        }

        if (limit <= 50) {

            return readingRepository
                    .findTop50ByClassroomIdOrderByRecordedAtDesc(
                            classroomId
                    );
        }

        if (limit <= 100) {

            return readingRepository
                    .findTop100ByClassroomIdOrderByRecordedAtDesc(
                            classroomId
                    );
        }

        return readingRepository
                .findTop200ByClassroomIdOrderByRecordedAtDesc(
                        classroomId
                );
    }

    // =====================================================
    // GET CURRENT MODE
    // =====================================================

    @GetMapping("/latest-mode/{classroomId}")
    public SystemMode getLatestMode(
            @PathVariable Long classroomId
    ) {

        return systemModeRepository
                .findTopByClassroomIdOrderByCalculatedAtDesc(
                        classroomId
                )
                .orElse(null);
    }

    // =====================================================
    // GET LATEST ALARMS
    // =====================================================

    @GetMapping("/latest-alarms/{classroomId}")
    public List<AlarmLog> getLatestAlarms(
            @PathVariable Long classroomId
    ) {

        return alarmRepository
                .findTop100ByClassroomIdOrderByTriggeredAtDesc(
                        classroomId
                );
    }

    // =====================================================
    // GET TOTAL READINGS
    // =====================================================

    @GetMapping("/count/{classroomId}")
    public Long getTotalReadings(
            @PathVariable Long classroomId
    ) {

        return readingRepository
                .countByClassroomId(
                        classroomId
                );
    }

    // =====================================================
    // GET DASHBOARD SUMMARY
    // =====================================================

    @GetMapping("/summary/{classroomId}")
    public Map<String, Object> getSummary(
            @PathVariable Long classroomId
    ) {

        Map<String, Object> summary =
                new HashMap<>();

        long totalReadings =
                readingRepository
                .countByClassroomId(
                        classroomId
                );

        long totalAlarms =
                alarmRepository
                .countByClassroomId(
                        classroomId
                );

        SystemMode latestMode =
                systemModeRepository
                .findTopByClassroomIdOrderByCalculatedAtDesc(
                        classroomId
                )
                .orElse(null);

        List<SensorReading> latest =
                readingRepository
                .findTop10ByClassroomIdOrderByRecordedAtDesc(
                        classroomId
                );

        double latestDb = 0;

        String latestPrediction = "UNKNOWN";

        if (!latest.isEmpty()) {

            latestDb =
                    latest.get(0)
                    .getDbLevel();

            latestPrediction =
                    latest.get(0)
                    .getMlPrediction();
        }

        summary.put(
                "totalReadings",
                totalReadings
        );

        summary.put(
                "totalAlarms",
                totalAlarms
        );

        summary.put(
                "latestDb",
                latestDb
        );

        summary.put(
                "latestPrediction",
                latestPrediction
        );

        summary.put(
                "currentMode",
                latestMode != null
                        ? latestMode.getMode()
                        : "DEFAULT"
        );

        summary.put(
                "threshold",
                latestMode != null
                        ? latestMode.getThresholdValue()
                        : 0
        );

        return summary;
    }

    // =====================================================
    // MANUAL LEARNING ENGINE
    // =====================================================

    @GetMapping("/learn/{classroomId}")
    public SystemMode learnMode(
            @PathVariable Long classroomId
    ) {

        return learningEngineService.learn(
                classroomId
        );
    }
}