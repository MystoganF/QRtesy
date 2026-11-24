<div align="center">
  <img src="qrtesy-web/public/favicon.svg" alt="QRtesy Logo" width="76" height="76" />
  <h1>QRtesy</h1>
  <p><strong>Event Registration, Waitlist Management & Cryptographic QR Ticketing Platform</strong></p>

  <p>
    <a href="https://qrtesy-peach.vercel.app"><strong>Live Deployment</strong></a> •
    <a href="https://github.com/MystoganF/QRtesy"><strong>Git Repository</strong></a> •
    <a href="https://qrtesy-backend.onrender.com/api/health"><strong>API Health</strong></a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-0f172a?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21" />
    <img src="https://img.shields.io/badge/Spring_Boot-4.2-0f172a?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot" />
    <img src="https://img.shields.io/badge/React-TypeScript-0f172a?style=flat-square&logo=react&logoColor=white" alt="React TypeScript" />
    <img src="https://img.shields.io/badge/PostgreSQL-Supabase-0f172a?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Status-Level_2_Complete-16a34a?style=flat-square" alt="Level 2 Complete" />
  </p>
</div>

---

## Overview & Live Links

**QRtesy** is a full-stack event registration and door check-in system engineered for high-concurrency seat allocation, deterministic waitlist management, HMAC-SHA256 signed QR passes, role-based staff operations, and persistent audit logging.

* **Live Application (Frontend):** [https://qrtesy-peach.vercel.app](https://qrtesy-peach.vercel.app)
* **Backend API Health Check:** [https://qrtesy-backend.onrender.com/api/health](https://qrtesy-backend.onrender.com/api/health)
* **Source Repository:** [https://github.com/MystoganF/QRtesy](https://github.com/MystoganF/QRtesy)

### Demo Staff Credentials

| Role | Username | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Event Organizer** | `staff` | `ChangeMe123!` | Full access: Create/Edit/Close events, Cancel/Promote registrations, Export CSV, Audit Trail, Door Check-in |
| **Door Staff** | `door` | `DoorStaff123!` | Operational access: Dashboard overview, Attendee lookup, Camera/Manual QR Check-in, Undo Check-in |

---

## Level Reached

### **Level 2 — Full Production Feature Set (100% Complete)**

Beyond baseline event creation and registration (Level 1), this implementation delivers all Level 2 operational, security, concurrency, and observability requirements:

1. **Transactional Concurrency & Pessimistic Locking:** Row-level database locking (`FOR UPDATE`) prevents overselling the final remaining seat under simultaneous registrations and prevents duplicate check-in races across multiple door scanners.
2. **Cryptographic Ticket Verification:** QR codes embed HMAC-SHA256 signed tokens tied to the registration record, rejecting sequential guessing (`QRT-000001`) or tampered payloads at the door.
3. **Capacity-Aware Waitlist & Manual Promotion:** Full events automatically queue overflow registrants in FIFO order (`Waitlisted · #N`). Cancelling a confirmed attendee frees capacity and surfaces a one-click **Promote** action for the `#1` waitlisted attendee.
4. **Post-Commit Email Notifications (Resend HTTPS API):** Dispatches HTML and plain-text confirmation and waitlist-promotion passes containing deep links (`/ticket/lookup?code=...`) only after database transactions commit.
5. **Role-Based Access Control (RBAC):** Enforces strict separation between `ORGANIZER` and `DOOR_STAFF` across both Spring Security endpoint rules and React route/UI guards.
6. **Persistent Audit Ledger:** Records every administrative and check-in mutation (`EVENT_CREATED`, `EVENT_UPDATED`, `EVENT_CLOSED`, `TICKET_CHECKED_IN`, `CHECKIN_UNDONE`, `REGISTRATION_CANCELLED`, `WAITLIST_PROMOTED`) with actor identity, event association, and PHT (`Asia/Manila`) timestamps.
7. **Event Lifecycle Hardening & Post-Start No-Show Tracking:** Blocks reverting active events with registrations back to `Draft`, locks `Closed` events against further edits or registrations, and computes post-start no-show metrics dynamically.

---

## System Architecture

```text
QRtesy/
├── qrtesy-backend/                  # Java 21 + Spring Boot 4 (Vertical Slice Architecture)
│   ├── src/main/java/com/qrtesy/
│   │   ├── modules/
│   │   │   ├── audit/               # Immutable staff action logging & query slices
│   │   │   ├── auth/                # Stateless JWT HttpOnly cookie auth & staff seeding
│   │   │   ├── events/              # Public listing, organizer CRUD, close lifecycle, dashboard
│   │   │   ├── notifications/       # Transaction-synchronized Resend HTTP email dispatcher
│   │   │   └── registrations/       # Pessimistic-locked registration, lookup, cancel, promote, check-in
│   │   └── shared/                  # SecurityConfig, RateLimitFilter, HMAC QR signer, HealthController
│   ├── src/main/resources/
│   │   ├── db/migration/            # Flyway versioned SQL migrations (V1–V3)
│   │   └── application.yml          # HikariCP pool tuning (max-pool-size: 3) & runtime config
│   └── Dockerfile                   # Multi-stage Eclipse Temurin 21 Alpine build
└── qrtesy-web/                      # React + TypeScript + Vite + Tailwind CSS
    ├── public/favicon.svg           # Custom QRtesy brand mark
    ├── vercel.json                  # SPA client-side route rewrites
    └── src/                         # Feature-isolated API modules, RoleGuards, Camera QR scanner
