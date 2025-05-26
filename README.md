# Medical Appointment System - Backend

A Spring Boot backend application for managing medical appointments between doctors and patients.

## Features

- User authentication and authorization (JWT-based)
- Role-based access control (Doctor/Patient)
- Appointment management (create, update, cancel, approve, reject)
- Doctor schedule management
- Medical notes for appointments
- RESTful API endpoints

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Security
- Spring Data JPA
- PostgreSQL
- Maven
- JWT for authentication
- Lombok
- MapStruct

## Prerequisites

- JDK 17 or later
- Maven 3.6 or later
- PostgreSQL 12 or later

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/appointment-system-backend.git
   cd appointment-system-backend
   ```

2. Configure the database:
   - Create a PostgreSQL database
   - Update `application.properties` with your database credentials

3. Build the project:
   ```bash
   mvn clean install
   ```

4. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8080`

## API Documentation

API documentation is available at `/swagger-ui.html` when running the application.

## Environment Variables

Create an `application-dev.properties` file with the following properties:

```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/appointment_system
spring.datasource.username=your_username
spring.datasource.password=your_password

# JWT Configuration
jwt.secret=your_jwt_secret
jwt.expiration=86400000

# Server Configuration
server.port=8080
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. 