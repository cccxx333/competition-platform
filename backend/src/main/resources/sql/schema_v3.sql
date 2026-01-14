-- Schema initialization script based on db_schema.md
-- This script assumes an empty database and should run before init.sql

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_no VARCHAR(32) NOT NULL,
    role VARCHAR(20) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(64),
    username VARCHAR(64),
    email VARCHAR(128),
    phone VARCHAR(32),
    avatar_url VARCHAR(255),
    school VARCHAR(128),
    major VARCHAR(128),
    grade VARCHAR(32),
    created_at DATETIME,
    updated_at DATETIME,
    UNIQUE KEY uk_users_account_no (account_no),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    category VARCHAR(64),
    description TEXT,
    is_active BIT(1) DEFAULT b'1',
    created_at DATETIME,
    updated_at DATETIME,
    UNIQUE KEY uk_skills_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    level INT,
    created_at DATETIME,
    CONSTRAINT fk_user_skills_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS competitions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    organizer VARCHAR(128),
    start_date DATE,
    end_date DATE,
    registration_deadline DATE,
    min_team_size INT DEFAULT 1,
    max_team_size INT NOT NULL,
    category VARCHAR(64),
    level VARCHAR(64),
    status VARCHAR(20) DEFAULT 'UPCOMING',
    created_by BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_competitions_created_by FOREIGN KEY (created_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS competition_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    importance INT DEFAULT 1,
    CONSTRAINT fk_competition_skills_competition FOREIGN KEY (competition_id) REFERENCES competitions (id),
    CONSTRAINT fk_competition_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    leader_id BIGINT NOT NULL,
    name VARCHAR(128) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'RECRUITING',
    closed_at DATETIME,
    closed_by BIGINT,
    created_at DATETIME,
    updated_at DATETIME,
    CONSTRAINT fk_teams_competition FOREIGN KEY (competition_id) REFERENCES competitions (id),
    CONSTRAINT fk_teams_leader FOREIGN KEY (leader_id) REFERENCES users (id),
    CONSTRAINT fk_teams_closed_by FOREIGN KEY (closed_by) REFERENCES users (id),
    UNIQUE KEY uk_teams_competition_leader (competition_id, leader_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teacher_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    applied_at DATETIME,
    reviewed_at DATETIME,
    reviewed_by BIGINT,
    review_comment VARCHAR(255),
    generated_team_id BIGINT,
    CONSTRAINT fk_teacher_applications_competition FOREIGN KEY (competition_id) REFERENCES competitions (id),
    CONSTRAINT fk_teacher_applications_teacher FOREIGN KEY (teacher_id) REFERENCES users (id),
    CONSTRAINT fk_teacher_applications_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id),
    CONSTRAINT fk_teacher_applications_generated_team FOREIGN KEY (generated_team_id) REFERENCES teams (id),
    UNIQUE KEY uk_teacher_applications_competition_teacher (competition_id, teacher_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teacher_application_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    teacher_application_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    weight INT DEFAULT 1,
    CONSTRAINT fk_teacher_application_skills_application FOREIGN KEY (teacher_application_id) REFERENCES teacher_applications (id),
    CONSTRAINT fk_teacher_application_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS team_skills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    skill_id BIGINT NOT NULL,
    weight INT DEFAULT 1,
    CONSTRAINT fk_team_skills_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_skills_skill FOREIGN KEY (skill_id) REFERENCES skills (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    competition_id BIGINT NOT NULL,
    team_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    is_active BIT(1) NOT NULL DEFAULT b'1',
    applied_at DATETIME,
    reviewed_at DATETIME,
    reviewed_by BIGINT,
    removed_at DATETIME,
    removed_by BIGINT,
    reason VARCHAR(255),
    CONSTRAINT fk_applications_competition FOREIGN KEY (competition_id) REFERENCES competitions (id),
    CONSTRAINT fk_applications_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_applications_student FOREIGN KEY (student_id) REFERENCES users (id),
    CONSTRAINT fk_applications_reviewer FOREIGN KEY (reviewed_by) REFERENCES users (id),
    CONSTRAINT fk_applications_remover FOREIGN KEY (removed_by) REFERENCES users (id),
    UNIQUE KEY uk_applications_student_competition_active (student_id, competition_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS team_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'MEMBER',
    joined_at DATETIME,
    left_at DATETIME,
    CONSTRAINT fk_team_members_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_members_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS team_discussion_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    parent_post_id BIGINT,
    content TEXT NOT NULL,
    created_at DATETIME,
    updated_at DATETIME,
    deleted_at DATETIME,
    deleted_by BIGINT,
    CONSTRAINT fk_team_discussion_posts_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_discussion_posts_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_team_discussion_posts_parent FOREIGN KEY (parent_post_id) REFERENCES team_discussion_posts (id),
    CONSTRAINT fk_team_discussion_posts_deleted_by FOREIGN KEY (deleted_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS team_submissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id BIGINT NOT NULL,
    competition_id BIGINT NOT NULL,
    submitted_by BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_url VARCHAR(512) NOT NULL,
    remark VARCHAR(255),
    submitted_at DATETIME,
    is_current BIT(1) DEFAULT b'1',
    CONSTRAINT fk_team_submissions_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_team_submissions_competition FOREIGN KEY (competition_id) REFERENCES competitions (id),
    CONSTRAINT fk_team_submissions_submitter FOREIGN KEY (submitted_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS user_behaviors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    behavior_type VARCHAR(20) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT NOT NULL,
    weight INT DEFAULT 1,
    created_at DATETIME,
    CONSTRAINT fk_user_behaviors_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Recommended high-frequency indexes
CREATE INDEX idx_competitions_status_deadline ON competitions (status, registration_deadline);
CREATE INDEX idx_applications_team_status ON applications (team_id, status);
CREATE INDEX idx_team_members_team_left_at ON team_members (team_id, left_at);
CREATE INDEX idx_user_behaviors_user_created_at ON user_behaviors (user_id, created_at);
