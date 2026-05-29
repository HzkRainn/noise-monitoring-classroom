CREATE DATABASE IF NOT EXISTS noise_monitoring_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE noise_monitoring_db;

-- ===============================
-- 1. CLASSROOMS
-- ===============================
CREATE TABLE classrooms (
    id INT AUTO_INCREMENT PRIMARY KEY,
    room_name VARCHAR(100),
    building VARCHAR(100),
    capacity INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;


-- ===============================
-- 2. SENSOR READINGS
-- ===============================
CREATE TABLE sensor_readings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id INT,
    db_level FLOAT,
    dominant_frequency FLOAT,
    variance FLOAT,
    spike_count INT,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_classroom (classroom_id),
    
    CONSTRAINT fk_sensor_classroom
        FOREIGN KEY (classroom_id)
        REFERENCES classrooms(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;


-- ===============================
-- 3. SYSTEM MODES
-- ===============================
CREATE TABLE system_modes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id INT,
    mode VARCHAR(50),
    mean_db FLOAT,
    std_dev FLOAT,
    threshold_value FLOAT,
    calculated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_mode_classroom (classroom_id),
    
    CONSTRAINT fk_mode_classroom
        FOREIGN KEY (classroom_id)
        REFERENCES classrooms(id)
        ON DELETE SET NULL
) ENGINE=InnoDB;


-- ===============================
-- 4. NOISE CLASSIFICATION
-- ===============================
CREATE TABLE noise_classification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reading_id BIGINT,
    classification ENUM('HUMAN','NON_HUMAN'),
    confidence_score FLOAT,
    classified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_reading (reading_id),
    
    CONSTRAINT fk_classification_reading
        FOREIGN KEY (reading_id)
        REFERENCES sensor_readings(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;


-- ===============================
-- 5. ALARM LOGS
-- ===============================
CREATE TABLE alarm_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    classroom_id INT,
    reading_id BIGINT,
    triggered_threshold FLOAT,
    actual_db FLOAT,
    mode_at_time ENUM(
        'DEFAULT',
        'DISCUSSION',
        'FOCUSED',
        'CHAOTIC',
        'HUMAN_ACTIVITY',
        'MACHINE_NOISE',
        'EXAM_MODE'
    ),
    triggered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_alarm_classroom (classroom_id),
    INDEX idx_alarm_reading (reading_id),
    
    CONSTRAINT fk_alarm_classroom
        FOREIGN KEY (classroom_id)
        REFERENCES classrooms(id)
        ON DELETE SET NULL,
        
    CONSTRAINT fk_alarm_reading
        FOREIGN KEY (reading_id)
        REFERENCES sensor_readings(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;