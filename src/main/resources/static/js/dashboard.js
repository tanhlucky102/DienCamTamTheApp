document.addEventListener('DOMContentLoaded', () => {

    // --- Sidebar Toggle Logic ---
    const sidebar = document.getElementById('right-sidebar');
    const toggleBtn = document.getElementById('toggle-sidebar');
    const closeBtn = document.getElementById('close-sidebar');
    const overlay = document.getElementById('sidebar-overlay');

    const openSidebar = () => {
        if (!sidebar) return;
        if(window.innerWidth <= 1024) {
            sidebar.classList.add('active');
            if (overlay) overlay.classList.add('active');
        } else {
            sidebar.scrollIntoView({ behavior: 'smooth' });
        }
    };

    const closeSidebar = () => {
        if (sidebar) sidebar.classList.remove('active');
        if (overlay) overlay.classList.remove('active');
    };

    if(toggleBtn) toggleBtn.addEventListener('click', openSidebar);
    if(closeBtn) closeBtn.addEventListener('click', closeSidebar);
    if(overlay) overlay.addEventListener('click', closeSidebar);

    // --- Account Modal Logic ---
    const accModal = document.getElementById('account-modal');
    const openAccBtn = document.getElementById('open-account-modal');
    const headerProfileBtn = document.getElementById('header-profile-btn');
    const closeAccBtn = document.getElementById('close-account-modal');
    const accForm = document.getElementById('account-form');

    const openModalFn = (e) => { e.preventDefault(); accModal.classList.remove('hidden'); };
    if (openAccBtn) openAccBtn.addEventListener('click', openModalFn);
    if (headerProfileBtn) headerProfileBtn.addEventListener('click', openModalFn);
    if (closeAccBtn) closeAccBtn.addEventListener('click', () => { accModal.classList.add('hidden'); });

    // Redundant scroll-to-top logic removed as it's now handled by global.js

    if (accForm) {
        accForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const btn = accForm.querySelector('button');
            const originalText = btn.innerText;
            btn.innerText = 'ĐANG LƯU...';
            setTimeout(() => {
                btn.innerText = originalText;
                accModal.classList.add('hidden');
                document.querySelector('.user-greeting').innerText = 'Xin chào, ' + document.getElementById('acc-name').value;
            }, 500);
        });
    }

    // Quản lý Lịch sử tra cứu
    let historyData = JSON.parse(localStorage.getItem('dctt_history')) || [];

    const renderHistory = () => {
        const historyList = document.getElementById('history-list');
        if (!historyList) return;
        historyList.innerHTML = '';

        // Hiển thị 5 mục tra cứu gần nhất
        const displayLimit = Math.min(historyData.length, 5);
        for (let i = historyData.length - 1; i >= historyData.length - displayLimit; i--) {
            const item = historyData[i];
            const newItem = document.createElement('div');
            newItem.className = 'history-item';
            newItem.dataset.index = i;
            newItem.innerHTML = `
                <p class="history-name">${item.fullname}</p>
                <p class="history-meta">${item.dateStr || 'Gần đây'}</p>
                <p class="history-category">Tra cứu: ${item.category}</p>
            `;
            newItem.addEventListener('click', function() {
                restoreResults(this.dataset.index);
            });
            historyList.appendChild(newItem);
        }
    };

    // Khởi tạo luồng dữ liệu mẫu nếu mảng rỗng
    if (historyData.length === 0) {
        const now = new Date().toLocaleDateString('vi-VN');
        historyData = [
            { fullname: 'Nguyễn Văn A', category: 'Tài lộc - tiền bạc', dob: '15/06/1990', content: generateDecodingContent('Tài lộc - tiền bạc'), dateStr: now },
            { fullname: 'Xuân Bắc Đào', category: 'Tình cảm - Hôn nhân', dob: '01/11/1999', content: generateDecodingContent('Tình cảm - Hôn nhân'), dateStr: now }
        ];
        localStorage.setItem('dctt_history', JSON.stringify(historyData));
    }

    renderHistory();

    // Xử lý điều hướng khi bấm từ tab Lịch Sử
    const urlParams = new URLSearchParams(window.location.search);
    const historyId = urlParams.get('history_id');
    if (historyId !== null && historyData[historyId]) {
        setTimeout(() => restoreResults(historyId), 500);
    }

    function restoreResults(index) {
        const data = historyData[index];
        if (!data) return;

        document.getElementById('display-name').innerText = data.fullname + " (Lịch sử)";
        document.getElementById('decoding-category').innerText = data.category;
        document.getElementById('decoding-content').innerHTML = data.content;

        const resultsSection = document.getElementById('results-section');
        const resultsOverlay = document.getElementById('results-overlay');
        
        resultsSection.classList.remove('hidden');
        if (resultsOverlay) resultsOverlay.classList.add('active');

        if(window.innerWidth <= 1024) closeSidebar();
    }

    // Modal Results logic
    const closeResultsBtn = document.getElementById('close-results-btn');
    const resultsOverlay = document.getElementById('results-overlay');
    
    const closeResults = () => {
        const resultsSection = document.getElementById('results-section');
        if (resultsSection) resultsSection.classList.add('hidden');
        if (resultsOverlay) resultsOverlay.classList.remove('active');
        
        // Revert magical animation when closing popup
        const wheelImg = document.querySelector('.wheel-img');
        const inputCards = document.querySelectorAll('.input-card');
        if(wheelImg) wheelImg.classList.remove('fast-spin');
        if(inputCards) inputCards.forEach(card => card.classList.remove('sucked-in'));
    };
    
    if (closeResultsBtn) closeResultsBtn.addEventListener('click', closeResults);
    if (resultsOverlay) resultsOverlay.addEventListener('click', closeResults);

    // Xử lý sự kiện tra cứu và gọi API Diễn Cầm
    const form = document.getElementById('horoscope-form');
    const resultsSection = document.getElementById('results-section');
    const wheelImgBtn = document.querySelector('.wheel-img');
    const inputCards = document.querySelectorAll('.input-card');
    const partnerCard = document.getElementById('partner-card');

    // Tự động hiện partner card nếu chọn Tình cảm hoặc Tất cả
    const lookupCategorySelect = document.getElementById('lookup-category');
    if (lookupCategorySelect) {
        lookupCategorySelect.addEventListener('change', (e) => {
            if (e.target.value === 'Tình cảm - Hôn nhân' || e.target.value === 'Tất cả') {
                partnerCard.classList.remove('disabled-card');
            } else {
                partnerCard.classList.add('disabled-card');
            }
        });
    }

    if (wheelImgBtn) {
        wheelImgBtn.addEventListener('click', () => {
            if (form && !form.checkValidity()) {
                form.reportValidity();
                return;
            }
            
            // Trigger Magical Animation
            wheelImgBtn.classList.add('fast-spin');
            if (inputCards) {
                inputCards.forEach(card => card.classList.add('sucked-in'));
            }

            // Wait for animation to finish before fetching
            setTimeout(() => {
                form.dispatchEvent(new Event('submit', { cancelable: true, bubbles: true }));
            }, 1200);
        });
    }

    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();

            // Get values
            const fullnameValue = document.getElementById('fullname').value;
            const categoryValue = document.getElementById('lookup-category').value;
            const dobValue = `${document.getElementById('birth-day').value}/${document.getElementById('birth-month').value}/${document.getElementById('birth-year').value}`;
            
            const requestPayload = {
                fullname: fullnameValue,
                gender: document.getElementById('gender').value,
                calendarType: 'solar', // Mặc định là Dương lịch như yêu cầu
                birthHour: document.getElementById('birth-hour').value,
                birthDay: document.getElementById('birth-day').value,
                birthMonth: document.getElementById('birth-month').value,
                birthYear: document.getElementById('birth-year').value,
                lookupCategory: categoryValue,
                partnerBirthDay: document.getElementById('partner-birth-day').value,
                partnerBirthMonth: document.getElementById('partner-birth-month').value,
                partnerBirthYear: document.getElementById('partner-birth-year').value
            };

            // Gắn tín hiệu gọi Backend
            fetch('/api/divination/process', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestPayload)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error("Lỗi khi kết nối đến Server Diễn Cầm");
                }
                return response.json();
            })
            .then(data => {
                // Đổ dữ liệu định danh lên bảng
                const displayName = data.fullname || fullnameValue;
                const displayDob = data.dob || dobValue;
                const displayCategory = data.category || categoryValue;

                document.getElementById('display-name').innerText = displayName;
                document.getElementById('decoding-category').innerText = displayCategory;

                const meta = document.getElementById('result-meta');
                if (meta) {
                    meta.innerHTML = `
                        <span class="chip">Họ tên: ${displayName}</span>
                        <span class="chip">Ngày sinh: ${displayDob}</span>
                        <span class="chip">Danh mục: ${displayCategory}</span>
                    `;
                }

                // Đổ nội dung luận giải
                const decodingText = data.content || generateDecodingContent(categoryValue);
                const decodingTarget = document.getElementById('decoding-content');
                decodingTarget.innerHTML = decodingText;

                resultsSection.classList.remove('hidden');
                const resultsOverlay = document.getElementById('results-overlay');
                if (resultsOverlay) resultsOverlay.classList.add('active');

                appendHistory(data.fullname || fullnameValue, data.category || categoryValue, data.dob || dobValue, decodingText);
            })
            .catch(error => {
                console.error("Lỗi:", error);
                alert("Đã xảy ra lỗi khi giải mã lá số. Vui lòng thử lại!");
                // Revert animation on error
                if(wheelImgBtn) wheelImgBtn.classList.remove('fast-spin');
                if(inputCards) inputCards.forEach(card => card.classList.remove('sucked-in'));
            });
        });
    }

    // Hàm dự phòng dữ liệu luận giải
    function generateDecodingContent(category) {
        let content = '';

        content += `<p><strong>Khái quát bản mệnh:</strong> Người mang bản mệnh này thường có tính cách vững chắc, chung thủy và có trách nhiệm cao. Có khả năng chịu đựng áp lực tốt, là chỗ dựa vững tâm cho người xung quanh.</p>`;

        switch (category) {
            case 'Tình cảm - Hôn nhân':
                content += `
                    <p><strong>Cung Phu Thê (Tình duyên):</strong> Có sao Đào Hoa chiếu mệnh, đường tình duyên khá phong phú nhưng dễ trải qua sóng gió thời tuổi trẻ. Sau năm 28 tuổi hôn nhân sẽ viên mãn.</p>
                    <p><strong>Lời khuyên:</strong> Nên học cách lắng nghe và cảm thông với đối phương nhiều hơn, tránh cái tôi quá lớn gây cãi vã không đáng có.</p>
                `;
                break;
            case 'Công danh - sự nghiệp':
                content += `
                    <p><strong>Cung Quan Lộc (Sự nghiệp):</strong> Có Thiên Phủ giáng đáo, công danh thuận lợi, dễ được cất nhắc lên các vị trí quản lý hoặc làm chủ doanh nghiệp. Thời kỳ phát đạt mạnh nhất là từ 32-45 tuổi.</p>
                    <p><strong>Lời khuyên:</strong> Tuyệt đối không nên dính dáng đến các vấn đề luật pháp, cần minh bạch trong sổ sách.</p>
                `;
                break;
            case 'Tài lộc - tiền bạc':
                content += `
                    <p><strong>Cung Tài Bạch (Tài chính):</strong> Tài lộc dồi dào, tiền bạc kiếm được dễ dàng nhờ các công việc phụ. Tuy nhiên, dễ bị hao tài tán lộc vì cung Điền Trạch chưa thực sự vững vàng.</p>
                    <p><strong>Lời khuyên:</strong> Nên có kế hoạch tiết kiệm dài hạn và mua bất động sản làm tài sản tích luỹ thay vì giữ tiền mặt.</p>
                `;
                break;
            case 'Sức khỏe - thọ mệnh':
                content += `
                    <p><strong>Cung Tật Ách (Sức khỏe):</strong> Cần chú ý các bệnh liên quan đến đường tiêu hóa, dạ dày và hô hấp, đặc biệt khi thời tiết chuyển mùa.</p>
                    <p><strong>Lời khuyên:</strong> Sinh hoạt điều độ, tránh thức khuya làm việc quá sức. Nên tập thiền hoặc yoga.</p>
                `;
                break;
            case 'Gia đình - quan hệ':
                content += `
                    <p><strong>Cung Huynh Đệ & Phụ Mẫu (Gia đạo):</strong> Gia đình hòa thuận, anh chị em yêu thương giúp đỡ lẫn nhau. Giai đoạn trung vận, bạn có thể phải gánh vác việc họ hàng nhiều.</p>
                `;
                break;
            case 'Vận hạn':
                content += `
                    <p><strong>Tiểu hạn & Đại hạn:</strong> Đại hạn 10 năm tới có sao Thái Bạch và La Hầu chiếu mệnh luân phiên. Năm nay chủ yếu tránh xuất hành đường thủy, đề phòng tiểu nhân hãm hại sau lưng.</p>
                `;
                break;
            case 'Bản thân - tính cách - số mệnh':
                content += `
                    <p><strong>Cung Mệnh & Thân:</strong> Tiền vận gian truân vất vả lập nghiệp, trung vận và hậu vận được hưởng phúc lộc rạng rỡ. Bạn thông minh, nhanh nhẹn nhưng đôi khi lại vội vàng.</p>
                `;
                break;
            case 'Tất cả':
            default:
                content += `
                    <p><strong>Công danh:</strong> Có Thiên Phủ giáng đáo, công danh thuận lợi, phát đạt từ trung vận.</p>
                    <p><strong>Tài Bạc:</strong> Tiền bạc sinh sôi nảy nở, dễ thành danh trong kinh doanh.</p>
                    <p><strong>Tình Duyên:</strong> Đào hoa chiếu mệnh, hôn nhân ấm êm viên mãn bề lâu sau 28 tuổi.</p>
                    <p><strong>Sức khỏe:</strong> Cẩn thận bệnh tiêu hóa. Có sao Thiên Lương giải ách nên mọi điều bình an.</p>
                `;
                break;
        }

        return content;
    }

    // Ghi History
    function appendHistory(name, category, dob, content) {
        const now = new Date();
        const dateStr = now.toLocaleDateString('vi-VN') + " - Vừa tra cứu";

        historyData.push({ fullname: name, category, dob, content, dateStr });
        localStorage.setItem('dctt_history', JSON.stringify(historyData));

        renderHistory();
    }
});
