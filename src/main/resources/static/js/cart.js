/**
 * ADP Commerce - Cart Page JavaScript
 * 장바구니 API 연동: 조회, 수량 변경, 삭제, 비우기, 주문하기
 */

document.addEventListener('DOMContentLoaded', () => {
    loadCartItems();
    initClearCart();
    initOrderButton();
});

/* ===========================
   State
   =========================== */
let cartItems = [];

/* ===========================
   장바구니 상품 로드
   =========================== */
async function loadCartItems() {
    try {
        const res = await fetch('/api/cart?size=100', {
            method: 'GET',
            credentials: 'include'
        });

        if (res.status === 401 || res.status === 403) {
            // 비로그인 시 기존 더미가 표시됨
            return;
        }

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const json = await res.json();
        if (!json.success) throw new Error('장바구니 조회 실패');

        cartItems = json.data.content || [];
        renderCartItems(cartItems);
        updateCartTotal();
    } catch (err) {
        console.error('장바구니 로드 실패:', err);
    }
}

/* ===========================
   장바구니 렌더링
   =========================== */
function renderCartItems(items) {
    const container = document.getElementById('cart-items');
    if (!container) return;

    container.innerHTML = '';

    if (items.length === 0) {
        container.innerHTML = `
            <div class="cart-empty" style="text-align:center;padding:60px 0;color:#999;">
                <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#ccc" stroke-width="1.5" style="margin-bottom:16px;">
                    <path d="M6 2L3 6v14a2 2 0 002 2h14a2 2 0 002-2V6l-3-4z"/>
                    <line x1="3" y1="6" x2="21" y2="6"/>
                    <path d="M16 10a4 4 0 01-8 0"/>
                </svg>
                <p style="font-size:16px;font-weight:600;margin-bottom:8px;">장바구니가 비어있습니다</p>
                <p style="font-size:14px;">상품을 추가해보세요</p>
            </div>
        `;
        return;
    }

    items.forEach(item => {
        const itemEl = document.createElement('div');
        itemEl.className = 'cart-item';
        itemEl.id = `cart-item-${item.cartProductId}`;

        itemEl.innerHTML = `
            <div class="cart-item-inner">
                <div class="cart-item-img">
                    <span class="item-img-placeholder">상품 이미지</span>
                </div>
                <div class="cart-item-info">
                    <h3 class="cart-item-name">${escapeHtml(item.productName)}</h3>
                    <div class="cart-item-qty">
                        <button class="qty-btn qty-minus" data-id="${item.cartProductId}">−</button>
                        <span class="qty-value">${item.quantity}</span>
                        <button class="qty-btn qty-plus" data-id="${item.cartProductId}">+</button>
                    </div>
                </div>
                <div class="cart-item-right">
                    <span class="cart-item-price">금액: <strong>${formatPrice(item.subtotal)}</strong> 원</span>
                    <button class="cart-item-remove" data-id="${item.cartProductId}">✕</button>
                </div>
            </div>
        `;

        container.appendChild(itemEl);
    });

    // 이벤트 바인딩
    initQuantityButtons();
    initRemoveButtons();
}

/* ===========================
   수량 변경
   =========================== */
function initQuantityButtons() {
    document.querySelectorAll('.qty-minus').forEach(btn => {
        btn.addEventListener('click', async () => {
            const cartProductId = btn.dataset.id;
            const valueEl = btn.parentElement.querySelector('.qty-value');
            let val = parseInt(valueEl.textContent);
            if (val > 1) {
                const newQty = val - 1;
                try {
                    const res = await fetch(`/api/cart/${cartProductId}`, {
                        method: 'PATCH',
                        credentials: 'include',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ quantity: newQty })
                    });

                    if (res.ok) {
                        valueEl.textContent = newQty;
                        updateItemInState(cartProductId, newQty);
                        updateCartTotal();
                    }
                } catch (err) {
                    console.error('수량 변경 실패:', err);
                }
            }
        });
    });

    document.querySelectorAll('.qty-plus').forEach(btn => {
        btn.addEventListener('click', async () => {
            const cartProductId = btn.dataset.id;
            const valueEl = btn.parentElement.querySelector('.qty-value');
            let val = parseInt(valueEl.textContent);
            const newQty = val + 1;
            try {
                const res = await fetch(`/api/cart/${cartProductId}`, {
                    method: 'PATCH',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ quantity: newQty })
                });

                if (res.ok) {
                    valueEl.textContent = newQty;
                    updateItemInState(cartProductId, newQty);
                    updateCartTotal();
                }
            } catch (err) {
                console.error('수량 변경 실패:', err);
            }
        });
    });
}

function updateItemInState(cartProductId, newQty) {
    const item = cartItems.find(i => String(i.cartProductId) === String(cartProductId));
    if (item) {
        item.quantity = newQty;
        item.subtotal = item.price * newQty;

        // UI 업데이트
        const itemEl = document.getElementById(`cart-item-${cartProductId}`);
        if (itemEl) {
            const priceEl = itemEl.querySelector('.cart-item-price strong');
            if (priceEl) priceEl.textContent = formatPrice(item.subtotal);
        }
    }
}

/* ===========================
   상품 삭제
   =========================== */
function initRemoveButtons() {
    document.querySelectorAll('.cart-item-remove').forEach(btn => {
        btn.addEventListener('click', async () => {
            const cartProductId = btn.dataset.id;
            const item = btn.closest('.cart-item');

            try {
                const res = await fetch(`/api/cart/${cartProductId}`, {
                    method: 'DELETE',
                    credentials: 'include'
                });

                if (res.ok) {
                    if (item) {
                        item.style.opacity = '0';
                        item.style.transform = 'translateX(20px)';
                        item.style.transition = 'all 0.3s ease';
                        setTimeout(() => {
                            item.remove();
                            cartItems = cartItems.filter(i => String(i.cartProductId) !== String(cartProductId));
                            updateCartTotal();

                            if (cartItems.length === 0) {
                                renderCartItems([]);
                            }
                        }, 300);
                    }
                }
            } catch (err) {
                console.error('상품 삭제 실패:', err);
            }
        });
    });
}

/* ===========================
   장바구니 비우기
   =========================== */
function initClearCart() {
    const clearBtn = document.getElementById('cart-clear-btn');
    if (clearBtn) {
        clearBtn.addEventListener('click', async () => {
            if (!confirm('장바구니를 비우시겠습니까?')) return;

            try {
                const res = await fetch('/api/cart', {
                    method: 'DELETE',
                    credentials: 'include'
                });

                if (res.ok) {
                    cartItems = [];
                    renderCartItems([]);
                    updateCartTotal();
                }
            } catch (err) {
                console.error('장바구니 비우기 실패:', err);
            }
        });
    }
}

/* ===========================
   주문하기
   =========================== */
function initOrderButton() {
    const orderBtn = document.getElementById('cart-order-btn');
    if (orderBtn) {
        orderBtn.addEventListener('click', async () => {
            if (cartItems.length === 0) {
                alert('장바구니가 비어있습니다.');
                return;
            }

            orderBtn.disabled = true;
            orderBtn.textContent = '주문 생성 중...';
            orderBtn.style.opacity = '0.6';

            try {
                const orderItems = cartItems.map(item => ({
                    productId: item.productId,
                    quantity: item.quantity
                }));

                const res = await fetch('/api/orders', {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ orderItems })
                });

                if (res.status === 401 || res.status === 403) {
                    alert('로그인이 필요합니다.');
                    window.location.href = '/login';
                    return;
                }

                if (!res.ok) throw new Error(`HTTP ${res.status}`);

                const json = await res.json();
                if (!json.success) throw new Error('주문 생성 실패');

                const orderUid = json.data.orderUid;

                // 장바구니 비우기
                await fetch('/api/cart', {
                    method: 'DELETE',
                    credentials: 'include'
                });

                window.location.href = `/checkout/${orderUid}`;
            } catch (err) {
                console.error('주문 생성 실패:', err);
                alert('주문 생성에 실패했습니다. 다시 시도해주세요.');

                orderBtn.disabled = false;
                orderBtn.textContent = '주문하기';
                orderBtn.style.opacity = '';
            }
        });
    }
}

/* ===========================
   합계 업데이트
   =========================== */
function updateCartTotal() {
    const totalEl = document.getElementById('cart-total-value');
    if (!totalEl) return;

    const total = cartItems.reduce((sum, item) => sum + (item.subtotal || 0), 0);
    totalEl.innerHTML = `${formatPrice(total)} 원`;
}

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
