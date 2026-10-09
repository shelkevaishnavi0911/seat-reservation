# Seat Reservation at Scale — Engineering Write-up

## 1. Overview

This project implements a seat reservation REST API using Java, Spring Boot, Spring Data JPA, and MySQL. The objective is to support concurrent booking requests while maintaining reservation consistency, preventing double booking, enforcing per-user limits, and providing operational observability.

The service exposes endpoints for show creation, seat reservation, show details, reservation cancellation, health checks, and application metrics.

## 2. Technology Choices

Java 17: Backend application development.
Spring Boot 3.4.12: REST APIs, dependency injection, configuration, and Actuator integration.
Spring Data JPA and Hibernate: Persistence and database transaction management.
MySQL 8.4: Persistent storage and database-level constraints.
Spring Security: Request authentication and authorization.
Micrometer and Prometheus: Application metrics and monitoring.
Docker: Containerization and deployment.

These technologies provide a familiar backend stack and support transactional database operations.

3. Data Model

The main entities are:

Show

Stores the show name, ticket price in paise, per-user booking limit, creation timestamp, and active status.

 Seat

Stores the show association, seat number, status, booking user, and reservation reference.

A database uniqueness constraint on `(show_id, seat_number)` prevents duplicate seat records for the same show.

Seat statuses are:

* `AVAILABLE`
* `HELD`
* `CONFIRMED`

 Reservation

Stores the show, authenticated user, total amount in paise, reservation status, idempotency key, request hash, and creation timestamp.

A uniqueness constraint on `(show_id, booked_by_user, idempotency_key)` protects against duplicate reservation records for the same user and show.
 4. Preventing Double Booking

The main concurrency requirement is that two users must not successfully reserve the same seat.

The implementation uses conditional database updates that change a seat from `AVAILABLE` to `CONFIRMED` only if it is still available.

Conceptually:

```sql
UPDATE seats
SET status = 'CONFIRMED'
WHERE show_id = ?
  AND seat_number = ?
  AND status = 'AVAILABLE';
```

The affected-row count determines the result:

 One row updated: the seat was successfully claimed.
Zero rows updated: the seat is unavailable, so the request fails with `409 Conflict`.

The database performs the condition check and update atomically, avoiding a separate availability check followed by an unprotected write.

The service also uses a transaction so a failed multi-seat reservation does not leave some of the requested seats booked.

5. Transaction and Locking Strategy

Reservation processing locks the relevant show row using a pessimistic write lock. This serializes reservation operations for the same show and coordinates per-user booking-limit checks.

Within the transaction, the service validates the request, checks idempotency, enforces the booking limit, and processes seats in a deterministic order.

If any seat cannot be reserved, the transaction is rolled back.

This approach favors correctness and implementation simplicity for the assessment. Its trade-off is reduced parallelism for reservations belonging to the same show. A higher-throughput production design could use more granular locking or atomic database operations, provided it preserves the booking-limit and idempotency invariants.

## 6. Idempotency

Clients supply an idempotency key with each reservation request.

The intended behavior is:

1. The same user submits the same key and the same request body again: return the original reservation rather than create another booking.
2. The same user reuses the key with a different request body: return `409 Conflict`.
3. A new key represents a new reservation attempt.

The request hash allows the service to compare the body of a retry with the original request. A database uniqueness constraint provides additional protection against duplicate records.

Idempotency checks must occur before enforcing the per-user booking limit for a retry; otherwise, a previously successful request could be incorrectly rejected after it has consumed the user's remaining booking allowance.

## 7. Per-User Booking Limit

Each show has a configurable `per_user_limit`, defaulting to four seats when not specified.

The service checks the user's existing confirmed seats before accepting a new reservation. The show-level lock coordinates concurrent reservation requests so that simultaneous requests cannot independently pass the limit check and exceed the configured allowance.

The expected invariant is:

```text
confirmed seats for a user on a show <= per_user_limit
```

This behavior must be verified with both sequential and concurrent requests.

## 8. Cancellation

A reservation can be cancelled by its owning authenticated user.

The service checks reservation ownership and status, marks the reservation as cancelled, and releases its associated confirmed seats back to `AVAILABLE`.

An already-cancelled reservation returns `409 Conflict`. A reservation that cannot be found for the authenticated user returns `404 Not Found`.

Cancellation and seat release must execute within a transaction to keep reservation and seat state consistent.

## 9. API Error Handling

The API uses structured error responses containing:

* Timestamp
* HTTP status
* Error name
* Safe message
* Request path
* Request ID

The intended HTTP status mapping includes:

| Status                      | Use                                                 |
| --------------------------- | --------------------------------------------------- |
| `201 Created`               | Show or reservation created                         |
| `200 OK`                    | Successful read or cancellation                     |
| `400 Bad Request`           | Invalid request or failed validation                |
| `401 Unauthorized`          | Missing or invalid authentication                   |
| `404 Not Found`             | Show or reservation not found                       |
| `409 Conflict`              | Seat, idempotency, booking-limit, or state conflict |
| `500 Internal Server Error` | Unexpected server failure                           |

Unexpected exceptions are logged for diagnosis while the response avoids exposing internal exception details.

## 10. Observability

### Request correlation

The request filter accepts an incoming `X-Request-ID` or generates a UUID if one is absent. It returns the ID in the response header and stores it in the logging MDC so request-processing logs can be correlated.

The MDC value is removed in a `finally` block to avoid leaking request context across reused server threads.

### Health checks

Spring Boot Actuator exposes health, liveness, and readiness endpoints. Database readiness should be verified against the deployed configuration.

### Metrics

Micrometer exposes application and JVM metrics through Actuator. Custom reservation counters include successful reservations and seat conflicts.

The Prometheus endpoint makes the metrics available for scraping by a compatible monitoring system.

## 11. Testing Strategy

The following scenarios are important for validating correctness:

1. Create a show with unique seat numbers.
2. Reserve an available seat.
3. Submit simultaneous requests for the same seat and verify only one succeeds.
4. Retry the same reservation with the same idempotency key and body.
5. Reuse an idempotency key with a different request body.
6. Attempt a multi-seat booking where one seat is unavailable and verify transaction rollback.
7. Verify the per-user booking limit, including concurrent requests.
8. Cancel a reservation and verify that the seats become available.
9. Attempt cancellation as a different user and cancel the same reservation twice.
10. Verify that show counts satisfy:

```text
available_seats + held_seats + confirmed_seats = total_seats
```

11. Verify health endpoints and Prometheus metrics.
12. Run the burst-test script against the configured API and inspect response codes and final seat states.

Automated tests should be run using:

```bash
mvn test
```

Concurrency correctness should be evaluated using real simultaneous requests against the deployed or locally running service, rather than inferred from sequential tests alone.

## 12. Deployment Considerations

The application is intended to run as a Dockerized Spring Boot service connected to MySQL.

The deployment should:

* Configure the database URL and credentials through environment variables.
* Use persistent database storage.
* Expose the application on the platform-provided port.
* Provide a health-check endpoint.
* Keep database credentials and other secrets out of source control.
* Verify the public API and database connection after deployment.

The public deployment URL and repository URL should be added here after they have been verified.

**Live API:** `

**Repository:** https://github.com/shelkevaishnavi0911/seat-reservation

## 13. Limitations and Future Improvements

The current authentication filter is an assignment-level mechanism that treats the Bearer-token value as the user ID. It does not validate signed JWTs or verify token issuer, audience, and expiry. A production system should integrate with a trusted identity provider.

Additional improvements include:

* Automated stress and concurrency tests in CI.
* Database migration tooling and explicit schema migrations.
* More granular locking for higher booking throughput.
* Rate limiting and request-size limits.
* Production-grade token validation and authorization.
* Distributed tracing and centralized log aggregation.
* A dedicated payment workflow if payment processing becomes part of the requirements.

## 14. AI Tool Usage

AI tools were used as development assistance for implementation guidance, debugging, code review, and documentation. The author is responsible for reviewing the code, verifying the behavior, running the tests, and accurately reporting the implementation and test results.
