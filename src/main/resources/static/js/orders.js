/**
 * ADP Commerce - Orders Page JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    initShippingInfoButtons();
    initOrderActionButtons();
});

function initShippingInfoButtons() {
    document.querySelectorAll('.btn-shipping-info').forEach(btn => {
        btn.addEventListener('click', () => {
            // TODO: 배송지 정보 모달/팝업
            alert('배송지 정보를 확인합니다.');
        });
    });
}

function initOrderActionButtons() {
    document.querySelectorAll('.btn-order-action').forEach(btn => {
        btn.addEventListener('click', () => {
            const action = btn.textContent.trim();
            if (action === '환불 요청') {
                if (confirm('환불을 요청하시겠습니까?')) {
                    // TODO: 환불 API 호출
                    btn.textContent = '환불 처리 중';
                    btn.disabled = true;
                    btn.style.opacity = '0.5';
                }
            } else if (action === '리뷰 요청') {
                // TODO: 리뷰 작성 페이지로 이동
                console.log('리뷰 작성');
            }
        });
    });
}
