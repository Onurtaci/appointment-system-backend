# Appointment System - Backend API

A Spring Boot REST API for managing medical appointments between doctors and patients.

## Features

### User Management
- User registration and authentication (JWT)
- Role-based authorization (PATIENT, DOCTOR)
- Secure password hashing with BCrypt

### Appointment Management
- Create, update, and manage appointments
- Conflict detection for overlapping appointments
- Appointment status management (PENDING, APPROVED, REJECTED)
- Flexible appointment duration (15-120 minutes in 15-minute increments)
- Automatic available time slot calculation

### Doctor Schedule Management
- Flexible working hours (Morning, Afternoon, Full Day shifts)
- Automatic lunch break handling (12:00-13:00)
- Weekly schedule management
- Available time slot calculation algorithm
- Schedule summary endpoints

### Patient Tracking
- Doctors can add notes to appointments
- Patients can view appointment history and doctor notes
- Enhanced patient history tracking

## Tech Stack

- **Spring Boot 3.2.3** - Main framework
- **Spring Security** - Security and authentication
- **Spring Data JPA** - Database access
- **PostgreSQL** - Database
- **Flyway** - Database migration management
- **MapStruct** - DTO mapping
- **Lombok** - Code reduction
- **JWT** - Token-based authentication

## Prerequisites

- Java 17+
- PostgreSQL 14+
- Maven 3.8+

## Installation

### 1. Database Setup

```sql
-- Create database in PostgreSQL
CREATE DATABASE clinic_db;
CREATE USER clinic WITH PASSWORD 'clinic123';
GRANT ALL PRIVILEGES ON DATABASE clinic_db TO clinic;
```

### 2. Application Setup

```bash
# Install dependencies
mvn clean install

# Run database migrations
mvn flyway:migrate

# Start the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## API Documentation

Once the application is running, access Swagger UI at `http://localhost:8080/swagger-ui.html`

### Main Endpoints

#### Authentication
- `POST /api/auth/register` - User registration
- `POST /api/auth/login` - User login
- `GET /api/auth/me` - Current user information

#### Appointment Management
- `POST /api/appointments` - Create new appointment
- `GET /api/appointments/me` - Patient appointments
- `GET /api/appointments/doctor/me` - Doctor appointments
- `PATCH /api/appointments/{id}/status` - Update appointment status
- `POST /api/appointments/{id}/notes` - Add notes to appointment
- `GET /api/appointments/available-slots` - Get available time slots

#### Doctor Schedule Management
- `POST /api/doctor-schedules/{doctorId}` - Create schedule
- `GET /api/doctor-schedules/{doctorId}` - List schedules
- `PUT /api/doctor-schedules/{doctorId}/{scheduleId}` - Update schedule
- `DELETE /api/doctor-schedules/{doctorId}/{scheduleId}` - Delete schedule
- `GET /api/doctor-schedules/{doctorId}/available-slots` - Get available slots
- `GET /api/doctor-schedules/{doctorId}/availability` - Check availability
- `GET /api/doctor-schedules/{doctorId}/weekly-summary` - Weekly summary

## Project Structure

```
src/
├── main/
│   ├── java/com/clinic/appointmentsystem/
│   │   ├── domain/           # Business logic entities
│   │   ├── application/      # Application services
│   │   ├── persistence/      # Data access layer
│   │   ├── webapi/           # REST API controllers
│   │   └── infrastructure/   # Infrastructure components
│   └── resources/
│       ├── db/migration/     # Database migrations
│       └── application.yaml  # Configuration
└── test/                     # Test files
```

## Configuration

### Application Configuration (`application.yaml`)

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/clinic_db
    username: clinic
    password: clinic123
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false

jwt:
  secret: your-secret-key-here
  expiration: 3600000  # 1 hour
```

## Testing

```bash
# Run tests
mvn test
```

## License

This project is licensed under the MIT License. 