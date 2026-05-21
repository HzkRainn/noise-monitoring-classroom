package com.noise.monitoring.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class MLPredictionService {

        public String predict(
                double dbLevel,
                double dominantFrequency,
                double variance,
                int spikeCount
        ) {

        try {

                ProcessBuilder processBuilder =
                        new ProcessBuilder(
                                "python",
                                "ml-engine/prediction/predict.py",
                                String.valueOf(dbLevel),
                                String.valueOf(dominantFrequency),
                                String.valueOf(variance),
                                String.valueOf(spikeCount)
                        );

                processBuilder.redirectErrorStream(true);

                Process process = processBuilder.start();

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        process.getInputStream()
                                )
                        );

                String result = reader.readLine();

                process.waitFor();

                return result;

        } catch (Exception e) {

                e.printStackTrace();

                return "UNKNOWN";
        }
    }
}