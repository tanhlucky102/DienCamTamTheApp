document.addEventListener('DOMContentLoaded', () => {
    // Basic sidebar toggle logic for history page
    const sidebar = document.getElementById('right-sidebar');
    const toggleBtn = document.getElementById('toggle-sidebar');
    const closeBtn = document.getElementById('close-sidebar');
    const overlay = document.getElementById('sidebar-overlay');

    const openSidebar = () => {
        if(window.innerWidth <= 1024) { sidebar.classList.add('active'); overlay.classList.add('active'); }
        else { sidebar.scrollIntoView({ behavior: 'smooth' }); }
    };
    const closeSidebar = () => { sidebar.classList.remove('active'); overlay.classList.remove('active'); };

    if(toggleBtn) toggleBtn.addEventListener('click', openSidebar);
    if(closeBtn) closeBtn.addEventListener('click', closeSidebar);
    if(overlay) overlay.addEventListener('click', closeSidebar);

    // Sidebar history rendering
    const historyData = JSON.parse(localStorage.getItem('dctt_history')) || [];
    const sidebarHistoryList = document.getElementById('history-list');
    if (sidebarHistoryList) {
        sidebarHistoryList.innerHTML = '';
        const limit = Math.min(historyData.length, 5);
        for (let i = historyData.length - 1; i >= historyData.length - limit; i--) {
            const item = historyData[i];
            const newItem = document.createElement('div');
            newItem.className = 'history-item';
            newItem.innerHTML = `
                <p class="history-name">${item.fullname}</p>
                <p class="history-meta">${item.dateStr || 'Gần đây'}</p>
                <p class="history-category">Tra cứu: ${item.category}</p>
            `;
            newItem.addEventListener('click', () => {
                window.location.href = `/dashboard?history_id=${i}`;
            });
            sidebarHistoryList.appendChild(newItem);
        }
    }

    // MAIN History Grid Logic
    const grid = document.getElementById('full-history-grid');
    const searchInput = document.getElementById('history-search');

    const renderGrid = (filterTxt = '') => {
        if (!grid) return;
        grid.innerHTML = '';
        
        const normalizeString = (str) => {
            if (!str) return '';
            return str.normalize('NFD')
                      .replace(/[\u0300-\u036f]/g, '')
                      .replace(/đ/g, 'd').replace(/Đ/g, 'D')
                      .toLowerCase();
        };
        const lowerFilter = normalizeString(filterTxt);

        let matchCount = 0;
        // Reverse to show newest first
        for (let i = historyData.length - 1; i >= 0; i--) {
            const item = historyData[i];
            const dateS = item.dateStr || '';
            const catS = item.category || '';
            const nameS = item.fullname || '';

            // Search match: ignore accents
            if (filterTxt) {
                const normName = normalizeString(nameS);
                const normCat = normalizeString(catS);
                const normDate = normalizeString(dateS);
                
                if (!normName.includes(lowerFilter) && !normCat.includes(lowerFilter) && !normDate.includes(lowerFilter)) {
                    continue;
                }
            }

            matchCount++;
            const card = document.createElement('div');
            card.className = 'history-item glass-panel';
            card.style.cursor = 'pointer';
            card.style.transition = 'all 0.3s ease';
            card.style.padding = '1.5rem';

            card.innerHTML = `
                <h3 style="color: var(--color-primary); margin-bottom: 0.5rem; font-family: var(--font-heading);">${nameS}</h3>
                <p style="color: var(--color-text-muted); font-size: 0.85rem; margin-bottom: 1rem;">DOB: ${item.dob} | ${dateS.split(' - ')[0]}</p>
                <span style="display: inline-block; padding: 0.3rem 0.8rem; background: rgba(212,175,55,0.1); border: 1px solid var(--color-primary); border-radius: 20px; font-size: 0.75rem; color: var(--color-primary);">Tra cứu: ${catS}</span>
            `;

            // Hover effects
            card.addEventListener('mouseenter', () => card.style.transform = 'translateY(-5px)');
            card.addEventListener('mouseleave', () => card.style.transform = 'translateY(0)');

            card.addEventListener('click', () => {
                window.location.href = '/dashboard?history_id=' + i;
            });

            grid.appendChild(card);
        }

        if (matchCount === 0) {
            grid.innerHTML = '<p class="text-muted" style="text-align:center; grid-column: 1/-1; padding: 3rem; font-size:1.1rem;">Không tìm thấy lịch sử nào phù hợp với bộ lọc: "'+filterTxt+'"</p>';
        }
    };

    renderGrid();

    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            renderGrid(e.target.value);
        });
    }
});
