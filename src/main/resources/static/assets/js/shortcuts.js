// ============================================================
// Dawson's Laundry - Keyboard Shortcuts (FINAL WORKING)
// ============================================================

(function() {
    'use strict';
    
    console.log('⌨️ Keyboard shortcuts loading...');
    
    // Store shortcuts in an object
    // NOTE: 'n' below is bound to Alt+N, not Ctrl+N (see ALT SHORTCUTS
    // block below). Ctrl+N is a hard-reserved "open new window"
    // shortcut in every major browser - Chrome, Firefox and Edge all
    // refuse to let ANY web page's preventDefault() stop it, by design
    // (so a malicious page can never trap a keyboard-only user from
    // opening a new tab to escape it). There is no JS workaround; the
    // only fix is to use a different key combination.
    const SHORTCUT_ACTIONS = {
        's': function() {
            console.log('✅ Ctrl+S → Save');
            const path = window.location.pathname;
            if (path.includes('new-bill.html')) {
                const saveBtn = document.getElementById('save-btn');
                const savePrintBtn = document.getElementById('save-print-btn');
                if (saveBtn && !saveBtn.disabled) {
                    saveBtn.click();
                } else if (savePrintBtn && !savePrintBtn.disabled) {
                    savePrintBtn.click();
                }
            } else if (path.includes('bill-detail.html')) {
                const pdfBtn = document.getElementById('pdf-btn');
                if (pdfBtn) {
                    pdfBtn.click();
                }
            }
        },
        'p': function() {
            console.log('✅ Ctrl+P → Print');
            const path = window.location.pathname;
            if (path.includes('bill-detail.html')) {
                const printBtn = document.getElementById('print-btn');
                if (printBtn) {
                    printBtn.click();
                }
            } else if (path.includes('new-bill.html')) {
                const savePrintBtn = document.getElementById('save-print-btn');
                if (savePrintBtn && !savePrintBtn.disabled) {
                    savePrintBtn.click();
                }
            }
        },
        'f': function() {
            console.log('✅ Ctrl+F → Focus Search');
            focusSearchInput();
        }
    };

    // Alt shortcuts (used instead of Ctrl for keys the browser won't
    // let a web page override, e.g. Ctrl+N)
    const ALT_SHORTCUT_ACTIONS = {
        'n': function() {
            console.log('✅ Alt+N → New Bill');
            window.location.href = 'new-bill.html';
        }
    };
    
    // Navigation shortcuts
    const NAV_SHORTCUTS = {
        'd': 'dashboard.html',
        'b': 'bills.html',
        'c': 'customers.html',
        'p': 'products.html',
        'r': 'reports.html'
    };
    
    // ============================================================
    // NOTE: We used to have a separate capture-phase listener here
    // that called stopImmediatePropagation() to block the browser's
    // native Ctrl+N/S/P/F behaviour. That listener ran BEFORE
    // handleShortcuts (registered below, bubble phase) and its
    // stopImmediatePropagation() call prevented handleShortcuts from
    // ever running for those keys - which is why the shortcuts
    // appeared "broken". Calling e.preventDefault() inside
    // handleShortcuts itself (see CTRL SHORTCUTS block below) is
    // enough to stop the browser's native action, so that separate
    // blocker has been removed.
    // ============================================================
    // MAIN SHORTCUT HANDLER
    // ============================================================
    
    function handleShortcuts(e) {
        const tag = e.target.tagName.toLowerCase();
        const isTypingField = (tag === 'input' || tag === 'textarea' || tag === 'select');

        // ============================================================
        // CTRL SHORTCUTS - must run BEFORE the "typing in a field" check
        // below. Ctrl+key combos never insert characters, so there's no
        // reason to skip them while a field has focus - and on
        // new-bill.html the product-search box is auto-focused the
        // instant the page loads, so "skip while typing" effectively
        // meant "skip always". That's why Ctrl+S / Ctrl+P fell through
        // to the browser's native Save Page / Print dialogs instead of
        // reaching our e.preventDefault().
        // ============================================================
        if (e.ctrlKey && !e.shiftKey) {
            const key = e.key.toLowerCase();
            if (SHORTCUT_ACTIONS[key]) {
                e.preventDefault();
                e.stopPropagation();
                SHORTCUT_ACTIONS[key]();
                return;
            }
        }
        
        // ============================================================
        // CTRL+SHIFT NAVIGATION - same reasoning, runs regardless of focus
        // ============================================================
        if (e.ctrlKey && e.shiftKey) {
            const key = e.key.toLowerCase();
            if (NAV_SHORTCUTS[key]) {
                e.preventDefault();
                e.stopPropagation();
                console.log('✅ Ctrl+Shift+' + key.toUpperCase() + ' → ' + NAV_SHORTCUTS[key]);
                window.location.href = NAV_SHORTCUTS[key];
                return;
            }
        }
        
        // ============================================================
        // ALT SHORTCUTS - runs regardless of focus, same reasoning as
        // Ctrl above. This is where Alt+N (New Bill) lives now, since
        // Ctrl+N can't be intercepted by any browser.
        // ============================================================
        if (e.altKey && !e.ctrlKey && !e.shiftKey) {
            const key = e.key.toLowerCase();
            if (ALT_SHORTCUT_ACTIONS[key]) {
                e.preventDefault();
                e.stopPropagation();
                ALT_SHORTCUT_ACTIONS[key]();
                return;
            }
        }
        
        // ============================================================
        // BILLS PAGE - ALT+Number Filters - modifier-based, also runs
        // regardless of focus (Alt+digit doesn't type a character)
        // ============================================================
        if (window.location.pathname.includes('bills.html') && e.altKey) {
            const statusMap = {
                '1': 'ALL',
                '2': 'PENDING',
                '3': 'PAID',
                '4': 'VOIDED',
                '5': 'RETURNED'
            };
            
            if (statusMap[e.key]) {
                e.preventDefault();
                e.stopPropagation();
                console.log('✅ Alt+' + e.key + ' → Filter: ' + statusMap[e.key]);
                const statusSelect = document.getElementById('filter-status');
                if (statusSelect) {
                    statusSelect.value = statusMap[e.key];
                    const applyBtn = document.getElementById('filter-apply');
                    if (applyBtn) applyBtn.click();
                }
                return;
            }
        }
        
        // ============================================================
        // ESCAPE - also runs regardless of focus
        // ============================================================
        if (e.key === 'Escape') {
            e.preventDefault();
            e.stopPropagation();
            closeAllModals();
            return;
        }
        
        // ============================================================
        // Everything below is a plain, unmodified key (+, Delete,
        // arrows, Enter) that WOULD collide with normal typing, so skip
        // it while a field has focus - EXCEPT the product-search box on
        // the New Bill page, which relies on exactly these keys (arrow
        // keys / Enter) to navigate and pick a product, and needs them
        // to keep working while it's focused (it's auto-focused, so
        // this is where the user's cursor sits most of the time).
        // ============================================================
        const isProductSearchBox = e.target.id === 'product-search';
        if (isTypingField && !isProductSearchBox) {
            return;
        }
        
        // ============================================================
        // NEW BILL PAGE SHORTCUTS
        // ============================================================
        if (window.location.pathname.includes('new-bill.html')) {
            // + key - Custom Item
            if (e.key === '+') {
                e.preventDefault();
                e.stopPropagation();
                console.log('✅ + → Custom Item');
                if (typeof window.openCustomItemModal === 'function') {
                    window.openCustomItemModal();
                }
                return;
            }
            
            // Delete - Remove last item
            if (e.key === 'Delete' || e.key === 'Del') {
                e.preventDefault();
                e.stopPropagation();
                console.log('✅ Delete → Remove Last');
                if (typeof window.removeLastItem === 'function') {
                    window.removeLastItem();
                }
                return;
            }
            
            // Arrow keys - Navigate products
            if (e.key === 'ArrowDown' || e.key === 'ArrowUp') {
                e.preventDefault();
                e.stopPropagation();
                if (typeof window.handleProductNavigation === 'function') {
                    window.handleProductNavigation(e.key);
                }
                return;
            }
            
            // Enter - Select product
            if (e.key === 'Enter') {
                e.preventDefault();
                e.stopPropagation();
                if (typeof window.handleProductSelect === 'function') {
                    window.handleProductSelect();
                }
                return;
            }
        }
    }
    
    // ============================================================
    // HELPER FUNCTIONS
    // ============================================================
    
    function focusSearchInput() {
        const searchIds = ['global-search-input', 'product-search', 'search-input', 'filter-search', 'search'];
        for (const id of searchIds) {
            const input = document.getElementById(id);
            if (input) {
                input.focus();
                input.select();
                return;
            }
        }
    }
    
    function closeAllModals() {
        document.querySelectorAll('.modal-overlay.open').forEach(modal => {
            modal.classList.remove('open');
        });
        const dropdown = document.getElementById('customerDropdown');
        if (dropdown) dropdown.style.display = 'none';
        const searchDropdown = document.getElementById('global-search-dropdown');
        if (searchDropdown) searchDropdown.style.display = 'none';
        if (typeof window.clearPendingProduct === 'function') {
            window.clearPendingProduct();
        }
    }
    
    // ============================================================
    // INITIALIZE
    // ============================================================
    
    // Remove any existing listener, then add a single one.
    // (Previously this was added on BOTH document and window, which
    // made every non-Ctrl shortcut - Escape, +, Delete, arrows, Enter,
    // Alt+number - fire twice.)
    document.removeEventListener('keydown', handleShortcuts);
    document.addEventListener('keydown', handleShortcuts);
    
    console.log('✅ Keyboard shortcuts ready!');
    
})();