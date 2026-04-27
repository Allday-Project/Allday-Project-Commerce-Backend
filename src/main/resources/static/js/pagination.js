/**
 * ADP Commerce - Pagination Component
 * 페이지 네비게이션 컴포넌트 (10개 상품/페이지, 2행 5열)
 */

class Pagination {
    /**
     * @param {string} containerId - 페이지네이션 컨테이너 ID
     * @param {Object} options - 설정 옵션
     * @param {number} options.maxVisiblePages - 표시할 최대 페이지 번호 개수 (기본값: 5)
     */
    constructor(containerId, options = {}) {
        this.container = document.getElementById(containerId);
        if (!this.container) {
            console.error(`Pagination container not found: ${containerId}`);
            return;
        }

        this.currentPage = 1;
        this.totalPages = 1;
        this.maxVisiblePages = options.maxVisiblePages || 5;
        this.onPageChangeCallback = null;
    }

    /**
     * 페이지네이션 렌더링
     * @param {number} currentPage - 현재 페이지 번호
     * @param {number} totalPages - 전체 페이지 수
     */
    render(currentPage, totalPages) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;

        // 페이지가 1개 이하면 페이지네이션 숨김
        if (totalPages <= 1) {
            this.container.innerHTML = '';
            this.container.style.display = 'none';
            return;
        }

        this.container.style.display = 'flex';

        const paginationHTML = this._buildPaginationHTML();
        this.container.innerHTML = paginationHTML;

        this._attachEventListeners();
    }

    /**
     * 페이지네이션 HTML 생성
     * @private
     */
    _buildPaginationHTML() {
        const pages = this._calculateVisiblePages();
        let html = '<div class="pagination-wrapper">';

        // 이전 버튼
        html += `
            <button class="pagination-btn pagination-prev" 
                    ${this.currentPage === 1 ? 'disabled' : ''} 
                    data-action="prev">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="15 18 9 12 15 6"/>
                </svg>
            </button>
        `;

        // 페이지 번호
        html += '<div class="pagination-numbers">';

        // 첫 페이지
        if (pages.showFirstEllipsis) {
            html += `<button class="pagination-number" data-page="1">1</button>`;
            html += '<span class="pagination-ellipsis">...</span>';
        }

        // 중간 페이지들
        pages.visible.forEach(page => {
            const isActive = page === this.currentPage;
            html += `
                <button class="pagination-number ${isActive ? 'active' : ''}" 
                        data-page="${page}"
                        ${isActive ? 'aria-current="page"' : ''}>
                    ${page}
                </button>
            `;
        });

        // 마지막 페이지
        if (pages.showLastEllipsis) {
            html += '<span class="pagination-ellipsis">...</span>';
            html += `<button class="pagination-number" data-page="${this.totalPages}">${this.totalPages}</button>`;
        }

        html += '</div>';

        // 다음 버튼
        html += `
            <button class="pagination-btn pagination-next" 
                    ${this.currentPage === this.totalPages ? 'disabled' : ''} 
                    data-action="next">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="9 18 15 12 9 6"/>
                </svg>
            </button>
        `;

        html += '</div>';
        return html;
    }

    /**
     * 표시할 페이지 번호 계산
     * @private
     */
    _calculateVisiblePages() {
        const { currentPage, totalPages, maxVisiblePages } = this;
        const visible = [];
        let showFirstEllipsis = false;
        let showLastEllipsis = false;

        if (totalPages <= maxVisiblePages) {
            // 전체 페이지가 maxVisiblePages 이하면 모두 표시
            for (let i = 1; i <= totalPages; i++) {
                visible.push(i);
            }
        } else {
            // 현재 페이지를 중심으로 표시할 범위 계산
            const halfVisible = Math.floor(maxVisiblePages / 2);
            let startPage = Math.max(1, currentPage - halfVisible);
            let endPage = Math.min(totalPages, currentPage + halfVisible);

            // 시작이나 끝에 가까우면 범위 조정
            if (currentPage <= halfVisible + 1) {
                endPage = Math.min(totalPages, maxVisiblePages);
            } else if (currentPage >= totalPages - halfVisible) {
                startPage = Math.max(1, totalPages - maxVisiblePages + 1);
            }

            // 첫 페이지 생략 표시 여부
            if (startPage > 1) {
                showFirstEllipsis = true;
                startPage = Math.max(2, startPage);
            }

            // 마지막 페이지 생략 표시 여부
            if (endPage < totalPages) {
                showLastEllipsis = true;
                endPage = Math.min(totalPages - 1, endPage);
            }

            for (let i = startPage; i <= endPage; i++) {
                visible.push(i);
            }
        }

        return { visible, showFirstEllipsis, showLastEllipsis };
    }

    /**
     * 이벤트 리스너 연결
     * @private
     */
    _attachEventListeners() {
        // 페이지 번호 클릭
        const numberButtons = this.container.querySelectorAll('.pagination-number');
        numberButtons.forEach(btn => {
            btn.addEventListener('click', () => {
                const page = parseInt(btn.dataset.page, 10);
                this.goToPage(page);
            });
        });

        // 이전/다음 버튼 클릭
        const prevBtn = this.container.querySelector('[data-action="prev"]');
        const nextBtn = this.container.querySelector('[data-action="next"]');

        if (prevBtn) {
            prevBtn.addEventListener('click', () => this.previousPage());
        }

        if (nextBtn) {
            nextBtn.addEventListener('click', () => this.nextPage());
        }
    }

    /**
     * 특정 페이지로 이동
     * @param {number} pageNumber - 이동할 페이지 번호
     */
    goToPage(pageNumber) {
        if (pageNumber < 1 || pageNumber > this.totalPages || pageNumber === this.currentPage) {
            return;
        }

        this.currentPage = pageNumber;
        this.render(this.currentPage, this.totalPages);

        if (this.onPageChangeCallback) {
            this.onPageChangeCallback(pageNumber);
        }
    }

    /**
     * 다음 페이지로 이동
     */
    nextPage() {
        if (this.currentPage < this.totalPages) {
            this.goToPage(this.currentPage + 1);
        }
    }

    /**
     * 이전 페이지로 이동
     */
    previousPage() {
        if (this.currentPage > 1) {
            this.goToPage(this.currentPage - 1);
        }
    }

    /**
     * 페이지 변경 콜백 등록
     * @param {Function} callback - 페이지 변경 시 호출될 콜백 함수
     */
    onPageChange(callback) {
        this.onPageChangeCallback = callback;
    }

    /**
     * 현재 페이지 번호 반환
     * @returns {number}
     */
    getCurrentPage() {
        return this.currentPage;
    }

    /**
     * 전체 페이지 수 반환
     * @returns {number}
     */
    getTotalPages() {
        return this.totalPages;
    }
}
