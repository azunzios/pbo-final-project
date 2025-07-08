-- Buat database jika belum ada
CREATE DATABASE IF NOT EXISTS petcare;

USE petcare;

-- Buat tabel pets
CREATE TABLE
    IF NOT EXISTS pets (
        id INT AUTO_INCREMENT PRIMARY KEY,
        name VARCHAR(100) NOT NULL,
        type VARCHAR(50) NOT NULL,
        birth_date DATE,
        weight DECIMAL(5, 2) DEFAULT 0.0,
        length DECIMAL(5, 2) DEFAULT 0.0,
        gender VARCHAR(20),
        notes TEXT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Buat tabel schedules
CREATE TABLE
    IF NOT EXISTS schedules (
        id INT AUTO_INCREMENT PRIMARY KEY,
        pet_id INT NOT NULL,
        care_type VARCHAR(50) NOT NULL,
        schedule_time TIME NOT NULL,
        days VARCHAR(50) NOT NULL,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (pet_id) REFERENCES pets (id) ON DELETE CASCADE
    );

-- Buat tabel care_logs
CREATE TABLE
    IF NOT EXISTS care_logs (
        id INT AUTO_INCREMENT PRIMARY KEY,
        pet_id INT NOT NULL,
        schedule_id INT,
        care_type VARCHAR(50) NOT NULL,
        timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        notes TEXT,
        done_by VARCHAR(100),
        FOREIGN KEY (pet_id) REFERENCES pets (id) ON DELETE CASCADE,
        FOREIGN KEY (schedule_id) REFERENCES schedules (id) ON DELETE SET NULL
    );

CREATE TABLE
    IF NOT_EXISTS pet_measurements (
        id INT AUTO_INCREMENT PRIMARY KEY,
        pet_id INT NOT NULL,
        recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        weight DECIMAL(5, 2),
        length DECIMAL(5, 2),
        notes TEXT,
        FOREIGN KEY (pet_id) REFERENCES pets (id)
    );

-- Tambahkan kolom baru ke tabel schedules
ALTER TABLE schedules
ADD COLUMN recurrence VARCHAR(20) DEFAULT 'Once' AFTER days,
ADD COLUMN category VARCHAR(50) DEFAULT 'General' AFTER recurrence,
ADD COLUMN notes TEXT AFTER category;

-- Ubah tipe data schedule_time menjadi DATETIME
ALTER TABLE schedules MODIFY COLUMN schedule_time DATETIME NOT NULL;

-- Masukkan data sampel ke tabel pets
INSERT INTO
    pets (name, type, birth_date, weight, gender, notes)
VALUES
    (
        'Buddy',
        'Dog',
        '2020-05-15',
        12.5,
        'Male',
        'Likes to play fetch'
    ),
    (
        'Whiskers',
        'Cat',
        '2021-01-10',
        4.2,
        'Female',
        'Loves to sleep in the sun'
    ),
    (
        'Tweety',
        'Bird',
        '2022-03-20',
        0.1,
        'Male',
        'Sings in the morning'
    );

-- Masukkan data sampel ke tabel schedules
INSERT INTO
    schedules (pet_id, care_type, schedule_time, days)
VALUES
    (
        1,
        'Feeding',
        '2025-07-06 08:00:00',
        'Mon,Tue,Wed,Thu,Fri,Sat,Sun'
    ),
    (
        1,
        'Walking',
        '2025-07-06 16:30:00',
        'Mon,Wed,Fri'
    ),
    (
        2,
        'Feeding',
        '2025-07-06 09:00:00',
        'Mon,Tue,Wed,Thu,Fri,Sat,Sun'
    ),
    (
        3,
        'Feeding',
        '2025-07-06 08:30:00',
        'Mon,Tue,Wed,Thu,Fri,Sat,Sun'
    );

-- Perbarui data berdasarkan care_type
UPDATE schedules
SET
    recurrence = 'Daily',
    category = 'Feeding'
WHERE
    care_type = 'Feeding';

UPDATE schedules
SET
    recurrence = 'Weekly',
    category = 'Exercise'
WHERE
    care_type = 'Walking';

-- Tampilkan struktur tabel schedules (opsional untuk debug)
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

-- Add default admin user (password: admin123)
INSERT IGNORE INTO users (username, password_hash, email, full_name)
VALUES ('admin', SHA2('admin123', 256), 'admin@example.com', 'Administrator');

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(64) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Settings table
CREATE TABLE IF NOT EXISTS user_settings (
    user_id INT PRIMARY KEY,
    require_notes BOOLEAN DEFAULT true,
    enable_notifications BOOLEAN DEFAULT true,
    reminder_minutes INT DEFAULT 15,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add user_id to existing tables
ALTER TABLE pets 
    ADD COLUMN user_id INT,
    ADD FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE schedules 
    ADD COLUMN user_id INT,
    ADD FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE care_logs 
    ADD COLUMN user_id INT,
    ADD FOREIGN KEY (user_id) REFERENCES users(id);
    
-- Add default admin user (password: admin123)
INSERT IGNORE INTO users (username, password_hash, email, full_name)
VALUES ('admin', SHA2('admin123', 256), 'admin@example.com', 'Administrator');

ALTER TABLE pet_measurements
DROP FOREIGN KEY pet_measurements_ibfk_1;

ALTER TABLE pet_measurements
ADD CONSTRAINT pet_measurements_ibfk_1
FOREIGN KEY (pet_id) REFERENCES pets(id)
ON DELETE CASCADE;

CREATE TABLE schedule_instances (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_id INTEGER,
    date DATE,
    is_done BOOLEAN,
    notes TEXT,
    done_at TIMESTAMP,
    FOREIGN KEY(schedule_id) REFERENCES schedules(id)
);

ALTER TABLE schedule_instances
DROP FOREIGN KEY schedule_instances_ibfk_1;

ALTER TABLE schedule_instances
ADD CONSTRAINT schedule_instances_ibfk_1
FOREIGN KEY (schedule_id) REFERENCES schedules(id)
ON DELETE CASCADE;


