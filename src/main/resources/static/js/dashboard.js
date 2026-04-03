document.addEventListener('DOMContentLoaded', () => {

    // --- Sidebar Toggle Logic ---
    const sidebar = document.getElementById('right-sidebar');
    const toggleBtn = document.getElementById('toggle-sidebar');
    const closeBtn = document.getElementById('close-sidebar');
    const overlay = document.getElementById('sidebar-overlay');

    // ================== WHEEL ANIMATION LOGIC ================== //
    let currentWheelRotation = 0;
    let currentSpinSpeed = 0.05; // Tốc độ rà idle mặc định
    let targetSpinSpeed = 0.05;
    let isWheelSpinning = true;

    function renderWheelFrame() {
        if (Math.abs(targetSpinSpeed) > 0.005 || Math.abs(currentSpinSpeed) > 0.005) {
            currentSpinSpeed += (targetSpinSpeed - currentSpinSpeed) * 0.03; // Easing (Gia tốc trớn)
            currentWheelRotation += currentSpinSpeed;

            const wheelImgBtn = document.querySelector('.wheel-img');
            if (wheelImgBtn) {
                const scale = Math.abs(currentSpinSpeed) > 2 ? 1.05 : 1;
                wheelImgBtn.style.transform = `scale(${scale}) rotate(${currentWheelRotation}deg)`;

                // Nếu quay cực nhanh, thêm hiệu ứng sáng chói rực
                if (Math.abs(currentSpinSpeed) > 5) {
                    wheelImgBtn.style.filter = 'drop-shadow(0 0 60px rgba(220, 185, 110, 0.9))';
                } else if (!wheelImgBtn.matches(':hover')) {
                    wheelImgBtn.style.filter = 'drop-shadow(0 25px 0px rgba(220, 185, 110, 0.25))';
                }
            }
            requestAnimationFrame(renderWheelFrame);
        } else {
            currentSpinSpeed = 0;
            isWheelSpinning = false;
        }
    }

    function setSpinSpeed(speed) {
        targetSpinSpeed = speed;
        if (!isWheelSpinning) {
            isWheelSpinning = true;
            renderWheelFrame();
        }
    }

    // Khởi chạy vòng lặp vô tận trạng thái chờ
    renderWheelFrame();

    const openSidebar = () => {
        if (!sidebar) return;
        if (window.innerWidth <= 1024) {
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

    if (toggleBtn) toggleBtn.addEventListener('click', openSidebar);
    if (closeBtn) closeBtn.addEventListener('click', closeSidebar);
    if (overlay) overlay.addEventListener('click', closeSidebar);

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
            newItem.addEventListener('click', function () {
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

        // Bỏ việc bật Modal cũ:
        // Đảo qua mở Flashcards
        showFlashcards(data.content, data.fullname, data.category);

        if (window.innerWidth <= 1024) closeSidebar();
    }

    // Modal Results logic
    const closeResultsBtn = document.getElementById('close-results-btn');
    const resultsOverlay = document.getElementById('results-overlay');

    const closeResults = () => {
        const resultsSection = document.getElementById('results-section');
        if (resultsSection) resultsSection.classList.add('hidden');
        if (resultsOverlay) resultsOverlay.classList.remove('active');

        // Revert magical animation when closing popup
        setSpinSpeed(0.05); // Trở về trạng thái idle tra cứu
        const inputCards = document.querySelectorAll('.input-card');
        if (inputCards) inputCards.forEach(card => card.classList.remove('sucked-in'));
    };

    if (closeResultsBtn) closeResultsBtn.addEventListener('click', closeResults);
    if (resultsOverlay) resultsOverlay.addEventListener('click', closeResults);

    // Xử lý sự kiện tra cứu và gọi API Diễn Cầm
    const form = document.getElementById('horoscope-form');
    const resultsSection = document.getElementById('results-section');
    const wheelImgBtn = document.querySelector('.wheel-img');
    const inputCards = document.querySelectorAll('.input-card');

    if (wheelImgBtn) {
        wheelImgBtn.addEventListener('click', () => {
            // Chặn tính năng hút thẻ nếu đang có thẻ nào đó được soi chiếu (viewing)
            // Lệnh return này kết hợp với document click listener sẽ khiến lá bài đơn giản chỉ là đáp xuống bàn.
            if (activeViewingCard) {
                return;
            }

            // Chặn tính năng tương tác bát quái khi đang xem kết quả
            // Lúc này bài đã rải ra, bắt buộc user phải bấm nút "Dọn bài" để thu hồi
            if (generatedCards.length > 0) {
                return;
            }

            if (form && !form.checkValidity()) {
                form.reportValidity();
                return;
            }

            // Trigger Magical Animation (Gia tốc vút lên mượt mà vô cực)
            setSpinSpeed(12); // Tốc độ xoay nhanh cỡ vừa phải (khoảng 700 độ/s) thay vì 18 như trước
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
                calendarType: document.getElementById('calendar-type').value,
                birthHour: document.getElementById('birth-hour').value,
                birthDay: document.getElementById('birth-day').value,
                birthMonth: document.getElementById('birth-month').value,
                birthYear: document.getElementById('birth-year').value,
                lookupCategory: categoryValue
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

                    // Hiển thị Flashcards thay vì Modal
                    showFlashcards(decodingText, displayName, displayCategory);

                    appendHistory(data.fullname || fullnameValue, data.category || categoryValue, data.dob || dobValue, decodingText);
                })
                .catch(error => {
                    console.error("Lỗi:", error);
                    alert("Đã xảy ra lỗi khi giải mã lá số. Vui lòng thử lại!");
                    // Revert animation on error
                    setSpinSpeed(0.05);
                    const inputCards = document.querySelectorAll('.input-card');
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

    // ================== FLASHCARDS LOGIC ================== //
    let highestZ = 300;
    const flashcardsContainer = document.getElementById('flashcards-container');
    const arrangeBtn = document.getElementById('arrange-cards-btn');
    const clearBtn = document.getElementById('clear-cards-btn');
    let generatedCards = [];
    let activeViewingCard = null;

    // Handle clicking outside the deeply viewed card
    document.addEventListener('click', (e) => {
        if (activeViewingCard) {
            // Check if the click is outside the active viewing card
            if (!activeViewingCard.contains(e.target) && e.target !== arrangeBtn && e.target !== clearBtn) {
                closeViewingCard(activeViewingCard);
            }
        }
    });

    function closeViewingCard(card) {
        if (!card) return;
        card.dataset.viewState = card.dataset.prevState; // 'idle' or 'stacked'
        card.style.transition = 'all 0.6s cubic-bezier(0.25, 1, 0.5, 1)';

        // Return to original coords and transform
        card.style.left = card.dataset.origLeft;
        card.style.top = card.dataset.origTop;
        card.style.transform = card.dataset.origTransform;
        card.style.width = card.dataset.origWidth;
        card.style.height = card.dataset.origHeight;

        // Trả Z-index về nguyên trạng ngay lập tức để ngắt dứt điểm mọi race condition chèn ép lớp ảo
        if (card.dataset.stableZIndex) {
            card.style.zIndex = card.dataset.stableZIndex;
        } else {
            card.style.zIndex = card.dataset.origZIndex;
        }

        // Khóa click trong lúc bay
        card.dataset.isAnimating = "true";
        setTimeout(() => {
            card.dataset.isAnimating = "false";
        }, 500);

        // Keep it face up on "mặt detail" (front)
        const flashcardInner = card.querySelector('.flashcard');
        if (flashcardInner) flashcardInner.classList.remove('is-flipped');

        card.dataset.viewed = "true";

        if (activeViewingCard === card) {
            activeViewingCard = null;
        }
    }

    function showFlashcards(contentHtml, nameStr, categoryStr) {
        clearAllCards();
        arrangeBtn.style.display = 'block';
        clearBtn.style.display = 'block';

        const segments = extractSegments(contentHtml);

        // Dữ liệu đã tải xong. Bát quái giảm tốc rà rà để nhả thẻ (tạo cảm giác lực ly tâm đẩy thẻ ra)
        setSpinSpeed(3);

        const vpW = window.innerWidth;
        const vpH = window.innerHeight;
        const wheelRadius = 380;
        let w = 130, h = 200; // Thẻ nhỏ hơn nhiều

        segments.forEach((seg, index) => {
            const cardWrapper = document.createElement('div');
            cardWrapper.className = 'flashcard-wrapper';
            cardWrapper.dataset.viewState = 'idle';

            // Tìm tâm thật sự của vòng bát quái trên màn hình
            let wheelCenterX = vpW / 2;
            let wheelCenterY = vpH / 2;
            const wheelImgBtn = document.querySelector('.wheel-img');
            if (wheelImgBtn && wheelImgBtn.getBoundingClientRect) {
                const rect = wheelImgBtn.getBoundingClientRect();
                wheelCenterX = rect.left + rect.width / 2;
                wheelCenterY = rect.top + rect.height / 2;
            }

            // Rơi tự do khắp màn hình nhưng chừa vùng bát quái ra
            let targetLeft, targetTop;
            let overlapsCenter = true;
            let attempts = 0;
            const visualWheelRadius = 400; // Khoảng cách an toàn cách bánh xe

            while (overlapsCenter && attempts < 150) {
                targetLeft = 20 + Math.random() * (vpW - w - 40);
                targetTop = 90 + Math.random() * (vpH - h - 110);

                // Toán học: Tính điểm trên lá bài gần tâm bát quái nhất
                let closestX = Math.max(targetLeft, Math.min(wheelCenterX, targetLeft + w));
                let closestY = Math.max(targetTop, Math.min(wheelCenterY, targetTop + h));
                let distToWheelCenter = Math.sqrt(Math.pow(closestX - wheelCenterX, 2) + Math.pow(closestY - wheelCenterY, 2));

                if (distToWheelCenter >= visualWheelRadius) {
                    overlapsCenter = false;
                }
                attempts++;
            }

            if (overlapsCenter) {
                targetLeft = Math.random() > 0.5 ? 10 : (vpW - w - 10);
            }

            const randomRot = (Math.random() - 0.5) * 60; // -30 to 30 deg

            // Ban đầu thẻ nằm thu lại thành 1 chấm ở tâm bánh xe Bát Quái
            cardWrapper.style.width = w + 'px';
            cardWrapper.style.height = h + 'px';
            cardWrapper.style.left = (wheelCenterX - w / 2) + 'px';
            cardWrapper.style.top = (wheelCenterY - h / 2) + 'px';
            cardWrapper.style.zIndex = highestZ++;
            cardWrapper.style.opacity = '0';
            cardWrapper.style.transform = `scale(0.1) rotate(500deg)`;
            cardWrapper.style.transition = 'all 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

            const match = seg.title.match(/^(\d+)[.\s]/);
            const cornerNum = match ? match[1] : (index + 1);

            // Dataset attribute to easily sort without relying on DOM elements that might be modified
            cardWrapper.dataset.cardNumber = cornerNum;

            // Ban đầu thẻ ngửa nhưng ta set 'is-flipped' để nó úp lại (hiện mặt Brown)
            cardWrapper.innerHTML = `
                <div class="flashcard is-flipped">
                    <!-- MẶT NGỬA (Mặt Detail - 0deg internal) -->
                    <div class="flashcard-face flashcard-back" style="transform: rotateY(0deg) !important; padding: 5cqw; display: flex; flex-direction: column; align-items: center; justify-content: center; overflow: hidden;">
                        <!-- Số đỏ đô phía bên trái như yêu cầu -->
                        <div style="position: absolute; top: 10px; left: 15px; color: #8c1010; font-weight: 900; font-size: 15cqw; font-family: 'Times New Roman', serif;">${cornerNum}</div>
                        
                        <h3 style="color: #8c1010; font-size: 10cqw; border-bottom: 2px solid #8c1010; padding-bottom: 2cqw; text-align: center; margin-bottom: 5cqw; font-family: 'Times New Roman', serif; width: 80%;">QUẺ SỐ ${cornerNum}</h3>
                        <p style="text-align: center; font-size: 8cqw; color: #5c3a21; font-weight: bold; width: 100%; white-space: normal;">${seg.title}</p>
                        <p style="text-align: center; font-size: 6cqw; color: #885c3b; margin-top: 8cqw; font-style: italic;">(Bấm để xem lí giải)</p>
                    </div>

                    <!-- MẶT ÚP (Mặt Brown Cover - 180deg internal ban đầu) -->
                    <div class="flashcard-face flashcard-front cover-face" style="transform: rotateY(180deg) !important;">
                        <div class="corner-number">${cornerNum}</div>
                        <div class="que-title">Quẻ số</div>
                        <div class="que-number">${cornerNum}</div>
                        <!-- Ẩn subtitle mặt này để trông giống mặt úp -->
                        <div class="que-subtitle" style="display:none;">${seg.title}</div>
                    </div>
                    
                    <!-- LƯU TRỮ LÍ GIẢI (Ẩn đi) -->
                    <div class="ligiai-data" style="display:none;">
                        ${seg.html}
                    </div>
                </div>
            `;

            flashcardsContainer.appendChild(cardWrapper);
            generatedCards.push(cardWrapper);

            // Bắn thẻ văng ra từ tâm bát quái đến vị trí đích random
            const flyDelay = index * 200 + 300;
            setTimeout(() => {
                cardWrapper.style.opacity = '1';
                cardWrapper.style.transform = `scale(1) rotateZ(${randomRot}deg)`;
                cardWrapper.style.left = targetLeft + 'px';
                cardWrapper.style.top = targetTop + 'px';
            }, flyDelay);

            const flashcardInner = cardWrapper.querySelector('.flashcard');

            // 3-Click Interaction workflow
            cardWrapper.addEventListener('click', (e) => {
                if (cardWrapper.dataset.isDragging === "true") return;
                // Nếu thẻ đang trong quá trình bay lượn (zoom ra hoặc thu về), cấm click để chống hỏng Z-index gốc
                if (cardWrapper.dataset.isAnimating === "true") return;

                const currentState = cardWrapper.dataset.viewState || 'idle';

                if (currentState === 'idle' || currentState === 'stacked') {
                    // Khóa click
                    cardWrapper.dataset.isAnimating = "true";
                    setTimeout(() => cardWrapper.dataset.isAnimating = "false", 600);

                    // Start deepest viewing flow
                    if (activeViewingCard && activeViewingCard !== cardWrapper) {
                        closeViewingCard(activeViewingCard);
                    }

                    // Start viewing - straight to center
                    cardWrapper.dataset.prevState = currentState;
                    cardWrapper.dataset.origLeft = cardWrapper.style.left;
                    cardWrapper.dataset.origTop = cardWrapper.style.top;
                    cardWrapper.dataset.origTransform = cardWrapper.style.transform;
                    cardWrapper.dataset.origWidth = cardWrapper.style.width;
                    cardWrapper.dataset.origHeight = cardWrapper.style.height;
                    cardWrapper.dataset.origZIndex = cardWrapper.style.zIndex;

                    cardWrapper.dataset.viewState = 'viewing';
                    cardWrapper.style.transition = 'all 0.6s cubic-bezier(0.25, 1, 0.5, 1)';
                    cardWrapper.style.transform = 'rotateZ(0deg) scale(1)';

                    cardWrapper.style.width = '300px';
                    cardWrapper.style.height = '465px';

                    // Highest z-index for viewing
                    highestZ += 10;
                    cardWrapper.style.zIndex = highestZ;

                    // Straight to center
                    cardWrapper.style.left = (window.innerWidth / 2 - 150) + 'px';
                    cardWrapper.style.top = (window.innerHeight / 2 - 232.5) + 'px';

                    // Thực hiện lật ra Mặt Detail (Ngửa)
                    flashcardInner.classList.remove('is-flipped');

                    activeViewingCard = cardWrapper;

                    // Phép thuật: Sau khi hoàn thành lật mặt detail, biến mặt Úp cũ (sau lưng) thành mặt Lí Giải
                    setTimeout(() => {
                        if (cardWrapper.dataset.viewedCoverRemoved !== "true") {
                            cardWrapper.dataset.viewedCoverRemoved = "true";
                            const backFace = flashcardInner.querySelector('.cover-face');
                            const ligiaiData = flashcardInner.querySelector('.ligiai-data');
                            if (backFace && ligiaiData) {
                                // Đổi class từ mặt Brown thành mặt Yellow
                                backFace.className = 'flashcard-face flashcard-back';
                                backFace.style.transform = 'rotateY(180deg) !important';
                                // Chèn nội dung lí giải
                                backFace.innerHTML = ligiaiData.innerHTML;
                            }
                        }
                    }, 400);

                } else if (currentState === 'viewing') {
                    // Click while in center toggles faces (Detail <-> Lí giải)
                    // Cả 2 face bây giờ đều là Yellow (Detail và Lí giải)
                    if (!flashcardInner.classList.contains('is-flipped')) {
                        flashcardInner.classList.add('is-flipped');
                    } else {
                        flashcardInner.classList.remove('is-flipped');
                    }
                }
            });

            makeDraggable(cardWrapper);
        });

        // Lên lịch để bánh xe dừng hẳn sau khi tất cả thẻ đã bay ra và an tọa
        const totalDuration = segments.length * 200 + 1000;
        setTimeout(() => {
            if (generatedCards.length > 0) {
                setSpinSpeed(0);
            }
        }, totalDuration);
    }

    function clearAllCards() {
        flashcardsContainer.innerHTML = '';
        generatedCards = [];
        arrangeBtn.style.display = 'none';
        clearBtn.style.display = 'none';
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            const wheelImgBtn = document.querySelector('.wheel-img');
            const vpW = window.innerWidth;
            const vpH = window.innerHeight;
            let wheelCenterX = vpW / 2;
            let wheelCenterY = vpH / 2;

            if (wheelImgBtn) {
                if (wheelImgBtn.getBoundingClientRect) {
                    const rect = wheelImgBtn.getBoundingClientRect();
                    wheelCenterX = rect.left + rect.width / 2;
                    wheelCenterY = rect.top + rect.height / 2;
                }

                if (generatedCards.length > 0) {
                    setSpinSpeed(-12); // Xoay ngược vô cực với gia tốc mượt
                }
            }

            // Tính toán sắp xếp theo vị trí hiển thị để hút dần từ trái -> phải (lốc xoáy)
            const sortedByX = [...generatedCards].sort((a, b) => {
                const rectA = a.getBoundingClientRect();
                const rectB = b.getBoundingClientRect();
                return rectA.left - rectB.left;
            });

            sortedByX.forEach((cardWrapper, idx) => {
                const delay = Math.floor(idx / 3) * 100; // Nhóm 3 lá bay cùng 1 nhịp
                setTimeout(() => {
                    cardWrapper.style.transition = 'all 1s cubic-bezier(0.175, 0.885, 0.32, 1.275)';
                    cardWrapper.style.pointerEvents = 'none';
                    cardWrapper.style.transform = 'scale(0.01) rotate(720deg)';
                    cardWrapper.style.opacity = '0';
                    cardWrapper.style.left = (wheelCenterX - parseInt(cardWrapper.style.width) / 2) + 'px';
                    cardWrapper.style.top = (wheelCenterY - parseInt(cardWrapper.style.height) / 2) + 'px';
                }, delay);
            });

            arrangeBtn.style.display = 'none';
            clearBtn.style.display = 'none';

            const totalClearDuration = (Math.floor(generatedCards.length / 3) * 100) + 1200;
            setTimeout(() => {
                clearAllCards();
                setSpinSpeed(0.05); // Dịch chuyển lá bài xong thì NGỪNG XOAY NHANH, vè Idle của Tra Cứu
                const inputCards = document.querySelectorAll('.input-card');
                if (inputCards) inputCards.forEach(card => card.classList.remove('sucked-in'));
            }, totalClearDuration);
        });
    }

    if (arrangeBtn) {
        arrangeBtn.addEventListener('click', () => {
            const vpW = window.innerWidth;
            const vpH = window.innerHeight;
            const safeWheelRadius = 400; // Cách tâm 400px cực kì an toàn

            const CARDS_PER_COL = 10;

            // Sort logic: dựa vào properties trên wrapper data-attribute chống lỗi khi thẻ bị đổi ruột
            generatedCards.sort((a, b) => {
                const numA = parseInt(a.dataset.cardNumber) || 0;
                const numB = parseInt(b.dataset.cardNumber) || 0;
                return numA - numB;
            });

            if (activeViewingCard) {
                closeViewingCard(activeViewingCard);
            }

            // Mở nhẹ delay để đợi thẻ viewing về chỗ (nếu có)
            setTimeout(() => {
                // Sau khi sort, ta gán lại z-index để thẻ nào xếp trước thì nằm dưới
                highestZ += generatedCards.length;
                let baseZ = highestZ;

                generatedCards.forEach((card, idx) => {
                    const flashcard = card.querySelector('.flashcard');
                    // Không ghi đè inline transform của flashcard nếu không sẽ làm mất css rotateY của is-flipped

                    card.dataset.viewState = 'stacked';
                    card.style.transition = 'all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1)';

                    // Cực kỳ quan trọng: hủy độ xoay tự do của Wrapper từ lúc rớt bài!
                    card.style.transform = 'scale(1) rotateZ(0deg)';

                    const w = parseInt(card.style.width) || 130;
                    const h = parseInt(card.style.height) || 200;

                    const columnIdx = Math.floor(idx / CARDS_PER_COL);
                    const stackPos = idx % CARDS_PER_COL;

                    const isLeft = (columnIdx % 2 === 0);
                    const stackGroup = Math.floor(columnIdx / 2);

                    let targetLeft, targetTop;

                    const stackOffsetX = w + 30; // 2 cột cạnh nhau cách nhau 30px
                    const staggerY = 35; // Lùi thẳng tuột dọc xuống 35px

                    const cardsInThisCol = Math.min(generatedCards.length - columnIdx * CARDS_PER_COL, CARDS_PER_COL);
                    const totalStackHeight = h + (cardsInThisCol - 1) * staggerY;

                    if (isLeft) {
                        targetLeft = (vpW / 2) - safeWheelRadius - w - (stackGroup * stackOffsetX);
                        targetTop = (vpH / 2) - (totalStackHeight / 2) + (stackPos * staggerY);
                    } else {
                        targetLeft = (vpW / 2) + safeWheelRadius + (stackGroup * stackOffsetX);
                        targetTop = (vpH / 2) - (totalStackHeight / 2) + (stackPos * staggerY);
                    }

                    card.style.left = targetLeft + 'px';
                    card.style.top = targetTop + 'px';

                    // Xếp xuôi: Thẻ bốc sau (idx lớn hơn) đè thẻ trước
                    const targetZ = baseZ + idx;
                    card.style.zIndex = targetZ;

                    // Chống race-condition kịch khung bằng một hằng số vĩnh viễn cho bộ bài này
                    card.dataset.stableZIndex = targetZ;
                    card.dataset.origZIndex = targetZ;

                    setTimeout(() => { card.style.transition = 'width 0.3s, height 0.3s, top 0.4s, left 0.4s, transform 0.4s'; }, 800);
                });
            }, 150);
        });
    }

    function extractSegments(contentHtml) {
        const tempDiv = document.createElement('div');
        tempDiv.innerHTML = contentHtml;
        const segments = [];

        // Dữ liệu từ API thường bọc trong thẻ div đầu tiên
        const wrapper = tempDiv.querySelector('div') || tempDiv;
        const nodes = Array.from(wrapper.childNodes);

        let currentSegmentHtml = '';
        let currentTitle = '';

        nodes.forEach(node => {
            if (node.nodeType === Node.ELEMENT_NODE) {
                if (node.tagName === 'H4') {
                    if (currentSegmentHtml.trim() || currentTitle) {
                        segments.push({ title: currentTitle || 'THÔNG TIN BẢN MỆNH', html: currentSegmentHtml });
                        currentSegmentHtml = '';
                    }
                    currentTitle = node.innerText.replace(/Sở\s+/i, '').trim();
                } else if (node.tagName === 'HR') {
                    // Dọn dẹp dấu gạch ngang
                } else {
                    currentSegmentHtml += node.outerHTML;
                }
            } else if (node.nodeType === Node.TEXT_NODE) {
                if (node.textContent.trim()) {
                    currentSegmentHtml += '<p>' + node.textContent + '</p>';
                }
            }
        });

        if (currentSegmentHtml.trim() || currentTitle) {
            segments.push({ title: currentTitle || 'THÔNG TIN BẢN MỆNH', html: currentSegmentHtml });
        }

        if (segments.length === 0 && contentHtml.trim().length > 0) {
            segments.push({ title: 'TỔNG LUẬN', html: '<p>' + contentHtml + '</p>' });
        }

        return segments.filter(seg => seg.html.replace(/<[^>]*>?/gm, '').trim().length > 0);
    }

    function makeDraggable(el) {
        let isDragging = false;
        let startX, startY, initialLeft, initialTop;

        const mousedown = (e) => {
            isDragging = false;
            el.dataset.isDragging = "false";
            startX = e.clientX || (e.touches && e.touches[0].clientX);
            startY = e.clientY || (e.touches && e.touches[0].clientY);
            initialLeft = parseFloat(el.style.left || 0);
            initialTop = parseFloat(el.style.top || 0);
            el.style.zIndex = highestZ++;

            document.addEventListener('mousemove', mousemove);
            document.addEventListener('mouseup', mouseup);
            // Touch
            document.addEventListener('touchmove', mousemove, { passive: false });
            document.addEventListener('touchend', mouseup);
        };

        const mousemove = (ev) => {
            const cx = ev.clientX || (ev.touches && ev.touches[0].clientX);
            const cy = ev.clientY || (ev.touches && ev.touches[0].clientY);

            const dx = cx - startX;
            const dy = cy - startY;
            if (Math.abs(dx) > 3 || Math.abs(dy) > 3) {
                isDragging = true;
                el.dataset.isDragging = "true";
            }
            if (isDragging) {
                if (ev.cancelable) ev.preventDefault(); // prevent scroll
                el.style.left = (initialLeft + dx) + 'px';
                el.style.top = (initialTop + dy) + 'px';
            }
        };
        const mouseup = () => {
            document.removeEventListener('mousemove', mousemove);
            document.removeEventListener('mouseup', mouseup);
            document.removeEventListener('touchmove', mousemove);
            document.removeEventListener('touchend', mouseup);

            // Hủy isDragging delay nhẹ để sự kiện click không bị trig
            setTimeout(() => { if (isDragging) el.dataset.isDragging = "false"; isDragging = false; }, 50);
        };

        el.addEventListener('mousedown', mousedown);
        el.addEventListener('touchstart', mousedown, { passive: false });
    }

});
