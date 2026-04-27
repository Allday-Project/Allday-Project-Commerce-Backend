/**
 * ADP Commerce - Login Page JavaScript
 */

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('login-form');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const submitBtn = document.getElementById('login-submit-btn');
    const errorDiv = document.getElementById('login-error');

    if (!form) return;

    // 폼 제출 처리
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = emailInput?.value?.trim();
        const password = passwordInput?.value?.trim();

        // 클라이언트 사이드 검증
        if (!email || !password) {
            showError('이메일과 비밀번호를 모두 입력해주세요.');
            return;
        }

        // 로딩 상태 표시
        setLoading(true);

        try {
            const response = await fetch('/api/auth/login', {
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
                // 로그인 성공
                showSuccess('로그인 성공! 메인 페이지로 이동합니다.');
                window.location.href = '/';
            } else {
                // 로그인 실패 (비밀번호 불일치 등)
                const errorMessage = data.message || '로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요.';
                showError(errorMessage);
                setLoading(false);
            }
        } catch (error) {
            console.error('Login error:', error);
            showError('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            setLoading(false);
        }
    });

    function showError(message) {
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
            errorDiv.className = 'login-error error';
        }
    }

    function showSuccess(message) {
        if (errorDiv) {
            errorDiv.textContent = message;
            errorDiv.style.display = 'block';
            errorDiv.className = 'login-error success';
        }
    }

    function setLoading(loading) {
        if (submitBtn) {
            submitBtn.disabled = loading;
            submitBtn.textContent = loading ? '로그인 중...' : '로그인';
        }
    }
});