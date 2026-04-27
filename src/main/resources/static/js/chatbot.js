/**
 * ADP Commerce - Chatbot Module
 * 실시간 챗봇 메시지 정렬 및 WebSocket 통신 처리
 */

class ChatbotPanel {
    constructor(panelId) {
        this.panel = document.getElementById(panelId);
        this.body = document.getElementById('chatbot-body');
        this.input = document.getElementById('chatbot-input');
        this.sendBtn = document.getElementById('chatbot-send-btn');
        this.toggleBtn = document.getElementById('chatbot-toggle');
        this.closeBtn = document.getElementById('chatbot-close-btn');
        this.endBtn = document.getElementById('chatbot-end-btn');
        
        this.stompClient = null;
        this.currentRoomId = null;
        this.currentUserId = null;
        this.currentUserRole = null; // 'USER' or 'ADMIN'
        this.isInConsultation = false;
        this.isAgentJoined = false; // 상담원 입장 여부
        this.currentSubscription = null; // STOMP 구독 객체 (중복 방지)
        this._warningTimer = null; // 비활성 경고 타이머
        
        this.init();
    }
    
    init() {
        // Set user info from session (passed from server)
        if (window.chatbotUserInfo && window.chatbotUserInfo.id) {
            this.currentUserId = window.chatbotUserInfo.id;
            this.currentUserRole = window.chatbotUserInfo.role;
        }
        
        // Initialize event listeners
        this.initEventListeners();
        
        // Connect to WebSocket only if logged in
        if (this.currentUserId) {
            this.connectWebSocket();
            
            // ADMIN일 경우 페이지 로드 시 뱃지 초기화
            if (this.currentUserRole === 'ADMIN') {
                this.initAdminBadge();
            }
            
            // 고객(USER)일 경우 활성 방 확인 → WebSocket 구독 (알림 수신용)
            if (this.currentUserRole === 'USER') {
                this.initUserActiveRoom();
            }
        }
    }
    
    /**
     * 고객 페이지 로드 시 활성 채팅방 확인
     * 활성 방이 있으면 WebSocket 구독하여 패널 닫혀있어도 메시지 수신 → 알림 점 표시
     */
    async initUserActiveRoom() {
        try {
            const response = await fetch('/api/chat/rooms/my');
            if (response.ok) {
                const result = await response.json();
                if (result.success && result.data) {
                    const room = result.data;
                    this.currentRoomId = room.id;
                    // UI는 메뉴 화면 유지 (isInConsultation 설정 안 함)
                    // WebSocket만 구독하여 알림 점 표시용
                    if (this.stompClient && this.stompClient.connected) {
                        this.subscribeToRoom(room.id);
                    }
                }
            }
        } catch (e) {
            console.error('활성 방 확인 실패:', e);
        }
    }
    
    async initAdminBadge() {
        try {
            // WAITING + IN_PROGRESS 채팅방 수 합산
            let totalActive = 0;
            for (const status of ['WAITING', 'IN_PROGRESS']) {
                const response = await fetch(`/api/chat/admin/rooms?page=0&size=1&chatRoomStatus=${status}`);
                if (response.ok) {
                    const result = await response.json();
                    if (result.success && result.data) {
                        totalActive += (result.data.totalElements || 0);
                    }
                }
            }
            this.updateAdminBadge(totalActive, true);
        } catch (e) {
            console.error('관리자 뱃지 초기화 실패:', e);
        }
    }
    
    updateAdminBadge(count, isAbsolute = false) {
        const badge = document.getElementById('admin-chat-badge');
        if (badge) {
            let newCount = isAbsolute ? count : parseInt(badge.textContent || '0') + count;
            newCount = Math.max(0, newCount);
            
            badge.textContent = newCount;
            if (newCount > 0) {
                badge.style.display = 'flex';
                badge.style.justifyContent = 'center';
                badge.style.alignItems = 'center';
                badge.style.color = 'white';
                badge.style.fontSize = '10px';
                badge.style.fontWeight = 'bold';
                badge.style.width = '16px';
                badge.style.height = '16px';
                badge.style.borderRadius = '50%';
            } else {
                badge.style.display = 'none';
            }
        }
    }
    
    initEventListeners() {
        // Toggle panel
        if (this.toggleBtn) {
            this.toggleBtn.addEventListener('click', () => {
                this.panel.classList.toggle('active');
                if (this.panel.classList.contains('active')) {
                    this.hideNotificationDot();
                }
            });
        }
        
        // Close panel on outside click
        document.addEventListener('click', (e) => {
            const container = document.getElementById('chatbot-container');
            if (container && !container.contains(e.target)) {
                this.panel.classList.remove('active');
            }
        });
        
        // Send message on button click
        if (this.sendBtn) {
            this.sendBtn.addEventListener('click', () => {
                if (!this.isInConsultation) return;
                this.sendMessage();
            });
        }
        
        // Send message on Enter key
        if (this.input) {
            this.input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    if (!this.isInConsultation) return;
                    this.sendMessage();
                }
            });
        }
        
        // Close button in header - just closes the panel
        if (this.closeBtn) {
            this.closeBtn.addEventListener('click', () => {
                this.panel.classList.remove('active');
            });
        }
        
        // End consultation button
        if (this.endBtn) {
            this.endBtn.addEventListener('click', () => {
                if (this.isInConsultation) {
                    if (confirm('상담을 종료하시겠습니까?')) {
                        this.endConsultation();
                    }
                } else {
                    alert('진행 중인 상담이 없습니다.');
                }
            });
        }
        
        // Menu button handlers
        const orderBtn = document.getElementById('chatbot-order-btn');
        const cartBtn = document.getElementById('chatbot-cart-btn');
        const mypageBtn = document.getElementById('chatbot-mypage-btn');
        const agentBtn = document.getElementById('chatbot-agent-btn');
        
        // Admin menu handlers
        const adminConsultationBtn = document.getElementById('chatbot-admin-consultation-btn');
        const adminOrderBtn = document.getElementById('chatbot-admin-order-btn');
        const adminProductBtn = document.getElementById('chatbot-admin-product-btn');
        
        if (orderBtn) orderBtn.addEventListener('click', () => { this.handleMenuClick('/orders'); });
        if (cartBtn) cartBtn.addEventListener('click', () => { this.handleMenuClick('/cart'); });
        if (mypageBtn) mypageBtn.addEventListener('click', () => { this.handleMenuClick('/mypage'); });
        if (agentBtn) agentBtn.addEventListener('click', () => { 
            this.handleAgentConnect();
        });
        
        if (adminConsultationBtn) adminConsultationBtn.addEventListener('click', () => { this.handleMenuClick('/admin/consultations'); });
        if (adminOrderBtn) adminOrderBtn.addEventListener('click', () => { alert('전체 주문 관리 페이지는 준비 중입니다.'); });
        if (adminProductBtn) adminProductBtn.addEventListener('click', () => { alert('상품 관리 페이지는 준비 중입니다.'); });
    }
    
    hideMenu() {
        const menu = document.getElementById('chatbot-menu');
        if (menu) {
            menu.style.display = 'none';
        }
    }
    
    showMenu() {
        const menu = document.getElementById('chatbot-menu');
        if (menu) {
            menu.style.display = 'flex';
        }
    }
    
    connectWebSocket() {
        try {
            const socket = new SockJS('/ws-chat');
            this.stompClient = Stomp.over(socket);
            
            // Disable debug logging
            this.stompClient.debug = null;
            
            this.stompClient.connect({}, (frame) => {
                console.log('WebSocket 연결 성공');
                
                // Subscribe to current room if exists
                if (this.currentRoomId) {
                    this.subscribeToRoom(this.currentRoomId);
                }
                
                // ADMIN일 경우 전역 상담 알림 구독
                if (this.currentUserRole === 'ADMIN') {
                    this.stompClient.subscribe('/sub/admin/consultations', (message) => {
                        // 활성 채팅방 수 기반으로 뱃지 재조회
                        this.initAdminBadge();
                    });
                }
            }, (error) => {
                console.error('WebSocket 연결 실패:', error);
                
                // Retry connection after 5 seconds
                setTimeout(() => {
                    if (this.currentUserId) {
                        this.connectWebSocket();
                    }
                }, 5000);
            });
        } catch (e) {
            console.error('WebSocket 초기화 실패:', e);
        }
    }
    
    subscribeToRoom(roomId) {
        if (!this.stompClient || !this.stompClient.connected) {
            console.error('WebSocket not connected');
            return;
        }
        
        // 기존 구독 해제 (중복 방지)
        if (this.currentSubscription) {
            try { this.currentSubscription.unsubscribe(); } catch(e) {}
            this.currentSubscription = null;
        }
        
        this.currentRoomId = roomId;
        
        // Subscribe to room messages
        this.currentSubscription = this.stompClient.subscribe('/sub/chat/' + roomId, (message) => {
            const chatMessage = JSON.parse(message.body);
            this.receiveMessage(chatMessage);
        });
        
        console.log('Subscribed to chat room:', roomId);
    }
    
    sendMessage() {
        const message = this.input.value.trim();
        if (!message) return;
        
        if (!this.isAgentJoined) {
            this.showError('상담원이 아직 연결되지 않았습니다.');
            return;
        }
        
        // Check if we have a room ID and WebSocket connection
        if (!this.currentRoomId) {
            console.error('No active chat room');
            this.showError('채팅방이 연결되지 않았습니다.');
            return;
        }
        
        if (!this.stompClient || !this.stompClient.connected) {
            console.error('WebSocket not connected');
            this.showError('연결이 끊어졌습니다. 페이지를 새로고침해주세요.');
            return;
        }
        
        // Send message via WebSocket
        this.stompClient.send(
            '/pub/chat/' + this.currentRoomId,
            { 'content-type': 'application/json' },
            JSON.stringify({ content: message })
        );
        
        this.input.value = '';
    }
    
    receiveMessage(message) {
        const isOwnMessage = this.isOwnMessage(message);
        
        // 상담 화면이 활성화된 경우에만 메시지 렌더링 (메뉴 화면에서는 렌더 안 함)
        if (this.isInConsultation) {
            this.renderMessage(message, isOwnMessage, false);
            this.scrollToBottom();
        }
        
        // 패널 닫힘 + 상대방 메시지 → 알림 점 (고객 전용)
        if (this.currentUserRole !== 'ADMIN' && !isOwnMessage &&
            !this.panel.classList.contains('active') &&
            message.senderType !== 'SYSTEM') {
            this.showNotificationDot();
        }
        
        // 비활성 타이머 리셋
        if (this.isInConsultation && this.isAgentJoined) {
            this.resetInactivityTimer();
        }
    }
    
    isOwnMessage(message) {
        // SYSTEM 메시지는 항상 상대방(왼쪽) 취급
        if (message.senderType === 'SYSTEM') return false;
        
        // senderId 기반 비교 (가장 정확)
        if (message.senderId && this.currentUserId) {
            return String(message.senderId) === String(this.currentUserId);
        }
        
        // fallback: role 기반 비교 (UserRole: USER/ADMIN ↔ SenderType: CUSTOMER/ADMIN)
        const mappedRole = this.currentUserRole === 'USER' ? 'CUSTOMER' : this.currentUserRole;
        return message.senderType === mappedRole;
    }
    
    renderMessage(message, isOwnMessage, isHistorical = false) {
        if (!this.body) return;
        
        // SYSTEM 메시지는 가운데 시스템 알림으로 표시
        if (message.senderType === 'SYSTEM' || message.messageType === 'SYSTEM') {
            const systemEl = document.createElement('div');
            systemEl.className = 'chat-system-message';
            systemEl.textContent = message.content;
            systemEl.style.cssText = `
                padding: 8px 14px;
                margin: 8px auto;
                background: #f0f0f0;
                color: #888;
                border-radius: 12px;
                font-size: 12px;
                text-align: center;
                max-width: 80%;
                width: fit-content;
            `;
            this.body.appendChild(systemEl);
            
            // 실시간 메시지일 때만 side-effect 실행 (히스토리 로딩 시는 무시)
            if (!isHistorical) {
                if (message.content.includes('상담원이 연결되었습니다')) {
                    this.isAgentJoined = true;
                    this.setInputEnabled(true);
                    const waitingEl = this.body.querySelector('.chat-waiting-message');
                    if (waitingEl) waitingEl.remove();
                    this.resetInactivityTimer();
                }
                
                if (message.content.includes('상담이 종료되었습니다') || message.content.includes('자동 종료되었습니다')) {
                    this.handleRemoteClose();
                }
            }
            return;
        }
        
        // Create message container
        const messageContainer = document.createElement('div');
        messageContainer.className = 'chat-message-container';
        messageContainer.className += isOwnMessage ? ' chat-message-own' : ' chat-message-other';
        
        // Create message bubble
        const messageBubble = document.createElement('div');
        messageBubble.className = 'chat-message-bubble';
        messageBubble.textContent = message.content;
        
        // Create timestamp
        const timestamp = document.createElement('div');
        timestamp.className = 'chat-message-time';
        timestamp.textContent = this.formatTime(message.createdAt);
        
        // Append elements
        messageContainer.appendChild(messageBubble);
        messageContainer.appendChild(timestamp);
        
        this.body.appendChild(messageContainer);
    }
    
    scrollToBottom() {
        if (!this.body) return;
        
        this.body.scrollTo({
            top: this.body.scrollHeight,
            behavior: 'smooth'
        });
    }
    
    formatTime(dateTimeString) {
        if (!dateTimeString) return '';
        // 서버는 UTC(LocalDateTime)로 전송하므로 'Z'를 붙여 브라우저가 로컬 시간으로 변환하도록 함
        const isoString = dateTimeString.endsWith('Z') ? dateTimeString : dateTimeString + 'Z';
        const date = new Date(isoString);
        const hours = date.getHours().toString().padStart(2, '0');
        const minutes = date.getMinutes().toString().padStart(2, '0');
        return `${hours}:${minutes}`;
    }
    
    showError(message) {
        if (!this.body) return;
        
        const errorEl = document.createElement('div');
        errorEl.className = 'chat-error-message';
        errorEl.textContent = message;
        
        this.body.appendChild(errorEl);
        this.scrollToBottom();
        
        // Remove error after 5 seconds
        setTimeout(() => errorEl.remove(), 5000);
    }
    
    handleMenuClick(url) {
        // 로그인 체크
        if (!this.currentUserId) {
            this.showLoginRequired();
            return;
        }
        
        window.location.href = url;
    }
    
    openAdminConsultation(roomId) {
        // 이미 같은 방을 보고 있으면 패널만 열기
        if (this.currentRoomId === roomId && this.isInConsultation) {
            this.panel.classList.add('active');
            return;
        }
        
        this.hideMenu();
        this.clearMessages();
        this.currentRoomId = roomId;
        this.isInConsultation = true;
        this.isAgentJoined = true;
        this.setInputEnabled(true);
        
        // Subscribe if already connected
        if (this.stompClient && this.stompClient.connected) {
            this.subscribeToRoom(roomId);
        } else {
            this.connectWebSocket();
        }
        
        const titleEl = document.querySelector('.chatbot-title');
        if (titleEl) titleEl.textContent = '상담 채팅';
        if (this.endBtn) this.endBtn.style.display = 'inline-block';
        
        this.loadRoomMessages(roomId);
        this.panel.classList.add('active');
    }
    
    async handleAgentConnect() {
        // 로그인 체크
        if (!this.currentUserId) {
            this.showLoginRequired();
            return;
        }
        
        this.hideMenu();
        this.clearMessages();
        
        // 상담원 연결 중 메시지 표시
        const connectingMsg = document.createElement('div');
        connectingMsg.className = 'chat-system-message';
        connectingMsg.textContent = '상담원을 연결하고 있습니다...';
        connectingMsg.style.cssText = `
            padding: 10px 14px;
            margin: 8px auto;
            background: #f0f0f0;
            color: #666;
            border-radius: 12px;
            font-size: 13px;
            text-align: center;
            max-width: 80%;
            width: fit-content;
        `;
        this.body.appendChild(connectingMsg);
        this.scrollToBottom();
        
        try {
            // 채팅방 생성/조회 API 호출
            const response = await fetch('/api/chat/rooms', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ title: '상담 요청' })
            });
            
            if (!response.ok) {
                throw new Error('상담방 생성에 실패했습니다.');
            }
            
            const apiResponse = await response.json();
            
            if (apiResponse.success && apiResponse.data) {
                const room = apiResponse.data;
                this.currentRoomId = room.id;
                this.isInConsultation = true;
                
                // 상태에 따라 입력 제어
                if (room.chatRoomStatus === 'IN_PROGRESS') {
                    this.isAgentJoined = true;
                    this.setInputEnabled(true);
                } else {
                    this.isAgentJoined = false;
                    this.setInputEnabled(false);
                }
                
                // WebSocket 구독
                if (this.stompClient && this.stompClient.connected) {
                    this.subscribeToRoom(room.id);
                }
                
                // 헤더 타이틀 변경
                const titleEl = document.querySelector('.chatbot-title');
                if (titleEl) titleEl.textContent = '상담 채팅';
                
                // 상담 종료 버튼 노출
                if (this.endBtn) this.endBtn.style.display = 'inline-block';
                
                // 연결 중 메시지 제거 후 이전 대화 내역 로드
                connectingMsg.remove();
                await this.loadRoomMessages(room.id);
                
                // WAITING 상태이면 대기 메시지 표시
                if (room.chatRoomStatus === 'WAITING') {
                    this.showWaitingMessage();
                }
                
                // IN_PROGRESS 상태이면 비활성 타이머 시작
                if (room.chatRoomStatus === 'IN_PROGRESS') {
                    this.resetInactivityTimer();
                }
            } else {
                throw new Error('상담방 생성 응답이 올바르지 않습니다.');
            }
        } catch (error) {
            console.error('상담원 연결 실패:', error);
            connectingMsg.textContent = '상담원 연결에 실패했습니다. 다시 시도해주세요.';
            connectingMsg.style.background = '#ffebee';
            connectingMsg.style.color = '#c62828';
            
            // 메뉴 다시 표시
            setTimeout(() => {
                connectingMsg.remove();
                this.showMenu();
            }, 3000);
        }
    }
    
    async endConsultation() {
        if (this.currentRoomId) {
            try {
                await fetch(`/api/chat/rooms/${this.currentRoomId}/close`, {
                    method: 'POST'
                });
            } catch (e) {
                console.error('상담 종료 실패:', e);
            }
        }
        
        this.isInConsultation = false;
        this.isAgentJoined = false;
        this.currentRoomId = null;
        this.clearInactivityTimers();
        this.setInputEnabled(true);
        
        // UI 초기화
        const titleEl = document.querySelector('.chatbot-title');
        if (titleEl) titleEl.textContent = '챗봇 상담';
        if (this.endBtn) this.endBtn.style.display = 'none';
        
        // 메시지 영역 초기화 + 메뉴 표시
        this.clearMessages();
        this.showMenu();
        
        // 종료 메시지
        const endMsg = document.createElement('div');
        endMsg.className = 'chat-system-message';
        endMsg.textContent = '상담이 종료되었습니다.';
        endMsg.style.cssText = `
            padding: 10px 14px;
            margin: 8px 0;
            background: #f5f5f5;
            color: #999;
            border-radius: 8px;
            font-size: 13px;
            text-align: center;
        `;
        this.body.appendChild(endMsg);
        setTimeout(() => endMsg.remove(), 3000);
    }
    
    /**
     * 상대방이 상담을 종료했을 때 호출
     * 3초 후 자동으로 챗봇 메인 화면으로 복귀
     */
    handleRemoteClose() {
        // 이미 종료 처리 중이면 무시
        if (!this.isInConsultation) return;
        
        this.isInConsultation = false;
        this.isAgentJoined = false;
        this.currentRoomId = null;
        this.clearInactivityTimers();
        this.setInputEnabled(true);
        
        setTimeout(() => {
            const titleEl = document.querySelector('.chatbot-title');
            if (titleEl) titleEl.textContent = '챗봇 상담';
            if (this.endBtn) this.endBtn.style.display = 'none';
            
            this.clearMessages();
            this.showMenu();
        }, 3000);
    }
    
    async loadRoomMessages(roomId) {
        try {
            const response = await fetch(`/api/chat/rooms/${roomId}/messages?size=30`);
            if (response.ok) {
                const apiResponse = await response.json();
                if (apiResponse.success && apiResponse.data && apiResponse.data.content) {
                    this.loadMessageHistory(apiResponse.data.content);
                }
            }
        } catch (e) {
            console.error('메시지 로드 실패:', e);
        }
    }
    
    showLoginRequired() {
        // 로그인 필요 메시지 표시
        const loginMessage = document.createElement('div');
        loginMessage.className = 'chat-login-message';
        loginMessage.innerHTML = `
            <p style="margin-bottom: 10px; font-size: 14px; color: #333;">로그인이 필요한 서비스입니다.</p>
            <button class="login-redirect-btn" style="
                padding: 8px 20px;
                background: #1a1a1a;
                color: white;
                border: none;
                border-radius: 6px;
                cursor: pointer;
                font-size: 13px;
                font-weight: 600;
            ">로그인하기</button>
        `;
        loginMessage.style.cssText = `
            padding: 20px;
            margin: 8px 0;
            background: #f8f9fa;
            border: 1px solid #e9ecef;
            border-radius: 8px;
            text-align: center;
        `;
        
        const loginBtn = loginMessage.querySelector('.login-redirect-btn');
        if (loginBtn) {
            loginBtn.addEventListener('click', () => {
                window.location.href = '/login';
            });
        }
        
        this.body.appendChild(loginMessage);
        this.scrollToBottom();
        
        // 5초 후 메시지 제거
        setTimeout(() => loginMessage.remove(), 5000);
    }
    
    clearMessages() {
        if (!this.body) return;
        // 메뉴는 유지하고 메시지만 제거
        const menu = document.getElementById('chatbot-menu');
        this.body.innerHTML = '';
        if (menu) {
            this.body.appendChild(menu);
        }
    }
    
    loadMessageHistory(messages) {
        // API는 최신순(DESC)으로 반환하므로, 채팅 표시를 위해 시간순(ASC)으로 뒤집음
        const sorted = [...messages].reverse();
        sorted.forEach(message => {
            const isOwnMessage = this.isOwnMessage(message);
            this.renderMessage(message, isOwnMessage, true);
        });
        
        this.scrollToBottom();
    }
    
    // === 대기 메시지 ===
    showWaitingMessage() {
        if (!this.body) return;
        const waitingEl = document.createElement('div');
        waitingEl.className = 'chat-waiting-message';
        waitingEl.textContent = '상담원 연결 대기 중입니다...';
        this.body.appendChild(waitingEl);
        this.scrollToBottom();
    }
    
    // === 입력 활성화/비활성화 ===
    setInputEnabled(enabled) {
        if (this.input) {
            this.input.disabled = !enabled;
            this.input.placeholder = enabled ? '메시지를 입력하세요' : '상담원 연결을 기다리는 중...';
        }
    }
    
    // === 알림 점 ===
    showNotificationDot() {
        const dot = document.getElementById('chatbot-notification-dot');
        if (dot) dot.style.display = 'block';
    }
    
    hideNotificationDot() {
        const dot = document.getElementById('chatbot-notification-dot');
        if (dot) dot.style.display = 'none';
    }
    
    // === 비활성 타이머 (10분 자동 종료, 7분에 경고) ===
    resetInactivityTimer() {
        this.clearInactivityTimers();
        
        // 7분(420초) 후 경고 메시지
        this._warningTimer = setTimeout(() => {
            if (this.isInConsultation && this.body) {
                const warningEl = document.createElement('div');
                warningEl.className = 'chat-warning-message';
                warningEl.id = 'inactivity-warning';
                warningEl.textContent = '⚠️ 3분 후 응답이 없으면 상담이 자동 종료됩니다.';
                this.body.appendChild(warningEl);
                this.scrollToBottom();
            }
        }, 7 * 60 * 1000);
    }
    
    clearInactivityTimers() {
        if (this._warningTimer) {
            clearTimeout(this._warningTimer);
            this._warningTimer = null;
        }
        const warningEl = document.getElementById('inactivity-warning');
        if (warningEl) warningEl.remove();
    }
}

// Initialize chatbot when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    if (document.getElementById('chatbot-panel')) {
        window.chatbotPanel = new ChatbotPanel('chatbot-panel');
    }
});