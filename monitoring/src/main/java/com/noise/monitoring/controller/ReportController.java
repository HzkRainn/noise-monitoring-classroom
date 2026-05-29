package com.noise.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.noise.monitoring.model.AlarmLog;
import com.noise.monitoring.model.SensorReading;

import com.noise.monitoring.repository.AlarmLogRepository;
import com.noise.monitoring.repository.SensorReadingRepository;

import com.noise.monitoring.service.ReportService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private SensorReadingRepository readingRepository;

    @Autowired
    private AlarmLogRepository alarmRepository;

    // =====================================================
    // RETENTION CONFIG
    // =====================================================

    private static final long WARNING_LIMIT =
            12000;

    private static final long AUTO_CLEANUP_LIMIT =
            15000;

    private static final long RETAIN_AFTER_CLEANUP =
            8000;

    // =====================================================
    // EXPORT REPORT
    // =====================================================

    @GetMapping("/export/{classroomId}")
    public ResponseEntity<byte[]> export(
            @PathVariable Long classroomId
    ) throws Exception {

        byte[] excelData =
                reportService.exportByClassroom(
                        classroomId
                );

        String filename =
                generateFilename(
                        classroomId,
                        false
                );

        HttpHeaders headers =
                buildExcelHeaders(filename);

        return new ResponseEntity<>(
                excelData,
                headers,
                HttpStatus.OK
        );
    }

    // =====================================================
    // EXPORT + CLEANUP
    // =====================================================

    @GetMapping("/export-and-clean/{classroomId}")
    public ResponseEntity<?> exportAndClean(
            @PathVariable Long classroomId
    ) {

        try {

            long totalReadings =
                    readingRepository
                    .countByClassroomId(
                            classroomId
                    );

            if (totalReadings < 11500) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "success",
                                        false,

                                        "message",
                                        "Manual cleanup hanya dapat dilakukan jika total data >= 11500",

                                        "currentData",
                                        totalReadings
                                )
                        );
            }

            byte[] excelData =
                    reportService.exportAndClean(
                            classroomId
                    );

            String filename =
                    generateFilename(
                            classroomId,
                            true
                    );

            HttpHeaders headers =
                    buildExcelHeaders(filename);

            return new ResponseEntity<>(
                    excelData,
                    headers,
                    HttpStatus.OK
            );

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(
                            Map.of(
                                    "success",
                                    false,

                                    "message",
                                    e.getMessage()
                            )
                    );
        }
    }

    // =====================================================
    // LATEST SENSOR READINGS
    // =====================================================

    @GetMapping("/latest-readings/{classroomId}")
    public List<SensorReading> getLatestReadings(
            @PathVariable Long classroomId
    ) {

        return readingRepository
                .findTop200ByClassroomIdOrderByRecordedAtDesc(
                        classroomId
                );
    }

    // =====================================================
    // LATEST ALARM LOGS
    // =====================================================

    @GetMapping("/latest-alarms/{classroomId}")
    public List<AlarmLog> getLatestAlarms(
            @PathVariable Long classroomId
    ) {

        return alarmRepository
                .findTop100ByClassroomIdOrderByTriggeredAtDesc(
                        classroomId
                );
    }

    // =====================================================
    // REPORT COUNTS
    // =====================================================

    @GetMapping("/counts/{classroomId}")
    public Map<String, Object> getCounts(
            @PathVariable Long classroomId
    ) {

        long totalReadings =
                readingRepository
                .countByClassroomId(
                        classroomId
                );

        long totalAlarms =
                alarmRepository
                .countByClassroomId(
                        classroomId
                );

        String databaseStatus =
                determineDatabaseStatus(
                        totalReadings
                );

        boolean canManualCleanup =
                totalReadings >= 11500;

        Map<String, Object> response =
                new HashMap<>();

        response.put(
                "totalReadings",
                totalReadings
        );

        response.put(
                "totalAlarms",
                totalAlarms
        );

        response.put(
                "databaseStatus",
                databaseStatus
        );

        response.put(
                "cleanupThreshold",
                AUTO_CLEANUP_LIMIT
        );

        response.put(
                "retainedTrainingData",
                RETAIN_AFTER_CLEANUP
        );

        response.put(
                "canManualCleanup",
                canManualCleanup
        );

        response.put(
                "lastUpdated",
                LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"
                        )
                )
        );

        return response;
    }

    // =====================================================
    // DATABASE HEALTH
    // =====================================================

    @GetMapping("/database-health/{classroomId}")
    public Map<String, Object> getDatabaseHealth(
            @PathVariable Long classroomId
    ) {

        long totalReadings =
                readingRepository
                .countByClassroomId(
                        classroomId
                );

        long totalAlarms =
                alarmRepository
                .countByClassroomId(
                        classroomId
                );

        String status =
                determineDatabaseStatus(
                        totalReadings
                );

        String maintenanceMessage =
                getMaintenanceMessage(
                        totalReadings
                );

        return Map.of(

                "classroomId",
                classroomId,

                "totalReadings",
                totalReadings,

                "totalAlarms",
                totalAlarms,

                "status",
                status,

                "maintenance",
                maintenanceMessage,

                "retentionPolicy",
                AUTO_CLEANUP_LIMIT
                        + " / "
                        + RETAIN_AFTER_CLEANUP,

                "generatedAt",
                LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"
                        )
                )
        );
    }

    // =====================================================
    // BUILD HEADERS
    // =====================================================

    private HttpHeaders buildExcelHeaders(
            String filename
    ) {

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                )
        );

        headers.setContentDisposition(

                ContentDisposition
                        .attachment()
                        .filename(filename)
                        .build()
        );

        return headers;
    }

    // =====================================================
    // GENERATE FILENAME
    // =====================================================

    private String generateFilename(
            Long classroomId,
            boolean cleaned
    ) {

        String timestamp =
                LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyyMMdd_HHmmss"
                        )
                );

        if (cleaned) {

            return
                    "noise_report_cleanup_classroom_"
                    + classroomId
                    + "_"
                    + timestamp
                    + ".xlsx";
        }

        return
                "noise_report_classroom_"
                + classroomId
                + "_"
                + timestamp
                + ".xlsx";
    }

    // =====================================================
    // DATABASE STATUS
    // =====================================================

    private String determineDatabaseStatus(
            long totalReadings
    ) {

        if (totalReadings >= AUTO_CLEANUP_LIMIT) {

            return "AUTO CLEANUP ACTIVE";
        }

        if (totalReadings >= WARNING_LIMIT) {

            return "APPROACHING LIMIT";
        }

        return "SAFE";
    }

    // =====================================================
    // MAINTENANCE MESSAGE
    // =====================================================

    private String getMaintenanceMessage(
            long totalReadings
    ) {

        if (totalReadings >= AUTO_CLEANUP_LIMIT) {

            return
                    "Database memerlukan cleanup otomatis untuk menjaga performa sistem.";
        }

        if (totalReadings >= WARNING_LIMIT) {

            return
                    "Database mulai mendekati batas penyimpanan dan perlu monitoring.";
        }

        return
                "Database dalam kondisi stabil dan optimal.";
    }
}