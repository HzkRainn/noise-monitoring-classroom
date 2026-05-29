package com.noise.monitoring.service;

import com.noise.monitoring.dto.NoiseRequest;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class MLPredictionService {

    // =====================================================
    // PYTHON PREDICTION
    // =====================================================

    public PredictionResult predictNoise(
            NoiseRequest request
    ) {

        try {

            if (request == null) {

                return new PredictionResult(
                        "UNKNOWN",
                        0.0
                );
            }

            // =============================================
            // PYTHON PROCESS
            // =============================================

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "python",
                            "ml-engine/prediction/predict.py",

                            String.valueOf(
                                    request.getDbLevel()
                            ),

                            String.valueOf(
                                    request.getDominantFrequency()
                            ),

                            String.valueOf(
                                    request.getVariance()
                            ),

                            String.valueOf(
                                    request.getSpikeCount()
                            )
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    process.getInputStream()
                            )
                    );

            String output =
                    reader.readLine();

            int exitCode =
                    process.waitFor();

            // =============================================
            // EXIT VALIDATION
            // =============================================

            if (exitCode != 0) {

                System.out.println(
                        "ML PREDICTION ERROR"
                );

                return new PredictionResult(
                        "UNKNOWN",
                        0.0
                );
            }

            if (
                    output == null
                    ||
                    output.isBlank()
            ) {

                return new PredictionResult(
                        "UNKNOWN",
                        0.0
                );
            }

            // =============================================
            // OUTPUT FORMAT
            // prediction|confidence
            // =============================================

            String[] parts =
                    output.split("\\|");

            String prediction =
                    parts[0].trim();

            double confidence = 0.0;

            if (parts.length > 1) {

                try {

                    confidence =
                            Double.parseDouble(
                                    parts[1]
                            );

                } catch (Exception e) {

                    confidence = 0.0;
                }
            }

            // =============================================
            // DEBUG LOG
            // =============================================

            System.out.println(
                    "ML RESULT : "
                    + prediction
            );

            System.out.println(
                    "ML CONFIDENCE : "
                    + confidence
            );

            System.out.println(
                    "CONFIDENCE : "
                    + confidence
            );

            // =============================================
            // RETURN RESULT
            // =============================================

            return new PredictionResult(
                    prediction,
                    confidence
            );

        } catch (Exception e) {

            System.out.println(
                    "ML PREDICTION FAILED"
            );

            e.printStackTrace();

            return new PredictionResult(
                    "UNKNOWN",
                    0.0
            );
        }
    }

    // =====================================================
    // INNER RESULT CLASS
    // =====================================================

    public static class PredictionResult {

        private String prediction;

        private double confidence;

        public PredictionResult(
                String prediction,
                double confidence
        ) {

            this.prediction = prediction;
            this.confidence = confidence;
        }

        public String getPrediction() {
            return prediction;
        }

        public void setPrediction(
                String prediction
        ) {
            this.prediction = prediction;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(
                double confidence
        ) {
            this.confidence = confidence;
        }
    }
}