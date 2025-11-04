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

3) Create Session (new: path includes uid and pose)
- URL: POST /api/{uid}/sessions/{pose}
- Purpose: create a new yoga session record for the user identified by `uid`. The `pose` path segment is used as the session's `asana` value.
- Authentication: required (Authorization: Bearer <token>) — the application requires a valid JWT for non-auth endpoints. Note: the controller currently accepts any authenticated request and will create a session for the `uid` provided in the path; it does NOT currently verify that the authenticated user matches `uid`.
- Request path parameters:
  - `uid` (Long) — the user id to attach the session to
  - `pose` (String) — the asana name (e.g. `downward-dog` or `uttanasana`)
- Request JSON body (only `status` and `date` are required in the body; `asana` in the body is ignored because `pose` is used):
  {
    "status": "pending" | "completed",
    "date": "YYYY-MM-DD"
  }
- Notes:
  - `status` is case-insensitive and must be either `pending` or `completed` (stored as uppercase enum values PENDING/COMPLETED).
  - `date` must be an ISO date string (yyyy-MM-dd). The backend parses it to LocalDate.
  - The `pose` path variable is used as the session's `asana` value. If you include an `asana` field in the JSON body it will be ignored by the endpoint.
- Response:
  - 200 OK: returns the saved Session object (JSON) including generated `id`, `status`, `date`, `asana` and possibly a user reference.
  - 401 Unauthorized: missing/invalid token
  - 400 Bad Request: invalid body (e.g., bad date or invalid status)

Example request (curl):

POST /api/123/sessions/uttanasana
Content-Type: application/json
Authorization: Bearer <jwt-token>

{
  "status": "pending",
  "date": "2025-11-04"
}

Notes and implementation details
- Passwords are stored hashed with BCrypt. Never store plaintext in production.
- JWT secret and expiration are set in `application.properties` (keys: `jwt.secret`, `jwt.expiration-ms`). Replace the secret with a secure random value in production and store it securely (env var or secrets manager).
- Database: PostgreSQL connection is configured via `spring.datasource.*` in `application.properties`.
- Date format: ISO (yyyy-MM-dd). The service uses `LocalDate.parse()`.

4) List Sessions for current user
- URL: GET /api/sessions
- Purpose: return the list of sessions for the current authenticated user. The server extracts the `uid` (user id) from the JWT token payload (claim `uid`).
- Authentication: required (Authorization: Bearer <token>)
- Request: no JSON body required. Set the Authorization header with your JWT.
- Response:
  - 200 OK: returns an array of Session objects belonging to the user.
  - 401 Unauthorized: missing/invalid token

Example request (curl):

GET /api/sessions
Authorization: Bearer <jwt-token>

Response 200 OK
[
  {
    "id": 1,
    "status": "PENDING",
    "date": "2025-11-04",
    "asana": "uttanasana"
  },
  {
    "id": 2,
    "status": "COMPLETED",
    "date": "2025-11-05",
    "asana": "downward-dog"
  }
]


