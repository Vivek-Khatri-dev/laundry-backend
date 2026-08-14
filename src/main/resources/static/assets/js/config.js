// ============================================================
// Dawson's Laundry — Frontend configuration
// This frontend is served by Spring Boot itself (from
// src/main/resources/static), so the API is same-origin — a
// relative path works whether you're on localhost or a real domain.
//
// Only change this if you ever split the frontend out to run on
// its own server/port again; then point it at the backend's full
// URL and add that origin to app.cors.allowed-origins.
// ============================================================
window.APP_CONFIG = {
  API_BASE_URL: "/api"
};
