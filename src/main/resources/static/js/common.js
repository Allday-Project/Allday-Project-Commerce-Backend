/**
 * ADP Commerce - Common JavaScript
 * 검색, 챗봇 등 공통 인터랙션 처리
 */

document.addEventListener('DOMContentLoaded', () => {
    initSearch();
    initChatbot();
});

/* ===========================
   Search
   =========================== */
function initSearch() {
    const searchInput = document.getElementById('search-input');
    const searchDropdown = document.getElementById('search-dropdown');
    const searchWrapper = document.getElementById('search-wrapper');
    const keywordList = document.getElementById('search-keyword-list');

    if (!searchInput || !searchDropdown) return;

    // 검색 입력 포커스 시 드롭다운 표시
    searchInput.addEventListener('focus', () => {
        searchDropdown.classList.add('active');
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', (e) => {
        if (searchWrapper && !searchWrapper.contains(e.target)) {
            searchDropdown.classList.remove('active');
        }
    });

    // 키워드 클릭 시 검색
    if (keywordList) {
        keywordList.addEventListener('click', (e) => {
            const li = e.target.closest('li');
            if (li) {
                const text = li.textContent.replace(/^\d+\.\s*/, '').trim();
                searchInput.value = text;
                searchDropdown.classList.remove('active');
                // 검색 실행 (추후 연동)
                performSearch(text);
            }
        });
    }

    // Enter 키 검색
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const query = searchInput.value.trim();
            if (query) {
                searchDropdown.classList.remove('active');
                performSearch(query);
            }
        }
    });

    // 인기 검색어 바 키워드 클릭
    const keywordTags = document.querySelectorAll('.keyword-tag');
    keywordTags.forEach(tag => {
        tag.addEventListener('click', () => {
            const text = tag.textContent.replace(/^\d+\.\s*/, '').trim();
            searchInput.value = text;
            performSearch(text);
        });
    });
}

function performSearch(query) {
    console.log('검색:', query);
    // TODO: 서버 검색 API 연동
}

/* ===========================
   Chatbot
   =========================== */
function initChatbot() {
    const toggleBtn = document.getElementById('chatbot-toggle');
    const panel = document.getElementById('chatbot-panel');
    const chatInput = document.getElementById('chatbot-input');
    const sendBtn = document.getElementById('chatbot-send-btn');
    const endBtn = document.getElementById('chatbot-end-btn');

    if (!toggleBtn || !panel) return;

    // 토글
    toggleBtn.addEventListener('click', () => {
        panel.classList.toggle('active');
    });

    // 외부 클릭으로 닫기
    document.addEventListener('click', (e) => {
        const container = document.getElementById('chatbot-container');
        if (container && !container.contains(e.target)) {
            panel.classList.remove('active');
        }
    });

    // 상담 종료
    if (endBtn) {
        endBtn.addEventListener('click', () => {
            panel.classList.remove('active');
        });
    }

    // 메시지 전송
    if (sendBtn && chatInput) {
        sendBtn.addEventListener('click', () => {
            sendChatMessage(chatInput);
        });

        chatInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                sendChatMessage(chatInput);
            }
        });
    }

    // 메뉴 버튼 클릭
    const orderBtn = document.getElementById('chatbot-order-btn');
    const cartBtn = document.getElementById('chatbot-cart-btn');
    const mypageBtn = document.getElementById('chatbot-mypage-btn');
    const agentBtn = document.getElementById('chatbot-agent-btn');

    if (orderBtn) orderBtn.addEventListener('click', () => { window.location.href = '/orders'; });
    if (cartBtn) cartBtn.addEventListener('click', () => { window.location.href = '/cart'; });
    if (mypageBtn) mypageBtn.addEventListener('click', () => { window.location.href = '/mypage'; });
    if (agentBtn) agentBtn.addEventListener('click', () => { addChatbotMessage('상담원을 연결 중입니다...'); });
}

function sendChatMessage(input) {
    const message = input.value.trim();
    if (!message) return;

    addChatbotMessage(message, true);
    input.value = '';

    // TODO: 챗봇 API 연동
    setTimeout(() => {
        addChatbotMessage('죄송합니다, 현재 자동 응답 기능을 준비 중입니다.');
    }, 800);
}

function addChatbotMessage(text, isUser = false) {
    const body = document.getElementById('chatbot-body');
    if (!body) return;

    const msgEl = document.createElement('div');
    msgEl.className = 'chatbot-message' + (isUser ? ' chatbot-message-user' : ' chatbot-message-bot');
    msgEl.textContent = text;

    // 스타일 인라인 (간단한 메시지)
    msgEl.style.padding = '8px 12px';
    msgEl.style.marginTop = '8px';
    msgEl.style.borderRadius = '8px';
    msgEl.style.fontSize = '13px';
    msgEl.style.lineHeight = '1.5';
    msgEl.style.maxWidth = '80%';

    if (isUser) {
        msgEl.style.background = '#1a1a1a';
        msgEl.style.color = '#fff';
        msgEl.style.marginLeft = 'auto';
        msgEl.style.textAlign = 'right';
    } else {
        msgEl.style.background = '#f0f0f0';
        msgEl.style.color = '#333';
    }

    body.appendChild(msgEl);
    body.scrollTop = body.scrollHeight;
}
