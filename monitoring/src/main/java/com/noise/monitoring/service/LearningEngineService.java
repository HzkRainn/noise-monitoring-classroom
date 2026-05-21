package com.noise.monitoring.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.model.SystemMode;
import com.noise.monitoring.repository.SensorReadingRepository;
import com.noise.monitoring.repository.SystemModeRepository;

import java.time.LocalDateTime;
import java.util.List;

    @Service
        public class LearningEngineService {

            @Autowired
            private SensorReadingRepository readingRepository;

            @Autowired
            private SystemModeRepository modeRepository;

            public SystemMode learn(Long classroomId) {

                // Ambil 100 terakhir untuk statistik utama
                List<SensorReading> readings =
                        readingRepository
                        .findTop100ByClassroomIdOrderByRecordedAtDesc(classroomId);

                // Ambil 10 terakhir untuk frequency override
                List<SensorReading> last10 =
                        readingRepository
                        .findTop10ByClassroomIdOrderByRecordedAtDesc(classroomId);

                if (readings.isEmpty()) {
                    SystemMode emptyMode = new SystemMode();
                    emptyMode.setClassroomId(classroomId);
                    emptyMode.setMode(SystemMode.ModeType.DEFAULT);
                    emptyMode.setMeanDb(0);
                    emptyMode.setStdDev(0);
                    emptyMode.setThresholdValue(0);
                    emptyMode.setCalculatedAt(LocalDateTime.now());
                    return modeRepository.save(emptyMode);
                }

                // === STATISTICAL CALCULATION (100 data) ===
                double mean = readings.stream()
                        .mapToDouble(SensorReading::getDbLevel)
                        .average()
                        .orElse(0.0);

                double variance = readings.stream()
                        .mapToDouble(r -> Math.pow(r.getDbLevel() - mean, 2))
                        .average()
                        .orElse(0.0);

                double stdDev = Math.sqrt(variance);

                int totalSpikes = readings.stream()
                        .mapToInt(SensorReading::getSpikeCount)
                        .sum();

                // === FREQUENCY CALCULATION (10 data only) ===
                double avgFrequency = last10.stream()
                        .mapToDouble(SensorReading::getDominantFrequency)
                        .average()
                        .orElse(0.0);

                // === MAJORITY DOMINANCE CALCULATION ===
                long highNoiseCount = readings.stream()
                        .filter(r -> r.getDbLevel() > 80)
                        .count();

                boolean majorityHighNoise =
                        highNoiseCount >= (readings.size() * 0.6);

                SystemMode.ModeType detectedMode;

                // === MAJORITY DOMINANCE CHECK ===
                if (majorityHighNoise) {
                    detectedMode = SystemMode.ModeType.CHAOTIC;
                }
                else if (mean < 60 && stdDev < 3 && totalSpikes < 20) {
                    detectedMode = SystemMode.ModeType.FOCUSED;
                }
                else if (mean >= 60 && mean <= 75 && stdDev < 8) {
                    detectedMode = SystemMode.ModeType.DISCUSSION;
                }
                else if (mean > 75) {
                    detectedMode = SystemMode.ModeType.CHAOTIC;
                }
                else {
                    detectedMode = SystemMode.ModeType.DEFAULT;
                }

                // === FREQUENCY OVERRIDE (responsive) ===
                if (avgFrequency >= 80 && avgFrequency <= 300) {
                    detectedMode = SystemMode.ModeType.HUMAN_ACTIVITY;
                }
                else if (avgFrequency > 1000) {
                    detectedMode = SystemMode.ModeType.MACHINE_NOISE;
                }

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

                double threshold = mean + (k * stdDev);

                SystemMode mode = new SystemMode();
                mode.setClassroomId(classroomId);
                mode.setMode(detectedMode);
                mode.setMeanDb(mean);
                mode.setStdDev(stdDev);
                mode.setThresholdValue(threshold);
                mode.setCalculatedAt(LocalDateTime.now());

                return modeRepository.save(mode);
            }
        }