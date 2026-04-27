/**
 * ADP Commerce - Orders Page JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    loadOrders();
});

let cursorId = null;
let isLoading = false;

function loadOrders() {
    if (isLoading) return;
    isLoading = true;
    
    const container = document.getElementById('order-list-container');
    const loading = document.getElementById('order-loading');
    
    if (loading) loading.style.display = 'block';
    
    const url = cursorId ? `/api/orders?cursorId=${cursorId}&size=10` : `/api/orders?size=10`;
    
    fetch(url)
        .then(res => res.json())
        .then(data => {
            if (data.success && data.data) {
                const orders = data.data.content;
                cursorId = data.data.nextCursorId; // 다음 페이지 커서
                
                if (!orders || orders.length === 0) {
                    if (!cursorId && container.innerHTML.trim() === '') {
                        container.innerHTML = '<div style="text-align:center; padding: 40px; color: #999;">주문 내역이 없습니다.</div>';
                    }
                } else {
                    renderOrders(orders, container);
                    initShippingInfoButtons();
                    initOrderActionButtons();
                }
            } else {
                if (container.innerHTML.trim() === '') {
                    container.innerHTML = '<div style="text-align:center; padding: 40px; color: #999;">주문 내역을 불러오지 못했습니다.</div>';
                }
            }
        })
        .catch(err => {
            console.error(err);
            if (container.innerHTML.trim() === '') {
                container.innerHTML = '<div style="text-align:center; padding: 40px; color: #999;">서버 오류가 발생했습니다.</div>';
            }
        })
        .finally(() => {
            isLoading = false;
            if (loading) loading.style.display = 'none';
        });
}

function renderOrders(orders, container) {
    let html = '';
    
    orders.forEach(order => {
        // Map status based on requirements
        let mappedStatus = order.status;
        if (order.status === '결제 완료') {
            mappedStatus = '배송 완료';
        }
        
        // Setup data-status for filtering
        let filterCategory = 'COMPLETED'; // 기본값 (배송 중/완료 등)
        if (order.status === '결제 대기') {
            filterCategory = 'PENDING_PAYMENT';
        } else if (order.status === '구매 확정') {
            filterCategory = 'CONFIRMED';
        } else if (order.status === '환불 처리 중' || order.status === '환불 완료' || order.status === '주문 취소됨') {
            filterCategory = 'REFUNDED';
        }

        const dateObj = new Date(order.orderDate);
        const dateStr = `${dateObj.getFullYear()}.${String(dateObj.getMonth() + 1).padStart(2, '0')}.${String(dateObj.getDate()).padStart(2, '0')} 결제`;
        
        let actionBtnsHtml = '';
        if (filterCategory === 'PENDING_PAYMENT') {
            actionBtnsHtml = `
                <button class="btn-order-action btn-pay" data-uid="${order.orderUid}" style="padding: 6px 12px; border: 1px solid #1a1a1a; background: #1a1a1a; color: #fff; border-radius: 4px; font-size: 12px; cursor: pointer;">결제하기</button>
                <button class="btn-order-action btn-cancel" style="padding: 6px 12px; border: 1px solid #ddd; background: #fff; color: #1a1a1a; border-radius: 4px; font-size: 12px; cursor: pointer;">주문 취소</button>
            `;
        } else if (mappedStatus === '배송 완료') {
            actionBtnsHtml = `
                <button class="btn-shipping-info" style="padding: 6px 12px; border: 1px solid #ddd; background: #fff; color: #1a1a1a; border-radius: 4px; font-size: 12px; cursor: pointer;">배송조회</button>
                <button class="btn-order-action btn-refund" style="padding: 6px 12px; border: 1px solid #ddd; background: #fff; color: #1a1a1a; border-radius: 4px; font-size: 12px; cursor: pointer;">환불 요청</button>
                <button class="btn-order-action btn-confirm" style="padding: 6px 12px; border: 1px solid #2e7d32; background: #2e7d32; color: #fff; border-radius: 4px; font-size: 12px; cursor: pointer;">구매확정</button>
            `;
        }
        
        let itemsHtml = '';
        if (order.items && order.items.length > 0) {
            // 최대 2개 항목만 렌더링
            const displayItems = order.items.slice(0, 2);
            
            displayItems.forEach(item => {
                itemsHtml += `
                    <div class="order-item-content" style="display: flex; align-items: center; border-top: 1px solid #eee; margin-top: 16px; padding-top: 16px; gap: 16px;">
                        <div class="order-item-img" style="flex-shrink: 0; width: 80px; height: 80px; background: #f5f5f5; border-radius: 8px; overflow: hidden; display: flex; align-items: center; justify-content: center;">
                            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                        </div>
                        <div class="order-item-info" style="flex: 1; display: flex; flex-direction: column; justify-content: center;">
                            <div class="order-item-name" style="font-weight: 500; margin-bottom: 4px;">${item.productName}</div>
                            <div class="order-item-price" style="color: #666; font-size: 14px;">
                                <span style="font-weight: 600; color: #1a1a1a;">${item.itemAmount.toLocaleString()}원</span> <span class="order-item-qty">· ${item.quantity}개</span>
                            </div>
                        </div>
                    </div>
                `;
            });
            
            // 3개 이상일 경우 축약 텍스트 추가
            if (order.items.length > 2) {
                const moreCount = order.items.length - 2;
                itemsHtml += `
                    <div style="text-align: center; padding: 12px 0; color: #666; font-size: 13px; background: #fafafa; margin-top: 12px; border-radius: 4px;">
                        외 ${moreCount}건의 상품이 더 있습니다. <a href="/orders/${order.orderUid}" style="color: #1a1a1a; font-weight: 600; text-decoration: underline;">주문 상세보기</a>
                    </div>
                `;
            }
        }
        
        html += `
            <div class="order-item" data-filter-category="${filterCategory}" style="border: 1px solid #eaeaea; border-radius: 12px; padding: 20px; margin-bottom: 20px; background: #fff; box-shadow: 0 2px 8px rgba(0,0,0,0.02);">
                <div class="order-item-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px;">
                    <div style="display: flex; align-items: center; gap: 12px;">
                        <span class="order-date" style="font-weight: 600;">${dateStr}</span>
                        <span class="order-uid" style="color: #888; font-size: 13px;">주문번호: ${order.orderUid}</span>
                        <span class="order-item-status-text" style="font-size: 13px; font-weight: 600; padding: 4px 8px; border-radius: 4px; background: ${mappedStatus === '환불 처리 중' ? '#fee' : '#f5f5f5'}; color: ${mappedStatus === '환불 처리 중' ? '#e53e3e' : '#1a1a1a'};">${mappedStatus}</span>
                    </div>
                    <div style="display: flex; align-items: center; gap: 16px;">
                        <div class="order-actions" style="display: flex; gap: 6px;">
                            ${actionBtnsHtml}
                        </div>
                        ${filterCategory !== 'PENDING_PAYMENT' ? `<a href="/orders/${order.orderUid}" class="order-detail-link" style="font-size: 13px; color: #1a1a1a; font-weight: 500;">주문 상세보기 &gt;</a>` : ''}
                    </div>
                </div>
                ${itemsHtml}
            </div>
        `;
    });
    
    container.innerHTML += html;
    applyFilter();
}
        
        // Removed original logic

function initShippingInfoButtons() {
    document.querySelectorAll('.btn-shipping-info').forEach(btn => {
        // 이미 이벤트 리스너가 달린 요소 처리 (중복 방지)
        if (btn.dataset.init) return;
        btn.dataset.init = 'true';
        
        btn.addEventListener('click', () => {
            alert('해당 주문건은 배송이 완료되었습니다.');
        });
    });
}

function initOrderActionButtons() {
    document.querySelectorAll('.btn-order-action').forEach(btn => {
        if (btn.dataset.init) return;
        btn.dataset.init = 'true';
        
        btn.addEventListener('click', () => {
            const action = btn.textContent.trim();
            const container = btn.closest('.order-actions');
            
            if (action === '결제하기') {
                const uid = btn.getAttribute('data-uid');
                window.location.href = `/checkout?orderUid=${uid}`;
            } else if (action === '주문 취소') {
                if (confirm('주문을 취소하시겠습니까?')) {
                    btn.closest('.order-item').style.opacity = '0.5';
                    if (container) container.style.display = 'none';
                    const statusEl = btn.closest('.order-item-header').querySelector('.order-item-status-text');
                    if (statusEl) {
                        statusEl.textContent = '주문 취소됨';
                        statusEl.style.color = '#999';
                        statusEl.style.background = '#f5f5f5';
                    }
                    btn.closest('.order-item').setAttribute('data-filter-category', 'REFUNDED');
                    alert('주문이 취소되었습니다.');
                    applyFilter();
                }
            } else if (action === '환불 요청') {
                if (confirm('환불을 요청하시겠습니까?')) {
                    if (container) container.style.display = 'none';
                    const statusEl = btn.closest('.order-item-header').querySelector('.order-item-status-text');
                    if (statusEl) {
                        statusEl.textContent = '환불 처리 중';
                        statusEl.style.color = '#e53e3e';
                        statusEl.style.background = '#fee';
                    }
                    btn.closest('.order-item').setAttribute('data-filter-category', 'REFUNDED');
                    applyFilter();
                }
            } else if (action === '구매확정') {
                if (confirm('구매확정하시겠습니까? 구매확정 시 환불이 불가능합니다.')) {
                    if (container) container.style.display = 'none';
                    const statusEl = btn.closest('.order-item-header').querySelector('.order-item-status-text');
                    if (statusEl) {
                        statusEl.textContent = '구매 확정';
                        statusEl.style.color = '#1a1a1a';
                        statusEl.style.background = '#f5f5f5';
                    }
                    btn.closest('.order-item').setAttribute('data-filter-category', 'CONFIRMED');
                    applyFilter();
                }
            }
        });
    });
}

let currentFilter = 'all';

document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            document.querySelectorAll('.filter-tab').forEach(t => {
                t.classList.remove('active');
                t.style.background = 'white';
                t.style.color = '#666';
                t.style.borderColor = '#ccc';
            });
            const target = e.target;
            target.classList.add('active');
            target.style.background = '#1a1a1a';
            target.style.color = 'white';
            target.style.borderColor = '#1a1a1a';
            
            currentFilter = target.getAttribute('data-filter');
            applyFilter();
        });
    });
});

function applyFilter() {
    document.querySelectorAll('.order-item').forEach(item => {
        if (currentFilter === 'all') {
            item.style.display = 'block';
        } else {
            const cat = item.getAttribute('data-filter-category');
            if (cat === currentFilter) {
                item.style.display = 'block';
            } else {
                item.style.display = 'none';
            }
        }
    });
}

// 스크롤 시 추가 로드 (무한 스크롤)
window.addEventListener('scroll', () => {
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 500) {
        if (cursorId && !isLoading) {
            loadOrders();
        }
    }
});
