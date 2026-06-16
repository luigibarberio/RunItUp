document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const emailInput = document.getElementById('email');
    const passwordInput = document.getElementById('password');
    const loginBtn = document.querySelector('.login-btn');
    
    // Funzione per mostrare il toast in caso di reindirizzamento proveniente dall'auth filter
    const showRedirectToast = () => {
        if (window.showToast) {
            window.showToast("Per accedere a questa pagina, effettua il login.", "error");
        }
    };

    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('redirected') === 'true') {
        showRedirectToast();
    }

    const state = {
        email: false,
        password: false
    };

    // --- Attiva il pulsante di login solo se entrambi i campi sono validi ---
    const checkFormValidity = () => {
        const isValid = Object.values(state).every(val => val === true);
        loginBtn.disabled = !(isValid);
    };

    // --- FUNZIONI DI VALIDAZIONE ---
    const validateEmail = () => {
        // 1. Controlla che non sia vuota
        if (!FormUtils.checkText('email', 'emailError', 'L\'email è obbligatoria.')){
            state.email = false;
        }else if (!FormUtils.checkRegex('email', 'emailError', FormUtils.emailRegex, 'Inserisci un formato email valido.')) {
            state.email = false;
        }
        else {
            state.email = true;
        }
        checkFormValidity();
    };

    const validatePassword = () => {
        // 1. Controlla che non sia vuota
        if (!FormUtils.checkText('password', 'passwordError', 'La password è obbligatoria.')) {
            state.password = false;
        } else if (passwordInput.value.length < 4) { // 2. Controllo manuale specifico per la lunghezza
            FormUtils.showError(passwordInput, 'passwordError', 'La password deve avere almeno 4 caratteri.');
            state.password = false;
        } else {
            state.password = true;
        }
        checkFormValidity();
    };

    // --- EVENT LISTENERS ---
    emailInput.addEventListener('blur', validateEmail);
    emailInput.addEventListener('input', () => {
        FormUtils.clearError(emailInput, 'emailError');
        state.email = false;
        checkFormValidity();
    });

    passwordInput.addEventListener('blur', validatePassword);
    passwordInput.addEventListener('input', validatePassword);

    // --- INVIO FORM ---
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const isEmailValid = state.email;
        const isPassValid = state.password;

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
                const overlay = document.getElementById('successOverlay');
                const title = document.getElementById('overlayTitle');
                const text = document.getElementById('overlayText');
                
                // Possiamo anche personalizzare i testi al volo se vogliamo
                title.textContent = "Accesso Eseguito!";
                text.textContent = "Benvenuto su RunItUp, ti stiamo reindirizzando...";
                
                overlay.classList.remove('hidden');
                
                // Redirect unificato
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