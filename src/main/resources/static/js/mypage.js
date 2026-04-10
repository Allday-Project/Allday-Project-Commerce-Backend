/**
 * ADP Commerce - MyPage JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    initEditButtons();
});

function initEditButtons() {
    // 이름 변경
    const btnEditName = document.getElementById('btn-edit-name');
    if (btnEditName) {
        btnEditName.addEventListener('click', () => {
            const valueEl = document.getElementById('info-name-value');
            toggleInlineEdit(valueEl, btnEditName, 'name');
        });
    }

    // 비밀번호 설정
    const btnEditPassword = document.getElementById('btn-edit-password');
    if (btnEditPassword) {
        btnEditPassword.addEventListener('click', () => {
            // TODO: 비밀번호 변경 모달/페이지
            alert('비밀번호 변경 기능은 준비 중입니다.');
        });
    }

    // 전화번호 설정
    const btnEditPhone = document.getElementById('btn-edit-phone');
    if (btnEditPhone) {
        btnEditPhone.addEventListener('click', () => {
            const valueEl = document.getElementById('info-phone-value');
            toggleInlineEdit(valueEl, btnEditPhone, 'phone');
        });
    }

    // 주소 설정
    const btnEditAddress = document.getElementById('btn-edit-address');
    if (btnEditAddress) {
        btnEditAddress.addEventListener('click', () => {
            const valueEl = document.getElementById('info-address-value');
            toggleInlineEdit(valueEl, btnEditAddress, 'address');
        });
    }
}

function toggleInlineEdit(valueEl, btn, fieldName) {
    if (!valueEl) return;

    // 이미 수정 모드면 저장
    const existingInput = valueEl.parentElement.querySelector('.inline-edit-input');
    if (existingInput) {
        valueEl.textContent = existingInput.value || valueEl.dataset.original;
        existingInput.remove();
        valueEl.style.display = '';
        btn.textContent = fieldName === 'name' ? '변경' : '설정';
        return;
    }

    // 수정 모드 진입
    valueEl.dataset.original = valueEl.textContent;
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'inline-edit-input';
    input.value = valueEl.textContent === '전화번호 설정' || valueEl.textContent === '주소 설정' ? '' : valueEl.textContent;
    input.placeholder = fieldName === 'phone' ? '010-1234-5678' : fieldName === 'address' ? '배송지 주소를 입력하세요' : '이름을 입력하세요';

    // 스타일
    input.style.cssText = 'font-size:14px;padding:6px 10px;border:1px solid #1a1a1a;border-radius:4px;outline:none;width:100%;max-width:300px;';

    valueEl.style.display = 'none';
    valueEl.parentElement.insertBefore(input, valueEl);
    input.focus();
    btn.textContent = '확인';

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            btn.click();
        }
    });
}
