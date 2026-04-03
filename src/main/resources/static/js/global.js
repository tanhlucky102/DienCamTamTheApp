/**
 * global.js - Shared functionality across all pages
 */
document.addEventListener('DOMContentLoaded', () => {
    // 1. Global Scroll to Top Logic
    const scrollElements = document.querySelectorAll('.global-scroll-top');
    scrollElements.forEach(el => {
        el.addEventListener('click', () => {
            window.scrollTo({
                top: 0,
                behavior: 'smooth'
            });
        });
    });

    // 2. Ensuring all nav-back buttons have consistent behavior if not already handled
    const backButtons = document.querySelectorAll('.btn-nav-back');
    backButtons.forEach(btn => {
        if (!btn.getAttribute('onclick')) {
            btn.addEventListener('click', () => {
                window.history.back();
            });
        }
    });

    // 3. Account Dropdown Toggle
    const accountBtn = document.getElementById('account-btn');
    const accountDropdown = document.getElementById('account-dropdown');

    if (accountBtn && accountDropdown) {
        accountBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            if (accountDropdown.style.display === 'none' || accountDropdown.classList.contains('show') === false) {
                accountDropdown.style.display = 'flex';
                // Trigger reflow
                void accountDropdown.offsetWidth;
                accountDropdown.classList.add('show');
            } else {
                accountDropdown.classList.remove('show');
                setTimeout(() => accountDropdown.style.display = 'none', 300);
            }
        });

        // Close when clicking outside
        document.addEventListener('click', (e) => {
            if (!accountBtn.contains(e.target) && !accountDropdown.contains(e.target)) {
                accountDropdown.classList.remove('show');
                setTimeout(() => {
                    if(!accountDropdown.classList.contains('show')) {
                       accountDropdown.style.display = 'none';
                    }
                }, 300);
            }
        });
    }

});
