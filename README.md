# bend-right-backend API

This document lists the HTTP API endpoints exposed by the bend-right-backend service, request/response shapes and notes about authentication and CORS.

Base URL: http://localhost:8081

Authentication
- Auth uses JWT. Obtain a token via POST /api/auth/login. Send the token in requests using the header:
  Authorization: Bearer <token>

CORS
- Requests from http://localhost:5173 through http://localhost:5180 are allowed.

Endpoints

1) Signup
- URL: POST /api/auth/signup
- Purpose: create a new user account
- Request JSON body:
  {
    "userName": "Alice",
    "email": "user@example.com",
    "password": "plaintext-password"
  }
- Responses:
  - 201 Created: account created (Location header points to /api/users/{id})
  - 400 Bad Request: if email already registered

Example request:

POST /api/auth/signup
Content-Type: application/json

{
  "userName": "Alice",
  "email": "alice@example.com",
  "password": "s3cret123"
}

2) Login
- URL: POST /api/auth/login
- Purpose: authenticate and get a JWT token
- Request JSON body:
  {
    "email": "user@example.com",
    "password": "plaintext-password"
  }
- Response JSON (200 OK):
  {
    "token": "<jwt-token>"
  }
- Errors:
  - 401 Unauthorized: invalid credentials

Example request/response:

POST /api/auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "s3cret123"
}

Response 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}

3) Create Session
- URL: POST /api/sessions
- Purpose: create a new yoga session record for the authenticated user
- Authentication: required (Authorization: Bearer <token>)
- Request JSON body:
  {
    "status": "pending" | "completed",
    "date": "YYYY-MM-DD",
    "asana": "Downward Dog"
  }
- Notes:
  - `status` is case-insensitive and must be either `pending` or `completed` (stored as uppercase enum values PENDING/COMPLETED).
  - `date` must be an ISO date string (yyyy-MM-dd). The backend parses it to LocalDate.
- Response:
  - 200 OK: returns the saved Session object (JSON) including generated `id`, `status`, `date`, `asana`, and (depending on JPA serialization) user reference.
  - 401 Unauthorized: missing/invalid token
  - 400 Bad Request: invalid body (e.g., bad date or invalid status)

Example request:

POST /api/sessions
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "status": "pending",
  "date": "2025-11-04",
  "asana": "Downward Dog"
}

Notes and implementation details
- Passwords are stored hashed with BCrypt. Never store plaintext in production.
- JWT secret and expiration are set in `application.properties` (keys: `jwt.secret`, `jwt.expiration-ms`). Replace the secret with a secure random value in production and store it securely (env var or secrets manager).
- Database: PostgreSQL connection is configured via `spring.datasource.*` in `application.properties`.
- Date format: ISO (yyyy-MM-dd). The service uses `LocalDate.parse()`.

Possible future endpoints (not implemented yet)
- GET /api/sessions — list sessions for authenticated user (pagination/filtering)
- GET /api/sessions/{id} — get a single session
- DELETE /api/sessions/{id} — remove a session

If you want me to add any of the above or include OpenAPI/Swagger auto-generated docs, I can add them next.
