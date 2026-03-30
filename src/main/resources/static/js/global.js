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

    // Add more global behaviors here if needed
});
