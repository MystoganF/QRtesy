<div align="center">
  <img src="qrtesy-web/public/favicon.svg" alt="QRtesy Logo" width="76" height="76" />
  <h1>QRtesy</h1>
  <p><strong>Event Registration, Waitlist Management & Cryptographic QR Ticketing Platform</strong></p>

  <p>
    <a href="https://qrtesy-peach.vercel.app"><strong>Live Deployment (HTTPS)</strong></a> •
    <a href="https://github.com/MystoganF/QRtesy"><strong>Git Repository</strong></a> •
    <a href="https://qrtesy-backend.onrender.com/api/health"><strong>Backend API Health</strong></a>
  </p>

  <p>
    <img src="https://img.shields.io/badge/Java-21-0f172a?style=flat-square&logo=openjdk&logoColor=white" alt="Java 21" />
    <img src="https://img.shields.io/badge/Spring_Boot-4.2-0f172a?style=flat-square&logo=springboot&logoColor=white" alt="Spring Boot 4.2" />
    <img src="https://img.shields.io/badge/React-TypeScript-0f172a?style=flat-square&logo=react&logoColor=white" alt="React TypeScript" />
    <img src="https://img.shields.io/badge/PostgreSQL-Supabase-0f172a?style=flat-square&logo=postgresql&logoColor=white" alt="PostgreSQL" />
    <img src="https://img.shields.io/badge/Level_Reached-Level_3_(6%2F6_Advanced)-16a34a?style=flat-square" alt="Level 3 Complete" />
  </p>
</div>

---

## Overview & Deployed Links

**QRtesy** is a full-stack event registration, capacity-aware waitlist management, and cryptographic QR check-in application built for reliable door operations under concurrent traffic. All event schedules and check-in timestamps operate in **Cebu local time (`Asia/Manila`, UTC+8)** using 12-hour AM/PM formatting.

* **Live Web Application (Frontend):** [https://qrtesy-peach.vercel.app](https://qrtesy-peach.vercel.app)
* **GitHub Repository:** [https://github.com/MystoganF/QRtesy](https://github.com/MystoganF/QRtesy)

### Demo Staff Login Credentials

| Role | Username | Password | Access Scope |
| :--- | :--- | :--- | :--- |
| **Organizer** | `staff` | `ChangeMe123!` | Full administrative & operational access: Create, edit, and close events; cancel or promote registrations; export CSV rosters; inspect audit logs; perform door check-ins |
| **Door Staff** | `door` | `DoorStaff123!` | Operational check-in access only: View dashboard metrics, search attendees, scan or enter QR tickets, and undo check-ins |

---

## Level Reached

### **Level 3 · Advanced — All 6 of 6 Advanced Items Completed**

This submission implements the complete **Level 1 (Core)**, **Level 2 (Intermediate)**, and **all 6 optional items in Level 3 (Advanced)**:

* **Level 1 · Core:** Public event browsing and detail views, Philippine mobile format validation (`09XXXXXXXXX`), unique `QRT-XXXXXX` ticket code generation, duplicate email prevention, mobile-responsive QR ticket pass, self-service ticket lookup (`code + email`), BCrypt-hashed staff authentication, color + text door check-in statuses, and searchable attendee rosters.
* **Level 2 · Intermediate:** Full event lifecycle management (`Draft`, `Published`, `Closed`) with time and capacity floor guards, FIFO waitlist queueing (`#1`, `#2`, ...), manual `#1` waitlist promotion, attendee and staff cancellations, live door check-in counter, post-start no-show tracking, per-event CSV export, and audited check-in undo.
* **Level 3 · Advanced (6/6):**
  1. **Camera QR Scanning:** Live phone and laptop camera decoding with a ~2-second status toast, automatic reset for continuous door scanning, and manual code entry fallback.
  2. **Last-Spot & Check-In Concurrency Protection:** Pessimistic row-level database locking (`FOR UPDATE`) preventing overselling on the final seat and preventing duplicate check-in races.
  3. **Cryptographic QR Content & Rate Limiting:** HMAC-SHA256 signed QR payloads (blocking sequential guessing or tampering) paired with endpoint rate limiting on ticket lookup and check-in.
  4. **Email Confirmations:** Post-commit HTML and plain-text emails dispatched via Resend's HTTPS API upon registration confirmation, waitlist entry, and waitlist promotion.
  5. **Role Separation & Immutable Audit Trail:** Strict RBAC (`ORGANIZER` vs. `DOOR_STAFF`) enforced at both Spring Security and React Router layers, backed by a filterable persistent audit log.
  6. **HTTPS Production Deployment:** Deployed across Vercel (HTTPS), Render (Docker), and Supabase PostgreSQL so reviewers can test mobile camera QR scanning directly from a smartphone.

---

## Tech Stack

* **Backend:** Java 21, Spring Boot 4.2, Spring Security (Stateless JWT in `HttpOnly` cookies + BCrypt cost 12), Spring Data JPA, Flyway Migrations, HikariCP
* **Frontend:** React, TypeScript, Vite, Tailwind CSS, React Router, HTML5 Camera QR Scanner
* **Database & Cloud Services:** PostgreSQL (Supabase), Docker on Render (Backend), Vercel (Frontend), Resend HTTPS Email API, UptimeRobot (`/api/health` uptime monitor)

---

## Repository Structure

```text
QRtesy/
├── .env.example                     # Sample environment variables template
├── README.md                        # Project documentation, setup guide, and acceptance checklist
├── qrtesy-backend/                  # Java 21 + Spring Boot 4 API (Vertical Slice Architecture)
│   ├── Dockerfile                   # Multi-stage Eclipse Temurin 21 Alpine production build
│   ├── pom.xml                      # Maven dependencies and build configuration
│   └── src/
│       ├── main/java/com/qrtesy/
│       │   ├── modules/
│       │   │   ├── audit/           # Immutable staff action logging & query endpoints
│       │   │   ├── auth/            # JWT cookie login/logout/me & staff account seeder
│       │   │   ├── events/          # Public event browsing, organizer CRUD, close lifecycle, dashboard
│       │   │   ├── notifications/   # Transaction-synchronized Resend email service
│       │   │   └── registrations/   # Pessimistic-locked registration, lookup, cancel, promote, check-in
│       │   └── shared/              # SecurityConfig, RateLimitFilter, TicketQrSigner, HealthController
│       ├── main/resources/
│       │   ├── application.yml      # Runtime configuration & HikariCP pool cap
│       │   └── db/migration/        # Flyway SQL migrations (V1–V3)
│       └── test/java/com/qrtesy/    # JUnit 5 & Mockito unit/concurrency/security tests
└── qrtesy-web/                      # React + TypeScript + Vite SPA
    ├── public/favicon.svg           # Custom QRtesy SVG brand icon
    ├── vercel.json                  # SPA client-side route rewrites
    └── src/
        ├── app/                     # API client, providers, and router configuration
        ├── modules/                 # Feature-isolated modules (audit, auth, checkin, events, registrations)
        └── shared/                  # AppShell layout, Asia/Manila date/time formatters, design tokens
