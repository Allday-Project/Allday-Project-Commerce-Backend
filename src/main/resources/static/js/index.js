/**
 * ADP Commerce - Index Page JavaScript
 * 카테고리 탭, 정렬, 필터 인터랙션
 */

document.addEventListener('DOMContentLoaded', () => {
    initCategoryTabs();
    initSortDropdown();
    initOnSaleFilter();
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
    console.log('카테고리 필터:', category);
    // TODO: 서버 API 연동 또는 클라이언트 필터링
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
    console.log('정렬:', sortType);
    // TODO: 서버 API 연동 또는 클라이언트 정렬
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
    console.log('판매 중 필터:', onSaleOnly);
    // TODO: 서버 API 연동 또는 클라이언트 필터링
}
