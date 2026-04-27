/**
 * Admin Consultation Page
 * 관리자 상담 목록 관리 및 실시간 업데이트
 */

let stompClient = null;
let currentStatus = 'ALL';
let currentPage = 0;
const pageSize = 20;

document.addEventListener('DOMContentLoaded', () => {
    initStatusTabs();
    loadConsultations();
    connectWebSocket();
});

/* ===========================
   Status Tabs
   =========================== */
function initStatusTabs() {
    const tabs = document.querySelectorAll('.status-tab');
    
    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // Update active state
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            
            // Update current status and reload
            currentStatus = tab.dataset.status;
            currentPage = 0;
            loadConsultations();
        });
    });
}

/* ===========================
   Load Consultations
   =========================== */
async function loadConsultations() {
    const listContainer = document.getElementById('consultation-list');
    const emptyState = document.getElementById('consultation-empty');
    const paginationContainer = document.getElementById('pagination-container');
    
    // Show loading
    listContainer.innerHTML = `
        <div class="consultation-loading">
            <div class="loading-spinner"></div>
            <p>상담 목록을 불러오는 중...</p>
        </div>
    `;
    emptyState.style.display = 'none';
    paginationContainer.style.display = 'none';
    
    try {
        // Build API URL
        let url = `/api/chat/admin/rooms?page=${currentPage}&size=${pageSize}&sort=createdAt,desc`;
        if (currentStatus !== 'ALL') {
            url += `&chatRoomStatus=${currentStatus}`;
        }
        
        const response = await fetch(url);
        const result = await response.json();
        
        if (!result.success) {
            throw new Error(result.message || '상담 목록을 불러오는데 실패했습니다');
        }
        
        const pageData = result.data;
        const consultations = pageData.content || [];
        
        // Update counts
        updateStatusCounts();
        
        // Render consultations
        if (consultations.length === 0) {
            listContainer.innerHTML = '';
            emptyState.style.display = 'flex';
        } else {
            renderConsultations(consultations);
            emptyState.style.display = 'none';
            
            // Render pagination if needed
            if (pageData.totalPages > 1) {
                renderPagination(pageData);
                paginationContainer.style.display = 'flex';
            }
        }
    } catch (error) {
        console.error('상담 목록 로드 실패:', error);
        listContainer.innerHTML = `
            <div class="consultation-empty">
                <p class="empty-title">상담 목록을 불러오는데 실패했습니다</p>
                <p class="empty-subtitle">${error.message}</p>
            </div>
        `;
    }
}

/* ===========================
   Render Consultations
   =========================== */
function renderConsultations(consultations) {
    const listContainer = document.getElementById('consultation-list');
    
    listContainer.innerHTML = consultations.map(consultation => {
        const statusClass = consultation.chatRoomStatus.toLowerCase().replace('_', '-');
        const statusText = getStatusText(consultation.chatRoomStatus);
        const timeAgo = getTimeAgo(consultation.lastMessageAt || consultation.createdAt);
        
        return `
            <div class="consultation-item" data-room-id="${consultation.id}">
                <div class="consultation-main">
                    <span class="consultation-status-badge ${statusClass}">
                        ${statusText}
                    </span>
                    <div class="consultation-info">
                        <div class="consultation-title">${escapeHtml(consultation.title)}</div>
                        <div class="consultation-meta">
                            <span class="consultation-customer">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <path d="M20 21v-2a4 4 0 00-4-4H8a4 4 0 00-4 4v2"/>
                                    <circle cx="12" cy="7" r="4"/>
                                </svg>
                                고객 ID: ${consultation.userId}${consultation.userEmail ? ' (' + escapeHtml(consultation.userEmail) + ')' : ''}
                            </span>
                            <span class="consultation-time">
                                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                                    <circle cx="12" cy="12" r="10"/>
                                    <polyline points="12 6 12 12 16 14"/>
                                </svg>
                                ${timeAgo}
                            </span>
                        </div>
                    </div>
                </div>
                <div class="consultation-actions">
                    ${consultation.chatRoomStatus === 'WAITING' ? 
                        `<button class="consultation-action-btn" onclick="joinConsultation(${consultation.id})">
                            상담 시작
                        </button>` : 
                        `<button class="consultation-action-btn secondary" onclick="openConsultation(${consultation.id})">
                            상담 보기
                        </button>`
                    }
                </div>
            </div>
        `;
    }).join('');
    
    // Add click handlers to items
    document.querySelectorAll('.consultation-item').forEach(item => {
        item.addEventListener('click', (e) => {
            // Don't trigger if clicking on button
            if (e.target.closest('button')) return;
            
            const roomId = item.dataset.roomId;
            openConsultation(roomId);
        });
    });
}

/* ===========================
   Update Status Counts
   =========================== */
async function updateStatusCounts() {
    try {
        // Fetch counts for each status
        const statuses = ['ALL', 'WAITING', 'IN_PROGRESS', 'COMPLETED'];
        
        for (const status of statuses) {
            let url = `/api/chat/admin/rooms?page=0&size=1`;
            if (status !== 'ALL') {
                url += `&chatRoomStatus=${status}`;
            }
            
            const response = await fetch(url);
            const result = await response.json();
            
            if (result.success) {
                const count = result.data.totalElements || 0;
                const countEl = document.getElementById(`count-${status.toLowerCase().replace('_', '-')}`);
                if (countEl) {
                    countEl.textContent = count;
                }
            }
        }
    } catch (error) {
        console.error('상태별 카운트 업데이트 실패:', error);
    }
}

/* ===========================
   Pagination
   =========================== */
function renderPagination(pageData) {
    const container = document.getElementById('pagination-container');
    const currentPage = pageData.number;
    const totalPages = pageData.totalPages;
    
    let paginationHTML = '<div class="pagination">';
    
    // Previous button
    paginationHTML += `
        <button class="pagination-btn" ${currentPage === 0 ? 'disabled' : ''} 
                onclick="goToPage(${currentPage - 1})">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="15 18 9 12 15 6"/>
            </svg>
        </button>
    `;
    
    // Page numbers
    const maxVisible = 5;
    let startPage = Math.max(0, currentPage - Math.floor(maxVisible / 2));
    let endPage = Math.min(totalPages - 1, startPage + maxVisible - 1);
    
    if (endPage - startPage < maxVisible - 1) {
        startPage = Math.max(0, endPage - maxVisible + 1);
    }
    
    if (startPage > 0) {
        paginationHTML += `<button class="pagination-btn" onclick="goToPage(0)">1</button>`;
        if (startPage > 1) {
            paginationHTML += `<span class="pagination-ellipsis">...</span>`;
        }
    }
    
    for (let i = startPage; i <= endPage; i++) {
        paginationHTML += `
            <button class="pagination-btn ${i === currentPage ? 'active' : ''}" 
                    onclick="goToPage(${i})">
                ${i + 1}
            </button>
        `;
    }
    
    if (endPage < totalPages - 1) {
        if (endPage < totalPages - 2) {
            paginationHTML += `<span class="pagination-ellipsis">...</span>`;
        }
        paginationHTML += `<button class="pagination-btn" onclick="goToPage(${totalPages - 1})">${totalPages}</button>`;
    }
    
    // Next button
    paginationHTML += `
        <button class="pagination-btn" ${currentPage === totalPages - 1 ? 'disabled' : ''} 
                onclick="goToPage(${currentPage + 1})">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <polyline points="9 18 15 12 9 6"/>
            </svg>
        </button>
    `;
    
    paginationHTML += '</div>';
    container.innerHTML = paginationHTML;
}

function goToPage(page) {
    currentPage = page;
    loadConsultations();
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

/* ===========================
   WebSocket Connection
   =========================== */
function connectWebSocket() {
    const socket = new SockJS('/ws-chat');
    stompClient = Stomp.over(socket);
    
    // Disable debug logging
    stompClient.debug = null;
    
    stompClient.connect({}, (frame) => {
        console.log('WebSocket 연결 성공:', frame);
        
        // Subscribe to admin consultation updates
        stompClient.subscribe('/sub/admin/consultations', (message) => {
            console.log('새로운 상담 업데이트:', message.body);
            
            // 뱃지 업데이트 (chatbotPanel 활성 방 수 재조회 위임)
            if (window.chatbotPanel) {
                window.chatbotPanel.initAdminBadge();
            }
            
            // Reload consultations when update received
            loadConsultations();
        });
    }, (error) => {
        console.error('WebSocket 연결 실패:', error);
        
        // Retry connection after 5 seconds
        setTimeout(() => {
            console.log('WebSocket 재연결 시도...');
            connectWebSocket();
        }, 5000);
    });
}

/* ===========================
   Consultation Actions
   =========================== */
async function joinConsultation(roomId) {
    try {
        const response = await fetch(`/api/chat/admin/rooms/${roomId}/join`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }
        });
        
        const result = await response.json();
        
        if (!result.success) {
            throw new Error(result.message || '상담 참여에 실패했습니다');
        }
        
        // Open consultation after joining
        openConsultation(roomId);
    } catch (error) {
        console.error('상담 참여 실패:', error);
        alert(error.message);
    }
}

function openConsultation(roomId) {
    if (window.chatbotPanel) {
        window.chatbotPanel.openAdminConsultation(roomId);
    } else {
        alert('챗봇 패널이 초기화되지 않았습니다.');
    }
}

/* ===========================
   Utility Functions
   =========================== */
function getStatusText(status) {
    const statusMap = {
        'WAITING': '대기중',
        'IN_PROGRESS': '상담중',
        'COMPLETED': '완료'
    };
    return statusMap[status] || status;
}

function getTimeAgo(dateString) {
    if (!dateString) return '';
    // Append 'Z' to treat as UTC if it's missing
    const isoString = dateString.endsWith('Z') ? dateString : dateString + 'Z';
    const date = new Date(isoString);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);
    
    if (diffMins < 1 || diffMs < 0) return '방금 전';
    if (diffMins < 60) return `${diffMins}분 전`;
    if (diffHours < 24) return `${diffHours}시간 전`;
    if (diffDays < 7) return `${diffDays}일 전`;
    
    return date.toLocaleDateString('ko-KR', { 
        year: 'numeric', 
        month: 'long', 
        day: 'numeric' 
    });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
