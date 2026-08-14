# Dawson's Laundry System — Backend (Spring Boot + MySQL)

This is the REST API that replaces the old console app's `FileDatabase` +
`service` classes. The frontend (HTML/CSS/JS) will call these endpoints.

## 1. Set up the database

1. Open MySQL Workbench, connect to your local server.
2. Run `database/schema.sql` (File → Open SQL Script → Execute).
   This creates the `dawsons_laundry` database, all tables, seed products,
   and one seed admin account:
   - **username:** `admin`
   - **password:** `Admin@123`

   Change this password immediately after your first login (via
   `POST /api/users` to create your real admin, then deactivate the seed one
   — or just log in and rotate it once a "change password" endpoint is added).

## 2. Configure the app

Edit `src/main/resources/application.properties`:
- `spring.datasource.password` → your MySQL root password
- `app.jwt.secret` → any long random string (32+ characters)
- `app.cors.allowed-origins` → the URL(s) your frontend will run on

## 3. Run it

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

## 4. Try it

```bash
# Log in
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'

# -> returns { "token": "...", "role": "ADMIN", ... }
# Use that token on every other call:

curl http://localhost:8080/api/products \
  -H "Authorization: Bearer <token>"
```

## API summary

| Method | Endpoint | Who | Notes |
|---|---|---|---|
| POST | `/api/auth/login` | anyone | returns JWT |
| POST | `/api/bills` | admin, cashier | create bill |
| GET | `/api/bills` | admin, cashier | all non-voided bills |
| GET | `/api/bills/{receiptNo}` | admin, cashier | one bill |
| PUT | `/api/bills/{receiptNo}` | admin, cashier | edit (blocked once PAID/VOIDED) |
| POST | `/api/bills/{receiptNo}/mark-paid` | admin, cashier | deliver/pay |
| POST | `/api/bills/{receiptNo}/return` | admin, cashier | reversible, requires reason |
| POST | `/api/bills/{receiptNo}/void` | **admin only** | replaces "delete" — reversible, requires reason |
| GET | `/api/bills/{receiptNo}/receipt.html` | admin, cashier | 80mm receipt HTML (for print) |
| GET | `/api/bills/{receiptNo}/receipt.pdf` | admin, cashier | 80mm receipt as PDF (for save) |
| GET | `/api/products` | admin, cashier | active products only |
| GET | `/api/products/all` | admin only | including disabled |
| POST | `/api/products` | admin only | add |
| PUT | `/api/products/{id}` | admin only | edit name/price |
| POST | `/api/products/{id}/disable` | admin only | soft-disable, not delete |
| GET | `/api/reports/daily?date=YYYY-MM-DD` | admin, cashier | defaults to today |
| GET | `/api/users` | admin only | list all accounts |
| POST | `/api/users` | admin only | create cashier/admin account |
| POST | `/api/users/{id}/deactivate` | admin only | disable login |
| GET | `/api/audit` | admin only | full action history |
| POST | `/api/backup` | admin only | downloads a `.sql` dump (requires `mysqldump` on PATH) |

## Design notes

- **No hard deletes anywhere.** Bills go to `VOIDED`/`RETURNED`, products go
  `active=false`, users go `active=false`. Nothing is ever physically removed,
  so history and audit trails stay intact.
- **Every write is attributed.** `bills.created_by` / `updated_by` plus a
  full `audit_log` table record who did what and when, satisfying the
  "every action tied to a user id" requirement.
- **Receipts are 80mm-formatted HTML**, reused both for the PDF "Save" button
  (rendered server-side to PDF) and for the "Save & Print" button (the
  frontend prints that same HTML via the browser, so what's saved is exactly
  what prints on the thermal roll). The cashier's name is pulled from the
  JWT/session, never typed manually.

## Frontend

The frontend lives in `src/main/resources/static/` — plain HTML/CSS/JS, no
build step. Spring Boot serves it automatically from the same port as the
API, so there's nothing extra to run:

```bash
mvn spring-boot:run
```

Then open **http://localhost:8080** — it redirects to `login.html` (seed
login: `admin` / `Admin@123`). Pages call the API at the relative path
`/api/...` (see `src/main/resources/static/assets/js/config.js`), which
works automatically since it's all one origin.

Only change `config.js`'s `API_BASE_URL` if you later split the frontend out
to run on its own server/port — then also add that origin to
`app.cors.allowed-origins` below.

See `src/main/resources/static/README.md` for a page-by-page breakdown.
