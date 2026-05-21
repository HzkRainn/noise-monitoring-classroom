package com.noise.monitoring.controller;

import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.repository.SensorReadingRepository;
import com.noise.monitoring.service.NoiseClassificationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/ml")
public class MLExportController {

    @Autowired
    private SensorReadingRepository repository;

    @Autowired
    private NoiseClassificationService classificationService;

    @GetMapping("/export-csv")
    public String exportCSV() {

        List<SensorReading> readings = repository.findAll();

        // path dataset python
        String filePath =
                "ml-engine/dataset/noise_dataset.csv";

        try (FileWriter writer = new FileWriter(filePath)) {

            // ===== HEADER CSV =====
            writer.append(
                    "dbLevel,dominantFrequency,variance,spikeCount,label\n"
            );

            // ===== DATA =====
            for (SensorReading reading : readings) {

                String label =
                        classificationService.classify(reading);

                writer.append(String.valueOf(reading.getDbLevel()))
                        .append(",");

                writer.append(String.valueOf(
                        reading.getDominantFrequency()))
                        .append(",");

                writer.append(String.valueOf(
                        reading.getVariance()))
                        .append(",");

                writer.append(String.valueOf(
                        reading.getSpikeCount()))
                        .append(",");

                writer.append(label)
                        .append("\n");
            }

            writer.flush();

            return "Dataset exported successfully → "
                    + filePath;

        } catch (IOException e) {

            e.printStackTrace();

            return "Export failed: "
                    + e.getMessage();
        }
    }
}