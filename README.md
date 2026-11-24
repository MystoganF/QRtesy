<div align="center">
  <img src="qrtesy-web/public/favicon.svg" alt="QRtesy Logo" width="76" height="76" />
  <h1>QRtesy</h1>
  <p><strong>Event Registration, Waitlist Management & Cryptographic QR Ticketing Platform</strong></p>

  <p>
    <a href="https://qrtesy-peach.vercel.app"><strong>Live Deployment (HTTPS)</strong></a> •
    <a href="https://github.com/MystoganF/QRtesy"><strong>Git Repository</strong></a> •
    <a href="https://qrtesy-backend.onrender.com/api/health"><strong>Backend Health</strong></a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-0f172a?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21" />
    <img src="https://img.shields.io/badge/Spring_Boot-4.2-0f172a?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/React-TypeScript-0f172a?style=flat-square&logo=react&logoColor=white" alt="React TypeScript" />
    <img src="https://img.shields.io/badge/PostgreSQL-Supabase-0f172a?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Level_Reached-Level_3_(6%2F6_Advanced)-16a34a?style=flat-square" alt="Level 3 Complete" />
  </p>
</div>

---

## Live Links & Demo Credentials

* **Deployed Web Application (Frontend):** [https://qrtesy-peach.vercel.app](https://qrtesy-peach.vercel.app)
* **Deployed API Health Endpoint (Backend):** [https://qrtesy-backend.onrender.com/api/health](https://qrtesy-backend.onrender.com/api/health)
* **GitHub Repository:** [https://github.com/MystoganF/QRtesy](https://github.com/MystoganF/QRtesy)

### Demo Staff Accounts

| Role | Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Organizer** | `staff` | `ChangeMe123!` | Full access: Create/Edit/Close events, Cancel/Promote registrations, Export CSV, Audit Trail, Door Check-in |
| **Door Staff** | `door` | `DoorStaff123!` | Check-in only: Dashboard overview, Attendee search, Camera/Manual QR check-in, Undo check-in |

---

## Level Reached

### **Level 3 · Advanced — All 6 of 6 Advanced Items Completed**

QRtesy fulfills every requirement across **Level 1 (Core)**, **Level 2 (Intermediate)**, and **all 6 optional items in Level 3 (Advanced)**:

* **Level 1 (Core):** Public event discovery in Cebu time (`Asia/Manila`), Philippine mobile validation (`09XXXXXXXXX`), unique `QRT-XXXXXX` ticket generation, mobile QR pass, self-service ticket lookup (`code + email`), BCrypt-secured staff authentication, color + text check-in feedback, and searchable attendee rosters.
* **Level 2 (Intermediate):** Full event lifecycle (`Draft`, `Published`, `Closed`) with time and capacity floor guards, FIFO waitlist queueing and manual `#1` promotion, attendee/staff cancellations, live check-in counter, post-start no-show tracking, CSV export, and audited check-in undo.
* **Level 3 (Advanced — 6/6):**
  1. **Camera QR Scanning:** Live phone/laptop camera decoding with a 2-second status toast, automatic reset for continuous door throughput, and manual code fallback.
  2. **Last-Spot & Check-In Concurrency Control:** Pessimistic row-level database locks (`FOR UPDATE`) preventing overselling and duplicate simultaneous check-ins.
  3. **Secure QR Content & Rate Limiting:** HMAC-SHA256 signed QR tokens (rejecting sequential/tampered codes) paired with endpoint rate limiting on ticket lookup and check-in.
  4. **Email Confirmations:** Post-commit HTML + plain-text emails via Resend API for confirmed registrations, waitlist entries, and waitlist promotions.
  5. **Roles & Audit Trail:** Strict RBAC (`ORGANIZER` vs. `DOOR_STAFF`) enforced at both Spring Security and React Router layers, backed by a filterable persistent audit log.
  6. **HTTPS Production Deployment:** Deployed on Vercel (HTTPS) + Render (Docker) + Supabase PostgreSQL so reviewers can test mobile camera scanning directly from a phone.

---

## Tech Stack

* **Backend:** Java 21, Spring Boot 4.2, Spring Security (Stateless JWT in `HttpOnly` cookies + BCrypt), Spring Data JPA, Flyway Migrations, HikariCP
* **Frontend:** React, TypeScript, Vite, Tailwind CSS, React Router, HTML5 Camera QR Scanner
* **Database & Infrastructure:** PostgreSQL (Supabase), Docker on Render (Backend), Vercel (Frontend), Resend HTTPS Email API, UptimeRobot (`/api/health`)

---

## Data Model

Database schemas are managed automatically via Flyway migrations (`qrtesy-backend/src/main/resources/db/migration/V1__init_schema.sql` through `V3__audit_logs_and_lifecycle.sql`):

| Table | Key Columns | Purpose |
| :--- | :--- | :--- |
| **`events`** | `id` (UUID), `title`, `description`, `venue`, `starts_at`, `ends_at`, `capacity`, `status` (`DRAFT`, `PUBLISHED`, `CLOSED`), `created_at` | Stores event metadata, Cebu (`Asia/Manila`) timestamps, capacity limits, and lifecycle state. |
| **`registrations`** | `id` (UUID), `event_id` (FK), `full_name`, `email`, `mobile`, `ticket_code` (unique), `status` (`CONFIRMED`, `WAITLISTED`, `CHECKED_IN`, `CANCELLED`), `registered_at`, `checked_in_at`, `checked_in_by` | Tracks attendee bookings, unique ticket codes, waitlist ordering, and door check-in audit fields. |
| **`staff_users`** | `id` (UUID), `name`, `username` (unique), `password_hash` (BCrypt), `role` (`ORGANIZER`, `DOOR_STAFF`) | Stores staff accounts with BCrypt-hashed credentials and role assignments. |
| **`audit_logs`** | `id` (UUID), `event_id` (FK), `staff_user_id` (FK), `actor_type`, `actor_label`, `action`, `target_id`, `summary`, `created_at` | Immutable ledger recording event mutations, cancellations, promotions, check-ins, and check-in undos. |

---

## Concurrency & Security Architecture

* **Preventing Last-Spot Overselling:** `RegisterAttendeeHandler` runs inside a `@Transactional` boundary and acquires a pessimistic write lock (`SELECT ... FOR UPDATE` via `EventRepository.findByIdForUpdate`) on the target `events` row before counting active (`CONFIRMED` + `CHECKED_IN`) seats. Concurrent requests serialize cleanly at the row level: the first request claims the final seat as `CONFIRMED`, and the second sees full capacity and is queued as `WAITLISTED`.
* **Preventing Duplicate Simultaneous Check-Ins:** `ScanTicketHandler` locks the target `registrations` row (`FOR UPDATE`) during check-in. When two door devices scan the same pass simultaneously, the first commits `CHECKED_IN` with `checked_in_at`, and the second reads the committed state and returns `"Already checked in"` with the original timestamp.
* **Cryptographic QR Integrity:** `TicketQrSigner` signs ticket payloads with HMAC-SHA256 using `JWT_SECRET`. Guessed sequential codes (`QRT-000001`) or altered signatures fail verification immediately with `"Invalid ticket"`.

---

## Local Setup & Seed Data Instructions

### Prerequisites
* **Java 21** & Maven Wrapper (`./mvnw`)
* **Node.js 20+** & `npm`
* **PostgreSQL 15+**

### 1. Clone the Repository
```bash
git clone [https://github.com/MystoganF/QRtesy.git](https://github.com/MystoganF/QRtesy.git)
cd QRtesy
