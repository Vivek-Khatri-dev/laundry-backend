# Dawson's Laundry — Frontend

A light-themed, static HTML/CSS/JS frontend for the Dawson's Laundry Spring Boot
API. No build step, no framework.

This folder lives inside `src/main/resources/static/`, so **Spring Boot serves
it automatically** — there's nothing separate to run. From the project root:

```bash
mvn spring-boot:run
```

Then visit **http://localhost:8080**. It redirects to `login.html` and talks
to the API at the relative path `/api/...` (same origin, no CORS setup
needed). Seed login: `admin` / `Admin@123`.

## Running the frontend on its own server instead

If you ever want to split it back out (e.g. host the frontend on a CDN and
the API elsewhere), copy this folder out from under `static/`, then open
`assets/js/config.js` and point it at the full backend URL:

```js
window.APP_CONFIG = {
  API_BASE_URL: "http://your-backend-host:8080/api"
};
```

...and add that frontend's origin to `app.cors.allowed-origins` in the
backend's `application.properties`.

## Pages

| File | Purpose | Access |
|---|---|---|
| `login.html` | Sign in, stores the JWT | everyone |
| `dashboard.html` | Today's snapshot: bill count, revenue, pending | everyone |
| `bills.html` | Search/filter every bill | everyone |
| `new-bill.html` | Create a bill: pick garments, set delivery date | everyone |
| `bill-detail.html` | View one bill, edit, mark paid, return, void, print/PDF receipt | everyone (void is admin-only) |
| `products.html` | Manage garment types & prices | view: everyone · edit: admin |
| `reports.html` | Daily totals for any date | everyone |
| `users.html` | Create/deactivate staff accounts | admin only |
| `audit.html` | Full action history | admin only |
| `backup.html` | Download a `.sql` database dump | admin only |

## How auth works

`assets/js/api.js` stores the JWT returned by `POST /api/auth/login` in
`localStorage` and attaches it as `Authorization: Bearer <token>` on every
request. A `401` response clears the session and redirects to `login.html`.
Admin-only pages also do a client-side role check and bounce non-admins to
the dashboard — the backend's `@PreAuthorize`/`SecurityConfig` rules are the
real enforcement either way.

## Structure

```
assets/css/style.css   design system: tokens, layout, components
assets/js/config.js    API base URL
assets/js/api.js       fetch wrapper + auth/session helpers
assets/js/app.js       sidebar/topbar shell, icons, toasts, modals, formatters
*.html                 one file per screen, no build step
```
