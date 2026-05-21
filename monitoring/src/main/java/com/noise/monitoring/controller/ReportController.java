package com.noise.monitoring.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.noise.monitoring.model.AlarmLog;
import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.repository.SensorReadingRepository;
import com.noise.monitoring.repository.AlarmLogRepository;
import com.noise.monitoring.service.ReportService;

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

    // ===== EXPORT =====
    @GetMapping("/export/{classroomId}")
    public ResponseEntity<byte[]> export(
            @PathVariable Long classroomId) throws Exception {

        byte[] excelData = reportService.exportByClassroom(classroomId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("noise_report_classroom_" + classroomId + ".xlsx")
                        .build());

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    @GetMapping("/export-and-clean/{classroomId}")
    public ResponseEntity<byte[]> exportAndClean(
            @PathVariable Long classroomId) throws Exception {

        byte[] excelData = reportService.exportAndClean(classroomId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("noise_report_classroom_" + classroomId + ".xlsx")
                        .build());

        return new ResponseEntity<>(excelData, headers, HttpStatus.OK);
    }

    // ===== LATEST DATA =====
    @GetMapping("/latest-readings/{classroomId}")
    public List<SensorReading> getLatestReadings(
            @PathVariable Long classroomId) {

        return readingRepository
                .findTop200ByClassroomIdOrderByRecordedAtDesc(classroomId);
    }

    @GetMapping("/latest-alarms/{classroomId}")
    public List<AlarmLog> getLatestAlarms(
            @PathVariable Long classroomId) {

        return alarmRepository
                .findTop100ByClassroomIdOrderByTriggeredAtDesc(classroomId);
    }

    // ===== COUNTS =====
    @GetMapping("/counts/{classroomId}")
    public Map<String, Object> getCounts(
            @PathVariable Long classroomId) {

        long totalReadings =
                readingRepository.countByClassroomId(classroomId);

        long totalAlarms =
                alarmRepository.countByClassroomId(classroomId);

        return Map.of(
                "totalReadings", totalReadings,
                "totalAlarms", totalAlarms
        );
    }
}