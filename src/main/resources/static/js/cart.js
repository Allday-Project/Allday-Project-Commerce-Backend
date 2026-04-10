/**
 * ADP Commerce - Cart Page JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    initQuantityButtons();
    initRemoveButtons();
    initClearCart();
    initOrderButton();
});

function initQuantityButtons() {
    document.querySelectorAll('.qty-minus').forEach(btn => {
        btn.addEventListener('click', () => {
            const valueEl = btn.parentElement.querySelector('.qty-value');
            let val = parseInt(valueEl.textContent);
            if (val > 1) {
                valueEl.textContent = val - 1;
                // TODO: API 호출
            }
        });
    });

    document.querySelectorAll('.qty-plus').forEach(btn => {
        btn.addEventListener('click', () => {
            const valueEl = btn.parentElement.querySelector('.qty-value');
            let val = parseInt(valueEl.textContent);
            valueEl.textContent = val + 1;
            // TODO: API 호출
        });
    });
}

function initRemoveButtons() {
    document.querySelectorAll('.cart-item-remove').forEach(btn => {
        btn.addEventListener('click', () => {
            const item = btn.closest('.cart-item');
            if (item) {
                item.style.opacity = '0';
                item.style.transform = 'translateX(20px)';
                item.style.transition = 'all 0.3s ease';
                setTimeout(() => item.remove(), 300);
                // TODO: API 호출
            }
        });
    });
}

function initClearCart() {
    const clearBtn = document.getElementById('cart-clear-btn');
    if (clearBtn) {
        clearBtn.addEventListener('click', () => {
            if (confirm('장바구니를 비우시겠습니까?')) {
                const items = document.getElementById('cart-items');
                if (items) items.innerHTML = '';
                // TODO: API 호출
            }
        });
    }
}

function initOrderButton() {
    const orderBtn = document.getElementById('cart-order-btn');
    if (orderBtn) {
        orderBtn.addEventListener('click', () => {
            // TODO: 주문 페이지로 이동
            console.log('주문하기');
        });
    }
}
