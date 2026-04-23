/**
 * ADP Commerce - MyPage JavaScript
 * 마이페이지 API 연동: 프로필 조회, 수정
 */

document.addEventListener('DOMContentLoaded', () => {
    loadUserProfile();
    initEditButtons();
});

/* ===========================
   프로필 로드
   =========================== */
async function loadUserProfile() {
    try {
        const res = await fetch('/api/users/me', {
            method: 'GET',
            credentials: 'include'
        });

        if (res.status === 401 || res.status === 403) return;
        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const json = await res.json();
        if (!json.success || !json.data) return;

        const user = json.data;

        // 이메일
        const emailEl = document.getElementById('info-email-value');
        if (emailEl && user.email) emailEl.textContent = user.email;

        // 이름
        const nameEl = document.getElementById('info-name-value');
        if (nameEl && user.name) nameEl.textContent = user.name;

        // 전화번호
        const phoneEl = document.getElementById('info-phone-value');
        if (phoneEl) phoneEl.textContent = user.phone || '전화번호 설정';

        // 주소
        const addressEl = document.getElementById('info-address-value');
        if (addressEl) addressEl.textContent = user.address || '주소 설정';
    } catch (err) {
        console.error('프로필 로드 실패:', err);
    }
}

/* ===========================
   수정 버튼
   =========================== */
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
        const newValue = existingInput.value.trim();
        if (newValue) {
            valueEl.textContent = newValue;
            saveUserField(fieldName, newValue);
        } else {
            valueEl.textContent = valueEl.dataset.original;
        }
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

async function saveUserField(fieldName, value) {
    try {
        const body = {};
        body[fieldName] = value;

        const res = await fetch('/api/users/me', {
            method: 'PATCH',
            credentials: 'include',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (res.ok) {
            console.log(`${fieldName} 업데이트 성공`);
        } else {
            console.error(`${fieldName} 업데이트 실패: HTTP ${res.status}`);
            const errorJson = await res.json().catch(() => null);
            if (errorJson && errorJson.data && errorJson.data.message) {
                alert(errorJson.data.message);
            }
        }
    } catch (err) {
        console.error(`${fieldName} 업데이트 실패:`, err);
    }
}
