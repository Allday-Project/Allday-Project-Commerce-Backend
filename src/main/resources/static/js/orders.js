/**
 * ADP Commerce - Orders Page JavaScript
 * 주문 목록 API 연동: 조회, 무한 스크롤
 */

document.addEventListener('DOMContentLoaded', () => {
    loadOrders();
});

/* ===========================
   State
   =========================== */
let currentCursorId = null;
let isLoading = false;
let hasMore = true;

/* ===========================
   주문 목록 로드
   =========================== */
async function loadOrders() {
    if (isLoading || !hasMore) return;
    isLoading = true;

    try {
        let url = '/api/orders?size=10';
        if (currentCursorId) {
            url += `&cursorId=${currentCursorId}`;
        }

        const res = await fetch(url, {
            method: 'GET',
            credentials: 'include'
        });

        if (res.status === 401 || res.status === 403) {
            // 비로그인 상태
            return;
        }

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const json = await res.json();
        if (!json.success) throw new Error('주문 목록 조회 실패');

        const data = json.data;
        const orders = data.content || [];

        if (orders.length === 0) {
            hasMore = false;
            const container = document.getElementById('orders-list') || document.querySelector('.orders-list');
            if (container && !currentCursorId) {
                container.innerHTML = `
                    <div style="text-align:center;padding:60px 0;color:#999;">
                        <p style="font-size:16px;font-weight:600;margin-bottom:8px;">주문 내역이 없습니다</p>
                        <p style="font-size:14px;">상품을 구매해보세요</p>
                    </div>
                `;
            }
            return;
        }

        renderOrders(orders);

        // 커서 업데이트
        if (data.hasNext === false) {
            hasMore = false;
        } else {
            const lastOrder = orders[orders.length - 1];
            if (lastOrder && lastOrder.orderId) {
                currentCursorId = lastOrder.orderId;
            }
        }
    } catch (err) {
        console.error('주문 목록 로드 실패:', err);
    } finally {
        isLoading = false;
    }
}

function renderOrders(orders) {
    const container = document.getElementById('orders-list') || document.querySelector('.orders-list');
    if (!container) return;

    // 더미 데이터 제거 (첫 로드 시)
    if (!currentCursorId) {
        const existingDummy = container.querySelectorAll('[id^="order-demo"]');
        existingDummy.forEach(el => el.remove());
    }

    orders.forEach(order => {
        const orderEl = document.createElement('div');
        orderEl.className = 'order-group';
        orderEl.id = `order-${order.orderId || order.orderUid}`;

        const statusBadge = getStatusBadge(order.status);
        const formattedDate = order.orderDate ? new Date(order.orderDate).toLocaleDateString('ko-KR') : '';

        orderEl.innerHTML = `
            <div class="order-group-header">
                <div class="order-group-info">
                    <span class="order-group-date">${formattedDate}</span>
                    <span class="order-group-id">${escapeHtml(order.orderUid || '')}</span>
                </div>
                <span class="order-status ${statusBadge.class}">${statusBadge.text}</span>
            </div>
            <div class="order-group-body">
                <div class="order-summary">
                    <span class="order-total">총 금액: ${formatPrice(order.totalAmount)}원</span>
                </div>
                <div class="order-actions">
                    <a href="/orders/${order.orderId || ''}" class="btn-order-detail">상세보기</a>
                </div>
            </div>
        `;

        container.appendChild(orderEl);
    });
}

function getStatusBadge(status) {
    const map = {
        'PENDING': { text: '결제 대기', class: 'status-pending' },
        'PAID': { text: '결제 완료', class: 'status-paid' },
        'SHIPPED': { text: '배송 중', class: 'status-shipped' },
        'DELIVERED': { text: '배송 완료', class: 'status-delivered' },
        'CANCELLED': { text: '취소', class: 'status-cancelled' },
        'REFUNDED': { text: '환불 완료', class: 'status-refunded' }
    };
    return map[status] || { text: status || '알 수 없음', class: '' };
}

/* ===========================
   무한 스크롤
   =========================== */
window.addEventListener('scroll', () => {
    if (window.innerHeight + window.scrollY >= document.body.offsetHeight - 200) {
        loadOrders();
    }
});

/* ===========================
   유틸리티
   =========================== */
function formatPrice(num) {
    if (num == null) return '0';
    return Number(num).toLocaleString('ko-KR');
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
