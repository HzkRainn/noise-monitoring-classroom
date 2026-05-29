package com.noise.monitoring.controller;

import com.noise.monitoring.service.DatasetExportService;
import com.noise.monitoring.service.MLTrainingService;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/ml")
@CrossOrigin("*")
public class MLController {

    @Autowired
    private DatasetExportService
            datasetExportService;

    @Autowired
    private MLTrainingService
            mlTrainingService;

    // =========================================
    // EXPORT DATASET CSV
    // =========================================

    @GetMapping("/export-dataset")
    public ResponseEntity<byte[]>
    exportDataset()
    throws IOException {

        byte[] csvData =
                datasetExportService
                .exportDatasetCSV();

        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=noise_dataset.csv"
                )

                .contentType(
                        MediaType.parseMediaType(
                                "text/csv"
                        )
                )

                .body(csvData);
    }

    // =========================================
    // RETRAIN MODEL
    // =========================================

    @GetMapping("/retrain")
    public String retrainModel() {

        return mlTrainingService
                .retrainModel();
    }
}