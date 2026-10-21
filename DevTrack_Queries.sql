CREATE DATABASE IF NOT EXISTS DevTrack;
USE DevTrack;

select * from work_tickets;
select * from knowledge_vault;

-- 1. Tickets Table
CREATE TABLE IF NOT EXISTS work_tickets (
    ticket_code VARCHAR(30) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    ticket_type VARCHAR(50) NOT NULL,
    git_branch VARCHAR(150),
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    status VARCHAR(30) DEFAULT 'TODO',
    time_logged INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
ALTER TABLE work_tickets ADD COLUMN assigned_date VARCHAR(20) DEFAULT '';

-- 2. KT & Error Solution Vault Table
CREATE TABLE IF NOT EXISTS knowledge_vault (
    entry_id INT AUTO_INCREMENT PRIMARY KEY,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    solution TEXT NOT NULL
);

-- Insert sample starter data
INSERT IGNORE INTO work_tickets VALUES
('PROJ-101', 'Fix NullPointerException on Login', 'BUG', 'bug/login-npe', 'HIGH', 'IN_PROGRESS', 45, NOW(), '2026-10-11'),
('PROJ-102', 'Add Excel Export to Student Report', 'FEATURE', 'feat/excel-export', 'MEDIUM', 'TODO', 0, NOW(), '2026-10-11');

INSERT IGNORE INTO knowledge_vault (category, title, solution) VALUES
('ERROR_FIX', 'Port 8080 already in use (Spring Boot / Tomcat)', 'Run in CMD (Admin):\nnetstat -ano | findstr :8080\ntaskkill /PID <PID_NUMBER> /F'),
('ERROR_FIX', 'Hibernate LazyInitializationException', 'Cause: Accessing lazy collection outside active Hibernate session.\nFix: Use JOIN FETCH in HQL or @Transactional on the Service method.');