document.addEventListener('DOMContentLoaded', () => {

    // --- Sidebar Toggle Logic ---
    const sidebar = document.getElementById('right-sidebar');
    const toggleBtn = document.getElementById('toggle-sidebar');
    const closeBtn = document.getElementById('close-sidebar');
    const overlay = document.getElementById('sidebar-overlay');

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
        const wheelImg = document.querySelector('.wheel-img');
        const inputCards = document.querySelectorAll('.input-card');
        if (wheelImg) wheelImg.classList.remove('fast-spin');
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
            // Nếu đã có thẻ, tức là đang ở trạng thái xem kết quả
            if (generatedCards.length > 0) {
                // Xoay ngược bát quái
                wheelImgBtn.classList.remove('paused');
                wheelImgBtn.classList.add('fast-spin-reverse');

                // Hút tất cả thẻ vào
                generatedCards.forEach(cardWrapper => {
                    cardWrapper.classList.add('sucked-in');
                });
                arrangeBtn.style.display = 'none';
                clearBtn.style.display = 'none';

                // Chờ hiệu ứng bay vào, dọn thẻ, nhả input ra
                setTimeout(() => {
                    clearAllCards();
                    wheelImgBtn.classList.remove('fast-spin-reverse');
                    if (inputCards) {
                        inputCards.forEach(card => card.classList.remove('sucked-in'));
                    }
                }, 1000);
                return;
            }

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
                    if (wheelImgBtn) {
                        wheelImgBtn.classList.remove('fast-spin');
                        wheelImgBtn.classList.remove('paused');
                    }
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
    let generatedCards = [];

    function showFlashcards(contentHtml, nameStr, categoryStr) {
        clearAllCards();
        arrangeBtn.style.display = 'block';
        clearBtn.style.display = 'block';

        const segments = extractSegments(contentHtml);

        const wheelImgBtn = document.querySelector('.wheel-img');
        if (wheelImgBtn) {
            wheelImgBtn.classList.remove('paused');
            wheelImgBtn.classList.add('fast-spin');
        }

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
            cardWrapper.style.left = (wheelCenterX - w/2) + 'px';
            cardWrapper.style.top = (wheelCenterY - h/2) + 'px';
            cardWrapper.style.zIndex = highestZ++;
            cardWrapper.style.opacity = '0';
            cardWrapper.style.transform = `scale(0.1) rotate(500deg)`;
            cardWrapper.style.transition = 'all 0.8s cubic-bezier(0.175, 0.885, 0.32, 1.275)';

            const match = seg.title.match(/^(\d+)[.\s]/);
            const cornerNum = match ? match[1] : (index + 1);

            cardWrapper.innerHTML = `
                <div class="flashcard">
                    <div class="flashcard-face flashcard-front">
                        <div class="corner-number">${cornerNum}</div>
                        <div class="que-title">Quẻ số</div>
                        <div class="que-number">${cornerNum}</div>
                        <div class="que-subtitle">${seg.title}</div>
                    </div>
                    <div class="flashcard-face flashcard-back">
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
                
                const currentState = cardWrapper.dataset.viewState;
                cardWrapper.style.zIndex = highestZ++;

                if (currentState === 'idle' || currentState === 'stacked') {
                    cardWrapper.dataset.viewState = 'viewing';
                    cardWrapper.style.transition = 'all 0.6s cubic-bezier(0.25, 1, 0.5, 1)';
                    cardWrapper.style.transform = 'rotateZ(0deg)'; // Straighten wrapper
                    
                    // Thẻ lúc xem to bằng bản gốc cũ
                    cardWrapper.style.width = '300px';
                    cardWrapper.style.height = '465px';
                    
                    const isLeftSide = parseFloat(cardWrapper.style.left) < (window.innerWidth / 2);
                    if (isLeftSide) {
                        cardWrapper.style.left = (window.innerWidth / 2 - 400) + 'px';
                    } else {
                        cardWrapper.style.left = (window.innerWidth / 2 + 100) + 'px';
                    }
                    cardWrapper.style.top = (window.innerHeight / 2 - 232) + 'px';
                    
                } else if (currentState === 'viewing') {
                    if (!flashcardInner.classList.contains('is-flipped')) {
                        flashcardInner.classList.add('is-flipped');
                    } else {
                        cardWrapper.dataset.viewState = 'graveyard';
                        cardWrapper.style.transition = 'all 0.8s cubic-bezier(0.25, 1, 0.5, 1)';
                        
                        // Scale lại thẻ nhỏ xíu cho tụ bài
                        cardWrapper.style.width = '100px';
                        cardWrapper.style.height = '155px';
                        
                        flashcardInner.classList.remove('is-flipped'); 
                        cardWrapper.style.transform = 'rotateZ(5deg)';
                        
                        cardWrapper.style.left = (window.innerWidth / 2 - 200) + 'px';
                        cardWrapper.style.top = (window.innerHeight - 200) + 'px';
                    }
                } else if (currentState === 'graveyard') {
                    cardWrapper.dataset.viewState = 'viewing';
                    cardWrapper.style.width = '300px';
                    cardWrapper.style.height = '465px';
                    cardWrapper.style.transform = 'rotateZ(0deg)';
                    cardWrapper.style.left = (window.innerWidth / 2 - 400) + 'px';
                    cardWrapper.style.top = (window.innerHeight / 2 - 232) + 'px';
                }
            });

            makeDraggable(cardWrapper);
        });

        // Dừng bát quái khi bắn hết thẻ
        const totalDuration = segments.length * 200 + 1000;
        setTimeout(() => {
            if (wheelImgBtn && generatedCards.length > 0) {
                wheelImgBtn.classList.remove('fast-spin');
                wheelImgBtn.classList.add('paused');
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
            if (wheelImgBtn && generatedCards.length > 0) {
                wheelImgBtn.classList.remove('paused');
                wheelImgBtn.classList.add('fast-spin-reverse');
            }

            generatedCards.forEach(cardWrapper => {
                cardWrapper.classList.add('sucked-in');
            });
            arrangeBtn.style.display = 'none';
            clearBtn.style.display = 'none';

            setTimeout(() => {
                clearAllCards();
                if (wheelImgBtn) wheelImgBtn.classList.remove('fast-spin-reverse');
                const inputCards = document.querySelectorAll('.input-card');
                if (inputCards) inputCards.forEach(card => card.classList.remove('sucked-in'));
            }, 1000);
        });
    }

    if (arrangeBtn) {
        arrangeBtn.addEventListener('click', () => {
            const vpW = window.innerWidth;
            const vpH = window.innerHeight;
            const safeWheelRadius = 400; // Cách tâm 400px cực kì an toàn
            
            const CARDS_PER_COL = 10;

            // Sort logic: người dùng muốn "xếp theo thứ tự" -> sort theo con số trên bài
            generatedCards.sort((a, b) => {
                const numA = parseInt(a.querySelector('.corner-number').innerText) || 0;
                const numB = parseInt(b.querySelector('.corner-number').innerText) || 0;
                return numA - numB;
            });

            // Sau khi sort, ta gán lại z-index để thẻ nào xếp trước thì nằm dưới
            highestZ += generatedCards.length;
            let baseZ = highestZ;

            generatedCards.forEach((card, idx) => {
                const flashcard = card.querySelector('.flashcard');
                flashcard.style.transform = 'rotateZ(0deg)'; // Hủy xoay nội bộ
                flashcard.classList.remove('is-flipped'); 
                
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
                card.style.zIndex = baseZ + idx;

                setTimeout(() => { card.style.transition = 'width 0.3s, height 0.3s, top 0.4s, left 0.4s, transform 0.4s'; }, 800);
            });
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
