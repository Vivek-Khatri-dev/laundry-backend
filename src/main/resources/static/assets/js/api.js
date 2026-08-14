// ============================================================
// Dawson's Laundry — API client
// Thin fetch() wrapper: attaches the JWT, parses errors from
// GlobalExceptionHandler's shape, and redirects to login on 401.
// ============================================================

const Auth = {
  KEY: "dl_session",

  save(session) {
    localStorage.setItem(this.KEY, JSON.stringify(session));
  },
  get() {
    try { return JSON.parse(localStorage.getItem(this.KEY)); } catch { return null; }
  },
  token() {
    const s = this.get();
    return s ? s.token : null;
  },
  role() {
    const s = this.get();
    return s ? s.role : null;
  },
  isAdmin() { return this.role() === "ADMIN"; },
  clear() { localStorage.removeItem(this.KEY); },
  requireLogin() {
    if (!this.token()) {
      window.location.href = "login.html";
      return false;
    }
    return true;
  },
  initials() {
    const s = this.get();
    if (!s || !s.fullName) return "?";
    return s.fullName.trim().split(/\s+/).slice(0, 2).map(w => w[0].toUpperCase()).join("");
  }
};

const Api = {
  async request(path, { method = "GET", body, headers = {}, raw = false } = {}) {
    const token = Auth.token();
    const finalHeaders = { ...headers };
    if (body !== undefined && !raw) finalHeaders["Content-Type"] = "application/json";
    if (token) finalHeaders["Authorization"] = "Bearer " + token;

    let res;
    try {
      res = await fetch(window.APP_CONFIG.API_BASE_URL + path, {
        method,
        headers: finalHeaders,
        body: body !== undefined ? (raw ? body : JSON.stringify(body)) : undefined
      });
    } catch (err) {
      throw new ApiError(0, "Can't reach the server. Check that the backend is running and the API URL in assets/js/config.js is correct.");
    }

    if (res.status === 401) {
      Auth.clear();
      if (!location.pathname.endsWith("login.html")) {
        location.href = "login.html?expired=1";
      }
      throw new ApiError(401, "Your session has expired. Please login again.");
    }

    if (res.status === 204) return null;

    const contentType = res.headers.get("content-type") || "";

    if (!res.ok) {
      let message = `Request failed (${res.status})`;
      if (contentType.includes("application/json")) {
        try {
          const data = await res.json();
          message = data.message || data.error || message;
        } catch { /* ignore */ }
      }
      
      // Add context for specific status codes
      if (res.status === 403) {
        message = "You don't have permission to perform this action. Please contact administrator.";
      } else if (res.status === 401) {
        message = "Your session has expired. Please login again.";
      } else if (res.status === 400 && message.includes("Current password")) {
        message = "Current password is incorrect. Please try again.";
      } else if (res.status === 404) {
        message = "The requested resource was not found.";
      }
      
      throw new ApiError(res.status, message);
    }

    if (contentType.includes("application/json")) return res.json();
    if (contentType.includes("text/")) return res.text();
    return res.blob();
  },

  get(path) { return this.request(path); },
  post(path, body) { return this.request(path, { method: "POST", body: body ?? {} }); },
  put(path, body) { return this.request(path, { method: "PUT", body }); },

  async downloadBlob(path, filenameFallback) {
    const token = Auth.token();
    const headers = {};
    if (token) headers["Authorization"] = "Bearer " + token;
    
    try {
        const res = await fetch(window.APP_CONFIG.API_BASE_URL + path, {
            method: 'POST',
            headers: headers
        });
        
        if (!res.ok) {
            let message = "Download failed (" + res.status + ")";
            if (res.status === 403) message = "You don't have permission.";
            if (res.status === 401) message = "Session expired. Please login again.";
            throw new ApiError(res.status, message);
        }
        
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filenameFallback;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
    } catch (error) {
        throw error;
    }
},

  async downloadExcel(path, filename) {
    const token = Auth.token();
    const headers = {};
    if (token) headers["Authorization"] = "Bearer " + token;
    
    try {
        const res = await fetch(window.APP_CONFIG.API_BASE_URL + path, {
            method: 'POST',
            headers: headers
        });
        
        if (!res.ok) {
            let message = "Download failed (" + res.status + ")";
            if (res.status === 403) message = "You don't have permission.";
            if (res.status === 401) message = "Session expired. Please login again.";
            throw new ApiError(res.status, message);
        }
        
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement("a");
        a.href = url;
        a.download = filename;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);
        
    } catch (error) {
        throw error;
    }
}
};

class ApiError extends Error {
  constructor(status, message) {
    super(message);
    this.status = status;
  }
}