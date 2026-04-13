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
            const centerBtnWrapper = document.getElementById('center-lookup-wrapper');
            if (wheelImgBtn) {
                const scale = Math.abs(currentSpinSpeed) > 2 ? 1.05 : 1;
                wheelImgBtn.style.transform = `scale(${scale}) rotate(${currentWheelRotation}deg)`;
                if (centerBtnWrapper) {
                    centerBtnWrapper.style.transform = `scale(${scale}) rotate(${currentWheelRotation}deg)`;
                }

                // Nếu quay cực nhanh, thêm hiệu ứng sáng chói rực
                if (Math.abs(currentSpinSpeed) > 5) {
                    wheelImgBtn.style.filter = 'drop-shadow(0 0 60px rgba(220, 185, 110, 0.9))';
                    if (centerBtnWrapper) centerBtnWrapper.style.filter = 'drop-shadow(0 0 60px rgba(255, 215, 0, 1))';
                } else if (!wheelImgBtn.matches(':hover')) {
                    wheelImgBtn.style.filter = 'drop-shadow(0 25px 0px rgba(220, 185, 110, 0.25))';
                    if (centerBtnWrapper) centerBtnWrapper.style.filter = 'none';
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

        // Trigger full divination animation — giống bấm Bát Quái
        setSpinSpeed(12);
        const inputCardsAll = document.querySelectorAll('.input-card');
        if (inputCardsAll) inputCardsAll.forEach(card => card.classList.add('sucked-in'));

        // Đợi animation hoàn thành rồi mới tung thẻ
        setTimeout(() => {
            // Cập nhật thông tin vào modal Xem Tất Cả để khi người dùng nhấn nút sẽ có nội dung
            const displayName = data.fullname;
            const displayDob = data.dob;
            const displayCategory = data.category;
            const cleanDecodingText = data.content;

            const nameEl = document.getElementById('display-name');
            if (nameEl) nameEl.innerText = displayName;

            const catEl = document.getElementById('decoding-category');
            if (catEl) catEl.innerText = displayCategory;

            const meta = document.getElementById('result-meta');
            if (meta) {
                meta.innerHTML = `
                    <span class="chip">Họ tên: ${displayName}</span>
                    <span class="chip">Ngày sinh: ${displayDob}</span>
                    <span class="chip">Danh mục: ${displayCategory}</span>
                `;
            }

            const decContent = document.getElementById('decoding-content');
            if (decContent) decContent.innerHTML = cleanDecodingText;

            // Bung thẻ
            showFlashcards(cleanDecodingText, displayName, displayCategory);
        }, 1200);

        if (window.innerWidth <= 1024) closeSidebar();
    }

    // Modal Results logic
    const closeResultsBtn = document.getElementById('close-results-btn');
    const resultsOverlay = document.getElementById('results-overlay');

    const closeResults = () => {
        const resultsSection = document.getElementById('results-section');
        if (resultsSection) resultsSection.classList.add('hidden');
        if (resultsOverlay) resultsOverlay.classList.remove('active');

        // Bỏ logic revert animation ở đây để người dùng vẫn ở giao diện các thẻ bài (sau tra cứu)
        // và chỉ thoát khi dọn bài (thu thẻ).
    };

    if (closeResultsBtn) closeResultsBtn.addEventListener('click', closeResults);
    if (resultsOverlay) resultsOverlay.addEventListener('click', closeResults);

    const form = document.getElementById('horoscope-form');
    const resultsSection = document.getElementById('results-section');
    const wheelImgBtn = document.querySelector('.wheel-img');
    const inputCards = document.querySelectorAll('.input-card');
    const centerLookupBtn = document.getElementById('center-lookup-btn');

    if (centerLookupBtn) {
        centerLookupBtn.addEventListener('click', () => {
            // Chặn tính năng hút thẻ nếu đang có thẻ nào đó được soi chiếu (viewing)
            // Lệnh return này kết hợp với document click listener sẽ khiến lá bài đơn giản chỉ là đáp xuống bàn.
            if (activeViewingCard) {
                return;
            }

            // Chặn tính năng tương tác bát quái khi đang xem kết quả
            // Lúc này chức năng của nút chuyển thành quay "VỀ"
            if (generatedCards.length > 0) {
                if (clearBtn) clearBtn.click();
                centerLookupBtn.innerText = "TRA";
                return;
            }

            if (form && !form.checkValidity()) {
                const firstInvalid = form.querySelector(':invalid');
                if (firstInvalid) {
                    showErrorHint(firstInvalid, firstInvalid.validationMessage || "Vui lòng nhập đầy đủ thông tin");
                    firstInvalid.focus();
                }
                return;
            }

            function showErrorHint(element, message) {
                // Xóa tooltip cũ nếu có
                const old = document.querySelector('.error-tooltip');
                if (old) old.remove();

                const tooltip = document.createElement('div');
                tooltip.className = 'error-tooltip';
                tooltip.innerText = message;

                // Gắn tooltip vào input-card gần nhất để tọa độ đi theo card (chống lệch khi transform)
                const parentCard = element.closest('.input-card');
                if (parentCard) {
                    parentCard.appendChild(tooltip);

                    const elRect = element.getBoundingClientRect();
                    const cardRect = parentCard.getBoundingClientRect();

                    // Tính toán vị trí tâm của input so với card
                    const leftPos = (elRect.left - cardRect.left) + (elRect.width / 2);
                    const topPos = (elRect.bottom - cardRect.top) + 10;

                    tooltip.style.left = leftPos + 'px';
                    tooltip.style.top = topPos + 'px';
                    tooltip.style.transform = 'translateX(-50%)'; // Căn giữa theo trục ngang
                } else {
                    // Fallback nếu không thấy card (hiếm gặp)
                    document.body.appendChild(tooltip);
                    const rect = element.getBoundingClientRect();
                    tooltip.style.left = (rect.left + rect.width / 2) + 'px';
                    tooltip.style.top = (rect.bottom + window.scrollY + 10) + 'px';
                    tooltip.style.transform = 'translateX(-50%)';
                }

                setTimeout(() => {
                    tooltip.style.opacity = '0';
                    setTimeout(() => tooltip.remove(), 400);
                }, 2500);
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
                birthMinute: document.getElementById('birth-minute').value,
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
                    try {
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
                        const decContent = document.getElementById('decoding-content');

                        // Hiển thị Log Box riêng biệt (Metadata)
                        const logBoxContainer = document.getElementById('log-box-container');
                        const tempDiv = document.createElement('div');
                        tempDiv.innerHTML = decodingText;
                        const logBox = tempDiv.querySelector('.log-box');

                        let cleanDecodingText = decodingText;
                        if (logBox && logBoxContainer) {
                            logBoxContainer.innerHTML = logBox.outerHTML;
                            cleanDecodingText = tempDiv.innerHTML;
                        } else {
                            if (logBoxContainer) logBoxContainer.innerHTML = '';
                        }

                        if (decContent) decContent.innerHTML = cleanDecodingText;
                        showFlashcards(cleanDecodingText, displayName, displayCategory);

                        // Cập nhật nút TRA thành VỀ
                        const topBtn = document.getElementById('center-lookup-btn');
                        if (topBtn) topBtn.innerText = "VỀ";

                        // Cố gắng lưu lịch sử
                        try {
                            appendHistory(displayName, displayCategory, displayDob, cleanDecodingText);
                        } catch (hErr) {
                            console.warn("Không thể lưu lịch sử:", hErr);
                        }

                    } catch (procErr) {
                        console.error("Lỗi xử lý dữ liệu:", procErr);
                        alert("Có lỗi xảy ra khi hiển thị kết quả. Vui lòng kiểm tra console.");
                    }
                })
                .catch(error => {
                    console.error("Lỗi kết nối:", error);
                    // Chỉ đẩy thông báo lỗi tổng nếu không có bất kỳ kết quả nào được hiển thị
                    if (!generatedCards || generatedCards.length === 0) {
                        alert("Đã xảy ra lỗi khi giải mã lá số. Vui lòng thử lại!");
                    }
                    // Revert animation on error
                    setSpinSpeed(0.05);
                    const inputCards = document.querySelectorAll('.input-card');
                    if (inputCards) inputCards.forEach(card => card.classList.remove('sucked-in'));
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
    const viewAllBtn = document.getElementById('view-all-btn');
    const actionBtnsWrapper = document.getElementById('action-btns-wrapper'); // Wrapper chứa 2 nút phải
    let generatedCards = [];
    let activeViewingCard = null;

    if (viewAllBtn) {
        viewAllBtn.addEventListener('click', () => {
            const resultsSection = document.getElementById('results-section');
            const resultsOverlay = document.getElementById('results-overlay');
            if (resultsSection && resultsOverlay) {
                if (resultsSection.classList.contains('hidden')) {
                    resultsSection.classList.remove('hidden');
                    resultsOverlay.classList.add('active');

                    // LẬT HẾT THẺ KHI XEM TẤT CẢ
                    generatedCards.forEach((card, index) => {
                        const inner = card.querySelector('.flashcard');
                        if (inner && inner.classList.contains('is-flipped')) {
                            // Tạo hiệu ứng lật so le (stagger) cực nhanh để tăng tính kịch tính
                            setTimeout(() => {
                                inner.classList.remove('is-flipped');
                                card.dataset.viewed = "true";
                            }, index * 20);
                        }
                    });

                } else {
                    resultsSection.classList.add('hidden');
                    resultsOverlay.classList.remove('active');
                }
            }
        });
    }

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

        // KHÔNG còn ép thẻ úp lại (add is-flipped) khi cất thẻ theo yêu cầu mới
        // (để thẻ giữ nguyên mặt ngửa nếu đã xem)
        // const flashcardInner = card.querySelector('.flashcard');
        // if (flashcardInner) flashcardInner.classList.add('is-flipped');

        card.dataset.viewed = "true";

        if (activeViewingCard === card) {
            activeViewingCard.classList.remove('viewing-active');
            activeViewingCard = null;
        }
    }

    function showFlashcards(contentHtml, nameStr, categoryStr) {
        clearAllCards();
        if (actionBtnsWrapper) actionBtnsWrapper.style.display = 'flex';
        if (viewAllBtn) viewAllBtn.style.display = 'flex';

        // Cập nhật dòng nhắc nhở
        const clickHint = document.querySelector('.click-hint');
        if (clickHint) clickHint.innerText = 'Bấm nút THU THẺ để trở lại';

        const segments = extractSegments(contentHtml);

        // Dữ liệu đã tải xong. Bát quái giảm tốc rà rà để nhả thẻ (tạo cảm giác lực ly tâm đẩy thẻ ra)
        setSpinSpeed(3);

        const vpW = window.innerWidth;
        const vpH = window.innerHeight;
        const wheelRadius = 380;
        let w, h;
        const isMobileS = vpW <= 1200;
        if (!isMobileS) {
            w = 130;
            h = 200;
        } else {
            const isPhoneS = vpW <= 500;
            w = isPhoneS ? 90 : 120;
            h = isPhoneS ? 140 : 170;
        }

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
                    <!-- MẶT NGỬA LÍ GIẢI (Mặt back) -->
                    <div class="flashcard-face flashcard-back" style="transform: rotateY(0deg) !important; padding: 5cqw; display: flex; flex-direction: column; overflow-y: auto;">
                        <h3 style="color: #8c1010; font-size: 8cqw; border-bottom: 2px solid rgba(140, 93, 51, 0.4); padding-bottom: 2cqw; text-align: center; margin-bottom: 5cqw; font-family: 'Times New Roman', serif; width: 100%;">${seg.title}</h3>
                        <div style="font-size: 5cqw; color: #3b220b; text-align: justify; line-height: 1.4; width: 100%;">
                            ${seg.html}
                        </div>
                    </div>

                    <!-- MẶT ÚP (Mặt Brown Cover - 180deg internal ban đầu) -->
                    <div class="flashcard-face flashcard-front cover-face" style="transform: rotateY(180deg) !important;">
                        <!-- Chữ được thêm ở góc trên bên trái như yêu cầu -->
                        <div style="position: absolute; top: 15px; left: 15px; right: 15px; text-align: left; color: #ebb96e; font-size: 8cqw; font-family: 'Times New Roman', serif; font-weight: bold; line-height: 1.3;">
                            <span class="highlight-num">${cornerNum}.</span> ${seg.title.replace(/^[0-9.\\s]+/, '').trim()}
                        </div>
                        
                        <div class="que-title" style="margin-top: 60%;">Quẻ số</div>
                        <div class="que-number">${cornerNum}</div>
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

                    cardWrapper.style.width = '340px';
                    cardWrapper.style.height = '530px';

                    // Highest z-index for viewing - Use high constant plus active class
                    cardWrapper.classList.add('viewing-active');
                    cardWrapper.style.zIndex = "10000";

                    // Centering Logic - Responsive based on viewport
                    if (window.innerWidth <= 768) {
                        // Mobile: Center Top (Higher up to avoid bottom stacks)
                        cardWrapper.style.left = (window.innerWidth / 2 - 170) + 'px';
                        cardWrapper.style.top = "80px";
                    } else if (window.innerHeight < 900) {
                        // Laptop: Push center lower to clear header comfortably
                        cardWrapper.style.left = (window.innerWidth / 2 - 170) + 'px';
                        cardWrapper.style.top = (window.innerHeight / 2 - 200) + 'px';
                    } else {
                        // Desktop: Absolute Center
                        cardWrapper.style.left = (window.innerWidth / 2 - 170) + 'px';
                        cardWrapper.style.top = (window.innerHeight / 2 - 265) + 'px';
                    }

                    // Thực hiện lật ra Mặt Detail (Ngửa)
                    flashcardInner.classList.remove('is-flipped');

                    activeViewingCard = cardWrapper;

                } else if (currentState === 'viewing') {
                    // Đã vô hiệu hóa lật mặt thứ 2 theo yêu cầu (chỉ lật 1 lần duy nhất)
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
        const lbContainer = document.getElementById('log-box-container');
        if (lbContainer) lbContainer.innerHTML = '';
        if (actionBtnsWrapper) actionBtnsWrapper.style.display = 'none';
        if (viewAllBtn) viewAllBtn.style.display = 'none';

        // Trả lại dòng nhắc nhở ban đầu
        const clickHint = document.querySelector('.click-hint');
        if (clickHint) clickHint.innerText = 'Bấm Nút TRA để Tra Cứu';
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

            if (actionBtnsWrapper) actionBtnsWrapper.style.display = 'none';
            if (viewAllBtn) viewAllBtn.style.display = 'none';

            const totalClearDuration = (Math.floor(generatedCards.length / 3) * 100) + 1200;
            setTimeout(() => {
                clearAllCards();
                setSpinSpeed(0.05); // Dịch chuyển lá bài xong thì NGỪNG XOAY NHANH, vè Idle của Tra Cứu
                const inputCards = document.querySelectorAll('.input-card');
                if (inputCards) inputCards.forEach(card => card.classList.remove('sucked-in'));

                const topBtn = document.getElementById('center-lookup-btn');
                if (topBtn) topBtn.innerText = "TRA";
            }, totalClearDuration);
        });
    }

    if (arrangeBtn) {
        arrangeBtn.addEventListener('click', () => {
            const vpW = window.innerWidth;
            const vpH = window.innerHeight;
            const safeWheelRadius = 400;

            generatedCards.sort((a, b) => {
                const numA = parseInt(a.dataset.cardNumber) || 0;
                const numB = parseInt(b.dataset.cardNumber) || 0;
                return numA - numB;
            });

            if (activeViewingCard) {
                closeViewingCard(activeViewingCard);
            }

            setTimeout(() => {
                highestZ += generatedCards.length;
                let baseZ = highestZ;

                generatedCards.forEach((card, idx) => {
                    card.dataset.viewState = 'stacked';
                    card.style.transition = 'all 0.8s cubic-bezier(0.25, 0.8, 0.25, 1)';
                    card.style.transform = 'scale(1) rotateZ(0deg)';

                    const w = parseInt(card.style.width) || 130;
                    const h = parseInt(card.style.height) || 200;

                    let targetLeft, targetTop;

                    const isMobile = vpW <= 1200;
                    if (isMobile) {
                        // === MOBILE / TABLET ===
                        const wheelEl = document.querySelector('.wheel-group');
                        const wheelRect = wheelEl ? wheelEl.getBoundingClientRect() : null;

                        const btnBarH = 80;
                        const areaTop = wheelRect
                            ? Math.round(wheelRect.bottom + window.scrollY + 8)
                            : Math.round(vpH * 0.48);
                        const areaBottom = vpH - btnBarH;
                        const areaLeft = 10;
                        const areaRight = vpW - 10;
                        const areaW = areaRight - areaLeft;
                        const areaH = Math.max(100, areaBottom - areaTop);

                        const isPhone = vpW <= 500;

                        // Kích thước thẻ
                        const maxCardW = isPhone ? 90 : 120;
                        const maxCardH = isPhone ? 140 : 170;
                        const gap = 8;

                        // iPhone: cứng 4 cột | iPad: tính tối ưu min 5 cột
                        const cols = isPhone
                            ? 4
                            : Math.max(5, Math.floor(areaW / (maxCardW + gap)));
                        const colW = Math.min(maxCardW, Math.floor((areaW - gap * (cols - 1)) / cols));
                        const colH = maxCardH;

                        // rowSpacing: cả phone lẫn tablet đều cứng 38px
                        // → chỉ lộ phần title (~38px) của thẻ bên dưới, không giãn toàn thẻ
                        const totalRows = Math.ceil(generatedCards.length / cols);
                        const rowSpacing = 38;

                        const col = idx % cols;
                        const row = Math.floor(idx / cols);

                        const totalGridW = cols * colW + (cols - 1) * gap;
                        const gridLeft = Math.round((vpW - totalGridW) / 2);

                        targetLeft = gridLeft + col * (colW + gap);
                        targetTop = areaTop + row * rowSpacing;

                        card.style.width = colW + 'px';
                        card.style.height = colH + 'px';
                    } else {
                        // === DESKTOP: 4 columns split LEFT and RIGHT ===
                        const desktopW = 130;
                        const desktopH = 200;
                        const COLS = 4;
                        const totalCards = generatedCards.length;
                        const CARDS_PER_COL = Math.ceil(totalCards / COLS);

                        const col = Math.floor(idx / CARDS_PER_COL);
                        const row = idx % CARDS_PER_COL;

                        const gapX = 25;
                        const staggerY = 42;
                        const safeRadius = 380; // Tránh bát quái trung tâm

                        if (col < 2) {
                            // Cột 0 và 1 ở bên TRÁI
                            // Cột 0 ngoài cùng, cột 1 sát bát quái
                            const leftBase = (vpW / 2) - safeRadius - desktopW;
                            if (col === 0) {
                                targetLeft = leftBase - desktopW - gapX;
                            } else {
                                targetLeft = leftBase;
                            }
                        } else {
                            // Cột 2 và 3 ở bên PHẢI
                            // Cột 2 sát bát quái, cột 3 ngoài cùng
                            const rightBase = (vpW / 2) + safeRadius;
                            if (col === 2) {
                                targetLeft = rightBase;
                            } else {
                                targetLeft = rightBase + desktopW + gapX;
                            }
                        }

                        targetTop = 110 + row * staggerY;

                        card.style.width = desktopW + 'px';
                        card.style.height = desktopH + 'px';
                    }

                    card.style.left = targetLeft + 'px';
                    card.style.top = targetTop + 'px';

                    const targetZ = baseZ + idx;
                    card.style.zIndex = targetZ;
                    card.dataset.stableZIndex = targetZ;
                    card.dataset.origZIndex = targetZ;

                    setTimeout(() => {
                        card.style.transition = 'width 0.3s, height 0.3s, top 0.4s, left 0.4s, transform 0.4s';
                    }, 800);
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

        // Loại bỏ log-box nếu nó vẫn còn lọt vào đây
        const lb = wrapper.querySelector('.log-box');
        if (lb) lb.remove();

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
            segments.push({ title: currentTitle || 'TỔNG QUAN', html: currentSegmentHtml });
        }

        // Đảm bảo không có đoạn trống
        return segments.filter(seg => seg.html.replace(/<[^>]*>?/gm, '').trim().length > 0);
    }

    function makeDraggable(el) {
        // Tắt tính năng kéo thả ở tất cả các thẻ theo yêu cầu của user
    }

    // ================== CUSTOM DROPDOWN SYSTEM ================== //
    function initCustomSelects() {
        const selects = document.querySelectorAll('.input-card select');

        selects.forEach(select => {
            // Check if already initialized
            if (select.parentNode.classList.contains('custom-select-wrapper')) return;

            // Hộp chứa chính
            const wrapper = document.createElement('div');
            wrapper.className = 'custom-select-wrapper';
            select.parentNode.insertBefore(wrapper, select);

            // Di chuyển select vào wrapper và ẩn đi
            wrapper.appendChild(select);
            select.classList.add('select-hide');

            // Tạo Trigger (Thanh hiển thị giá trị đang chọn)
            const trigger = document.createElement('div');
            trigger.className = 'custom-select-trigger';
            trigger.tabIndex = 0; // Để có thể focus
            trigger.innerText = select.options[select.selectedIndex].text;
            wrapper.appendChild(trigger);

            // Tạo danh sách Options
            const optionsContainer = document.createElement('div');
            optionsContainer.className = 'custom-options';

            Array.from(select.options).forEach((option, index) => {
                const optElement = document.createElement('div');
                optElement.className = 'custom-option';
                if (index === select.selectedIndex) optElement.classList.add('selected');
                optElement.innerText = option.text;
                optElement.dataset.value = option.value;

                optElement.addEventListener('click', (e) => {
                    e.stopPropagation();
                    // Cập nhật giá trị hiển thị và giá trị thực tế
                    select.selectedIndex = index;
                    trigger.innerText = option.text;

                    // Cập nhật class 'selected'
                    optionsContainer.querySelectorAll('.custom-option').forEach(el => el.classList.remove('selected'));
                    optElement.classList.add('selected');

                    // Đóng menu
                    wrapper.classList.remove('open');
                    const parentCard = wrapper.closest('.input-card');
                    if (parentCard) parentCard.classList.remove('active-card');

                    // Kích hoạt sự kiện change cho select gốc (để JS form capturer nhận diện)
                    select.dispatchEvent(new Event('change', { bubbles: true }));
                });

                optionsContainer.appendChild(optElement);
            });

            wrapper.appendChild(optionsContainer);

            // Xử lý sự kiện mở menu
            trigger.addEventListener('click', (e) => {
                e.stopPropagation();

                // Đóng các dropdown khác đang mở
                document.querySelectorAll('.custom-select-wrapper.open').forEach(w => {
                    if (w !== wrapper) {
                        w.classList.remove('open');
                        w.classList.remove('open-up');
                        const p = w.closest('.input-card');
                        if (p) p.classList.remove('active-card');
                    }
                });

                // Kiểm tra không gian phía dưới để quyết định hướng mở (Smart Flip)
                const rect = trigger.getBoundingClientRect();
                const spaceBelow = window.innerHeight - rect.bottom;
                const neededSpace = 260; // max-height (250) + margin (10)

                if (spaceBelow < neededSpace) {
                    wrapper.classList.add('open-up');
                } else {
                    wrapper.classList.remove('open-up');
                }

                const isOpen = wrapper.classList.toggle('open');
                const parentCard = wrapper.closest('.input-card');
                if (parentCard) {
                    if (isOpen) parentCard.classList.add('active-card');
                    else parentCard.classList.remove('active-card');
                }
            });
        });

        // Đóng dropdown khi click ra ngoài
        document.addEventListener('click', () => {
            document.querySelectorAll('.custom-select-wrapper.open').forEach(w => {
                w.classList.remove('open');
                const p = w.closest('.input-card');
                if (p) p.classList.remove('active-card');
            });
        });
    }

    // Chạy khởi tạo ngay và sau khi DOM ổn định (phòng trường hợp dynamic content)
    initCustomSelects();

});
