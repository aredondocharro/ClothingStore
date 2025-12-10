# ClothingStore — Identity & Notification Backend

A portfolio e‑commerce backend focused on **Identity** (auth, users) and **Notification** (emails), built with **DDD** and **Hexagonal Architecture**.

For a detailed README.md, go to docs/README.full.

---

## 1. Overview

This project is the backend for a future ClothingStore application.  
Right now it showcases:

- Clean, modular structure (bounded contexts, ports & adapters).
- Production‑style authentication and email flows.
- A realistic example of verification email **resend with token rotation** and outbox‑based email delivery.

---

## 2. Main Features

### Identity

- User **registration** with **email verification**.
- **Resend verification email**:
    - Issues a **new verification token** and **revokes** the previous one.
    - Old links stop working after a resend.
    - Neutral response (no user enumeration).
- **Login** with:
    - Short‑lived **access JWT** (Authorization header).
    - Long‑lived **refresh token** stored in an HttpOnly cookie.
- **Refresh**, **Logout**, and **Change Password** endpoints.
- **Forgot/Reset Password** with signed email links.
- Basic **admin** operations for users and roles.

### Notification

- **Thymeleaf**‑based email templates (HTML + i18n subjects).
- Email **outbox** table:
    - Records emails with `PENDING`, `SENT`, `FAILED`.
    - Dispatcher service sends emails via SMTP (MailHog in dev or external provider).
- Used for:
    - Registration verification.
    - Resend verification.
    - Password reset (and future order emails).

---

## 3. Architecture (Short)

- **Bounded Contexts**
    - **Identity**: user lifecycle, credentials, verification tokens, refresh sessions.
    - **Notification**: email templates, outbox, SMTP delivery.
- **Hexagonal structure**
    - **Domain**: entities, value objects, domain services, ports (no Spring).
    - **Application**: use cases orchestrating domain logic.
    - **Infrastructure**: controllers, JPA adapters, email outbox + SMTP adapters, security config.
- **Events & Outbox**
    - Identity publishes domain events (e.g. *verification email requested / resent*).
    - Notification consumes them and writes emails to the **email outbox**.
    - A dispatcher sends emails and updates their status.

---

## 4. Tech Stack

- Java 21, Spring Boot 3, Spring Web, Spring Security
- Spring Data JPA (PostgreSQL), Flyway
- JWT (Auth0 Java JWT)
- Thymeleaf, JavaMailSender, MailHog (dev)
- Maven, Docker, Docker Compose
- OpenAPI/Swagger UI

---

## 5. Running Locally

1. Start infrastructure (PostgreSQL + MailHog):

   ```bash
   docker compose up -d postgres mailhog
   ```

2. Configure your `.env` based on `.env.example`:
    - DB credentials (`DB_NAME`, `DB_USER`, `DB_PASSWORD`).
    - JWT secret and TTLs (`JWT_SECRET`, `JWT_ACCESS_SECONDS`, `JWT_REFRESH_SECONDS`, `JWT_VERIFICATION_SECONDS`).
    - Mail settings (`MAIL_HOST=mailhog`, `MAIL_PORT=1025`, etc.).
    - Verify/reset URLs used in emails.

3. Run the application:

   ```bash
   ./mvnw spring-boot:run
   ```

4. Open:
    - API: `http://localhost:8081`
    - Swagger: `http://localhost:8081/swagger-ui/index.html`
    - Mail UI (MailHog): `http://localhost:8025`

---

## 6. Identity API (Snapshot)

Key endpoints (most are documented in Swagger):

- `POST /auth/register` – Register a new user and send verification email.
- `GET  /auth/verify` – Verify email using token.
- `POST /auth/verify/resend` – Resend verification email (rotates token, neutral response).
- `POST /auth/login` – Login, returns access token and sets refresh cookie.
- `POST /auth/refresh` – Refresh access token using cookie.
- `POST /auth/logout` – Logout and invalidate refresh session.
- `POST /auth/password/forgot` – Send password reset email (neutral response).
- `POST /auth/password/reset` – Reset password using token.

Admin examples:

- `DELETE /admin/users/{id}` – Delete user.
- `PUT    /admin/users/{id}/roles` – Assign/replace roles.

---

## 7. Why It Matters (Portfolio Angle)

This project demonstrates:

- How to model **identity flows** (register, verify, resend, refresh, reset) with realistic constraints.
- How to keep **domain logic independent** from frameworks via ports and adapters.
- How to implement an **email outbox** and dispatcher pattern.
- How to avoid common security pitfalls:
    - Token reuse.
    - User enumeration in auth flows.
    - Storing sensitive tokens as plain text.

---

## 8. Roadmap (Short)

- Add domains: Products, Cart, Orders, Inventory, Payment.
- Generalize transactional outbox + async messaging for all cross‑context events.
- Improve observability (logs, metrics, tracing).
- Harden security (lockouts, audit log, advanced policies).

---

## 9. License

For learning and portfolio purposes. Licensing details to be defined.
