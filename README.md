# Seat Reservation at Scale

A concurrent seat reservation backend built with Java, Spring Boot, MySQL, and Docker. The service is designed to prevent double booking, support idempotent reservation requests, enforce per-user booking limits, and expose operational health and metrics.

## Features

* **Show management:** Create shows with a configurable list of seats and ticket prices.
* **Atomic seat reservation:** Prevent multiple users from successfully booking the same seat.
* **Idempotency:** Return the original reservation for a retry with the same key and request body; reject reuse of the key with a different request.
* **Booking limits:** Enforce a configurable maximum number of confirmed seats per user per show.
* **All-or-nothing reservations:** Roll back a multi-seat booking if any requested seat cannot be reserved.
* **Cancellation:** Cancel reservations and release their seats.
* **Authentication:** Associate reservations with the user identity supplied through the Bearer-token authentication filter.
* **Structured error responses:** Return consistent API errors with HTTP status, request path, timestamp, and request ID.
* **Observability:** Spring Boot Actuator health endpoints, Micrometer metrics, and Prometheus metrics.
* **Request correlation:** Accept or generate an `X-Request-ID` and include it in application logs and the response header.

## Technology Stack

| Component         | Technology                                   |
| ----------------- | -------------------------------------------- |
| Language          | Java 17                                      |
| Backend framework | Spring Boot 3.4.12                           |
| Database          | MySQL 8.4                                    |
| Persistence       | Spring Data JPA and Hibernate                |
| Security          | Spring Security                              |
| Validation        | Jakarta Bean Validation                      |
| Metrics           | Spring Boot Actuator, Micrometer, Prometheus |
| Build tool        | Maven                                        |
| Containerization  | Docker and Docker Compose                    |
| Testing framework | JUnit 5 and Spring Boot Test                 |

## Architecture

```text
Client / Postman
       |
       v
Spring Boot REST API
       |
       +--> Security and Bearer-token filter
       |
       +--> Request ID filter and logging
       |
       +--> Controllers
       |
       +--> Services
       |       |
       |       +--> Reservation rules
       |       +--> Idempotency checks
       |       +--> Transaction management
       |       +--> Booking-limit enforcement
       |
       +--> Spring Data JPA / Hibernate
                    |
                    v
                  MySQL

Actuator --> Health checks and application metrics
Prometheus endpoint --> Scrapeable metrics
```

## Getting Started

### Prerequisites

* Java 17 or a compatible supported JDK
* Maven 3.9+
* Docker Desktop with Docker Compose, or a local MySQL 8.4 instance
* Postman or another HTTP client

### 1. Clone the repository

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd seat-reservation
```

### 2. Start MySQL

The following example starts MySQL locally using Docker:

```bash
docker run --name seat-reservation-mysql \
  -e MYSQL_DATABASE=seat_reservation \
  -e MYSQL_USER=reservation_user \
  -e MYSQL_PASSWORD=reservation_password \
  -e MYSQL_ROOT_PASSWORD=root_password \
  -p 3307:3306 \
  -d mysql:8.4
```

Wait until MySQL is ready before starting the application.

### 3. Configure the database

Configure the connection in `src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3307/seat_reservation?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}
spring.datasource.username=${DB_USERNAME:reservation_user}
spring.datasource.password=${DB_PASSWORD:reservation_password}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

server.port=${PORT:8080}
```

Use environment variables for credentials in deployed environments. Do not commit production credentials to Git.

### 4. Build and run

Build the project:

```bash
mvn clean package
```

Run the generated JAR:

```bash
java -jar target/seat-reservation-0.0.1-SNAPSHOT.jar
```

Alternatively, run `SeatReservationApplication.java` from STS.

The API should be available at:

```text
http://localhost:8080
```

## API Reference

Unless an endpoint is explicitly public, send the authentication header:

```http
Authorization: Bearer <user-token>
Content-Type: application/json
```

For the current assignment implementation, the Bearer-token value is treated as the user ID. This is a demonstration authentication mechanism, not production-grade JWT verification.

### 1. Create a show

`POST /shows`

Request:

```json
{
  "name": "Metric Test",
  "seats": ["A1", "A2", "A3", "A4"],
  "price_paise": 30000,
  "per_user_limit": 4
}
```

`price_paise` is the ticket price in paise. For example, `30000` paise equals ₹300.

Example response (`201 Created`):

```json
{
  "show_id": 1,
  "name": "Metric Test",
  "total_seats": 4,
  "price_paise": 30000,
  "per_user_limit": 4
}
```

Use the actual `show_id` returned by the application.

### 2. Reserve seats

`POST /shows/{showId}/reserve`

Request:

```json
{
  "seats": ["A1", "A2"],
  "idempotency_key": "booking-request-001"
}
```

Example response (`201 Created`):

```json
{
  "reservation_id": 1,
  "show_id": 1,
  "user_id": "user-123",
  "seats": ["A1", "A2"],
  "amount_paise": 60000,
  "status": "CONFIRMED"
}
```

The amount is represented in paise using an integer value.

### 3. Get show details

`GET /shows/{showId}`

Returns show information, seat statuses, and availability counts.

The intended seat-count invariant is:

```text
available_seats + held_seats + confirmed_seats = total_seats
```

### 4. Cancel a reservation

`POST /shows/reservations/{reservationId}/cancel`

The reservation must belong to the authenticated user. A successful cancellation releases the confirmed seats.

Expected outcomes include:

* `200 OK` — cancellation succeeded.
* `404 Not Found` — reservation does not exist or does not belong to the authenticated user.
* `409 Conflict` — reservation is already cancelled.

### 5. Health and observability

Health:

```http
GET /actuator/health
```

Liveness:

```http
GET /actuator/health/liveness
```

Readiness:

```http
GET /actuator/health/readiness
```

Metrics:

```http
GET /actuator/metrics
GET /actuator/prometheus
```

The Prometheus endpoint exposes available application and JVM metrics. Custom reservation counters include successful reservations and seat conflicts.

## Concurrency and Consistency

The service uses database transactions and locking to protect reservation operations.

### Preventing double booking

Seat updates are conditional on the seat still being available. The update succeeds only if the database row matches the expected show, seat number, and current status.

Conceptually:

```sql
UPDATE seats
SET status = 'CONFIRMED'
WHERE show_id = ?
  AND seat_number = ?
  AND status = 'AVAILABLE';
```

An update count of one indicates that the seat was claimed. An update count of zero indicates that the seat is no longer available. The service returns a conflict response rather than reporting a successful booking.

### Multi-seat transactions

All seats in a reservation are processed within a transaction. If any requested seat cannot be booked, the transaction must roll back so that no partial reservation remains.

### Idempotent retries

Reservation requests include an idempotency key scoped to the show and authenticated user.

* Same key and same request: return the original reservation.
* Same key and different request: return `409 Conflict`.
* Database uniqueness constraints protect against duplicate idempotency records.

### Per-user booking limit

A configurable limit is stored for each show. The service checks the user's existing confirmed seats before accepting additional seats. Database locking is used to coordinate concurrent reservation requests for a show.

## Error Responses

Errors use a consistent response structure:

```json
{
  "timestamp": "2026-10-09T12:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Seat A1 is not available",
  "path": "/shows/1/reserve",
  "request_id": "example-request-id"
}
```

Common HTTP statuses:

| Status                      | Meaning                                               |
| --------------------------- | ----------------------------------------------------- |
| `201 Created`               | Resource or reservation created                       |
| `200 OK`                    | Successful read or cancellation                       |
| `400 Bad Request`           | Invalid request or validation failure                 |
| `401 Unauthorized`          | Authentication required                               |
| `404 Not Found`             | Show or reservation not found                         |
| `409 Conflict`              | Seat conflict, booking limit, or idempotency conflict |
| `500 Internal Server Error` | Unexpected server failure                             |

## Docker

Build the application image:

```bash
docker build -t seat-reservation:latest .
```

For Docker Compose deployments, the application container must connect to the MySQL service using the Compose service name and the container database port, typically `3306`. Do not use `localhost:3307` from inside the application container.

Configure database credentials and connection URLs through environment variables.

## Testing

Run the automated test suite:

```bash
mvn test
```

Before assessment submission, verify these scenarios:

1. Create a show and confirm its seats are initially available.
2. Reserve an available seat successfully.
3. Submit competing requests for the same seat and confirm only one succeeds.
4. Retry a reservation with the same idempotency key and body.
5. Reuse an idempotency key with a different body.
6. Attempt a multi-seat reservation containing an unavailable seat and verify rollback.
7. Verify the per-user booking limit.
8. Cancel a reservation and verify that its seats become available.
9. Check readiness, liveness, and Prometheus metrics.
10. Run a concurrent burst test and check the responses and final seat counts.

A burst-test script is provided as `burst.sh` where included in the repository. Run it against the configured local or deployed API and inspect its results.

## Deployment

**Live API:** 

**GitHub repository:https://github.com/shelkevaishnavi0911/seat-reservation

The deployment should use a persistent database, externally configured credentials, and an application health check. Verify the public health endpoint and the essential reservation flow after deployment.

## Security Considerations and Limitations

* The current Bearer-token filter treats the token value as a user ID. A production system must validate token signatures, issuer, audience, and expiry through a trusted identity provider.
* HTTPS should be used for public deployments.
* Production database credentials must be supplied through environment variables or a secret manager.
* Database migrations should be managed explicitly for production rather than relying on automatic schema updates.
* Payment processing is outside the scope of this reservation service; the displayed amount represents the reservation total, not a completed payment.

## AI Tool Usage

AI tools were used as development assistance for implementation guidance, debugging, documentation, and assessment preparation. The implementation, behavior, tests, and deployment claims should be reviewed and verified by the author before submission.

## Author

**Vaishnavi Shelke**

Java Backend Developer
