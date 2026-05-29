package com.noise.monitoring.service;

import com.noise.monitoring.dto.NoiseRequest;

import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.model.SystemMode;

import com.noise.monitoring.repository
        .SensorReadingRepository;

import com.noise.monitoring.repository
        .SystemModeRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;

@Service
public class LearningEngineService {

    @Autowired
    private SensorReadingRepository
            readingRepository;

    @Autowired
    private SystemModeRepository
            modeRepository;

    /*
     =========================================
     REALTIME SAVE PIPELINE
     =========================================
    */

    public SensorReading processNewReading(

            NoiseRequest request,

            String trainingLabel,

            String mlPrediction,

            Double confidence,

            String mqttSource,

            String mqttTopic
    ) {

        /*
         =========================================
         VALIDATION
         =========================================
        */

        if (request == null) {

            System.out.println(
                    "REQUEST NULL"
            );

            return null;
        }

        /*
         =========================================
         SAVE SENSOR READING
         =========================================
        */

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

        /*
         =========================================
         SAVE AI RESULT
         =========================================
        */

        reading.setTrainingLabel(
                trainingLabel
        );

        reading.setMlPrediction(
                mlPrediction
        );

        reading.setMlConfidence(
                confidence
        );

        /*
         =========================================
         SAVE MQTT INFO
         =========================================
        */

        reading.setMqttSource(
                mqttSource
        );

        reading.setMqttTopic(
                mqttTopic
        );

        /*
         =========================================
         ADAPTIVE LEARNING
         =========================================
        */

        SystemMode mode =
                learn(
                        request.getClassroomId()
                );

        /*
         =========================================
         NULL SAFE
         =========================================
        */

        if (mode != null) {

            reading.setModeAtTime(
                    mode.getMode()
            );

            reading.setThresholdAtTime(
                    mode.getThresholdValue()
            );
        }

        /*
         =========================================
         SAVE DATABASE
         =========================================
        */

        SensorReading savedReading =
                readingRepository.save(
                        reading
                );

        /*
         =========================================
         DEBUG
         =========================================
        */

        System.out.println(
                "READING SAVED SUCCESSFULLY"
        );

        System.out.println(
                "ID : "
                + savedReading.getId()
        );

        return savedReading;
    }

    /*
     =========================================
     ADAPTIVE LEARNING ENGINE
     =========================================
    */

    public SystemMode learn(
            Long classroomId
    ) {

        List<SensorReading> readings =
                readingRepository
                .findTop100ByClassroomIdOrderByRecordedAtDesc(
                        classroomId
                );

        List<SensorReading> last10 =
                readingRepository
                .findTop10ByClassroomIdOrderByRecordedAtDesc(
                        classroomId
                );

        /*
         =========================================
         EMPTY STATE
         =========================================
        */

        if (readings.isEmpty()) {

            SystemMode emptyMode =
                    new SystemMode();

            emptyMode.setClassroomId(
                    classroomId
            );

            emptyMode.setMode(
                    SystemMode.ModeType.DEFAULT
            );

            emptyMode.setMeanDb(0);

            emptyMode.setStdDev(0);

            emptyMode.setThresholdValue(0);

            emptyMode.setCalculatedAt(
                    LocalDateTime.now()
            );

            return modeRepository.save(
                    emptyMode
            );
        }

        /*
         =========================================
         STATISTICAL ANALYSIS
         =========================================
        */

        double mean =
                readings.stream()
                .mapToDouble(
                        SensorReading::getDbLevel
                )
                .average()
                .orElse(0.0);

        double variance =
                readings.stream()
                .mapToDouble(
                        r ->
                                Math.pow(
                                        r.getDbLevel() - mean,
                                        2
                                )
                )
                .average()
                .orElse(0.0);

        double stdDev =
                Math.sqrt(variance);

        int totalSpikes =
                readings.stream()
                .mapToInt(
                        SensorReading::getSpikeCount
                )
                .sum();

        double avgFrequency =
                last10.stream()
                .mapToDouble(
                        SensorReading
                                ::getDominantFrequency
                )
                .average()
                .orElse(0.0);

        /*
         =========================================
         MAJORITY DOMINANCE
         =========================================
        */

        long highNoiseCount =
                readings.stream()
                .filter(
                        r -> r.getDbLevel() > 80
                )
                .count();

        boolean majorityHighNoise =
                highNoiseCount
                >=
                (readings.size() * 0.6);

        SystemMode.ModeType detectedMode;

        /*
         =========================================
         MODE DETECTION
         =========================================
        */

        if (majorityHighNoise) {

            detectedMode =
                    SystemMode.ModeType.CHAOTIC;
        }

        else if (
                mean < 60
                &&
                stdDev < 3
                &&
                totalSpikes < 20
        ) {

            detectedMode =
                    SystemMode.ModeType.FOCUSED;
        }

        else if (
                mean >= 60
                &&
                mean <= 75
                &&
                stdDev < 8
        ) {

            detectedMode =
                    SystemMode.ModeType.DISCUSSION;
        }

        else if (mean > 75) {

            detectedMode =
                    SystemMode.ModeType.CHAOTIC;
        }

        else {

            detectedMode =
                    SystemMode.ModeType.DEFAULT;
        }

        /*
         =========================================
         FREQUENCY OVERRIDE
         =========================================
        */

        if (
                avgFrequency >= 80
                &&
                avgFrequency <= 300
        ) {

            detectedMode =
                    SystemMode.ModeType
                            .HUMAN_ACTIVITY;
        }

        else if (
                avgFrequency > 1000
        ) {

            detectedMode =
                    SystemMode.ModeType
                            .MACHINE_NOISE;
        }

        /*
         =========================================
         ADAPTIVE K FACTOR
         =========================================
        */

        double k;

        switch (detectedMode) {

            case EXAM_MODE:

                k = 1.5;
                break;

            case CHAOTIC:

                k = 3;
                break;

            default:

                k = 2;
        }

        double threshold =
                mean + (k * stdDev);

        /*
         =========================================
         SAVE SYSTEM MODE
         =========================================
        */

        SystemMode mode =
                new SystemMode();

        mode.setClassroomId(
                classroomId
        );

        mode.setMode(
                detectedMode
        );

        mode.setMeanDb(
                mean
        );

        mode.setStdDev(
                stdDev
        );

        mode.setThresholdValue(
                threshold
        );

        mode.setCalculatedAt(
                LocalDateTime.now()
        );

        return modeRepository.save(
                mode
        );
    }
}