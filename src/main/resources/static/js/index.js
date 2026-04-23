/**
 * ADP Commerce - Index Page JavaScript
 * 카테고리 탭, 정렬, 필터, 상품 목록 로드, 상품 상세 모달 인터랙션
 */

document.addEventListener('DOMContentLoaded', () => {
    initCategoryTabs();
    initSortDropdown();
    initOnSaleFilter();
    loadProducts();
    initProductDetailModal();
});

/* ===========================
   State
   =========================== */
let currentCategory = 'all';
let currentSort = 'newest';
let currentOnSale = false;
let currentPage = 1;
let currentProductDetail = null;

/* ===========================
   Category Tabs
   =========================== */
function initCategoryTabs() {
    const tabs = document.querySelectorAll('.tab-item');

    tabs.forEach(tab => {
        tab.addEventListener('click', (e) => {
            e.preventDefault();

            // 활성 탭 변경
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            currentCategory = tab.dataset.category;
            currentPage = 1;
            loadProducts();
        });
    });
}

/* ===========================
   Sort Dropdown
   =========================== */
function initSortDropdown() {
    const dropdown = document.getElementById('sort-dropdown');
    const sortBtn = document.getElementById('sort-btn');
    const sortLabel = document.getElementById('sort-label');
    const options = document.querySelectorAll('.sort-option');

    if (!dropdown || !sortBtn) return;

    // 드롭다운 토글
    sortBtn.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdown.classList.toggle('open');
    });

    // 옵션 선택
    options.forEach(option => {
        option.addEventListener('click', () => {
            options.forEach(o => o.classList.remove('active'));
            option.classList.add('active');

            if (sortLabel) {
                sortLabel.textContent = option.textContent;
            }

            dropdown.classList.remove('open');
            currentSort = option.dataset.sort;
            currentPage = 1;
            loadProducts();
        });
    });

    // 외부 클릭으로 닫기
    document.addEventListener('click', (e) => {
        if (!dropdown.contains(e.target)) {
            dropdown.classList.remove('open');
        }
    });
}

/* ===========================
   On-Sale Filter
   =========================== */
function initOnSaleFilter() {
    const checkbox = document.getElementById('on-sale-checkbox');

    if (!checkbox) return;

    checkbox.addEventListener('change', () => {
        currentOnSale = checkbox.checked;
        currentPage = 1;
        loadProducts();
    });
}

/* ===========================
   상품 목록 로드
   =========================== */
async function loadProducts() {
    try {
        let url = `/api/products?page=${currentPage}&size=20`;

        // 카테고리 필터
        if (currentCategory && currentCategory !== 'all') {
            url += `&category=${currentCategory.toUpperCase()}`;
        }

        // 판매중 필터
        if (currentOnSale) {
            url += `&status=ON_SALE`;
        }

        // 정렬
        if (currentSort === 'price-asc') {
            url += `&sort=price,asc`;
        } else if (currentSort === 'price-desc') {
            url += `&sort=price,desc`;
        } else if (currentSort === 'popular') {
            url += `&sort=viewCount,desc`;
        } else {
            url += `&sort=id,desc`;
        }

        const res = await fetch(url, { credentials: 'include' });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const json = await res.json();
        if (!json.success) throw new Error('상품 목록 조회 실패');

        const pageData = json.data;
        renderProducts(pageData.content || []);
    } catch (err) {
        console.error('상품 목록 로드 실패:', err);
        // 실패 시 기존 더미 데이터 유지
    }
}

function renderProducts(products) {
    const grid = document.getElementById('product-grid');
    if (!grid) return;

    // 상품이 없는 경우
    if (!products || products.length === 0) {
        // 더미 데이터가 유지되도록 반환
        return;
    }

    grid.innerHTML = '';

    products.forEach(product => {
        const card = document.createElement('article');
        card.className = 'product-card';
        card.id = `product-${product.id}`;

        const imgHtml = product.imageUrl
            ? `<img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" class="product-img" loading="lazy">`
            : `<div class="product-img-placeholder"></div>`;

        card.innerHTML = `
            <a href="javascript:void(0)" class="product-link" data-product-id="${product.id}">
                <div class="product-img-wrapper">
                    ${imgHtml}
                </div>
                <div class="product-info">
                    <h3 class="product-name">${escapeHtml(product.name)}</h3>
                    <p class="product-price">
                        <span class="price-currency">₩</span>
                        <span class="price-value">${formatPrice(product.price)}</span>
                    </p>
                </div>
            </a>
        `;

        // 상품 클릭 이벤트
        card.querySelector('.product-link').addEventListener('click', (e) => {
            e.preventDefault();
            openProductDetail(product.id);
        });

        grid.appendChild(card);
    });
}

/* ===========================
   상품 상세 모달
   =========================== */
function initProductDetailModal() {
    const modal = document.getElementById('product-detail-modal');
    const closeBtn = document.getElementById('product-detail-close');
    const cartBtn = document.getElementById('modal-btn-cart');
    const buyBtn = document.getElementById('modal-btn-buy');

    // 더미 상품 카드 클릭 시에도 모달 열기
    document.querySelectorAll('.product-card .product-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const productId = link.dataset.productId;
            if (productId) {
                openProductDetail(productId);
            }
        });
    });

    // 모달 닫기
    if (closeBtn) {
        closeBtn.addEventListener('click', () => closeProductModal());
    }
    if (modal) {
        modal.addEventListener('click', (e) => {
            if (e.target === modal) closeProductModal();
        });
    }

    // 장바구니 담기
    if (cartBtn) {
        cartBtn.addEventListener('click', () => addToCart());
    }

    // 주문하기
    if (buyBtn) {
        buyBtn.addEventListener('click', () => buyNow());
    }
}

async function openProductDetail(productId) {
    try {
        const res = await fetch(`/api/products/${productId}`, { credentials: 'include' });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const json = await res.json();
        if (!json.success) throw new Error('상품 조회 실패');

        currentProductDetail = json.data;
        renderProductDetail(currentProductDetail);

        const modal = document.getElementById('product-detail-modal');
        if (modal) {
            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    } catch (err) {
        console.error('상품 상세 로드 실패:', err);
        alert('상품 정보를 불러올 수 없습니다.');
    }
}

function renderProductDetail(product) {
    const imgEl = document.getElementById('modal-product-img');
    const placeholderEl = document.getElementById('modal-img-placeholder');
    const nameEl = document.getElementById('modal-product-name');
    const priceEl = document.getElementById('modal-product-price');
    const descEl = document.getElementById('modal-product-description');

    // 이미지
    if (imgEl) {
        if (product.imageUrl) {
            imgEl.src = product.imageUrl;
            imgEl.alt = product.name;
            imgEl.style.display = 'block';
            if (placeholderEl) placeholderEl.style.display = 'none';
        } else {
            imgEl.src = '';
            imgEl.style.display = 'none';
            if (placeholderEl) placeholderEl.style.display = 'block';
        }
    }

    // 상품명
    if (nameEl) nameEl.textContent = product.name;

    // 가격
    if (priceEl) {
        priceEl.innerHTML = `
            <span class="price-currency">₩</span>
            <span class="price-value">${formatPrice(product.price)}</span>
        `;
    }

    // 설명
    if (descEl) {
        if (product.description) {
            const lines = product.description.split('\n').filter(l => l.trim());
            descEl.innerHTML = `
                <ul class="detail-desc-list">
                    ${lines.map(line => `<li>• ${escapeHtml(line.trim())}</li>`).join('')}
                </ul>
            `;
        } else {
            descEl.innerHTML = `
                <ul class="detail-desc-list">
                    <li>• 상품 상세 정보가 없습니다.</li>
                </ul>
            `;
        }
    }

    // 배송비 및 가격 표시
    const detailRight = document.querySelector('.product-detail-right');
    if (detailRight) {
        // 기존 배송비/가격 정보가 있으면 업데이트
        let shippingInfo = detailRight.querySelector('.detail-shipping-info');
        if (!shippingInfo) {
            shippingInfo = document.createElement('div');
            shippingInfo.className = 'detail-shipping-info';
            // 가격 요소 앞에 삽입
            const priceParent = detailRight.querySelector('.detail-product-price');
            if (priceParent) {
                detailRight.insertBefore(shippingInfo, priceParent);
            }
        }
        shippingInfo.innerHTML = `
            <p style="font-size:12px;color:#888;margin-bottom:4px;">배송비</p>
            <p style="font-size:12px;color:#555;margin-bottom:8px;">₩3,000 (₩50,000 이상 구매 시 무료)</p>
        `;
    }
}

function closeProductModal() {
    const modal = document.getElementById('product-detail-modal');
    if (modal) {
        modal.classList.remove('active');
        document.body.style.overflow = '';
    }
    currentProductDetail = null;
}

/* ===========================
   장바구니 담기
   =========================== */
async function addToCart() {
    if (!currentProductDetail) return;

    try {
        const res = await fetch('/api/cart', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                productId: currentProductDetail.id,
                quantity: 1
            })
        });

        if (res.status === 401 || res.status === 403) {
            alert('로그인이 필요합니다.');
            window.location.href = '/login';
            return;
        }

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const json = await res.json();
        if (!json.success) throw new Error('장바구니 추가 실패');

        alert('장바구니에 추가되었습니다.');

        // 장바구니 뱃지 업데이트
        updateCartBadge();
        closeProductModal();
    } catch (err) {
        console.error('장바구니 추가 실패:', err);
        alert('장바구니 추가에 실패했습니다. 다시 시도해주세요.');
    }
}

/* ===========================
   바로 주문하기
   =========================== */
async function buyNow() {
    if (!currentProductDetail) return;

    try {
        const res = await fetch('/api/orders', {
            method: 'POST',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                orderItems: [{
                    productId: currentProductDetail.id,
                    quantity: 1
                }]
            })
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
        closeProductModal();
        window.location.href = `/checkout/${orderUid}`;
    } catch (err) {
        console.error('주문 생성 실패:', err);
        alert('주문 생성에 실패했습니다. 다시 시도해주세요.');
    }
}

/* ===========================
   장바구니 뱃지 업데이트
   =========================== */
async function updateCartBadge() {
    try {
        const res = await fetch('/api/cart?size=100', { credentials: 'include' });
        if (!res.ok) return;

        const json = await res.json();
        if (!json.success) return;

        const badge = document.getElementById('cart-badge');
        if (badge) {
            const count = json.data.content ? json.data.content.length : 0;
            badge.textContent = count;
            badge.style.display = count > 0 ? '' : 'none';
        }
    } catch (err) {
        // 무시
    }
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
