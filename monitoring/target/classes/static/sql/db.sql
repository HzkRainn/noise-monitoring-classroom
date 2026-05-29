-- =========================================================
-- SMART NOISE MONITORING SYSTEM
-- FINAL STABLE SYNCED DATABASE
-- DATABASE : noise_monitoring_db
-- STATUS   : FULLY SYNCHRONIZED VERSION
-- =========================================================

CREATE DATABASE IF NOT EXISTS noise_monitoring_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE noise_monitoring_db;

-- =========================================================
-- 1. CLASSROOMS
-- =========================================================

CREATE TABLE classrooms (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    room_name VARCHAR(100) NOT NULL,

    location VARCHAR(255),

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP

) ENGINE=InnoDB;

-- =========================================================
-- 2. SENSOR READINGS
-- =========================================================

CREATE TABLE sensor_readings (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    classroom_id BIGINT NULL,

    db_level DOUBLE NOT NULL,

    dominant_frequency DOUBLE NOT NULL,

    variance DOUBLE NOT NULL,

    spike_count INT NOT NULL,

    training_label VARCHAR(50),

    ml_prediction VARCHAR(50),

    mode_at_time ENUM(
        'DEFAULT',
        'DISCUSSION',
        'FOCUSED',
        'CHAOTIC',
        'HUMAN_ACTIVITY',
        'MACHINE_NOISE',
        'EXAM_MODE'
    ) DEFAULT 'DEFAULT',

    threshold_at_time DOUBLE DEFAULT 0,

    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    mqtt_source VARCHAR(100),

    mqtt_topic VARCHAR(100),

    CONSTRAINT fk_sensor_classroom
    FOREIGN KEY (classroom_id)
    REFERENCES classrooms(id)
    ON DELETE SET NULL

) ENGINE=InnoDB;

-- =========================================================
-- 3. SYSTEM MODES
-- =========================================================

CREATE TABLE system_modes (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    classroom_id BIGINT NOT NULL,

    mode ENUM(
        'DEFAULT',
        'DISCUSSION',
        'FOCUSED',
        'CHAOTIC',
        'HUMAN_ACTIVITY',
        'MACHINE_NOISE',
        'EXAM_MODE'
    ) DEFAULT 'DEFAULT',

    mean_db DOUBLE,

    std_dev DOUBLE,

    threshold_value DOUBLE,

    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_system_mode_classroom
    FOREIGN KEY (classroom_id)
    REFERENCES classrooms(id)
    ON DELETE CASCADE

) ENGINE=InnoDB;

-- =========================================================
-- 4. NOISE CLASSIFICATION
-- OPTIONAL ANALYTICS + RETRAINING TABLE
-- =========================================================

CREATE TABLE noise_classification (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    sensor_reading_id BIGINT NOT NULL,

    classification_label VARCHAR(50),

    confidence_score DOUBLE,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_noise_sensor
    FOREIGN KEY (sensor_reading_id)
    REFERENCES sensor_readings(id)
    ON DELETE CASCADE

) ENGINE=InnoDB;

-- =========================================================
-- 5. ALARM LOGS
-- =========================================================

CREATE TABLE alarm_logs (

    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    classroom_id BIGINT NOT NULL,

    reading_id BIGINT,

    threshold_snapshot DOUBLE,

    actual_db DOUBLE,

    mode_snapshot ENUM(
        'DEFAULT',
        'DISCUSSION',
        'FOCUSED',
        'CHAOTIC',
        'HUMAN_ACTIVITY',
        'MACHINE_NOISE',
        'EXAM_MODE'
    ) DEFAULT 'DEFAULT',

    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_alarm_classroom
    FOREIGN KEY (classroom_id)
    REFERENCES classrooms(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_alarm_reading
    FOREIGN KEY (reading_id)
    REFERENCES sensor_readings(id)
    ON DELETE CASCADE

) ENGINE=InnoDB;

-- =========================================================
-- 6. DEFAULT CLASSROOM DATA
-- =========================================================

INSERT INTO classrooms (room_name, location)
VALUES

('Classroom A', 'Building A'),
('Classroom B', 'Building B');

-- =========================================================
-- 7. REALTIME DASHBOARD VIEW
-- =========================================================

CREATE OR REPLACE VIEW realtime_dashboard_view AS

SELECT

    sr.id,

    sr.classroom_id,

    c.room_name,

    sr.db_level,

    sr.dominant_frequency,

    sr.variance,

    sr.spike_count,

    sr.training_label,

    sr.ml_prediction,

    sr.mode_at_time,

    sr.threshold_at_time,

    sr.recorded_at,

    sr.mqtt_source,

    sr.mqtt_topic

FROM sensor_readings sr

LEFT JOIN classrooms c
ON sr.classroom_id = c.id;

-- =========================================================
-- 8. ALARM ANALYTICS VIEW
-- =========================================================

CREATE OR REPLACE VIEW alarm_analytics_view AS

SELECT

    al.id,

    al.classroom_id,

    c.room_name,

    al.reading_id,

    al.threshold_snapshot,

    al.actual_db,

    (al.actual_db - al.threshold_snapshot)
    AS threshold_difference,

    al.mode_snapshot,

    al.triggered_at

FROM alarm_logs al

LEFT JOIN classrooms c
ON al.classroom_id = c.id;

-- =========================================================
-- 9. PERFORMANCE INDEXES
-- =========================================================

CREATE INDEX idx_sensor_recorded_at
ON sensor_readings(recorded_at);

CREATE INDEX idx_sensor_prediction
ON sensor_readings(ml_prediction);

CREATE INDEX idx_sensor_mode
ON sensor_readings(mode_at_time);

CREATE INDEX idx_alarm_triggered
ON alarm_logs(triggered_at);

CREATE INDEX idx_alarm_mode
ON alarm_logs(mode_snapshot);

CREATE INDEX idx_system_mode
ON system_modes(mode);

CREATE INDEX idx_system_calculated
ON system_modes(calculated_at);

CREATE INDEX idx_noise_classification
ON noise_classification(classification_label);

-- =========================================================
-- 10. OPTIONAL TRAINING PIPELINE
-- =========================================================
--
-- Import dataset CSV ke sensor_readings
--
-- UPDATE sensor_readings
-- SET classroom_id = 1
-- WHERE classroom_id IS NULL;
--
-- Generate historical classification:
--
-- INSERT INTO noise_classification (
--     sensor_reading_id,
--     classification_label,
--     confidence_score
-- )
-- SELECT
--     id,
--     ml_prediction,
--     0.95
-- FROM sensor_readings;
--
-- =========================================================
-- FINAL STATUS
-- =========================================================
--
-- ✔ MQTT Hybrid Compatible
-- ✔ Spring Boot Compatible
-- ✔ MariaDB Compatible
-- ✔ Adaptive Threshold Compatible
-- ✔ LearningEngineService Compatible
-- ✔ RealtimeNoiseProcessingService Compatible
-- ✔ Dashboard Compatible
-- ✔ ReportService Compatible
-- ✔ Retraining Pipeline Compatible
-- ✔ Random Forest Compatible
-- ✔ Alarm Analytics Compatible
-- ✔ Future Confusion Matrix Ready
-- ✔ ENUM-safe Mode Architecture
-- ✔ Foreign Key Stable
-- ✔ Recovery-safe Structure
--
-- =========================================================