/**
 * ADP Commerce - Index Page JavaScript
 * 카테고리 탭, 정렬, 필터 인터랙션, 페이지네이션
 */

// 전역 상태
let pagination = null;
let productDetailModal = null;
let currentFilters = {
    category: 'all',
    sort: 'newest',
    onSale: false,
    search: ''
};

document.addEventListener('DOMContentLoaded', () => {
    // URL에서 검색어 파라미터 읽기
    const urlParams = new URLSearchParams(window.location.search);
    const searchQuery = urlParams.get('search');
    if (searchQuery) {
        currentFilters.search = searchQuery;
        const searchInput = document.getElementById('search-input');
        if (searchInput) {
            searchInput.value = searchQuery;
        }
    }

    initCategoryTabs();
    initSortDropdown();
    initOnSaleFilter();
    initPagination();
    initProductDetailModal();
    loadProducts(1); // 초기 상품 로드
});

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

            const category = tab.dataset.category;
            filterByCategory(category);
        });
    });
}

function filterByCategory(category) {
    currentFilters.category = category;
    currentFilters.search = ''; // 카테고리 변경 시 검색어 초기화
    
    // URL에서 search 파라미터 제거
    const url = new URL(window.location);
    url.searchParams.delete('search');
    window.history.replaceState({}, '', url);
    
    // 페이지네이션을 1페이지로 리셋
    resetPagination();
    
    // 1페이지 상품 로드
    loadProducts(1);
}

function resetPagination() {
    if (pagination) {
        pagination.currentPage = 1;
    }
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
        option.addEventListener('click', (e) => {
            e.stopPropagation();
            options.forEach(o => o.classList.remove('active'));
            option.classList.add('active');

            if (sortLabel) {
                sortLabel.textContent = option.textContent;
            }

            dropdown.classList.remove('open');
            sortProducts(option.dataset.sort);
        });
    });

    // 외부 클릭으로 닫기
    document.addEventListener('click', (e) => {
        if (!dropdown.contains(e.target)) {
            dropdown.classList.remove('open');
        }
    });
}

function sortProducts(sortType) {
    currentFilters.sort = sortType;
    loadProducts(1); // 페이지 1로 리셋하여 상품 로드
}

/* ===========================
   On-Sale Filter
   =========================== */
function initOnSaleFilter() {
    const checkbox = document.getElementById('on-sale-checkbox');

    if (!checkbox) return;

    checkbox.addEventListener('change', () => {
        const onSaleOnly = checkbox.checked;
        filterOnSale(onSaleOnly);
    });
}

function filterOnSale(onSaleOnly) {
    currentFilters.onSale = onSaleOnly;
    loadProducts(1);
}

/* ===========================
   Product Detail Modal
   =========================== */
function initProductDetailModal() {
    productDetailModal = new ProductDetailModal('product-detail-modal');
}

/* ===========================
   Pagination
   =========================== */
function initPagination() {
    pagination = new Pagination('pagination-container', {
        maxVisiblePages: 5
    });

    // 페이지 변경 시 상품 로드
    pagination.onPageChange((page) => {
        loadProducts(page);
        scrollToTop();
    });
}

function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: 'smooth'
    });
}

/* ===========================
   Product Loading
   =========================== */
function loadProducts(page) {
    const productGrid = document.getElementById('product-grid');
    if (!productGrid) return;

    // 로딩 상태 표시
    showLoadingState(productGrid);

    // API 호출 파라미터 구성 (Spring Pageable 형식: page는 0부터 시작)
    const params = new URLSearchParams({
        page: page - 1, // Spring Pageable은 0-based index
        size: 10
    });

    // 카테고리 필터 추가 (ALL이 아닌 경우에만)
    if (currentFilters.category && currentFilters.category !== 'all') {
        params.append('category', currentFilters.category.toUpperCase());
    }

    if (currentFilters.search) {
        params.append('keyword', currentFilters.search);
    }

    // 정렬 파라미터 추가
    let sortParam = 'id,desc'; // 기본 정렬
    switch (currentFilters.sort) {
        case 'newest':
            sortParam = 'id,desc';
            break;
        case 'price-asc':
            sortParam = 'price,asc';
            break;
        case 'price-desc':
            sortParam = 'price,desc';
            break;
        case 'popular':
            sortParam = 'id,desc'; // 인기순은 임시로 최신순으로 처리
            break;
    }
    params.append('sort', sortParam);

    // 판매중 필터 추가
    if (currentFilters.onSale) {
        params.append('onSale', 'true');
    }

    // API 호출
    fetch(`/api/products?${params.toString()}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Failed to fetch products');
            }
            return response.json();
        })
        .then(apiResponse => {
            // ApiResponse<Page<GetAllProductResponse>> 구조
            if (apiResponse.success && apiResponse.data) {
                const pageData = apiResponse.data;
                let products = pageData.content;
                
                updateProductGrid(products);
                updatePagination(pageData.number + 1, pageData.totalPages);
            } else {
                throw new Error('Invalid API response');
            }
        })
        .catch(error => {
            console.error('상품 로드 실패:', error);
            showErrorState(productGrid);
        });
}

function updateProductGrid(products) {
    const productGrid = document.getElementById('product-grid');
    if (!productGrid) return;

    if (!products || products.length === 0) {
        productGrid.innerHTML = '<div class="empty-state">상품이 없습니다.</div>';
        return;
    }

    // 상품 카드 HTML 생성
    const productsHTML = products.map(product => {
        const hasImage = product.imageUrl && product.imageUrl !== '';
        const imageHTML = hasImage
            ? `<img src="${escapeHtml(product.imageUrl)}" alt="${escapeHtml(product.name)}" class="product-img" loading="lazy">`
            : `<div class="product-img-placeholder"></div>`;
        
        return `
        <article class="product-card" id="product-${product.id}" data-product-id="${product.id}">
            <a href="#" class="product-link" data-product-id="${product.id}">
                <div class="product-img-wrapper">
                    ${imageHTML}
                </div>
                <div class="product-info">
                    <h3 class="product-name">${escapeHtml(product.name)}</h3>
                    <p class="product-price">
                        <span class="price-currency">₩</span>
                        <span class="price-value">${formatPrice(product.price)}</span>
                    </p>
                </div>
            </a>
        </article>
    `}).join('');

    productGrid.innerHTML = productsHTML;
    
    // 상품 카드 클릭 이벤트 추가
    attachProductCardListeners();
}

/**
 * 상품 카드 클릭 이벤트 리스너 추가
 */
function attachProductCardListeners() {
    const productLinks = document.querySelectorAll('.product-link');
    
    productLinks.forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const productId = link.dataset.productId;
            if (productId && productDetailModal) {
                productDetailModal.open(parseInt(productId));
            }
        });
    });
}

function updatePagination(currentPage, totalPages) {
    if (pagination) {
        pagination.render(currentPage, totalPages);
    }
}

function showLoadingState(container) {
    container.innerHTML = `
        <div class="loading-state">
            <div class="loading-spinner"></div>
            <p>상품을 불러오는 중...</p>
        </div>
    `;
}

function showErrorState(container) {
    container.innerHTML = `
        <div class="error-state">
            <p>상품을 불러오는데 실패했습니다.</p>
            <button onclick="loadProducts(1)" class="retry-btn">다시 시도</button>
        </div>
    `;
}

/* ===========================
   Utility Functions
   =========================== */
function formatPrice(price) {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
