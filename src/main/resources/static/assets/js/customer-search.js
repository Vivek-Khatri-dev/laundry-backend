// ============================================================
// Dawson's Laundry - Customer Search
// ============================================================

const CustomerSearch = {
    customers: [],
    selectedCustomer: null,

    init(inputId, callback) {
        this.callback = callback;
        this.input = document.getElementById(inputId);
        if (!this.input) return;

        // Create dropdown container
        this.dropdown = document.createElement('div');
        this.dropdown.className = 'customer-dropdown';
        this.dropdown.style.cssText = `
            position: absolute;
            top: 100%;
            left: 0;
            right: 0;
            background: var(--surface);
            border: 1px solid var(--border);
            border-radius: 8px;
            box-shadow: var(--shadow-md);
            max-height: 200px;
            overflow-y: auto;
            display: none;
            z-index: 100;
        `;
        this.input.parentNode.style.position = 'relative';
        this.input.parentNode.appendChild(this.dropdown);

        // Input events
        this.input.addEventListener('input', debounce(() => this.search(), 300));
        this.input.addEventListener('focus', () => {
            if (this.input.value.length > 0) this.search();
        });
        this.input.addEventListener('blur', () => {
            setTimeout(() => this.hideDropdown(), 200);
        });

        // Keyboard shortcuts
        this.input.addEventListener('keydown', (e) => {
            const items = this.dropdown.querySelectorAll('.customer-item');
            if (!items.length) return;

            let currentIndex = -1;
            items.forEach((item, i) => {
                if (item.classList.contains('selected')) currentIndex = i;
            });

            if (e.key === 'ArrowDown') {
                e.preventDefault();
                currentIndex = Math.min(currentIndex + 1, items.length - 1);
                this.highlightItem(items, currentIndex);
            } else if (e.key === 'ArrowUp') {
                e.preventDefault();
                currentIndex = Math.max(currentIndex - 1, 0);
                this.highlightItem(items, currentIndex);
            } else if (e.key === 'Enter') {
                e.preventDefault();
                if (currentIndex >= 0) {
                    items[currentIndex].click();
                } else if (items.length > 0) {
                    items[0].click();
                }
            }
        });

        // Load initial customers
        this.loadCustomers();
    },

    async loadCustomers() {
        try {
            const customers = await Api.get('/customers');
            this.customers = customers || [];
        } catch (err) {
            console.error('Failed to load customers:', err);
        }
    },

    async search() {
        const query = this.input.value.trim();
        if (!query) {
            this.hideDropdown();
            return;
        }

        try {
            const results = await Api.get(`/customers/search?q=${encodeURIComponent(query)}`);
            this.showResults(results);
        } catch (err) {
            console.error('Search failed:', err);
        }
    },

    showResults(results) {
        this.dropdown.innerHTML = '';
        if (!results || !results.length) {
            this.dropdown.innerHTML = `
                <div style="padding:10px 14px;color:var(--ink-soft);font-size:13px;">
                    No customers found. Press Enter to create new.
                </div>
            `;
            this.dropdown.style.display = 'block';
            return;
        }

        results.forEach(customer => {
            const item = document.createElement('div');
            item.className = 'customer-item';
            item.style.cssText = `
                padding:10px 14px;
                cursor:pointer;
                border-bottom:1px solid var(--border);
                display:flex;
                justify-content:space-between;
                align-items:center;
            `;
            item.innerHTML = `
                <div>
                    <div style="font-weight:600;font-size:13px;">${escapeHtml(customer.name)}</div>
                    <div style="font-size:11px;color:var(--ink-soft);">
                        ${customer.phone || 'No phone'} · ${customer.totalOrders || 0} orders
                    </div>
                </div>
                <div style="font-size:12px;color:var(--accent);">
                    ${customer.totalSpent ? formatMoney(customer.totalSpent) : 'Rs. 0'}
                </div>
            `;
            item.addEventListener('mousedown', () => {
                this.selectCustomer(customer);
            });
            this.dropdown.appendChild(item);
        });

        this.dropdown.style.display = 'block';
    },

    highlightItem(items, index) {
        items.forEach((item, i) => {
            item.classList.toggle('selected', i === index);
            item.style.background = i === index ? 'var(--accent-soft)' : '';
        });
        if (index >= 0 && items[index]) {
            items[index].scrollIntoView({ block: 'nearest' });
        }
    },

    selectCustomer(customer) {
        this.selectedCustomer = customer;
        this.input.value = customer.name;
        this.input.dataset.customerId = customer.id;
        this.hideDropdown();
        if (this.callback) {
            this.callback(customer);
        }
        // Auto-fill phone if available
        const phoneInput = document.getElementById('customerPhone');
        if (phoneInput && customer.phone) {
            phoneInput.value = customer.phone;
        }
        toastSuccess(`Customer selected: ${customer.name}`);
    },

    hideDropdown() {
        this.dropdown.style.display = 'none';
    },

    getSelectedCustomer() {
        return this.selectedCustomer;
    },

    clear() {
        this.selectedCustomer = null;
        this.input.value = '';
        this.input.dataset.customerId = '';
        this.hideDropdown();
    }
};