/**
 * global.js - Shared functionality across all pages
 * Quản lý thông tin user từ server session và cập nhật UI.
 */

window.AuthHelper = {
    /** Tên hiển thị: ưu tiên fullName → username → "Người dùng" */
    getDisplayName(user) {
        if (!user) return 'Người dùng';
        return user.fullName || user.username || 'Người dùng';
    },
    /** Chữ cái đầu để hiển thị avatar */
    getAvatar(user) {
        const name = this.getDisplayName(user);
        return name.charAt(0).toUpperCase();
    }
};

document.addEventListener('DOMContentLoaded', async () => {

    // -------------------------------------------------------
    // 1. Fetch user info từ session backend để cập nhật header
    // -------------------------------------------------------
    try {
        const response = await fetch('/auth/me');
        if (response.ok) {
            const result = await response.json();
            if (result.status === 200 && result.data) {
                const user = result.data;
                const dropdownUsername = document.querySelector('.dropdown-username');
                const dropdownRole     = document.querySelector('.dropdown-role');

                if (dropdownUsername) {
                    dropdownUsername.textContent = window.AuthHelper.getDisplayName(user);
                }
                if (dropdownRole) {
                    dropdownRole.textContent = user.email ? user.email : 'Thành viên';
                }
                
                // Cập nhật avatar ở một số nơi nếu có class .user-avatar-text
                document.querySelectorAll('.user-avatar-text').forEach(el => {
                    el.textContent = window.AuthHelper.getAvatar(user);
                });
            }
        }
    } catch (e) {
        console.error("Lỗi lấy thông tin session", e);
    }

    // -------------------------------------------------------
    // 2. Scroll to top
    // -------------------------------------------------------
    document.querySelectorAll('.global-scroll-top').forEach(el => {
        el.addEventListener('click', () => window.scrollTo({ top: 0, behavior: 'smooth' }));
    });

    // -------------------------------------------------------
    // 3. Nav back buttons
    // -------------------------------------------------------
    document.querySelectorAll('.btn-nav-back').forEach(btn => {
        if (!btn.getAttribute('onclick')) {
            btn.addEventListener('click', () => window.history.back());
        }
    });

    // -------------------------------------------------------
    // 4. Account Dropdown Toggle
    // -------------------------------------------------------
    const accountBtn      = document.getElementById('account-btn');
    const accountDropdown = document.getElementById('account-dropdown');

    if (accountBtn && accountDropdown) {
        accountBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            const isShown = accountDropdown.classList.contains('show');
            if (!isShown) {
                accountDropdown.style.display = 'flex';
                void accountDropdown.offsetWidth; // reflow
                accountDropdown.classList.add('show');
            } else {
                accountDropdown.classList.remove('show');
                setTimeout(() => accountDropdown.style.display = 'none', 300);
            }
        });

        document.addEventListener('click', (e) => {
            if (!accountBtn.contains(e.target) && !accountDropdown.contains(e.target)) {
                accountDropdown.classList.remove('show');
                setTimeout(() => {
                    if (!accountDropdown.classList.contains('show')) {
                        accountDropdown.style.display = 'none';
                    }
                }, 300);
            }
        });
    }

    // -------------------------------------------------------
    // 5. Nút Đăng xuất – gọi API logout và chuyển hướng
    // -------------------------------------------------------
    const logoutItems = document.querySelectorAll('.logout-item');
    logoutItems.forEach(item => {
        item.addEventListener('click', async (e) => {
            e.preventDefault();
            try {
                await fetch('/auth/logout', { method: 'POST' });
            } catch (error) {
                console.error("Lỗi đăng xuất", error);
            }
            window.location.href = '/auth';
        });
    });
});
