/**
 * ADP Commerce - Checkout Page JavaScript
 * 주문서 페이지: 주문 조회, 주문자/배송지 등록, 결제 처리
 */

document.addEventListener('DOMContentLoaded', () => {
    initCheckout();
});

/* ===========================
   Global State
   =========================== */
let orderData = null;         // 주문서 데이터
let ordererInfo = null;       // 주문자 정보 (name, email, phone)
let shippingInfo = null;      // 배송 정보 (address, addressDetail, phone)

/* ===========================
   Init
   =========================== */
function initCheckout() {
    const orderUid = getOrderUidFromUrl();
    if (!orderUid) {
        alert('잘못된 접근입니다.');
        window.location.href = '/';
        return;
    }

    // 주문서 데이터 로드
    loadOrderData(orderUid);

    // 모달 이벤트 바인딩
    initOrdererModal();
    initShippingModal();

    // 결제 버튼
    const payBtn = document.getElementById('payment-submit-btn');
    if (payBtn) {
        payBtn.addEventListener('click', () => handlePayment());
    }
}

/* ===========================
   URL 파싱
   =========================== */
function getOrderUidFromUrl() {
    const path = window.location.pathname;
    const match = path.match(/\/checkout\/(.+)$/);
    return match ? match[1] : null;
}

/* ===========================
   주문서 데이터 로드
   =========================== */
async function loadOrderData(orderUid) {
    try {
        const res = await fetch(`/api/orders/${orderUid}`, {
            method: 'GET',
            credentials: 'include'
        });

        if (!res.ok) {
            throw new Error(`HTTP ${res.status}`);
        }

        const json = await res.json();
        if (!json.success) {
            throw new Error('주문서 조회 실패');
        }

        orderData = json.data;
        renderOrderData(orderData);
        loadUserInfo();
    } catch (err) {
        console.error('주문서 로드 실패:', err);
        alert('주문서를 불러올 수 없습니다. 다시 시도해주세요.');
    }
}

/* ===========================
   사용자 정보 로드
   =========================== */
async function loadUserInfo() {
    try {
        const res = await fetch('/api/users/me', {
            method: 'GET',
            credentials: 'include'
        });

        if (!res.ok) return;

        const json = await res.json();
        if (!json.success || !json.data) return;

        const user = json.data;

        // 주문자 정보가 있으면 자동 세팅
        if (user.name && user.name !== '미설정') {
            ordererInfo = {
                name: user.name,
                email: user.email || '',
                phone: user.phone || ''
            };
            showOrdererFilled(ordererInfo);
        }

        // 배송지 정보가 있으면 자동 세팅
        if (user.address && user.address !== '미설정') {
            shippingInfo = {
                address: user.address,
                addressDetail: '',
                phone: user.phone || ''
            };
            showShippingFilled(shippingInfo);
        }
    } catch (err) {
        console.error('사용자 정보 로드 실패:', err);
    }
}

/* ===========================
   주문서 렌더링
   =========================== */
function renderOrderData(data) {
    // 주문 번호
    const orderIdEl = document.getElementById('checkout-order-id');
    if (orderIdEl) orderIdEl.textContent = data.orderUid;

    // 주문 상품 목록
    const productsBlock = document.getElementById('checkout-products');
    if (productsBlock && data.items && data.items.length > 0) {
        productsBlock.style.display = '';
        const container = productsBlock.querySelector('.checkout-product-items') || productsBlock;

        // 기존 상품 아이템 제거 (블록 타이틀 유지)
        const existingItems = productsBlock.querySelectorAll('.checkout-product-item');
        existingItems.forEach(el => el.remove());

        data.items.forEach(item => {
            const itemEl = document.createElement('div');
            itemEl.className = 'checkout-product-item';
            itemEl.innerHTML = `
                <div class="checkout-product-img">
                    <div class="checkout-img-placeholder"></div>
                </div>
                <div class="checkout-product-info">
                    <span class="checkout-product-name">${escapeHtml(item.productName)}</span>
                    <span class="checkout-product-qty">수량: ${item.quantity}</span>
                    <span class="checkout-product-price">${formatPrice(item.itemAmount)}원</span>
                </div>
            `;
            productsBlock.appendChild(itemEl);
        });
    }

    // 결제 금액
    updatePaymentBox(data.totalAmount, data.deliveryFee, data.finalAmount);
}

/* ===========================
   결제 금액 업데이트
   =========================== */
function updatePaymentBox(totalAmount, deliveryFee, finalAmount) {
    const productPriceEl = document.getElementById('payment-product-price');
    const deliveryFeeEl = document.getElementById('payment-delivery-fee');
    const totalPriceEl = document.getElementById('payment-total-price');
    const submitAmountEl = document.getElementById('payment-submit-amount');

    if (productPriceEl) productPriceEl.textContent = formatPrice(totalAmount) + '원';
    if (deliveryFeeEl) deliveryFeeEl.textContent = formatPrice(deliveryFee) + '원';
    if (totalPriceEl) totalPriceEl.textContent = formatPrice(finalAmount) + '원';
    if (submitAmountEl) submitAmountEl.textContent = formatPrice(finalAmount);
}

/* ===========================
   주문자 모달
   =========================== */
function initOrdererModal() {
    const registerBtn = document.getElementById('btn-register-orderer');
    const changeBtn = document.getElementById('btn-change-orderer');
    const modal = document.getElementById('orderer-modal');
    const form = document.getElementById('orderer-form');

    if (registerBtn) {
        registerBtn.addEventListener('click', () => openModal('orderer-modal'));
    }
    if (changeBtn) {
        changeBtn.addEventListener('click', () => openModal('orderer-modal'));
    }

    // 모달 외부 클릭으로 닫기
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal('orderer-modal');
        });
    }

    // 폼 제출
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const name = document.getElementById('orderer-input-name').value.trim();
            const email = document.getElementById('orderer-input-email').value.trim();
            const phone = document.getElementById('orderer-input-phone').value.trim();

            if (!name) {
                alert('이름을 입력해주세요.');
                return;
            }
            if (!phone) {
                alert('전화번호를 입력해주세요.');
                return;
            }

            ordererInfo = { name, email, phone };
            showOrdererFilled(ordererInfo);
            closeModal('orderer-modal');
        });
    }
}

function showOrdererFilled(info) {
    const emptyEl = document.getElementById('orderer-empty');
    const filledEl = document.getElementById('orderer-filled');

    if (emptyEl) emptyEl.style.display = 'none';
    if (filledEl) {
        filledEl.style.display = '';
        const nameEl = document.getElementById('orderer-filled-name');
        const emailEl = document.getElementById('orderer-filled-email');
        const phoneEl = document.getElementById('orderer-filled-phone');

        if (nameEl) nameEl.textContent = info.name;
        if (emailEl) emailEl.textContent = info.email;
        if (phoneEl) phoneEl.textContent = info.phone;
    }
}

/* ===========================
   배송 주소 모달
   =========================== */
function initShippingModal() {
    const registerBtn = document.getElementById('btn-register-shipping');
    const changeBtn = document.getElementById('btn-change-shipping');
    const modal = document.getElementById('shipping-modal');
    const form = document.getElementById('shipping-form');

    if (registerBtn) {
        registerBtn.addEventListener('click', () => openModal('shipping-modal'));
    }
    if (changeBtn) {
        changeBtn.addEventListener('click', () => openModal('shipping-modal'));
    }

    // 모달 외부 클릭으로 닫기
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeModal('shipping-modal');
        });
    }

    // 폼 제출
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            const address = document.getElementById('shipping-address').value.trim();
            const addressDetail = document.getElementById('shipping-address-detail').value.trim();
            const phone = document.getElementById('shipping-phone').value.trim();

            if (!address) {
                alert('주소를 입력해주세요.');
                return;
            }
            if (!phone) {
                alert('전화번호를 입력해주세요.');
                return;
            }

            const fullAddress = addressDetail ? `${address} ${addressDetail}` : address;
            shippingInfo = { address: fullAddress, addressDetail, phone };
            showShippingFilled(shippingInfo);
            closeModal('shipping-modal');

            // 기본 배송지로 등록 체크 시 서버에 업데이트
            const defaultCheck = document.getElementById('shipping-default');
            if (defaultCheck && defaultCheck.checked) {
                updateUserAddress(fullAddress, phone);
            }
        });
    }
}

function showShippingFilled(info) {
    const emptyEl = document.getElementById('shipping-empty');
    const filledEl = document.getElementById('shipping-filled');

    if (emptyEl) emptyEl.style.display = 'none';
    if (filledEl) {
        filledEl.style.display = '';
        const addressEl = document.getElementById('shipping-filled-address');
        if (addressEl) addressEl.textContent = info.address;
    }
}

/* ===========================
   사용자 주소 업데이트
   =========================== */
async function updateUserAddress(address, phone) {
    try {
        const body = { address };
        if (phone) body.phone = phone;

        await fetch('/api/users/me', {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    } catch (err) {
        console.error('주소 업데이트 실패:', err);
    }
}

/* ===========================
   결제 처리
   =========================== */
async function handlePayment() {
    if (!orderData) {
        alert('주문서 정보를 불러오는 중입니다.');
        return;
    }

    if (!ordererInfo) {
        alert('주문자 정보를 등록해주세요.');
        return;
    }

    if (!shippingInfo) {
        alert('배송 주소를 등록해주세요.');
        return;
    }

    const payBtn = document.getElementById('payment-submit-btn');
    if (payBtn) {
        payBtn.disabled = true;
        payBtn.textContent = '결제 처리 중...';
        payBtn.style.opacity = '0.6';
    }

    try {
        // 1. 주문자 정보 업데이트
        await updateUserProfile(ordererInfo);

        // 2. 결제 생성
        const paymentRes = await fetch(`/api/orders/${orderData.orderUid}/payments`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                amount: orderData.totalAmount,
                deliveryFee: orderData.deliveryFee
            })
        });

        if (!paymentRes.ok) {
            throw new Error(`결제 생성 실패: HTTP ${paymentRes.status}`);
        }

        const paymentJson = await paymentRes.json();
        if (!paymentJson.success) {
            throw new Error('결제 생성 실패');
        }

        const paymentUid = paymentJson.data.paymentUid;

        // 3. 결제 확인
        const confirmRes = await fetch(`/api/orders/${orderData.orderUid}/payments/${paymentUid}/confirm`, {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' }
        });

        if (!confirmRes.ok) {
            throw new Error(`결제 확인 실패: HTTP ${confirmRes.status}`);
        }

        const confirmJson = await confirmRes.json();
        if (!confirmJson.success) {
            throw new Error('결제 확인 실패');
        }

        alert('결제가 완료되었습니다!');
        window.location.href = '/orders';
    } catch (err) {
        console.error('결제 처리 실패:', err);
        alert('결제 처리 중 오류가 발생했습니다. 다시 시도해주세요.');

        if (payBtn) {
            payBtn.disabled = false;
            payBtn.textContent = formatPrice(orderData.finalAmount) + '원 결제하기';
            payBtn.style.opacity = '';
        }
    }
}

async function updateUserProfile(info) {
    try {
        const body = {};
        if (info.name) body.name = info.name;
        if (info.phone) body.phone = info.phone;

        await fetch('/api/users/me', {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });
    } catch (err) {
        console.error('프로필 업데이트 실패:', err);
    }
}

/* ===========================
   모달 공통
   =========================== */
function openModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.add('active');
        document.body.style.overflow = 'hidden';
    }
}

function closeModal(id) {
    const modal = document.getElementById(id);
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
}

/* ===========================
   유틸리티
   =========================== */
function formatPrice(num) {
    if (num == null) return '0';
    return num.toLocaleString('ko-KR');
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}
