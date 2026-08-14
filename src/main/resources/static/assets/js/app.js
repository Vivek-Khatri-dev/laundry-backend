// ============================================================
// Dawson's Laundry — shared shell, icons, and small UI helpers
// ============================================================

const Icons = {
  home: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 11.5 12 4l9 7.5"/><path d="M5.5 10v9a1 1 0 0 0 1 1H9a1 1 0 0 0 1-1v-4a1 1 0 0 1 1-1h2a1 1 0 0 1 1 1v4a1 1 0 0 0 1 1h2.5a1 1 0 0 0 1-1v-9"/></svg>`,
  ticket: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M3 8.5A1.5 1.5 0 0 1 4.5 7h15A1.5 1.5 0 0 1 21 8.5v2a1.5 1.5 0 0 0 0 3v2A1.5 1.5 0 0 1 19.5 17h-15A1.5 1.5 0 0 1 3 15.5v-2a1.5 1.5 0 0 0 0-3z"/><path d="M13 7v10" stroke-dasharray="1.6 2.4"/></svg>`,
  plus: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/></svg>`,
  plusCircle: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 8v8M8 12h8"/></svg>`,
  shirt: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M8 4 4 7l2 3 2-1v10a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1V9l2 1 2-3-4-3-2 2h-2z"/></svg>`,
  chart: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 20V10M12 20V4M20 20v-7"/></svg>`,
  users: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="9" cy="8" r="3.2"/><path d="M3 20c0-3.3 2.7-5.5 6-5.5s6 2.2 6 5.5"/><path d="M16 4.8c1.6.4 2.8 1.8 2.8 3.4 0 1.7-1.2 3-2.8 3.4M20.8 20c0-2.7-1.8-4.7-4.3-5.3"/></svg>`,
  shield: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3.5 5 6v5.5c0 4.6 3 7.7 7 9 4-1.3 7-4.4 7-9V6z"/><path d="M9 12l2 2 4-4"/></svg>`,
  download: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 3v12m0 0-4-4m4 4 4-4"/><path d="M4 17v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2"/></svg>`,
  search: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="11" cy="11" r="7"/><path d="m21 21-4.3-4.3"/></svg>`,
  logout: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><path d="M16 17l5-5-5-5M21 12H9"/></svg>`,
  x: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M18 6 6 18M6 6l12 12"/></svg>`,
  check: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M20 6 9 17l-5-5"/></svg>`,
  checkCircle: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="m8 12.5 2.5 2.5L16 9.5"/></svg>`,
  alert: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 9v4.5M12 17h.01"/><path d="M10.4 3.9 2.3 18a1.7 1.7 0 0 0 1.5 2.5h16.4a1.7 1.7 0 0 0 1.5-2.5L13.6 3.9a1.7 1.7 0 0 0-3.2 0z"/></svg>`,
  printer: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9V4h12v5"/><rect x="4" y="9" width="16" height="8" rx="1.4"/><path d="M6 14h12v6H6z"/></svg>`,
  file: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M7 3h7l5 5v13a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1V4a1 1 0 0 1 1-1z"/><path d="M14 3v5h5"/></svg>`,
  edit: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M12 20h9"/><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z"/></svg>`,
  ban: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="m6 6 12 12"/></svg>`,
  undo: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 10h10a5 5 0 0 1 0 10H6"/><path d="m4 10 5-5M4 10l5 5"/></svg>`,
  eye: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12z"/><circle cx="12" cy="12" r="2.6"/></svg>`,
  trash: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M4 7h16M9 7V4h6v3m-8 0 1 13a1 1 0 0 0 1 1h6a1 1 0 0 0 1-1l1-13"/></svg>`,
  cash: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><rect x="2.5" y="6" width="19" height="12" rx="1.6"/><circle cx="12" cy="12" r="3"/><path d="M6 9v0M18 15v0"/></svg>`,
  clock: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><circle cx="12" cy="12" r="9"/><path d="M12 7v5l3.5 2"/></svg>`,
  bell: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6"/><path d="M10 19a2 2 0 0 0 4 0"/></svg>`,
  phone: `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"><path d="M6.6 10.8a15 15 0 0 0 6.6 6.6l2.2-2.2a1.3 1.3 0 0 1 1.3-.3c1 .3 2.1.5 3.2.5a1.3 1.3 0 0 1 1.3 1.3V20a1.3 1.3 0 0 1-1.3 1.3C10.6 21.3 2.7 13.4 2.7 4.3A1.3 1.3 0 0 1 4 3h3.3a1.3 1.3 0 0 1 1.3 1.3c0 1.1.2 2.2.5 3.2a1.3 1.3 0 0 1-.3 1.3z"/></svg>`
};

/** Logo mark: a claim-ticket silhouette with a torn/perforated edge and a "D" cut-out. */
function logoMarkSvg(size = 34) {
  return `
  <svg width="${size}" height="${size}" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
    <path d="M4 8.5A2.5 2.5 0 0 1 6.5 6H27l9 9v1c-2.2 0-2.2 3-2.2 3s0 3 2.2 3v1l-9 9H6.5A2.5 2.5 0 0 1 4 29.5z" fill="var(--accent, #2E9C8F)"/>
    <circle cx="10.5" cy="12" r="1.6" fill="var(--primary, #103B49)" opacity=".35"/>
    <path d="M14 12.5h4.6c3.3 0 5.6 2.4 5.6 5.5s-2.3 5.5-5.6 5.5H14z" stroke="#fff" stroke-width="2.1" stroke-linejoin="round" fill="none"/>
  </svg>`;
}

function brandLockup({ size = 34, dark = false } = {}) {
  return `
    <div class="brand">
      <span class="brand-mark">${logoMarkSvg(size)}</span>
      <span>
        <span class="brand-word">Dawson's</span>
        <span class="brand-tag">Laundry Co.</span>
      </span>
    </div>`;
}

const NAV_ITEMS = [
  { id: "dashboard", href: "dashboard.html", label: "Dashboard", icon: Icons.home },
  { id: "kanban", href: "kanban.html", label: "Order Tracking", icon: Icons.ticket },
  { id: "bills", href: "bills.html", label: "Bills", icon: Icons.file },
  { id: "new-bill", href: "new-bill.html", label: "New Bill", icon: Icons.plusCircle },
  { id: "customers", href: "customers.html", label: "Customers", icon: Icons.users },
  { id: "products", href: "products.html", label: "Products", icon: Icons.shirt },
  { id: "reports", href: "reports.html", label: "Reports", icon: Icons.chart },
];
const NAV_ITEMS_ADMIN = [
  { id: "users", href: "users.html", label: "Staff Accounts", icon: Icons.users },
  { id: "audit", href: "audit.html", label: "Audit Log", icon: Icons.shield },
  { id: "backup", href: "backup.html", label: "Backup", icon: Icons.download },
];

// ============================================================
// Password Change Modal
// ============================================================

function openChangePasswordModal() {
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay open";
    overlay.innerHTML = `
        <div class="modal" style="max-width:440px;">
            <div class="modal-head">
                <h3 style="font-size:16px;">Change Password</h3>
                <button class="icon-btn" id="cp-close">${Icons.x}</button>
            </div>
            <div class="modal-body">
                <div class="field">
                    <label>Current Password</label>
                    <div style="position:relative;">
                        <input type="password" id="cp-old" placeholder="Enter your current password" required style="padding-right:40px;">
                        <button type="button" class="password-toggle-btn" data-target="cp-old" style="position:absolute;right:10px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:var(--ink-faint);padding:4px;">
                            ${Icons.eye}
                        </button>
                    </div>
                </div>
                <div class="field">
                    <label>New Password</label>
                    <div style="position:relative;">
                        <input type="password" id="cp-new" placeholder="At least 6 characters" minlength="6" required style="padding-right:40px;">
                        <button type="button" class="password-toggle-btn" data-target="cp-new" style="position:absolute;right:10px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:var(--ink-faint);padding:4px;">
                            ${Icons.eye}
                        </button>
                    </div>
                </div>
                <div class="field">
                    <label>Confirm New Password</label>
                    <div style="position:relative;">
                        <input type="password" id="cp-confirm" placeholder="Re-enter new password" required style="padding-right:40px;">
                        <button type="button" class="password-toggle-btn" data-target="cp-confirm" style="position:absolute;right:10px;top:50%;transform:translateY(-50%);background:none;border:none;cursor:pointer;color:var(--ink-faint);padding:4px;">
                            ${Icons.eye}
                        </button>
                    </div>
                </div>
                <div id="cp-error" style="color:var(--danger);font-size:12.5px;display:none;margin-top:-8px;"></div>
            </div>
            <div class="modal-foot">
                <button class="btn btn-secondary" id="cp-cancel">Cancel</button>
                <button class="btn btn-primary" id="cp-save">${Icons.check} Update Password</button>
            </div>
        </div>
    `;
    document.body.appendChild(overlay);

    overlay.querySelectorAll('.password-toggle-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const targetId = this.dataset.target;
            const input = document.getElementById(targetId);
            if (input.type === 'password') {
                input.type = 'text';
                this.innerHTML = Icons.eye.replace('stroke="currentColor"', 'stroke="var(--accent)"');
            } else {
                input.type = 'password';
                this.innerHTML = Icons.eye;
            }
        });
    });

    const close = () => overlay.remove();
    overlay.querySelector("#cp-close").addEventListener("click", close);
    overlay.querySelector("#cp-cancel").addEventListener("click", close);
    overlay.addEventListener("click", (e) => { if (e.target === overlay) close(); });

    overlay.querySelector("#cp-save").addEventListener("click", async () => {
        const oldPass = overlay.querySelector("#cp-old").value;
        const newPass = overlay.querySelector("#cp-new").value;
        const confirmPass = overlay.querySelector("#cp-confirm").value;
        const errorEl = overlay.querySelector("#cp-error");

        errorEl.style.display = "none";

        if (!oldPass || !newPass || !confirmPass) {
            errorEl.textContent = "All fields are required.";
            errorEl.style.display = "block";
            return;
        }
        if (newPass.length < 6) {
            errorEl.textContent = "New password must be at least 6 characters.";
            errorEl.style.display = "block";
            return;
        }
        if (newPass !== confirmPass) {
            errorEl.textContent = "New passwords do not match.";
            errorEl.style.display = "block";
            return;
        }

        const btn = overlay.querySelector("#cp-save");
        btn.disabled = true;
        btn.innerHTML = `<span class="spinner"></span> Updating…`;

        try {
            await Api.post("/user/change-password", {
                oldPassword: oldPass,
                newPassword: newPass
            });
            toastSuccess("Password updated successfully!");
            close();
        } catch (err) {
            errorEl.textContent = apiErrorMessage(err);
            errorEl.style.display = "block";
            btn.disabled = false;
            btn.innerHTML = `${Icons.check} Update Password`;
        }
    });
}

// ============================================================
// renderShell - Working Version
// ============================================================

function renderShell({ active, title, subtitle = "", actions = "" }) {
  const session = Auth.get();
  if (!session) return;
  const isAdmin = session.role === "ADMIN";

  const navHtml = NAV_ITEMS.map(item =>
    `<a class="nav-link ${item.id === active ? "active" : ""}" href="${item.href}">${item.icon}<span>${item.label}</span></a>`
  ).join("");

  const adminNavHtml = isAdmin ? `
    <div class="sidebar-section-label">Administration</div>
    ${NAV_ITEMS_ADMIN.map(item =>
      `<a class="nav-link ${item.id === active ? "active" : ""}" href="${item.href}">${item.icon}<span>${item.label}</span></a>`
    ).join("")}` : "";

  document.getElementById("app-shell").innerHTML = `
    <!-- Sidebar Backdrop -->
    <div class="sidebar-backdrop" id="sidebar-backdrop"></div>
    
    <aside class="sidebar" id="sidebar">
      ${brandLockup({ size: 32 })}
      <nav class="sidebar-nav">
        ${navHtml}
        ${adminNavHtml}
      </nav>
      <div class="sidebar-foot">
        <div class="avatar">${Auth.initials()}</div>
        <div class="sidebar-foot-meta">
          <div class="sidebar-foot-name">${escapeHtml(session.fullName)}</div>
          <div class="sidebar-foot-role">${session.role === "ADMIN" ? "Administrator" : "Cashier"}</div>
          <button id="change-password-btn" style="background:none;border:none;color:rgba(234,243,242,.6);font-size:11px;cursor:pointer;padding:0;margin-top:2px;text-decoration:underline;">
            Change password
          </button>
        </div>
        <button class="logout-btn" id="sidebar-logout" title="Log out" style="background:var(--danger);color:#fff;border:none;border-radius:8px;padding:6px 10px;cursor:pointer;font-weight:600;display:flex;align-items:center;gap:4px;font-size:11px;">
          ${Icons.logout} Logout
        </button>
      </div>
    </aside>
    <div class="main">
      <header class="topbar">
        <div class="topbar-head">
          <button class="nav-toggle" id="sidebar-toggle" aria-label="Toggle navigation">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="3" y1="6" x2="21" y2="6"></line>
              <line x1="3" y1="12" x2="21" y2="12"></line>
              <line x1="3" y1="18" x2="21" y2="18"></line>
            </svg>
          </button>
          <div>
            <h1>${title}</h1>
            ${subtitle ? `<div class="topbar-sub">${subtitle}</div>` : ""}
          </div>
        </div>
        <div class="topbar-actions">
          ${actions}
          <button class="btn btn-danger btn-sm" id="topbar-logout" title="Log out">${Icons.logout} Logout</button>
        </div>
      </header>
      <main class="content" id="content"></main>
    </div>
  `;

  // ============================================================
  // SIDEBAR TOGGLE - Simplified
  // ============================================================
  const sidebar = document.getElementById('sidebar');
  const backdrop = document.getElementById('sidebar-backdrop');
  const toggleBtn = document.getElementById('sidebar-toggle');

  // Start with sidebar closed
  sidebar.classList.remove('open');
  backdrop.classList.remove('open');

  function toggleSidebar() {
    sidebar.classList.toggle('open');
    backdrop.classList.toggle('open');
  }

  function closeSidebar() {
    sidebar.classList.remove('open');
    backdrop.classList.remove('open');
  }

  if (toggleBtn) {
    toggleBtn.addEventListener('click', toggleSidebar);
  }

  if (backdrop) {
    backdrop.addEventListener('click', closeSidebar);
  }

  // ============================================================
  // CLOSE SIDEBAR WHEN NAV LINK IS CLICKED
  // ============================================================
  document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', function() {
      closeSidebar();
    });
  });

  // Close sidebar on Escape key
  document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape' && sidebar.classList.contains('open')) {
      closeSidebar();
    }
  });

  // Topbar logout
  document.getElementById("topbar-logout").addEventListener("click", () => {
    Auth.clear();
    location.href = "login.html";
  });

  // Sidebar logout
  document.getElementById("sidebar-logout").addEventListener("click", () => {
    Auth.clear();
    location.href = "login.html";
  });
  
  // Change password button
  const changePwBtn = document.getElementById("change-password-btn");
  if (changePwBtn) {
    changePwBtn.addEventListener("click", openChangePasswordModal);
  }
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str).replace(/[&<>"']/g, m => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[m]));
}

function formatMoney(value) {
  const n = Number(value || 0);
  return "Rs. " + n.toLocaleString("en-PK", { minimumFractionDigits: 0, maximumFractionDigits: 2 });
}

// Returns today's date (or a given Date) as a YYYY-MM-DD string using the
// browser's LOCAL timezone. Used to default date pickers (e.g. Reports) to
// "today" as the user actually sees it — new Date().toISOString() would use
// UTC instead and can be off by a day depending on the user's timezone.
function localDateStr(d) {
  const date = d ? new Date(d) : new Date();
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

// ============================================================
// DATE FORMATTING FIXED - No timezone conversion issues
// ============================================================

function formatDate(d) {
    if (!d) return "—";
    
    // If it's already in YYYY-MM-DD format, parse it manually
    if (typeof d === 'string' && d.match(/^\d{4}-\d{2}-\d{2}$/)) {
        const parts = d.split('-');
        const year = parseInt(parts[0]);
        const month = parseInt(parts[1]) - 1; // JS months are 0-indexed
        const day = parseInt(parts[2]);
        const date = new Date(year, month, day);
        if (!isNaN(date)) {
            return date.toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
        }
    }
    
    // Fallback for other formats
    const date = new Date(d);
    if (isNaN(date)) return d;
    return date.toLocaleDateString("en-GB", { day: "2-digit", month: "short", year: "numeric" });
}

function formatDateTime(d) {
    if (!d) return "—";
    
    // If it's already in YYYY-MM-DD format, parse it manually
    if (typeof d === 'string' && d.match(/^\d{4}-\d{2}-\d{2}$/)) {
        const parts = d.split('-');
        const year = parseInt(parts[0]);
        const month = parseInt(parts[1]) - 1;
        const day = parseInt(parts[2]);
        const date = new Date(year, month, day);
        if (!isNaN(date)) {
            return date.toLocaleString("en-GB", { 
                day: "2-digit", 
                month: "short", 
                year: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            });
        }
    }
    
    const date = new Date(d);
    if (isNaN(date)) return d;
    return date.toLocaleString("en-GB", { 
        day: "2-digit", 
        month: "short", 
        year: "numeric", 
        hour: "2-digit", 
        minute: "2-digit" 
    });
}

function statusBadge(status) {
  const map = {
    PENDING: "Pending",
    PAID: "Paid",
    VOIDED: "Voided",
    RETURNED: "Returned",
    PROCESSING: "Processing",
    READY: "Ready"
  };
  return `<span class="ticket-badge status-${status.toLowerCase()}">${map[status] || status}</span>`;
}

// ---------------- Toast ----------------
function ensureToastStack() {
  let stack = document.getElementById("toast-stack");
  if (!stack) {
    stack = document.createElement("div");
    stack.id = "toast-stack";
    document.body.appendChild(stack);
  }
  return stack;
}
function toast(message, type = "default") {
  const stack = ensureToastStack();
  const el = document.createElement("div");
  el.className = "toast " + type;
  const icon = type === "success" ? Icons.checkCircle : type === "error" ? Icons.alert : "";
  el.innerHTML = `${icon ? `<span style="width:16px;height:16px;display:flex">${icon}</span>` : ""}<span>${escapeHtml(message)}</span>`;
  stack.appendChild(el);
  setTimeout(() => {
    el.style.transition = "opacity .25s, transform .25s";
    el.style.opacity = "0";
    el.style.transform = "translateY(6px)";
    setTimeout(() => el.remove(), 250);
  }, 3400);
}
function toastSuccess(msg) { toast(msg, "success"); }
function toastError(msg) { toast(msg, "error"); }

function apiErrorMessage(err) {
  return err instanceof ApiError ? err.message : "Something went wrong. Please try again.";
}

// ---------------- Modal ----------------
function openModal(id) { document.getElementById(id).classList.add("open"); }
function closeModal(id) { document.getElementById(id).classList.remove("open"); }

/** Simple confirm dialog returning a Promise<boolean>, with an optional reason textarea. */
function confirmDialog({ title, message, confirmLabel = "Confirm", danger = false, requireReason = false }) {
  return new Promise((resolve) => {
    const overlay = document.createElement("div");
    overlay.className = "modal-overlay open";
    overlay.innerHTML = `
      <div class="modal" style="max-width:440px">
        <div class="modal-head">
          <h3 style="font-size:16px">${title}</h3>
          <button class="icon-btn" data-act="cancel">${Icons.x}</button>
        </div>
        <div class="modal-body">
          <p class="text-muted" style="margin:0 0 ${requireReason ? "14px" : "0"}">${message}</p>
          ${requireReason ? `
            <div class="field" style="margin-bottom:0">
              <label>Reason</label>
              <textarea id="confirm-reason" rows="3" placeholder="Required — explain why"></textarea>
            </div>` : ""}
        </div>
        <div class="modal-foot">
          <button class="btn btn-secondary" data-act="cancel">Cancel</button>
          <button class="btn ${danger ? "btn-danger-solid" : "btn-primary"}" data-act="confirm">${confirmLabel}</button>
        </div>
      </div>`;
    document.body.appendChild(overlay);
    const cleanup = (result) => { overlay.remove(); resolve(result); };
    overlay.querySelectorAll('[data-act="cancel"]').forEach(b => b.addEventListener("click", () => cleanup(false)));
    overlay.addEventListener("click", (e) => { if (e.target === overlay) cleanup(false); });
    overlay.querySelector('[data-act="confirm"]').addEventListener("click", () => {
      if (requireReason) {
        const reason = overlay.querySelector("#confirm-reason").value.trim();
        if (!reason) { toastError("Please provide a reason."); return; }
        cleanup(reason);
      } else {
        cleanup(true);
      }
    });
  });
}

function debounce(fn, wait = 250) {
  let t;
  return (...args) => { clearTimeout(t); t = setTimeout(() => fn(...args), wait); };
}