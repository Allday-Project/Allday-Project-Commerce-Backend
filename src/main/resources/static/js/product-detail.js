/**
 * ADP Commerce - Product Detail Modal JavaScript
 * 상품 상세 모달: API 호출, 장바구니 추가, 구매하기
 */

class ProductDetailModal {
    constructor(modalId) {
        this.modal = document.getElementById(modalId);
        this.closeBtn = document.getElementById('product-detail-close');
        this.productData = null;

        this.initEventListeners();
    }

    initEventListeners() {
        // 모달 닫기 (X 버튼)
        if (this.closeBtn) {
            this.closeBtn.addEventListener('click', () => this.close());
        }

        // 모달 오버레이 클릭으로 닫기
        if (this.modal) {
            this.modal.addEventListener('click', (e) => {
                if (e.target === this.modal) {
                    this.close();
                }
            });
        }

        // ESC 키로 닫기
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && this.modal && this.modal.classList.contains('active')) {
                this.close();
            }
        });

        // 장바구니 버튼
        const cartBtn = document.getElementById('modal-btn-cart');
        if (cartBtn) {
            cartBtn.addEventListener('click', () => this.handleAddToCart());
        }

        // 구매하기 버튼
        const buyBtn = document.getElementById('modal-btn-buy');
        if (buyBtn) {
            buyBtn.addEventListener('click', () => this.handleBuyNow());
        }
    }

    async open(productId) {
        if (!this.modal) return;

        try {
            // API에서 상품 정보 가져오기
            const response = await fetch(`/api/products/${productId}`);

            if (!response.ok) {
                throw new Error('Failed to fetch product');
            }

            const apiResponse = await response.json();

            if (apiResponse.success && apiResponse.data) {
                this.productData = apiResponse.data;
                this.renderProduct(apiResponse.data);
                this.modal.classList.add('active');
                document.body.style.overflow = 'hidden';
            } else {
                throw new Error('Invalid API response');
            }
        } catch (error) {
            console.error('상품 상세 로드 실패:', error);
            alert('상품 정보를 불러오는데 실패했습니다.');
        }
    }

    close() {
        if (this.modal) {
            this.modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    renderProduct(product) {
        // 이미지
        const img = document.getElementById('modal-product-img');
        const placeholder = document.getElementById('modal-img-placeholder');
        if (img) {
            if (product.imageUrl) {
                img.src = product.imageUrl;
                img.alt = product.name;
                img.style.display = 'block';
                if (placeholder) placeholder.style.display = 'none';
            } else {
                img.src = '';
                img.style.display = 'none';
                if (placeholder) placeholder.style.display = 'block';
            }
        }

        // 상품명
        const nameEl = document.getElementById('modal-product-name');
        if (nameEl) nameEl.textContent = product.name;

        // 설명
        const descEl = document.getElementById('modal-product-description');
        if (descEl && product.description) {
            const descList = descEl.querySelector('.detail-desc-list');
            if (descList) {
                descList.innerHTML = product.description.split('\n').map(line => {
                    return `<li>• ${this.escapeHtml(line.trim())}</li>`;
                }).join('');
            }
        }

        // 가격
        const priceEl = document.getElementById('modal-product-price');
        if (priceEl) {
            const priceValue = priceEl.querySelector('.price-value');
            if (priceValue) {
                priceValue.textContent = this.formatPrice(product.price);
            }
            
            // 원래 가격 (할인이 있는 경우)
            const originalPriceEl = document.getElementById('modal-price-original');
            if (originalPriceEl && product.originalPrice && product.originalPrice > product.price) {
                originalPriceEl.textContent = `₩${this.formatPrice(product.originalPrice)}`;
                originalPriceEl.style.display = 'inline';
            } else if (originalPriceEl) {
                originalPriceEl.style.display = 'none';
            }
        }

        // 배송비 정보
        const shippingEl = document.getElementById('modal-shipping-info');
        if (shippingEl) {
            const shippingValue = shippingEl.querySelector('.shipping-value');
            if (shippingValue) {
                shippingValue.textContent = '₩3,000 (₩50,000 이상 구매 시 무료)';
            }
        }
    }

    isUserLoggedIn() {
        // window.isLoggedIn is set by Thymeleaf in base.html
        return window.isLoggedIn === true;
    }

    handleAddToCart() {
        if (!this.isUserLoggedIn()) {
            this.showLoginRequired();
            return;
        }

        if (!this.productData) return;

        // 장바구니 추가 API 호출
        fetch('/api/cart', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                productId: this.productData.id,
                quantity: 1
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('장바구니 추가 실패');
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                this.showToast('장바구니에 추가되었습니다.');
                if (typeof initCartBadge === 'function') initCartBadge();
            } else {
                this.showToast('장바구니 추가에 실패했습니다.', 'error');
            }
        })
        .catch(error => {
            console.error('장바구니 추가 실패:', error);
            this.showToast('장바구니 추가에 실패했습니다.', 'error');
        });
    }

    handleBuyNow() {
        if (!this.isUserLoggedIn()) {
            this.showLoginRequired();
            return;
        }

        if (!this.productData) return;

        // 주문 생성 API 호출
        fetch('/api/orders', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                orderItems: [{
                    productId: this.productData.id,
                    quantity: 1
                }]
            })
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('주문 생성 실패');
            }
            return response.json();
        })
        .then(data => {
            if (data.success && data.data) {
                // 주문서 페이지로 이동
                window.location.href = `/checkout?orderUid=${data.data.orderUid}`;
            } else {
                this.showToast('주문 생성에 실패했습니다.', 'error');
            }
        })
        .catch(error => {
            console.error('주문 생성 실패:', error);
            this.showToast('주문 생성에 실패했습니다.', 'error');
        });
    }

    showLoginRequired() {
        // 모달 내에 로그인 안내 표시
        const overlay = document.createElement('div');
        overlay.className = 'login-required-overlay';
        overlay.style.cssText = `
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(0,0,0,0.7);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            z-index: 10;
            border-radius: 12px;
        `;

        overlay.innerHTML = `
            <div style="
                background: white;
                padding: 32px 40px;
                border-radius: 12px;
                text-align: center;
                box-shadow: 0 8px 32px rgba(0,0,0,0.2);
            ">
                <p style="font-size: 16px; font-weight: 600; color: #1a1a1a; margin-bottom: 8px;">로그인이 필요합니다</p>
                <p style="font-size: 14px; color: #666; margin-bottom: 24px;">이 기능을 사용하려면 로그인해주세요.</p>
                <div style="display: flex; gap: 12px; justify-content: center;">
                    <button id="login-required-cancel" style="
                        padding: 10px 24px;
                        background: #f0f0f0;
                        color: #333;
                        border: none;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 14px;
                        font-weight: 500;
                    ">취소</button>
                    <button id="login-required-go" style="
                        padding: 10px 24px;
                        background: #1a1a1a;
                        color: white;
                        border: none;
                        border-radius: 6px;
                        cursor: pointer;
                        font-size: 14px;
                        font-weight: 600;
                    ">로그인하기</button>
                </div>
            </div>
        `;

        const modalContent = document.getElementById('product-detail-modal-content');
        if (modalContent) {
            modalContent.style.position = 'relative';
            modalContent.appendChild(overlay);
        }

        // 버튼 이벤트
        const cancelBtn = overlay.querySelector('#login-required-cancel');
        const goBtn = overlay.querySelector('#login-required-go');

        if (cancelBtn) {
            cancelBtn.addEventListener('click', () => overlay.remove());
        }
        if (goBtn) {
            goBtn.addEventListener('click', () => {
                window.location.href = '/login';
            });
        }
    }

    showToast(message, type = 'success') {
        const toast = document.createElement('div');
        toast.className = 'toast-message ' + type;
        toast.textContent = message;
        toast.style.cssText = `
            position: fixed;
            bottom: 100px;
            left: 50%;
            transform: translateX(-50%);
            padding: 12px 24px;
            background: ${type === 'error' ? '#e53e3e' : '#1a1a1a'};
            color: white;
            border-radius: 8px;
            font-size: 14px;
            font-weight: 500;
            z-index: 9999;
            box-shadow: 0 4px 16px rgba(0,0,0,0.2);
            opacity: 0;
            transition: opacity 0.3s ease;
        `;

        document.body.appendChild(toast);

        setTimeout(() => { toast.style.opacity = '1'; }, 10);
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => toast.remove(), 300);
        }, 3000);
    }

    formatPrice(price) {
        return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }

    escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}
