package com.noise.monitoring.service;

import com.noise.monitoring.repository
        .SensorReadingRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

@Service
public class AutoRetrainService {

    @Autowired
    private SensorReadingRepository
            readingRepository;

    @Autowired
    private MLTrainingService
            mlTrainingService;

    // =========================================
    // RETRAIN LIMIT
    // =========================================

    private static final long
            RETRAIN_THRESHOLD = 100;

    private long
            lastRetrainCount = 0;

    // =========================================
    // CHECK AUTO RETRAIN
    // =========================================

    public void checkAutoRetrain() {

        long totalReadings =
                readingRepository.count();

        System.out.println(
                "\n================================="
        );

        System.out.println(
                "AUTO RETRAIN CHECK"
        );

        System.out.println(
                "TOTAL READINGS : "
                + totalReadings
        );

        System.out.println(
                "LAST RETRAIN COUNT : "
                + lastRetrainCount
        );

        // =====================================
        // CHECK THRESHOLD
        // =====================================

        if (
                totalReadings - lastRetrainCount
                >=
                RETRAIN_THRESHOLD
        ) {

            System.out.println(
                    "RETRAIN TRIGGERED"
            );

            String result =
                    mlTrainingService
                    .retrainModel();

            System.out.println(result);

            lastRetrainCount =
                    totalReadings;

        } else {

            System.out.println(
                    "RETRAIN NOT REQUIRED"
            );
        }

        System.out.println(
                "=================================\n"
        );
    }
}