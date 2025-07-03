-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS petcare;
USE petcare;

-- Create pets table
CREATE TABLE IF NOT EXISTS pets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    birth_date DATE,
    weight DOUBLE,
    gender VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create schedules table
CREATE TABLE IF NOT EXISTS schedules (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pet_id INT NOT NULL,
    care_type VARCHAR(50) NOT NULL,
    schedule_time TIME NOT NULL,
    days VARCHAR(50) NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE
);

-- Create care_logs table
CREATE TABLE IF NOT EXISTS care_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pet_id INT NOT NULL,
    schedule_id INT,
    care_type VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    notes TEXT,
    done_by VARCHAR(100),
    FOREIGN KEY (pet_id) REFERENCES pets(id) ON DELETE CASCADE,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE SET NULL
);

-- Insert sample data
INSERT INTO pets (name, type, birth_date, weight, gender, notes) VALUES
('Buddy', 'Dog', '2020-05-15', 12.5, 'Male', 'Likes to play fetch'),
('Whiskers', 'Cat', '2021-01-10', 4.2, 'Female', 'Loves to sleep in the sun'),
('Tweety', 'Bird', '2022-03-20', 0.1, 'Male', 'Sings in the morning');

INSERT INTO schedules (pet_id, care_type, schedule_time, days) VALUES
(1, 'Feeding', '08:00:00', 'Mon,Tue,Wed,Thu,Fri,Sat,Sun'),
(1, 'Walking', '16:30:00', 'Mon,Wed,Fri'),
(2, 'Feeding', '09:00:00', 'Mon,Tue,Wed,Thu,Fri,Sat,Sun'),
(3, 'Feeding', '08:30:00', 'Mon,Tue,Wed,Thu,Fri,Sat,Sun');
