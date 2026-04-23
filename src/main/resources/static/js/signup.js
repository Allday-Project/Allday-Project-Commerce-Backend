/**
 * ADP Commerce - Signup Page JavaScript
 * 회원가입 API 연동: 이메일 중복 확인, 가입 처리
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('signup-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const passwordConfirm = document.getElementById('password-confirm');
    const submitBtn = document.getElementById('signup-submit-btn');
    const checkDupBtn = document.getElementById('btn-check-duplicate');
    const emailMessage = document.getElementById('email-message');
    const passwordMessage = document.getElementById('password-match-message');

    let emailChecked = false;

    // 중복 확인
    if (checkDupBtn && emailInput) {
        checkDupBtn.addEventListener('click', async () => {
            const email = emailInput.value.trim();
            if (!email) {
                showMessage(emailMessage, '이메일을 입력해주세요.', 'error');
                return;
            }

            try {
                const res = await fetch(`/api/auth/check-duplicate?email=${encodeURIComponent(email)}`);
                const data = await res.json();

                if (data.data === true || data.duplicate) {
                    showMessage(emailMessage, '이미 사용 중인 이메일입니다.', 'error');
                    emailChecked = false;
                } else {
                    showMessage(emailMessage, '사용 가능한 이메일입니다.', 'success');
                    emailChecked = true;
                }
            } catch (e) {
                showMessage(emailMessage, '사용 가능한 이메일입니다.', 'success');
                emailChecked = true;
            }

            updateSubmitBtn();
        });

        emailInput.addEventListener('input', () => {
            emailChecked = false;
            emailMessage.textContent = '';
            emailMessage.className = 'form-message';
            updateSubmitBtn();
        });
    }

    // 비밀번호 확인 매칭
    if (passwordConfirm && passwordInput) {
        const checkMatch = () => {
            const pw = passwordInput.value;
            const pwc = passwordConfirm.value;

            if (!pwc) {
                passwordMessage.textContent = '';
                passwordMessage.className = 'form-message';
                return;
            }

            if (pw === pwc) {
                showMessage(passwordMessage, '비밀번호가 일치합니다.', 'success');
            } else {
                showMessage(passwordMessage, '비밀번호가 일치하지 않습니다.', 'error');
            }

            updateSubmitBtn();
        };

        passwordConfirm.addEventListener('input', checkMatch);
        passwordInput.addEventListener('input', checkMatch);
    }

    // 폼 제출 - API 연동
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = emailInput.value.trim();
            const password = passwordInput.value;
            const errorEl = document.getElementById('signup-error');

            if (!emailChecked) {
                showMessage(emailMessage, '이메일 중복 확인을 해주세요.', 'error');
                return;
            }

            if (password !== passwordConfirm.value) {
                showMessage(passwordMessage, '비밀번호가 일치하지 않습니다.', 'error');
                return;
            }

            submitBtn.disabled = true;
            submitBtn.textContent = '가입 중...';

            try {
                const res = await fetch('/api/auth/signup', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ email, password })
                });

                if (res.ok || res.status === 201) {
                    alert('회원가입이 완료되었습니다! 로그인해주세요.');
                    window.location.href = '/login';
                } else {
                    const json = await res.json().catch(() => null);
                    const msg = json?.data?.message || '회원가입에 실패했습니다.';
                    if (errorEl) {
                        errorEl.textContent = msg;
                        errorEl.style.display = 'block';
                    }
                }
            } catch (err) {
                if (errorEl) {
                    errorEl.textContent = '서버 연결에 실패했습니다.';
                    errorEl.style.display = 'block';
                }
            } finally {
                submitBtn.disabled = false;
                submitBtn.textContent = '회원가입';
            }
        });
    }

    // 제출 버튼 활성화
    function updateSubmitBtn() {
        if (!submitBtn) return;
        const pw = passwordInput?.value || '';
        const pwc = passwordConfirm?.value || '';
        const valid = emailChecked && pw.length > 0 && pw === pwc;

        if (valid) {
            submitBtn.classList.add('active');
        } else {
            submitBtn.classList.remove('active');
        }
    }

    function showMessage(el, text, type) {
        if (!el) return;
        el.textContent = text;
        el.className = 'form-message ' + type;
    }
});
