# BMAAS — Bug Management and Auto Assessment System

A full-stack web application for managing software projects, modules, developers, and (coming soon) bug tracking with auto-assessment analytics.

## Tech Stack

| Layer      | Technology                                                         |
|------------|--------------------------------------------------------------------|
| **Backend**  | Java 21, Spring Boot 3.3.5, Spring Security, Spring Data JPA, JWT |
| **Frontend** | React 18, Vite 5, TailwindCSS 3, Axios, React Router 6            |
| **Database** | PostgreSQL                                                         |

---

## Prerequisites

- **Java 21** (OpenJDK Temurin recommended)
- **Maven 3.9+**
- **Node.js 18+** and **npm 9+**
- **PostgreSQL 14+** running locally

---

## Database Setup

1. Start PostgreSQL if not already running:
   ```bash
   # macOS (Homebrew)
   brew services start postgresql@16
   ```

2. Create the database:
   ```bash
   createdb -U <your_pg_username> bmaas_db
   ```

3. Verify:
   ```bash
   psql -U <your_pg_username> -d bmaas_db -c "SELECT 1;"
   ```

> The app uses `spring.jpa.hibernate.ddl-auto=update`, so all tables are created/updated automatically on boot.

---

## Running the Backend

```bash
cd bmaas-backend

# Set environment variables (or rely on defaults in application.yml)
export DATABASE_URL=jdbc:postgresql://localhost:5432/bmaas_db
export DATABASE_USERNAME=<your_pg_username>
export DATABASE_PASSWORD=<your_pg_password>
export JWT_SECRET=<your_64_char_hex_secret>

# Build
mvn clean install -DskipTests

# Run
mvn spring-boot:run
```

The backend starts on **http://localhost:8080**.

---

## Running the Frontend

```bash
cd bmaas-frontend

# Install dependencies
npm install

# Start dev server
npm run dev
```

The frontend starts on **http://localhost:5173**.

---

## Environment Variables

### Backend (set via env vars or `application.yml` defaults)

| Variable            | Default                                                            | Description            |
|---------------------|--------------------------------------------------------------------|------------------------|
| `DATABASE_URL`      | `jdbc:postgresql://localhost:5432/bmaas_db`                        | JDBC connection string |
| `DATABASE_USERNAME` | `username`.                                                        | PostgreSQL username    |
| `DATABASE_PASSWORD` | `password`.                                                        | PostgreSQL password    |
| `JWT_SECRET`        | `secret key`  | JWT signing key (hex)  |
| `CORS_ORIGINS`      | `http://localhost:5173,http://localhost:3000`                       | Allowed CORS origins   |

### Frontend

| Variable        | Default                    | Description      |
|-----------------|----------------------------|------------------|
| `VITE_API_URL`  | `http://localhost:8080/api` | Backend API base |

---

## API Endpoints

### Authentication (public)

| Method | Endpoint              | Description       |
|--------|-----------------------|-------------------|
| POST   | `/api/auth/register`  | Register new user |
| POST   | `/api/auth/login`     | Login → JWT token |

### Projects (requires `PROJECT_MANAGER` role)

| Method | Endpoint              | Description         |
|--------|-----------------------|---------------------|
| GET    | `/api/projects`       | List user's projects|
| GET    | `/api/projects/{id}`  | Get project details |
| POST   | `/api/projects`       | Create project      |
| PUT    | `/api/projects/{id}`  | Update project      |
| DELETE | `/api/projects/{id}`  | Delete project      |

### Modules (requires `PROJECT_MANAGER` role)

| Method | Endpoint                                  | Description               |
|--------|-------------------------------------------|---------------------------|
| GET    | `/api/projects/{projectId}/modules`       | List modules in project   |
| POST   | `/api/projects/{projectId}/modules`       | Create module             |
| GET    | `/api/modules/{id}`                       | Get module details        |
| PUT    | `/api/modules/{id}`                       | Update module             |
| DELETE | `/api/modules/{id}`                       | Delete module             |

### Developers & Linkage

| Method | Endpoint                                  | Description                                       | Roles                          |
|--------|-------------------------------------------|---------------------------------------------------|--------------------------------|
| GET    | `/api/developers`                         | List all developers                               | `PROJECT_MANAGER`, `TESTER`, `DEVELOPER` |
| GET    | `/api/developers/{id}`                    | Get developer details                             | `PROJECT_MANAGER`, `TESTER`, `DEVELOPER` |
| GET    | `/api/developers/me`                      | Get authenticated developer profile               | `DEVELOPER`, `PROJECT_MANAGER` |
| GET    | `/api/developers/me/bugs`                 | List bugs assigned to authenticated developer     | `DEVELOPER`, `PROJECT_MANAGER` |
| POST   | `/api/developers`                         | Create developer record                           | `PROJECT_MANAGER`              |
| PUT    | `/api/developers/{id}`                    | Update developer record                           | `PROJECT_MANAGER`              |
| PUT    | `/api/developers/{id}/link-user`          | Link developer record to user account (`userId`)  | `PROJECT_MANAGER`              |
| DELETE | `/api/developers/{id}`                    | Delete developer record                           | `PROJECT_MANAGER`              |

### Developer Metrics & Leaderboard (Phase 5)

| Method | Endpoint                                  | Description                                                               | Roles                          |
|--------|-------------------------------------------|---------------------------------------------------------------------------|--------------------------------|
| GET    | `/api/developers/{id}/metrics`            | Get historical performance metrics for developer (null rates if 0 bugs)    | `PROJECT_MANAGER`, `DEVELOPER` |
| GET    | `/api/developers/{id}/workload`           | Get real-time live workload count (`ASSIGNED` + `IN_PROGRESS`)            | `PROJECT_MANAGER`, `TESTER`, `DEVELOPER` |
| GET    | `/api/developers/{id}/assignment-history` | Get developer's past assignment log                                      | `PROJECT_MANAGER`, `DEVELOPER` |
| GET    | `/api/developers/leaderboard`             | Leaderboard sortable by `bugsResolved`, `firstTimeFixRate`, `averageResolutionTimeHours`, `slaComplianceRate`, `currentWorkload` | `PROJECT_MANAGER` only |
| GET    | `/api/bugs/{bugId}/assignment-history`    | Get historical assignment log for specific bug                            | `PROJECT_MANAGER`, `TESTER`, `DEVELOPER` |

### SLA Rules Management (requires `PROJECT_MANAGER` role)

| Method | Endpoint              | Description                                                    | Roles                   |
|--------|-----------------------|----------------------------------------------------------------|-------------------------|
| GET    | `/api/sla-rules`      | List all configured SLA rules                                  | `PROJECT_MANAGER`       |
| GET    | `/api/sla-rules/{id}` | Get single SLA rule                                            | `PROJECT_MANAGER`       |
| POST   | `/api/sla-rules`      | Create new SLA rule                                            | `PROJECT_MANAGER`       |
| PUT    | `/api/sla-rules/{id}` | Update SLA rule duration (hours) or warning threshold (%)     | `PROJECT_MANAGER`       |
| DELETE | `/api/sla-rules/{id}` | Delete SLA rule                                                | `PROJECT_MANAGER`       |

### Bug & SLA Management

| Method | Endpoint                                                    | Description                                                        | Roles                          |
|--------|-------------------------------------------------------------|--------------------------------------------------------------------|--------------------------------|
| POST   | `/api/projects/{projectId}/modules/{moduleId}/bugs`         | Create bug report (auto-computes SLA deadline from priority rule)  | `TESTER`, `PROJECT_MANAGER`    |
| GET    | `/api/projects/{projectId}/bugs`                            | List bugs (filterable by `moduleId`, `status`, `severity`, `slaStatus`) | `TESTER`, `PROJECT_MANAGER` |
| GET    | `/api/projects/{projectId}/bugs/sla-summary`                | Get project SLA summary breakdown (`onTrack`, `atRisk`, `breached`)| `TESTER`, `PROJECT_MANAGER` |
| GET    | `/api/bugs/{bugId}`                                         | Get bug details (assignee-only for developers)                     | `TESTER`, `PROJECT_MANAGER`, `DEVELOPER` |
| PATCH  | `/api/bugs/{bugId}/assign`                                  | Manually assign bug to module-mapped developer (status &rarr; `ASSIGNED`) | `PROJECT_MANAGER`       |
| PATCH  | `/api/bugs/{bugId}/unassign`                                | Unassign developer and revert status to `OPEN`                     | `PROJECT_MANAGER`              |
| PUT    | `/api/bugs/{bugId}`                                         | Update bug (recomputes SLA deadline if priority changes)           | `TESTER`, `PROJECT_MANAGER`    |
| PATCH  | `/api/bugs/{bugId}/status`                                  | Update status (requires `resolutionNotes` when transitioning to `RESOLVED`)| `TESTER`, `PROJECT_MANAGER`, `DEVELOPER` |
| DELETE | `/api/bugs/{bugId}`                                         | Permanently delete bug                                             | `PROJECT_MANAGER` only         |

### SLA Status States

- **`ON_TRACK`**: Bug is active and elapsed time is below the configured warning threshold (e.g. < 80% of window).
- **`AT_RISK`**: Elapsed time has crossed the early warning threshold percentage, warning the team before breach.
- **`BREACHED`**: Current time has passed the calculated SLA deadline. An `EscalationLog` record is durably created.

### Bug Lifecycle State Machine

- **OPEN** &rarr; `ASSIGNED` (PM assigns developer), `IN_PROGRESS`, `REJECTED` (PM/Tester)
- **ASSIGNED** &rarr; `IN_PROGRESS` (Assigned developer starts work), `OPEN` (PM unassigns), `REJECTED`
- **IN_PROGRESS** &rarr; `RESOLVED` (Assigned developer submits fix + `resolutionNotes`), `OPEN` (PM unassigns), `ASSIGNED`
- **RESOLVED** &rarr; `CLOSED` (Tester verifies PASS), `REOPENED` (Tester verifies FAIL)
- **CLOSED** &rarr; `REOPENED` (Tester/PM)
- **REOPENED** &rarr; `ASSIGNED` (PM reassigns), `IN_PROGRESS` (Assigned developer resumes work), `OPEN`
- **REJECTED** &rarr; `REOPENED` (Tester/PM)

---

## User Roles & Permissions

| Role              | Permissions                                                                    |
|-------------------|--------------------------------------------------------------------------------|
| `PROJECT_MANAGER` | Full project & module CRUD, developer management & mapping, bug CRUD & triage  |
| `TESTER`          | Browse projects & modules, create/edit bug reports, manage bug status lifecycle|
| `DEVELOPER`       | Read-only view for assigned modules (extended in subsequent phases)            |

---

## Project Structure

```
BMAAS_Project/
├── bmaas-backend/                  # Spring Boot backend
│   ├── pom.xml
│   └── src/main/java/com/bmaas/
│       ├── BmaasApplication.java   # Main class (@EnableJpaAuditing)
│       ├── controller/             # REST controllers
│       ├── dto/                    # Request/Response DTOs
│       ├── entity/                 # JPA entities
│       ├── exception/              # Global exception handling
│       ├── repository/             # Spring Data repositories
│       ├── security/               # JWT + Spring Security config
│       └── service/                # Service layer (interfaces + impl)
├── bmaas-frontend/                 # React + Vite frontend
│   ├── package.json
│   └── src/
│       ├── api/api.js              # Axios API client with JWT interceptor
│       ├── pages/                  # React page components
│       └── App.jsx                 # Routes + PrivateRoute guard
└── README.md
```
