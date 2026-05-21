package com.noise.monitoring.service;

import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.repository.SensorReadingRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.List;

@Service
public class DatasetExportService {

    @Autowired
    private SensorReadingRepository repository;

    public byte[] exportDatasetCSV() {

        List<SensorReading> readings =
                repository.findAll();

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        PrintWriter writer =
                new PrintWriter(outputStream);

        // ===============================
        // CSV HEADER
        // ===============================
        writer.println(
            "dbLevel,dominantFrequency,variance,spikeCount,trainingLabel"
        );

        // ===============================
        // CSV DATA
        // ===============================
        for (SensorReading reading : readings) {

            // Skip jika label kosong
            if (reading.getTrainingLabel() == null)
                continue;

            writer.println(
                reading.getDbLevel() + "," +
                reading.getDominantFrequency() + "," +
                reading.getVariance() + "," +
                reading.getSpikeCount() + "," +
                reading.getTrainingLabel()
            );
        }

        writer.flush();

        return outputStream.toByteArray();
    }
}