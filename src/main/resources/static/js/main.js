// Wait for the DOM to be fully loaded
document.addEventListener('DOMContentLoaded', () => {

    // Auth Tabs Logic
    const tabBtns = document.querySelectorAll('.tab-btn');
    const authForms = document.querySelectorAll('.auth-form');
    const switchToLoginBtn = document.querySelector('.switch-to-login');

    if (tabBtns.length > 0) {
        // Function to switch active tab
        const switchTab = (tabId) => {
            // Update tabs
            tabBtns.forEach(btn => {
                if(btn.dataset.tab === tabId) {
                    btn.classList.add('active');
                } else {
                    btn.classList.remove('active');
                }
            });

            // Update forms with simple animation logic
            authForms.forEach(form => {
                if (form.id === `${tabId}-form`) {
                    form.classList.remove('hidden');
                    // slight delay to allow display block to apply before animating opacity
                    setTimeout(() => {
                        form.style.opacity = '1';
                        form.style.transform = 'translateY(0)';
                        form.style.pointerEvents = 'auto';
                        form.style.visibility = 'visible';
                    }, 50);
                } else {
                    form.style.opacity = '0';
                    form.style.transform = 'translateY(10px)';
                    form.style.pointerEvents = 'none';
                    // Wait for transition to finish before hiding
                    setTimeout(() => {
                        form.classList.add('hidden');
                    }, 400); // 400ms matches css transition
                }
            });
        };

        // Event listeners for tabs
        tabBtns.forEach(btn => {
            btn.addEventListener('click', () => {
                const tabId = btn.dataset.tab;
                switchTab(tabId);
            });
        });

        // Specific 'switch to login' link in register form
        if (switchToLoginBtn) {
            switchToLoginBtn.addEventListener('click', (e) => {
                e.preventDefault();
                switchTab('login');
            });
        }

        // Handle form submits for demonstration purposes
        const loginForm = document.getElementById('login-form');
        const registerForm = document.getElementById('register-form');

        if(loginForm) {
            loginForm.addEventListener('submit', (e) => {
                e.preventDefault();
                // Add a simple loading effect
                const btn = loginForm.querySelector('button');
                const originalText = btn.innerText;
                btn.innerText = 'ĐANG XỬ LÝ...';

                // Simulate network request then redirect
                setTimeout(() => {
                    window.location.href = '/dashboard';
                }, 800);
            });
        }

        if(registerForm) {
            registerForm.addEventListener('submit', (e) => {
                e.preventDefault();
                // Simulation
                const btn = registerForm.querySelector('button');
                btn.innerText = 'ĐANG XỬ LÝ...';
                setTimeout(() => {
                    switchTab('login');
                    btn.innerText = 'ĐĂNG KÝ';
                }, 800);
            });
        }
    }
});
