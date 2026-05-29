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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private SensorReadingRepository readingRepository;

    @Autowired
    private AlarmLogRepository alarmRepository;

    // =====================================================
    // RETENTION CONFIG
    // =====================================================

    private static final int AUTO_CLEANUP_LIMIT =
            15000;

    private static final int MANUAL_CLEANUP_LIMIT =
            11500;

    private static final int RETAIN_TRAINING_DATA =
            8000;

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
                        "Noise Monitoring Report"
                );

        // =====================================================
        // STYLES
        // =====================================================

        CellStyle titleStyle =
                workbook.createCellStyle();

        Font titleFont =
                workbook.createFont();

        titleFont.setBold(true);

        titleFont.setFontHeightInPoints(
                (short) 16
        );

        titleStyle.setFont(titleFont);

        CellStyle headerStyle =
                workbook.createCellStyle();

        Font headerFont =
                workbook.createFont();

        headerFont.setBold(true);

        headerFont.setColor(
                IndexedColors.WHITE.getIndex()
        );

        headerStyle.setFont(headerFont);

        headerStyle.setFillForegroundColor(
                IndexedColors.BLUE_GREY.getIndex()
        );

        headerStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        headerStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        headerStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        setBorder(headerStyle);

        CellStyle dataStyle =
                workbook.createCellStyle();

        dataStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        dataStyle.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        dataStyle.setWrapText(true);

        setBorder(dataStyle);

        CellStyle infoLabelStyle =
                workbook.createCellStyle();

        Font infoFont =
                workbook.createFont();

        infoFont.setBold(true);

        infoLabelStyle.setFont(infoFont);

        infoLabelStyle.setFillForegroundColor(
                IndexedColors.GREY_25_PERCENT.getIndex()
        );

        infoLabelStyle.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        infoLabelStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        setBorder(infoLabelStyle);

        CellStyle infoValueStyle =
                workbook.createCellStyle();

        infoValueStyle.setAlignment(
                HorizontalAlignment.CENTER
        );

        setBorder(infoValueStyle);

        // =====================================================
        // TITLE
        // =====================================================

        Row titleRow =
                sheet.createRow(0);

        Cell titleCell =
                titleRow.createCell(0);

        titleCell.setCellValue(
                "SMART NOISE MONITORING REPORT"
        );

        titleCell.setCellStyle(titleStyle);

        // =====================================================
        // REPORT INFO
        // =====================================================

        int totalReadings =
                readings.size();

        int totalAlarms =
                alarms.size();

        String systemStatus =
                totalReadings >= AUTO_CLEANUP_LIMIT
                        ? "AUTO CLEANUP ACTIVE"
                        : totalReadings >= 12000
                        ? "APPROACHING LIMIT"
                        : "SAFE";

        String generatedAt =
                LocalDateTime.now()
                .format(
                        DateTimeFormatter.ofPattern(
                                "yyyy-MM-dd HH:mm:ss"
                        )
                );

        // =====================================================
        // INFO TABLE (RIGHT SIDE)
        // =====================================================

        String[][] reportInfo = {

                {
                        "Classroom",
                        "Classroom " + classroomId
                },

                {
                        "Total Readings",
                        String.valueOf(totalReadings)
                },

                {
                        "Total Alarms",
                        String.valueOf(totalAlarms)
                },

                {
                        "Database Status",
                        systemStatus
                },

                {
                        "Retention Policy",
                        AUTO_CLEANUP_LIMIT
                                + " / "
                                + RETAIN_TRAINING_DATA
                },

                {
                        "Generated At",
                        generatedAt
                }
        };

        int infoStartCol = 12;

        for (int i = 0; i < reportInfo.length; i++) {

            Row row =
                    sheet.getRow(i);

            if (row == null) {

                row =
                        sheet.createRow(i);
            }

            Cell labelCell =
                    row.createCell(
                            infoStartCol
                    );

            labelCell.setCellValue(
                    reportInfo[i][0]
            );

            labelCell.setCellStyle(
                    infoLabelStyle
            );

            Cell valueCell =
                    row.createCell(
                            infoStartCol + 1
                    );

            valueCell.setCellValue(
                    reportInfo[i][1]
            );

            valueCell.setCellStyle(
                    infoValueStyle
            );
        }

        // =====================================================
        // HEADER
        // =====================================================

        int startRow = 3;

        Row header =
                sheet.createRow(startRow);

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

            Cell cell =
                    header.createCell(i);

            cell.setCellValue(
                    columns[i]
            );

            cell.setCellStyle(
                    headerStyle
            );
        }

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

        int rowIdx = startRow + 1;

        for (SensorReading reading : readings) {

            Row row =
                    sheet.createRow(rowIdx++);

            createCell(
                    row,
                    0,
                    reading.getId(),
                    dataStyle
            );

            createCell(
                    row,
                    1,
                    reading.getClassroomId(),
                    dataStyle
            );

            createCell(
                    row,
                    2,
                    reading.getDbLevel(),
                    dataStyle
            );

            createCell(
                    row,
                    3,
                    reading.getDominantFrequency(),
                    dataStyle
            );

            createCell(
                    row,
                    4,
                    reading.getVariance(),
                    dataStyle
            );

            createCell(
                    row,
                    5,
                    reading.getSpikeCount(),
                    dataStyle
            );

            createCell(
                    row,
                    6,
                    reading.getModeAtTime() != null
                            ? reading.getModeAtTime().name()
                            : "N/A",
                    dataStyle
            );

            createCell(
                    row,
                    7,
                    reading.getThresholdAtTime() != null
                            ? reading.getThresholdAtTime()
                            : 0,
                    dataStyle
            );

            boolean isAlarm =
                    alarmReadingIds.contains(
                            reading.getId()
                    );

            createCell(
                    row,
                    8,
                    isAlarm ? "YES" : "NO",
                    dataStyle
            );

            createCell(
                    row,
                    9,
                    reading.getRecordedAt()
                            .toString(),
                    dataStyle
            );
        }

        // =====================================================
        // AUTO SIZE
        // =====================================================

        int[] customWidths = {

                5000,
                5000,
                5000,
                6000,
                6000,
                5000,
                7000,
                6000,
                4500,
                9000
        };

        for (int i = 0; i < customWidths.length; i++) {

            sheet.setColumnWidth(
                    i,
                    customWidths[i]
            );
        }

        sheet.setColumnWidth(
                infoStartCol,
                6000
        );

        sheet.setColumnWidth(
                infoStartCol + 1,
                7000
        );

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

        int total =
                readings.size();

        // =====================================================
        // VALIDATION
        // =====================================================

        if (total < MANUAL_CLEANUP_LIMIT) {

            throw new RuntimeException(

                    "Cleanup hanya dapat dilakukan jika data >= "
                            + MANUAL_CLEANUP_LIMIT
            );
        }

        // =====================================================
        // EXPORT FIRST
        // =====================================================

        byte[] file =
                exportByClassroom(
                        classroomId
                );

        // =====================================================
        // CLEANUP
        // =====================================================

        if (total > RETAIN_TRAINING_DATA) {

            int deleteCount =
                    total -
                    RETAIN_TRAINING_DATA;

            List<SensorReading> toDelete =
                    new ArrayList<>(

                            readings.subList(
                                    0,
                                    deleteCount
                            )
                    );

            readingRepository.deleteAll(
                    toDelete
            );
        }

        return file;
    }

    // =====================================================
    // AUTO CLEANUP
    // =====================================================

    public void automaticCleanup(Long classroomId) {

        List<SensorReading> readings =
                readingRepository
                .findByClassroomIdOrderByRecordedAtAsc(
                        classroomId
                );

        int total =
                readings.size();

        if (total > AUTO_CLEANUP_LIMIT) {

            int deleteCount =
                    total -
                    RETAIN_TRAINING_DATA;

            List<SensorReading> toDelete =
                    new ArrayList<>(

                            readings.subList(
                                    0,
                                    deleteCount
                            )
                    );

            readingRepository.deleteAll(
                    toDelete
            );
        }
    }

    // =====================================================
    // CREATE CELL
    // =====================================================

    private void createCell(
            Row row,
            int column,
            Object value,
            CellStyle style
    ) {

        Cell cell =
                row.createCell(column);

        if (value instanceof Number) {

            cell.setCellValue(
                    ((Number) value)
                    .doubleValue()
            );

        } else {

            cell.setCellValue(
                    String.valueOf(value)
            );
        }

        cell.setCellStyle(style);
    }

    // =====================================================
    // BORDER STYLE
    // =====================================================

    private void setBorder(CellStyle style) {

        style.setBorderTop(
                BorderStyle.THIN
        );

        style.setBorderBottom(
                BorderStyle.THIN
        );

        style.setBorderLeft(
                BorderStyle.THIN
        );

        style.setBorderRight(
                BorderStyle.THIN
        );
    }
}