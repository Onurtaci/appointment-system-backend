-- Drop existing tables if they exist
DROP TABLE IF EXISTS appointments CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- Create users table
CREATE TABLE users
(
    id            UUID PRIMARY KEY,
    first_name    VARCHAR(60)  NOT NULL,
    last_name     VARCHAR(60)  NOT NULL,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(256) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at    TIMESTAMP WITHOUT TIME ZONE NOT NULL
);

-- Create appointments table
CREATE TABLE appointments
(
    id               UUID PRIMARY KEY,
    patient_id       UUID        NOT NULL REFERENCES users (id),
    doctor_id        UUID        NOT NULL REFERENCES users (id),
    appointment_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status           VARCHAR(20) NOT NULL,
    note             TEXT,
    created_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at       TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT uk_doctor_time UNIQUE (doctor_id, appointment_time)
);
