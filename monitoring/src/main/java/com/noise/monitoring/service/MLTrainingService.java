package com.noise.monitoring.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Service
public class MLTrainingService {

    // =========================================
    // RETRAIN MODEL
    // =========================================

    public String retrainModel() {

        try {

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "python",
                            "ml-engine/training/train.py"
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

            StringBuilder output =
                    new StringBuilder();

            String line;

            while (
                    (line = reader.readLine()) != null
            ) {

                output
                        .append(line)
                        .append("\n");
            }

            int exitCode =
                    process.waitFor();

            return
                    "MODEL RETRAIN SUCCESS\n\n"
                    +
                    output
                    +
                    "\nEXIT CODE : "
                    +
                    exitCode;

        } catch (Exception e) {

            return
                    "RETRAIN FAILED\n\n"
                    +
                    e.getMessage();
        }
    }
}