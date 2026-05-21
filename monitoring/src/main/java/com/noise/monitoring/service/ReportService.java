package com.noise.monitoring.service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.noise.monitoring.model.SensorReading;
import com.noise.monitoring.model.AlarmLog;
import com.noise.monitoring.repository.SensorReadingRepository;
import com.noise.monitoring.repository.AlarmLogRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Set;

@Service
public class ReportService {

    @Autowired
    private SensorReadingRepository readingRepository;

    @Autowired
    private AlarmLogRepository alarmRepository;

    // =====================================================
    // EXPORT XLSX REPORT
    // =====================================================
    public byte[] exportByClassroom(Long classroomId)
            throws IOException {

        List<SensorReading> readings =
                readingRepository
                .findByClassroomIdOrderByRecordedAtAsc(
                        classroomId
                );

        List<AlarmLog> alarms =
                alarmRepository.findByClassroomId(
                        classroomId
                );

        Workbook workbook =
                new XSSFWorkbook();

        Sheet sheet =
                workbook.createSheet(
                        "Noise Report"
                );

        // =====================================================
        // HEADER
        // =====================================================
        Row header = sheet.createRow(0);

        String[] columns = {

                "ID",
                "Classroom",
                "dB Level",
                "Frequency",
                "Variance",
                "Spike",
                "Mode",
                "Threshold",
                "Alarm",
                "Recorded At"
        };

        for (int i = 0; i < columns.length; i++) {

            Cell cell = header.createCell(i);

            cell.setCellValue(columns[i]);
        }

        int rowIdx = 1;

        // =====================================================
        // ALARM REFERENCE
        // =====================================================
        Set<Long> alarmReadingIds =
                alarms.stream()
                .map(AlarmLog::getReadingId)
                .collect(Collectors.toSet());

        // =====================================================
        // INSERT DATA
        // =====================================================
        for (SensorReading reading : readings) {

            Row row =
                    sheet.createRow(rowIdx++);

            row.createCell(0)
                    .setCellValue(
                            reading.getId()
                    );

            row.createCell(1)
                    .setCellValue(
                            reading.getClassroomId()
                    );

            row.createCell(2)
                    .setCellValue(
                            reading.getDbLevel()
                    );

            row.createCell(3)
                    .setCellValue(
                            reading.getDominantFrequency()
                    );

            row.createCell(4)
                    .setCellValue(
                            reading.getVariance()
                    );

            row.createCell(5)
                    .setCellValue(
                            reading.getSpikeCount()
                    );

            row.createCell(6)
                    .setCellValue(
                            reading.getModeAtTime() != null
                                    ? reading.getModeAtTime().name()
                                    : "N/A"
                    );

            row.createCell(7)
                    .setCellValue(
                            reading.getThresholdAtTime() != null
                                    ? reading.getThresholdAtTime()
                                    : 0
                    );

            boolean isAlarm =
                    alarmReadingIds.contains(
                            reading.getId()
                    );

            row.createCell(8)
                    .setCellValue(
                            isAlarm ? "YES" : "NO"
                    );

            row.createCell(9)
                    .setCellValue(
                            reading.getRecordedAt().toString()
                    );
        }

        // =====================================================
        // AUTO SIZE
        // =====================================================
        for (int i = 0; i < columns.length; i++) {

            sheet.autoSizeColumn(i);
        }

        // =====================================================
        // EXPORT
        // =====================================================
        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        workbook.write(out);

        workbook.close();

        return out.toByteArray();
    }

    // =====================================================
    // EXPORT + AUTO CLEAN
    // =====================================================
    public byte[] exportAndClean(Long classroomId)
            throws IOException {

        List<SensorReading> readings =
                readingRepository
                .findByClassroomIdOrderByRecordedAtAsc(
                        classroomId
                );

        int total = readings.size();

        // =====================================================
        // EXPORT FIRST
        // =====================================================
        byte[] file =
                exportByClassroom(classroomId);

        // =====================================================
        // AUTO CLEAN
        // =====================================================
        if (total > 15000) {

            // SISAKAN 8500 DATA TERBARU
            int deleteCount = total - 8500;

            List<SensorReading> toDelete =
                    new ArrayList<>(
                            readings.subList(0, deleteCount)
                    );

            // DELETE OLD READINGS
            readingRepository.deleteAll(toDelete);
        }

        return file;
    }
}