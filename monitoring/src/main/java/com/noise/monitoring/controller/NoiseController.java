package com.noise.monitoring.controller;

import com.noise.monitoring.dto.NoiseRequest;
import com.noise.monitoring.model.AlarmLog;
import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.model.SystemMode;
import com.noise.monitoring.repository.AlarmLogRepository;
import com.noise.monitoring.repository.SensorReadingRepository;
import com.noise.monitoring.repository.SystemModeRepository;
import com.noise.monitoring.service.LearningEngineService;
import com.noise.monitoring.service.MLPredictionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/noise")
@CrossOrigin("*")
public class NoiseController {

    @Autowired
    private SensorReadingRepository readingRepository;

    @Autowired
    private AlarmLogRepository alarmRepository;

    @Autowired
    private SystemModeRepository systemmodeRepository;

    @Autowired
    private LearningEngineService learningEngineService;

    @Autowired
    private MLPredictionService mlPredictionService;

    // =====================================================
    // SUBMIT SENSOR DATA
    // =====================================================
    @PostMapping
    public SensorReading receiveNoiseData(
            @RequestBody NoiseRequest request
    ) {

        // =========================================
        // GET CURRENT SYSTEM MODE
        // =========================================
        SystemMode latestMode =
                learningEngineService.learn(
                        request.getClassroomId()
                );

        // =========================================
        // AI PREDICTION
        // =========================================
        String prediction =
                mlPredictionService.predict(
                        request.getDbLevel(),
                        request.getDominantFrequency(),
                        request.getVariance(),
                        request.getSpikeCount()
                );

        // =========================================
        // CREATE SENSOR READING
        // =========================================
        SensorReading reading = new SensorReading();

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
                prediction
        );

        // =========================================
        // SAVE TO DATABASE
        // =========================================
        SensorReading savedReading =
                readingRepository.save(reading);

        // =========================================
        // ALARM DETECTION
        // =========================================
        if (latestMode != null &&
                request.getDbLevel() >
                        latestMode.getThresholdValue()) {

            AlarmLog alarm = new AlarmLog();

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
                    latestMode.getThresholdValue()
            );

            alarm.setModeAtTime(
                    latestMode.getMode()
            );

            alarm.setTriggeredAt(
                    LocalDateTime.now()
            );

            alarmRepository.save(alarm);
        }

        return savedReading;
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
    // GET TOTAL READINGS
    // =====================================================
    @GetMapping("/count/{classroomId}")
    public Long getTotalReadings(
            @PathVariable Long classroomId
    ) {

        return readingRepository.countByClassroomId(
                classroomId
        );
    }

    // =====================================================
    // GET CURRENT MODE
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