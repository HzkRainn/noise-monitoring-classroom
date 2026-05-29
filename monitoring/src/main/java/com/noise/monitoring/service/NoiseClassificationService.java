package com.noise.monitoring.service;

import org.springframework.stereotype.Service;

import com.noise.monitoring.model.SensorReading;

@Service
public class NoiseClassificationService {

    public String classify(
            SensorReading reading
    ) {

        if (
            reading.getSpikeCount() > 10 ||
            reading.getDominantFrequency() > 1000
        ) {

            return "NON_HUMAN";
        }

        if (
            reading.getDbLevel() > 65 &&
            reading.getVariance() < 5 &&
            reading.getSpikeCount() < 5
        ) {

            return "HUMAN";
        }

        return "HUMAN";
    }

    public String classifyNoise(
            Double dbLevel,
            Double dominantFrequency,
            Double variance,
            Integer spikeCount
    ) {

        /*
         =========================================
         MACHINE / NON HUMAN DETECTION
         =========================================
        */

        if (
                spikeCount > 10
                ||
                dominantFrequency > 1000
        ) {

            return "NON_HUMAN";
        }

        /*
         =========================================
         HUMAN ACTIVITY DETECTION
         =========================================
        */

        if (
                dbLevel > 65
                &&
                variance < 5
                &&
                spikeCount < 5
        ) {

            return "HUMAN";
        }

        /*
         =========================================
         DEFAULT HUMAN CLASSIFICATION
         =========================================
        */

        return "HUMAN";
    }
}