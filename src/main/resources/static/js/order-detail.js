document.addEventListener('DOMContentLoaded', () => {
    const pathParts = window.location.pathname.split('/');
    const orderUid = pathParts[pathParts.length - 1];

    if (!orderUid || !orderUid.startsWith('ORD')) {
        alert('잘못된 주문 번호입니다.');
        window.location.href = '/orders';
        return;
    }

    loadOrderDetail(orderUid);

    document.getElementById('btn-cancel-order')?.addEventListener('click', () => {
        alert('취소 기능은 구현 중입니다.');
    });
});

async function loadOrderDetail(orderUid) {
    try {
        const response = await fetch(`/api/orders/${orderUid}/details`);
        if (!response.ok) {
            throw new Error('주문 상세 내역을 불러오지 못했습니다.');
        }
        const result = await response.json();
        const order = result.data;

        // 헤더 (배열 또는 문자열 처리)
        let dateObj;
        if (Array.isArray(order.orderDate)) {
            // [year, month, day, hour, minute, second]
            dateObj = new Date(order.orderDate[0], order.orderDate[1] - 1, order.orderDate[2], order.orderDate[3] || 0, order.orderDate[4] || 0);
        } else {
            dateObj = new Date(order.orderDate);
        }
        
        const dateStr = `${dateObj.getFullYear()}.${String(dateObj.getMonth() + 1).padStart(2, '0')}.${String(dateObj.getDate()).padStart(2, '0')} 결제`;
        
        document.getElementById('detail-order-date').textContent = dateStr;
        document.getElementById('detail-order-number').textContent = order.orderUid;

        // 아이템 렌더링
        const itemsContainer = document.getElementById('delivery-items');
        itemsContainer.innerHTML = '';
        
        let mappedStatus = order.status;
        if (order.status === '결제 완료') mappedStatus = '배송 완료';
        
        document.getElementById('detail-order-status').textContent = mappedStatus;

        order.items.forEach(item => {
            
            const html = `
                <div class="delivery-item">
                    <div class="delivery-item-inner">
                        <div class="delivery-item-content" style="display: flex; gap: 16px;">
                            <div class="delivery-item-img" style="flex-shrink: 0; width: 100px; height: 100px; background: #f5f5f5; border-radius: 8px; display: flex; align-items: center; justify-content: center;">
                                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
                            </div>
                            <div class="delivery-item-info" style="flex: 1;">
                                <p style="font-weight: 600; margin-bottom: 8px; font-size: 16px;">${item.productName}</p>
                                <p style="color: #666; font-size: 14px; margin-bottom: 4px;">수량 : <span>${item.quantity}</span>개</p>
                                <p style="font-size: 15px; font-weight: 500;">금액 : <span>${item.itemAmount.toLocaleString()}</span> 원</p>
                            </div>
                        </div>
                    </div>
                </div>
            `;
            itemsContainer.insertAdjacentHTML('beforeend', html);
        });

        // 주문자 정보
        if (order.ordererInfo) {
            document.getElementById('recipient-name').textContent = order.ordererInfo.name || '알 수 없음';
            document.getElementById('recipient-phone').textContent = order.ordererInfo.phone || '알 수 없음';
            document.getElementById('recipient-address').textContent = order.ordererInfo.address || '알 수 없음';
        }

        // 결제 요약
        document.getElementById('payment-product-total').innerHTML = `<span>${order.totalAmount.toLocaleString()}</span> 원`;
        document.getElementById('payment-shipping-fee').innerHTML = `<span>${order.deliveryFee.toLocaleString()}</span> 원`;
        document.getElementById('payment-grand-total').innerHTML = `<span style="font-weight:700; color:#1a1a1a;">${order.finalAmount.toLocaleString()}</span> 원`;
        
        document.getElementById('order-total-value').textContent = `${order.finalAmount.toLocaleString()}원`;
        
    } catch (e) {
        alert(e.message);
        console.error(e);
    }
}
