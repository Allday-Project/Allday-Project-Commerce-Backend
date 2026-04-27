/**
 * ADP Commerce - MyPage JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    loadMyProfile();
    initEditButtons();
    initSaveForm();
});

function loadMyProfile() {
    fetch('/api/users/me')
        .then(res => res.json())
        .then(data => {
            if (data.success && data.data) {
                const user = data.data;
                document.getElementById('info-email-value').textContent = user.maskedEmail || user.email || '이메일 정보 없음';
                
                const nameEl = document.getElementById('info-name-value');
                const btnName = document.getElementById('btn-edit-name');
                if (nameEl) nameEl.textContent = user.name || '이름 설정 필요';
                if (btnName) btnName.textContent = user.name ? '변경' : '설정';
                
                const phoneEl = document.getElementById('info-phone-value');
                const btnPhone = document.getElementById('btn-edit-phone');
                if (phoneEl) phoneEl.textContent = user.phone || '전화번호 설정';
                if (btnPhone) btnPhone.textContent = user.phone ? '변경' : '설정';
                
                const addressEl = document.getElementById('info-address-value');
                const btnAddress = document.getElementById('btn-edit-address');
                if (addressEl) addressEl.textContent = user.address || '주소 설정';
                if (btnAddress) btnAddress.textContent = user.address ? '변경' : '설정';
            }
        })
        .catch(err => {
            console.error('Failed to load profile:', err);
        });
}

function initEditButtons() {
    const btnEditName = document.getElementById('btn-edit-name');
    if (btnEditName) {
        btnEditName.addEventListener('click', () => {
            const valueEl = document.getElementById('info-name-value');
            toggleInlineEdit(valueEl, btnEditName, 'name');
        });
    }

    const btnEditPassword = document.getElementById('btn-edit-password');
    if (btnEditPassword) {
        btnEditPassword.addEventListener('click', () => {
            const currentPassword = prompt('현재 비밀번호를 입력해주세요.');
            if (!currentPassword) return;

            const newPassword = prompt('새로운 비밀번호를 입력해주세요.');
            if (newPassword) {
                if (newPassword.length < 8) {
                    alert('비밀번호는 8자 이상이어야 합니다.');
                    return;
                }
                const confirmPassword = prompt('비밀번호를 다시 한 번 입력해주세요.');
                if (newPassword !== confirmPassword) {
                    alert('비밀번호가 일치하지 않습니다.');
                    return;
                }
                
                fetch('/api/users/password', {
                    method: 'PATCH',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ currentPassword: currentPassword, newPassword: newPassword })
                })
                .then(res => res.json())
                .then(data => {
                    if (data.success) {
                        alert('비밀번호가 성공적으로 변경되었습니다.');
                    } else {
                        alert(data.message || '비밀번호 변경에 실패했습니다.');
                    }
                })
                .catch(err => {
                    alert('서버 오류가 발생했습니다.');
                });
            }
        });
    }

    const btnEditPhone = document.getElementById('btn-edit-phone');
    if (btnEditPhone) {
        btnEditPhone.addEventListener('click', () => {
            const valueEl = document.getElementById('info-phone-value');
            toggleInlineEdit(valueEl, btnEditPhone, 'phone');
        });
    }

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

    const existingInput = valueEl.parentElement.querySelector('.inline-edit-input');
    const existingError = valueEl.parentElement.querySelector('.inline-error-msg');
    
    if (existingInput) {
        let newVal = existingInput.value.trim();
        
        // Validation logic per field
        if (newVal) {
            if (fieldName === 'name') {
                const nameRegex = /^[가-힣]{2,10}$/;
                if (!nameRegex.test(newVal)) {
                    if (existingError) {
                        existingError.textContent = '이름은 한글 2~10자로 입력해주세요.';
                        existingError.style.display = 'block';
                    }
                    return;
                }
            }
            if (fieldName === 'phone') {
                let formattedPhone = newVal.replace(/[-\s]/g, '');
                if (!/^01[0-9]{8,9}$/.test(formattedPhone)) {
                    if (existingError) {
                        existingError.textContent = '올바른 전화번호를 입력해주세요. (예: 01012345678)';
                        existingError.style.display = 'block';
                    }
                    return;
                }
                if (formattedPhone.length === 11) {
                    newVal = formattedPhone.replace(/(\d{3})(\d{4})(\d{4})/, '$1-$2-$3');
                } else if (formattedPhone.length === 10) {
                    newVal = formattedPhone.replace(/(\d{3})(\d{3})(\d{4})/, '$1-$2-$3');
                }
            }
            if (fieldName === 'address' && newVal.length < 5) {
                if (existingError) {
                    existingError.textContent = '상세한 주소를 입력해주세요.';
                    existingError.style.display = 'block';
                }
                return;
            }
        }

        valueEl.textContent = newVal || valueEl.dataset.original;
        existingInput.remove();
        if (existingError) existingError.remove();
        valueEl.style.display = '';
        btn.textContent = newVal ? '변경' : '설정';
        return;
    }

    valueEl.dataset.original = valueEl.textContent;
    const input = document.createElement('input');
    input.type = 'text';
    input.className = 'inline-edit-input';
    
    input.value = ''; // 항상 빈 칸으로 표시
    input.placeholder = fieldName === 'phone' ? '01012345678' : fieldName === 'address' ? '배송지 주소를 입력하세요' : '이름을 입력하세요 (한글 2~10자)';

    input.style.cssText = 'font-size:14px;padding:6px 10px;border:1px solid #1a1a1a;border-radius:4px;outline:none;width:100%;max-width:300px;';

    const errorMsg = document.createElement('div');
    errorMsg.className = 'inline-error-msg';
    errorMsg.style.cssText = 'color: red; font-size: 12px; margin-top: 4px; display: none;';
    
    valueEl.style.display = 'none';
    valueEl.parentElement.insertBefore(input, valueEl);
    valueEl.parentElement.insertBefore(errorMsg, valueEl);
    input.focus();
    btn.textContent = '확인';

    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            btn.click();
        }
    });
}

function initSaveForm() {
    const form = document.getElementById('mypage-form');
    if (form) {
        form.addEventListener('submit', (e) => {
            e.preventDefault();
            
            const btnSave = document.getElementById('mypage-save-btn');
            btnSave.disabled = true;
            btnSave.textContent = '저장 중...';

            let nameVal = document.getElementById('info-name-value').textContent.trim();
            let phoneVal = document.getElementById('info-phone-value').textContent.trim();
            let addressVal = document.getElementById('info-address-value').textContent.trim();

            if (nameVal.includes('설정 필요') || nameVal.includes('설정')) nameVal = '';
            if (phoneVal.includes('설정')) phoneVal = '';
            if (addressVal.includes('설정')) addressVal = '';

            let hasError = false;
            
            // Remove old messages
            document.querySelectorAll('.save-error-msg').forEach(el => el.remove());

            if (!nameVal) {
                document.getElementById('info-name-value').parentElement.style.border = '1px solid red';
                const msg = document.createElement('div');
                msg.className = 'save-error-msg';
                msg.style.cssText = 'color: red; font-size: 12px; margin-top: 4px;';
                msg.textContent = '이름을 입력해주세요.';
                document.getElementById('info-name-value').parentElement.appendChild(msg);
                hasError = true;
            } else {
                document.getElementById('info-name-value').parentElement.style.border = '';
            }

            if (!phoneVal) {
                document.getElementById('info-phone-value').parentElement.style.border = '1px solid red';
                const msg = document.createElement('div');
                msg.className = 'save-error-msg';
                msg.style.cssText = 'color: red; font-size: 12px; margin-top: 4px;';
                msg.textContent = '전화번호를 입력해주세요.';
                document.getElementById('info-phone-value').parentElement.appendChild(msg);
                hasError = true;
            } else {
                document.getElementById('info-phone-value').parentElement.style.border = '';
            }

            if (!addressVal) {
                document.getElementById('info-address-value').parentElement.style.border = '1px solid red';
                const msg = document.createElement('div');
                msg.className = 'save-error-msg';
                msg.style.cssText = 'color: red; font-size: 12px; margin-top: 4px;';
                msg.textContent = '주소를 입력해주세요.';
                document.getElementById('info-address-value').parentElement.appendChild(msg);
                hasError = true;
            } else {
                document.getElementById('info-address-value').parentElement.style.border = '';
            }

            if (hasError) {
                btnSave.disabled = false;
                btnSave.textContent = '저장';
                return;
            }

            fetch('/api/users/me', {
                method: 'PATCH',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: nameVal,
                    phone: phoneVal,
                    address: addressVal
                })
            })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    alert('정보가 성공적으로 저장되었습니다.');
                    loadMyProfile();
                } else {
                    // 백엔드 validation 에러 등의 메시지를 출력합니다.
                    const errorMessage = data.message || '저장에 실패했습니다. 입력값을 확인해주세요.';
                    alert(errorMessage);
                }
            })
            .catch(err => {
                console.error('저장 에러:', err);
                alert('저장 중 오류가 발생했습니다.');
            })
            .finally(() => {
                btnSave.disabled = false;
                btnSave.textContent = '저장';
            });
        });
    }
}
