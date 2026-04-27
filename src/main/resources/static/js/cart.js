/**
 * ADP Commerce - Cart Page JavaScript
 */

let cartItemsData = [];

document.addEventListener('DOMContentLoaded', () => {
    loadCartItems();
    initClearCart();
    initOrderButton();
});

function loadCartItems() {
    fetch('/api/cart')
        .then(res => res.json())
        .then(data => {
            if (data.success && data.data && data.data.content) {
                cartItemsData = data.data.content;
                renderCart();
            } else {
                cartItemsData = [];
                renderCart();
            }
        })
        .catch(err => {
            console.error('장바구니 조회 실패:', err);
            cartItemsData = [];
            renderCart();
        });
}

function renderCart() {
    const itemsContainer = document.getElementById('cart-items');
    if (!itemsContainer) return;

    if (cartItemsData.length === 0) {
        itemsContainer.innerHTML = '<div class="cart-item" style="text-align:center; padding:40px 0;">장바구니가 비어있습니다.</div>';
        updateTotal();
        return;
    }

    const html = cartItemsData.map(item => {
        const hasImage = item.imageUrl && item.imageUrl !== '';
        const imageHTML = hasImage
            ? `<img src="${item.imageUrl}" alt="${item.productName}" class="item-img" loading="lazy">`
            : `<span class="item-img-placeholder">상품 이미지</span>`;

        return `
            <div class="cart-item" id="cart-item-${item.cartProductId}">
                <div class="cart-item-inner">
                    <div class="cart-item-img">
                        ${imageHTML}
                    </div>
                    <div class="cart-item-info">
                        <h3 class="cart-item-name">${item.productName}</h3>
                        <div class="cart-item-qty">
                            <button class="qty-btn qty-minus" data-id="${item.cartProductId}">−</button>
                            <input type="number" class="qty-value-input" data-id="${item.cartProductId}" value="${item.quantity}" min="1" style="width: 40px; text-align: center; border: 1px solid #ccc; border-radius: 4px; margin: 0 8px;">
                            <button class="qty-btn qty-plus" data-id="${item.cartProductId}">+</button>
                        </div>
                    </div>
                    <div class="cart-item-right">
                        <span class="cart-item-price">금액: <strong>${formatPrice(item.subtotal)}</strong> 원</span>
                        <button class="cart-item-remove" data-id="${item.cartProductId}">✕</button>
                    </div>
                </div>
            </div>
        `;
    }).join('');

    itemsContainer.innerHTML = html;
    updateTotal();
    attachCartEvents();
}

function attachCartEvents() {
    document.querySelectorAll('.qty-minus').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            const item = cartItemsData.find(i => i.cartProductId == id);
            if (item && item.quantity > 1) {
                updateQuantity(id, item.quantity - 1);
            }
        });
    });

    document.querySelectorAll('.qty-plus').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            const item = cartItemsData.find(i => i.cartProductId == id);
            if (item) {
                updateQuantity(id, item.quantity + 1);
            }
        });
    });

    document.querySelectorAll('.qty-value-input').forEach(input => {
        input.addEventListener('change', (e) => {
            const id = e.target.getAttribute('data-id');
            const val = parseInt(e.target.value, 10);
            if (val > 0) {
                updateQuantity(id, val);
            } else {
                e.target.value = 1;
                updateQuantity(id, 1);
            }
        });
    });

    document.querySelectorAll('.cart-item-remove').forEach(btn => {
        btn.addEventListener('click', () => {
            const id = btn.getAttribute('data-id');
            deleteCartItem(id);
        });
    });
}

function updateQuantity(cartProductId, quantity) {
    fetch(`/api/cart/${cartProductId}`, {
        method: 'PATCH',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ quantity })
    }).then(res => {
        if (res.ok) {
            loadCartItems();
        } else {
            alert('수량 변경에 실패했습니다.');
        }
    }).catch(err => {
        console.error('수량 변경 에러:', err);
    });
}

function deleteCartItem(cartProductId) {
    if (!confirm('상품을 장바구니에서 삭제하시겠습니까?')) return;

    fetch(`/api/cart/${cartProductId}`, {
        method: 'DELETE'
    }).then(res => {
        if (res.ok) {
            loadCartItems();
            // Update badge
            if (typeof initCartBadge === 'function') initCartBadge();
        } else {
            alert('삭제에 실패했습니다.');
        }
    }).catch(err => {
        console.error('삭제 에러:', err);
    });
}

function initClearCart() {
    const clearBtn = document.getElementById('cart-clear-btn');
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            if (cartItemsData.length === 0) return;
            if (confirm('장바구니를 모두 비우시겠습니까?')) {
                fetch('/api/cart', { method: 'DELETE' })
                    .then(res => {
                        if (res.ok) {
                            loadCartItems();
                            if (typeof initCartBadge === 'function') initCartBadge();
                        } else {
                            alert('장바구니 비우기에 실패했습니다.');
                        }
                    });
            }
        });
    }
}

function initOrderButton() {
    const orderBtn = document.getElementById('cart-order-btn');
    if (orderBtn) {
        orderBtn.addEventListener('click', () => {
            if (cartItemsData.length === 0) {
                alert('장바구니가 비어있습니다.');
                return;
            }

            const orderItems = cartItemsData.map(item => ({
                productId: item.productId,
                quantity: item.quantity
            }));

            // 로딩 상태 처리
            orderBtn.disabled = true;
            orderBtn.textContent = '주문 처리 중...';

            fetch('/api/orders', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ orderItems })
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    window.location.href = `/checkout?orderUid=${data.data.orderUid}`;
                } else {
                    alert('주문 생성에 실패했습니다: ' + (data.message || '알 수 없는 오류'));
                    orderBtn.disabled = false;
                    orderBtn.textContent = '주문하기';
                }
            })
            .catch(err => {
                console.error('주문 실패:', err);
                alert('주문 생성 중 오류가 발생했습니다.');
                orderBtn.disabled = false;
                orderBtn.textContent = '주문하기';
            });
        });
    }
}

function updateTotal() {
    const totalEl = document.getElementById('cart-total-value');
    if (totalEl) {
        const sum = cartItemsData.reduce((acc, item) => acc + (item.subtotal || 0), 0);
        totalEl.innerHTML = `<strong>${formatPrice(sum)}</strong> 원`;
    }
}

function formatPrice(price) {
    if (!price) return '0';
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}
