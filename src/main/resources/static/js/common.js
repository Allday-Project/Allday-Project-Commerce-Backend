/**
 * ADP Commerce - Common JavaScript
 * 검색, 챗봇 등 공통 인터랙션 처리
 */

document.addEventListener('DOMContentLoaded', () => {
    initSearch();
    initPopularKeywords();
    initCartBadge();
    initLogout();
});

function initLogout() {
    const logoutBtn = document.getElementById('logout-link');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            fetch('/api/auth/logout', { method: 'POST' })
                .then(() => {
                    window.location.href = '/';
                })
                .catch(() => {
                    window.location.href = '/';
                });
        });
    }
}

function initCartBadge() {
    // 로그인한 유저만 카트 뱃지 업데이트
    if (window.isLoggedIn) {
        fetch('/api/cart')
            .then(res => res.json())
            .then(data => {
                if (data.success && data.data && data.data.content) {
                    const count = data.data.content.length;
                    const badge = document.getElementById('cart-badge');
                    if (badge) badge.textContent = count;
                }
            })
            .catch(() => {});
    }
}

/* ===========================
   Search
   =========================== */
function initSearch() {
    const searchInput = document.getElementById('search-input');
    const searchDropdown = document.getElementById('search-dropdown');
    const searchWrapper = document.getElementById('search-wrapper');
    const keywordList = document.getElementById('search-keyword-list');

    if (!searchInput || !searchDropdown) return;

    // 검색 입력 포커스 시 드롭다운 표시
    searchInput.addEventListener('focus', () => {
        searchDropdown.classList.add('active');
    });

    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', (e) => {
        if (searchWrapper && !searchWrapper.contains(e.target)) {
            searchDropdown.classList.remove('active');
        }
    });

    // 키워드 클릭 시 검색
    if (keywordList) {
        keywordList.addEventListener('click', (e) => {
            const li = e.target.closest('li');
            if (li && !li.classList.contains('empty-keyword')) {
                const text = li.textContent.replace(/^\d+\.\s*/, '').trim();
                searchInput.value = text;
                searchDropdown.classList.remove('active');
                performSearch(text);
            }
        });
    }

    // Enter 키 검색
    searchInput.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            const query = searchInput.value.trim();
            if (query) {
                searchDropdown.classList.remove('active');
                performSearch(query);
            }
        }
    });

    // 인기 검색어 바 키워드 클릭
    const keywordTags = document.querySelectorAll('.keyword-tag');
    keywordTags.forEach(tag => {
        tag.addEventListener('click', () => {
            const text = tag.textContent.replace(/^\d+\.\s*/, '').trim();
            searchInput.value = text;
            performSearch(text);
        });
    });
}

function performSearch(query) {
    // 검색어 기록 API 호출 (비동기, 결과 무시)
    fetch('/api/keywords/search', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ query: query })
    }).catch(() => {});

    // 검색 결과 페이지로 이동 (URL 파라미터로 검색어 전달)
    window.location.href = `/?search=${encodeURIComponent(query)}`;
}

/* ===========================
   Popular Keywords
   =========================== */
class PopularKeywords {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        if (!this.container) return;
        
        this.loadKeywords();
    }

    async loadKeywords() {
        try {
            const response = await fetch('/api/keywords/v2/top5');
            const data = await response.json();
            
            if (data.success && data.data) {
                this.renderKeywords(data.data);
            } else {
                this.renderEmptyState();
            }
        } catch (error) {
            console.error('인기 검색어 로드 실패:', error);
            this.renderEmptyState();
        }
    }

    renderKeywords(keywords) {
        if (!keywords || keywords.length === 0) {
            this.renderEmptyState();
            return;
        }

        // Clear container
        this.container.innerHTML = '';

        // Render into dropdown list
        keywords.forEach(keyword => {
            const li = document.createElement('li');
            li.innerHTML = `<span class="keyword-rank">${keyword.rank}.</span> ${keyword.keyword}`;
            
            li.addEventListener('click', () => {
                const searchInput = document.getElementById('search-input');
                const searchDropdown = document.getElementById('search-dropdown');
                if (searchInput) {
                    searchInput.value = keyword.keyword;
                    if (searchDropdown) searchDropdown.classList.remove('active');
                    performSearch(keyword.keyword);
                }
            });
            this.container.appendChild(li);
        });
    }

    renderEmptyState() {
        this.container.innerHTML = '<li class="empty-keyword"><span style="color:#999;font-size:13px;">현재 인기 검색어가 없습니다</span></li>';
    }

    handleKeywordClick(keyword) {
        const searchInput = document.getElementById('search-input');
        if (searchInput) {
            searchInput.value = keyword;
            performSearch(keyword);
        }
    }
}

function initPopularKeywords() {
    // Initialize popular keywords component for all users inside the dropdown
    const keywordList = document.getElementById('search-keyword-list');
    if (keywordList) {
        new PopularKeywords('search-keyword-list');
    }
}
