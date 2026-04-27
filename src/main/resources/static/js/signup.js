/**
 * ADP Commerce - Signup Page JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('signup-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const passwordConfirmInput = document.getElementById('password-confirm');
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
                emailChecked = false;
                updateSubmitBtn();
                return;
            }

            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(email)) {
                showMessage(emailMessage, '올바른 이메일 형식을 입력해주세요.', 'error');
                emailChecked = false;
                updateSubmitBtn();
                return;
            }

            try {
                const res = await fetch(`/api/auth/check-duplicate?email=${encodeURIComponent(email)}`);
                if (!res.ok) throw new Error('API error');
                const data = await res.json();

                if (data.data === true) {
                    showMessage(emailMessage, '이미 사용 중인 이메일입니다.', 'error');
                    emailChecked = false;
                } else {
                    showMessage(emailMessage, '사용 가능한 이메일입니다.', 'success');
                    emailChecked = true;
                }
            } catch (e) {
                showMessage(emailMessage, '중복 확인에 실패했습니다. 다시 시도해주세요.', 'error');
                emailChecked = false;
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
    if (passwordConfirmInput && passwordInput) {
        const checkMatch = () => {
            const pw = passwordInput.value;
            const pwc = passwordConfirmInput.value;

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

        passwordConfirmInput.addEventListener('input', checkMatch);
        passwordInput.addEventListener('input', checkMatch);
    }

    // 제출 버튼 활성화
    function updateSubmitBtn() {
        if (!submitBtn) return;
        const pw = passwordInput?.value || '';
        const pwc = passwordConfirmInput?.value || '';
        const valid = emailChecked && pw.length > 0 && pw === pwc;

        if (valid) {
            submitBtn.classList.add('active');
        } else {
            submitBtn.classList.remove('active');
        }
    }

    // 폼 제출 처리
    if (form) {
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            const email = emailInput?.value?.trim();
            const password = passwordInput?.value?.trim();
            const pwConfirmValue = passwordConfirmInput?.value?.trim();

            // 클라이언트 사이드 검증
            if (!email || !password || !pwConfirmValue) {
                showGlobalError('모든 필드를 입력해주세요.');
                return;
            }

            if (!emailChecked) {
                showGlobalError('이메일 중복 확인을 해주세요.');
                return;
            }

            if (password !== pwConfirmValue) {
                showGlobalError('비밀번호가 일치하지 않습니다.');
                return;
            }

            // 로딩 상태 표시
            setLoading(true);

            try {
                const response = await fetch('/api/auth/signup', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                    body: JSON.stringify({
                        email: email,
                        password: password
                    })
                });

                const data = await response.json();

                if (response.ok && data.success) {
                    // 회원가입 성공
                    showGlobalSuccess('회원가입이 완료되었습니다! 로그인 페이지로 이동합니다.');
                    window.location.href = '/login';
                } else {
                    // 회원가입 실패
                    const errorMessage = data.message || '회원가입에 실패했습니다. 다시 시도해주세요.';
                    showGlobalError(errorMessage);
                    setLoading(false);
                }
            } catch (error) {
                console.error('Signup error:', error);
                showGlobalError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
                setLoading(false);
            }
        });
    }

    function showMessage(el, text, type) {
        if (!el) return;
        el.textContent = text;
        el.className = 'form-message ' + type;
    }

    function showGlobalError(message) {
        const errorDiv = document.getElementById('signup-error');
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
            errorDiv.className = 'signup-error error';
        }
    }

    function showGlobalSuccess(message) {
        const errorDiv = document.getElementById('signup-error');
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
            errorDiv.className = 'signup-error success';
        }
    }

    function setLoading(loading) {
        if (submitBtn) {
            submitBtn.disabled = loading;
            submitBtn.textContent = loading ? '회원가입 중...' : '회원가입';
        }
    }
});