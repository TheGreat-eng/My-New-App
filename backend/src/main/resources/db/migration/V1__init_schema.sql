-- 1. Tạo bảng Users
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    full_name VARCHAR(100)
);

-- 2. Tạo bảng Roles
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- Nạp sẵn các Role cơ bản (Seed data)
INSERT INTO roles (name) VALUES ('ROLE_USER');
INSERT INTO roles (name) VALUES ('ROLE_DEV');
INSERT INTO roles (name) VALUES ('ROLE_APPROVER');
INSERT INTO roles (name) VALUES ('ROLE_ADMIN');

-- 3. Tạo bảng trung gian User_Roles (Many-to-Many)
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 4. Tạo bảng Apps
CREATE TABLE apps (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    icon_url VARCHAR(500),
    package_name VARCHAR(255), -- Ví dụ: com.viettel.app
    created_by BIGINT,
    CONSTRAINT fk_app_creator FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL
);

-- 5. Tạo bảng App_Versions
CREATE TABLE app_versions (
    id BIGSERIAL PRIMARY KEY,
    app_id BIGINT NOT NULL,
    version VARCHAR(50) NOT NULL, -- Ví dụ: 1.0.0
    file_url VARCHAR(500),
    file_size BIGINT,
    status VARCHAR(50) NOT NULL, -- DRAFT, PUBLISHED...
    release_note TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP,
    CONSTRAINT fk_version_app FOREIGN KEY (app_id) REFERENCES apps(id) ON DELETE CASCADE
);