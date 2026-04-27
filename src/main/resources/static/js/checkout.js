/**
 * ADP Commerce - Checkout Page JavaScript
 */

class AddressModal {
    constructor(modalId) {
        this.modal = document.getElementById(modalId);
        this.form = document.getElementById('shipping-form');
        this.addressInput = document.getElementById('shipping-address');
        this.addressDetailInput = document.getElementById('shipping-address-detail');
        this.submitBtn = document.getElementById('shipping-submit-btn');
        
        this.initEventListeners();
    }
    
    initEventListeners() {
        this.form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.submitAddress();
        });
        
        this.modal.addEventListener('click', (e) => {
            if (e.target === this.modal) this.close();
        });
    }
    
    open() {
        this.modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        this.addressInput.focus();
    }
    
    close() {
        this.modal.classList.remove('active');
        document.body.style.overflow = '';
        this.form.reset();
    }
    
    async submitAddress() {
        const address = this.addressInput.value.trim();
        const addressDetail = this.addressDetailInput.value.trim();
        if (!address) {
            alert('주소를 입력해주세요.');
            return;
        }
        
        const fullAddress = address + ' ' + addressDetail;
        this.submitBtn.disabled = true;
        this.submitBtn.textContent = '등록 중...';
        
        try {
            // 주소 정보를 User Profile에 저장
            const response = await fetch('/api/users/me', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ address: fullAddress })
            });
            
            if (!response.ok) throw new Error('주소 등록에 실패했습니다.');
            
            this.close();
            alert('배송 주소가 등록되었습니다.');
            if (window.checkoutPage) window.checkoutPage.loadUserProfile();
            
        } catch (error) {
            alert(error.message);
        } finally {
            this.submitBtn.disabled = false;
            this.submitBtn.textContent = '등록';
        }
    }
}

class OrdererModal {
    constructor(modalId) {
        this.modal = document.getElementById(modalId);
        this.form = document.getElementById('orderer-form');
        this.nameInput = document.getElementById('orderer-input-name');
        this.phoneInput = document.getElementById('orderer-input-phone');
        this.submitBtn = document.getElementById('orderer-submit-btn');
        
        this.initEventListeners();
    }
    
    initEventListeners() {
        this.form.addEventListener('submit', (e) => {
            e.preventDefault();
            this.submitOrderer();
        });
        
        this.modal.addEventListener('click', (e) => {
            if (e.target === this.modal) this.close();
        });
    }
    
    open() {
        this.modal.classList.add('active');
        document.body.style.overflow = 'hidden';
        this.nameInput.focus();
    }
    
    close() {
        this.modal.classList.remove('active');
        document.body.style.overflow = '';
        this.form.reset();
    }
    
    async submitOrderer() {
        const name = this.nameInput.value.trim();
        const phone = this.phoneInput.value.trim();
        let formattedPhone = phone.replace(/[-\s]/g, '');
        if (formattedPhone.length === 11) {
            formattedPhone = formattedPhone.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
        } else if (formattedPhone.length === 10) {
            formattedPhone = formattedPhone.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
        }

        this.submitBtn.disabled = true;
        this.submitBtn.textContent = '등록 중...';
        
        try {
            const response = await fetch('/api/users/me', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ name: name, phone: formattedPhone })
            });
            
            if (!response.ok) throw new Error('주문자 등록에 실패했습니다.');
            
            this.close();
            alert('주문자 정보가 등록되었습니다.');
            if (window.checkoutPage) window.checkoutPage.loadUserProfile();
            
        } catch (error) {
            alert(error.message);
        } finally {
            this.submitBtn.disabled = false;
            this.submitBtn.textContent = '등록';
        }
    }
}

class CheckoutPage {
    constructor() {
        this.paymentBtn = document.getElementById('payment-submit-btn');
        this.paymentAmount = document.getElementById('payment-submit-amount');
        this.orderUid = new URLSearchParams(window.location.search).get('orderUid');
        this.orderData = null;
        
        this.initEventListeners();
        this.loadOrderData();
        this.loadUserProfile();
    }
    
    initEventListeners() {
        if (this.paymentBtn) {
            this.paymentBtn.addEventListener('click', () => this.submitPayment());
        }
    }
    
    async loadOrderData() {
        if (!this.orderUid) {
            alert('잘못된 접근입니다. 주문 번호가 없습니다.');
            return;
        }
        
        try {
            const response = await fetch(`/api/orders/${this.orderUid}`);
            const result = await response.json();
            
            if (result.success && result.data) {
                this.orderData = result.data;
                this.renderOrderData(this.orderData);
            } else {
                throw new Error('주문 정보를 불러오는데 실패했습니다.');
            }
        } catch (error) {
            console.error(error);
            alert('주문 정보를 불러오는데 실패했습니다.');
        }
    }
    
    renderOrderData(data) {
        document.getElementById('checkout-order-id').textContent = data.orderUid;
        
        const productsContainer = document.getElementById('checkout-products');
        if (productsContainer && data.items) {
            productsContainer.innerHTML = '<h2 class="checkout-block-title">주문 상품</h2>';
            data.items.forEach(item => {
                productsContainer.innerHTML += `
                    <div class="checkout-product-item">
                        <div class="checkout-product-img">
                            <div class="checkout-img-placeholder"></div>
                        </div>
                        <div class="checkout-product-info">
                            <span class="checkout-product-name">${item.productName}</span>
                            <span class="checkout-product-qty">수량: <span>${item.quantity}</span></span>
                            <span class="checkout-product-price" style="font-size:14px; color:#666; margin-top:4px;">${item.itemAmount.toLocaleString()}원</span>
                        </div>
                    </div>
                `;
            });
        }
        
        document.getElementById('payment-product-price').innerHTML = `<span>${data.totalAmount.toLocaleString()}</span>원`;
        document.getElementById('payment-delivery-fee').innerHTML = `<span>${data.deliveryFee.toLocaleString()}</span>원`;
        document.getElementById('payment-total-price').innerHTML = `<span>${data.finalAmount.toLocaleString()}</span>원`;
        
        if (this.paymentAmount) {
            this.paymentAmount.textContent = data.finalAmount.toLocaleString();
        }
        
        this.validateCheckout();
    }
    
    async loadUserProfile() {
        try {
            const response = await fetch('/api/users/me/unmasked');
            const result = await response.json();
            
            if (result.success && result.data) {
                this.renderUserProfile(result.data);
            }
        } catch (error) {
            console.error('Failed to load user profile', error);
        }
    }
    
    renderUserProfile(user) {
        const ordererEmpty = document.getElementById('orderer-empty');
        const ordererFilled = document.getElementById('orderer-filled');
        const shippingEmpty = document.getElementById('shipping-empty');
        const shippingFilled = document.getElementById('shipping-filled');
        
        window.checkoutData = window.checkoutData || {};
        
        if (user.name && user.phone) {
            ordererEmpty.style.display = 'none';
            ordererFilled.style.display = 'block';
            document.getElementById('orderer-filled-name').textContent = user.name;
            document.getElementById('orderer-filled-email').textContent = user.maskedEmail || user.email;
            document.getElementById('orderer-filled-phone').textContent = user.phone;
            window.checkoutData.orderer = true;
        } else {
            ordererEmpty.style.display = 'block';
            ordererFilled.style.display = 'none';
            window.checkoutData.orderer = false;
        }
        
        if (user.address) {
            shippingEmpty.style.display = 'none';
            shippingFilled.style.display = 'block';
            document.getElementById('shipping-filled-address').textContent = user.address;
            window.checkoutData.shipping = true;
        } else {
            shippingEmpty.style.display = 'block';
            shippingFilled.style.display = 'none';
            window.checkoutData.shipping = false;
        }
        
        this.validateCheckout();
    }
    
    validateCheckout() {
        const isValid = window.checkoutData?.orderer && window.checkoutData?.shipping && this.orderData;
        if (this.paymentBtn) {
            if (isValid) {
                this.paymentBtn.disabled = false;
                this.paymentBtn.style.background = '#f5e642';
                this.paymentBtn.style.color = '#333';
                this.paymentBtn.style.cursor = 'pointer';
            } else {
                this.paymentBtn.disabled = true;
                this.paymentBtn.style.background = '#e8e8e8';
                this.paymentBtn.style.color = '#999';
                this.paymentBtn.style.cursor = 'not-allowed';
            }
        }
    }
    
    async submitPayment() {
        if (!window.checkoutData?.orderer || !window.checkoutData?.shipping) {
            alert('주문자 정보와 배송 주소를 모두 등록해주세요.');
            return;
        }
        
        if (!this.orderData) return;
        
        this.paymentBtn.disabled = true;
        this.paymentBtn.textContent = '결제 처리 중...';
        
        try {
            const response = await fetch(`/api/orders/${this.orderUid}/payments`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    amount: this.orderData.totalAmount,
                    deliveryFee: this.orderData.deliveryFee
                })
            });
            
            if (!response.ok) throw new Error('결제 처리에 실패했습니다.');
            const result = await response.json();
            
            if (result.success && result.data && result.data.paymentUid) {
                const confirmRes = await fetch(`/api/orders/${this.orderUid}/payments/${result.data.paymentUid}/confirm`, {
                    method: 'POST'
                });
                if (!confirmRes.ok) throw new Error('결제 승인에 실패했습니다.');
                
                // 장바구니 비우기 호출
                try {
                    await fetch('/api/cart', { method: 'DELETE' });
                } catch (e) {
                    console.error('장바구니 비우기 실패:', e);
                }
            }
            
            alert('결제가 완료되었습니다!');
            window.location.href = `/orders`;
            
        } catch (error) {
            alert(error.message);
            this.paymentBtn.disabled = false;
            this.paymentBtn.textContent = `${this.orderData.finalAmount.toLocaleString()}원 결제하기`;
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    const addressModal = new AddressModal('shipping-modal');
    const ordererModal = new OrdererModal('orderer-modal');
    
    window.checkoutPage = new CheckoutPage();
    
    const btnRegisterShipping = document.getElementById('btn-register-shipping');
    const btnChangeShipping = document.getElementById('btn-change-shipping');
    const btnRegisterOrderer = document.getElementById('btn-register-orderer');
    const btnChangeOrderer = document.getElementById('btn-change-orderer');
    
    if (btnRegisterShipping) btnRegisterShipping.addEventListener('click', () => addressModal.open());
    if (btnChangeShipping) btnChangeShipping.addEventListener('click', () => addressModal.open());
    if (btnRegisterOrderer) btnRegisterOrderer.addEventListener('click', () => ordererModal.open());
    if (btnChangeOrderer) btnChangeOrderer.addEventListener('click', () => ordererModal.open());
});
