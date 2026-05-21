package com.noise.monitoring.service;

import org.springframework.stereotype.Service;
import com.noise.monitoring.model.SensorReading;

@Service
public class NoiseClassificationService {

    public String classify(SensorReading reading) {

        // Rule akademik sederhana
        if (reading.getSpikeCount() > 10 ||
            reading.getDominantFrequency() > 1000)
            return "NON_HUMAN";

        if (reading.getDbLevel() > 65 &&
            reading.getVariance() < 5 &&
            reading.getSpikeCount() < 5)
            return "HUMAN";

        return "HUMAN";
    }
}