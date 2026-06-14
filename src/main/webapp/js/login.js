document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.querySelector('.login-btn');
    const successOverlay = document.getElementById('successOverlay');

    // --- FUNZIONI DI VALIDAZIONE ---
    const validateEmail = () => {
        // 1. Controlla che non sia vuota
        if (!FormUtils.checkText('email', 'emailError', 'L\'email è obbligatoria.')) return false;
        // 2. Controlla il formato Regex
        return FormUtils.checkRegex('email', 'emailError', FormUtils.emailRegex, 'Inserisci un formato email valido.');
    };

    const validatePassword = () => {
        // 1. Controlla che non sia vuota
        if (!FormUtils.checkText('password', 'passwordError', 'La password è obbligatoria.')) return false;
        
        // 2. Controllo manuale specifico per la lunghezza
        if (passwordInput.value.length < 4) {
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

        const isEmailValid = validateEmail();
        const isPassValid = validatePassword();
        if (!isEmailValid || !isPassValid) return;

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