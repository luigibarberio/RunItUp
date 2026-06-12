document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.querySelector('.login-btn');
    const successOverlay = document.getElementById('successOverlay');

    // --- FUNZIONI DI VALIDAZIONE ---
    const validateEmail = () => {
        FormUtils.clearError(emailInput, 'emailError');
        const value = emailInput.value.trim();
        
        if (!value) {
            FormUtils.showError(emailInput, 'emailError', 'L\'email è obbligatoria.');
            return false;
        } else if (!FormUtils.emailRegex.test(value)) {
            FormUtils.showError(emailInput, 'emailError', 'Inserisci un formato email valido.');
            return false;
        }
        return true;
    };

    const validatePassword = () => {
        FormUtils.clearError(passwordInput, 'passwordError');
        const value = passwordInput.value;
        
        if (!value) {
            FormUtils.showError(passwordInput, 'passwordError', 'La password è obbligatoria.');
            return false;
        } else if (value.length < 4) {
            FormUtils.showError(passwordInput, 'passwordError', 'La password deve avere almeno 4 caratteri.');
            return false;
        }
        return true;
    };

    // --- EVENT LISTENERS ---
    emailInput.addEventListener('blur', validateEmail);
    emailInput.addEventListener('input', () => FormUtils.clearError(emailInput, 'emailError'));

    passwordInput.addEventListener('blur', validatePassword);
    passwordInput.addEventListener('input', () => FormUtils.clearError(passwordInput, 'passwordError'));

    // --- INVIO FORM ---
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (!validateEmail() || !validatePassword()) return;

        loginBtn.disabled = true;
        loginBtn.querySelector('.btn-text').textContent = 'Accesso in corso...';

        try {
            const response = await fetch('login', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    email: emailInput.value.trim(),
                    password: passwordInput.value
                })
            });

            const data = await response.json();

            if (data.success) {
                successOverlay.classList.remove('hidden');
                setTimeout(() => window.location.href = data.redirectUrl, 1500);
            } else {
                if (window.showToast) window.showToast(data.message, "error");
                passwordInput.value = '';
                loginBtn.disabled = false;
                loginBtn.querySelector('.btn-text').textContent = 'Accedi';
            }
        } catch (error) {
            console.error("Errore Fetch:", error);
            if (window.showToast) window.showToast("Errore di comunicazione col server.", "error");
            loginBtn.disabled = false;
            loginBtn.querySelector('.btn-text').textContent = 'Accedi';
        }
    });
});