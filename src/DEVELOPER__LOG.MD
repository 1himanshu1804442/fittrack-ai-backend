# DEVELOPER_LOG.md
## Project: FitTrack AI Backend

This document serves as a post-mortem and technical log of the critical failures, architectural missteps, and debugging resolutions encountered during the Phase 1 & Phase 2 build of the FitTrack AI backend.

---

### 1. Database Integrity & Constraint Failures

**The Problem:** Application failed to start, throwing `DataIntegrityViolationException` and foreign key constraint violations.
**Root Cause:** Transitioning the database schema to support Spring Security. The `User` entity was updated to include a `password` field with a strict `nullable = false` constraint. However, the existing PostgreSQL database contained legacy dummy testing rows lacking this field. Furthermore, attempting to manually `DROP TABLE users` failed because the `workouts` table contained orphaned foreign keys linked to the old users.
**Symptoms:**
* Hibernate schema validation failures on boot.
* SQL errors blocking the initialization of the `UserController`.
  **Resolution:**
  Instead of bypassing the security constraints locally, the schema was wiped cleanly to enforce database integrity. Executed `DROP TABLE users CASCADE;` in the PostgreSQL console. This automatically cleared the dependent orphaned rows in the `workouts` table, allowing Hibernate to cleanly rebuild the production-ready schema with the strict constraints intact.

---

### 2. The "Amnesia Bug" (Stateless Authentication Failure)

**The Problem:** The `JwtRequestFilter` (The "Bouncer") continuously intercepted and rejected valid requests, returning `403 Forbidden` statuses.
**Root Cause:** A fundamental misunderstanding of in-memory statelessness. The `JwtUtil` class was configured to generate a secure random secret key mathematically in-memory upon application boot. During debugging, the Spring Boot server was restarted multiple times. Every restart wiped the previous secret key and generated a new one. The client (Postman) was submitting a token signed by the *old* key, causing a signature mismatch.
**Symptoms:**
* Postman returned `403 Forbidden`.
* Spring Boot console outputted `SignatureException: JWT signature does not match locally computed signature`.
  **Resolution:**
  Aligned testing practices with stateless architecture. Recognized that without a persistent environment variable for the `JWT_SECRET`, any server reboot instantly invalidates all minted tokens. The immediate fix required hitting the `/login` endpoint to request a fresh token for the current active server session before accessing the `/recommendation` endpoint.

---

### 3. Incident Response: API Key Exposure

**The Problem:** A localized server crash dumped the system environment variables into the application trace output.
**Root Cause:** Unhandled exceptions in the early stages of the `RecommendationService` caused Spring Boot to print verbose stack traces, which inadvertently included the `GEMINI_API_KEY` injected via the `@Value` annotation.
**Symptoms:**
* Plain-text exposure of third-party credentials in the local terminal.
  **Resolution:**
  Treated the local exposure as a production-level compromise.
1. Halted development immediately.
2. Accessed the Google AI Studio console.
3. Completely revoked the compromised API key.
4. Generated a fresh cryptographic key.
5. Injected the new key directly into the IDE's Run Configuration environment variables to ensure it remained out of the source code.

---

### 4. Third-Party LLM Deprecation (The 404 Trap)

**The Problem:** The backend successfully bypassed the JWT filter and reached out to the external LLM provider, but the HTTP client threw an exception.
**Root Cause:** The `RestTemplate` client was hardcoded to target `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent`. Google had officially deprecated and shut down the `1.5-flash` endpoint, rendering the URL nonexistent.
**Symptoms:**
* Console error: `404 Not Found on POST request for "...gemini-1.5-flash..."`
* Application failed to parse the resulting JSON tree.
  **Resolution:**
  Updated the hardcoded endpoint string in the `RecommendationService` to dynamically target the active `gemini-2.5-flash` model. This reinforced the necessity of tracking API versioning when building dependencies on external generative models.

---

### 5. Infinite Recursion in Hibernate Serialization

**The Problem:** Making a GET request to retrieve a user's data resulted in an infinite loop, eventually causing a `StackOverflowError` and crashing the server.
**Root Cause:** A bidirectional relationship existed between the `User` entity and the `Workout` entity. When Jackson attempted to serialize the `User` object into JSON, it called the `Workout` list. The `Workout` object then called its mapped `User`, which called the `Workout` list again, trapping the parser in an infinite loop.
**Symptoms:**
* Massive, endlessly scrolling JSON response payload.
* Server crash due to memory exhaustion.
  **Resolution:**
  Applied the `@JsonIgnore` annotation to the `User` mapping inside the `Workout` entity. This broke the bidirectional serialization loop, allowing the API to return a clean, structured JSON response representing the user and their associated workout history without traversing backwards.

---

### 6. Client-Side Malformed Payloads

**The Problem:** Valid JWT tokens were still being rejected by the backend during manual API testing.
**Root Cause:** Human error during payload construction in Postman. Three specific variations occurred:
1.  **Truncation:** Highlighting only the tail end of the token (e.g., `F3ScrcVKZ...`) instead of capturing the entire Base64 encoded string starting with the `eyJ` header.
2.  **String Pollution:** Accidentally copying the enclosing JSON quotation marks (`"eyJ..."`) from the `/login` response and pasting them into the Bearer token field.
3.  **Invisible Characters:** Highlighting an invisible newline character (`\n`) at the end of the token string.
    **Symptoms:**
* `MalformedJwtException` logged in the Spring console.
* Persistent `403 Forbidden` blocks.
  **Resolution:**
  Implemented strict double-checks on all Bearer tokens to ensure absolute string purity before triggering requests. Backspaced the end of the token input box to guarantee the elimination of hidden whitespace characters. 