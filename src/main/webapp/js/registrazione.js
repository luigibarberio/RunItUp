document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('registerForm');
    const registerBtn = document.getElementById('registerBtn');
    
    // Campi
    const inputs = {
        nome: document.getElementById('nome'),
        cognome: document.getElementById('cognome'),
        email: document.getElementById('email'),
        prefisso: document.getElementById('prefisso'),
        telefono: document.getElementById('telefono'),
        password: document.getElementById('password'),
        conferma: document.getElementById('confermaPassword')
    };

    // Stato validità campi
    const state = {
        nome: false,
        cognome: false,
        email: false,
        password: false,
        conferma: false,
        telefono: true // True di default perché opzionale
    };

    // Abilita il bottone solo se TUTTO lo stato è true
    const checkFormValidity = () => {
        const isValid = Object.values(state).every(val => val === true);
        registerBtn.disabled = !isValid;
    };

    // --- FUNZIONI DI VALIDAZIONE ---
    const validateTesto = (campo, minLen, errorId) => {
        FormUtils.clearError(inputs[campo], errorId);
        const val = inputs[campo].value.trim();
        if (val.length < minLen) {
            FormUtils.showError(inputs[campo], errorId, `Minimo ${minLen} caratteri.`);
            state[campo] = false;
        } else {
            state[campo] = true;
        }
        checkFormValidity();
    };

    const validateEmail = async () => {
        FormUtils.clearError(inputs.email, 'emailError');
        state.email = false; 
        checkFormValidity();

        const val = inputs.email.value.trim();
        if (!FormUtils.emailRegex.test(val)) {
            FormUtils.showError(inputs.email, 'emailError', 'Formato email non valido.');
            return;
        }

        // ==========================================
        // PUNTO D'ESAME: Controllo AJAX Email Live
        // ==========================================
        try {
            const response = await fetch(`check-email?email=${encodeURIComponent(val)}`);
            const data = await response.json();

            if (data.esiste) {
                FormUtils.showError(inputs.email, 'emailError', 'Questa email è già registrata.');
            } else {
                state.email = true; // Email perfetta e libera!
            }
        } catch (e) {
            console.error("Errore verifica email", e);
            state.email = true; // Permettiamo l'invio per non bloccare se il check fallisce
        }
        checkFormValidity();
    };

    const validatePhone = () => {
        FormUtils.clearError(inputs.telefono, 'telefonoError');
        const val = inputs.telefono.value.trim();
        if (val !== "" && !FormUtils.phoneRegex.test(val)) {
            FormUtils.showError(inputs.telefono, 'telefonoError', 'Formato numero di telefono non valido.');
            state.telefono = false;
        } else {
            state.telefono = true;
        }
        checkFormValidity();
    };

    const validatePassword = () => {
        FormUtils.clearError(inputs.password, 'passwordError');
        const val = inputs.password.value;
        if (val.length < 8) {
            FormUtils.showError(inputs.password, 'passwordError', 'Minimo 8 caratteri.');
            state.password = false;
        } else {
            state.password = true;
        }
        validateConferma(); // Ricontrolla la conferma se cambio la pass principale
    };

    const validateConferma = () => {
        FormUtils.clearError(inputs.conferma, 'confermaPasswordError');
        const p1 = inputs.password.value;
        const p2 = inputs.conferma.value;
        if (p2 === "") {
            state.conferma = false;
        } else if (p1 !== p2) {
            FormUtils.showError(inputs.conferma, 'confermaPasswordError', 'Le password non coincidono.');
            state.conferma = false;
        } else {
            state.conferma = true;
        }
        checkFormValidity();
    };

    // --- EVENT LISTENERS (BLUR e INPUT) ---
    inputs.nome.addEventListener('blur', () => validateTesto('nome', 2, 'nomeError'));
    inputs.nome.addEventListener('input', () => validateTesto('nome', 2, 'nomeError'));

    inputs.cognome.addEventListener('blur', () => validateTesto('cognome', 2, 'cognomeError'));
    inputs.cognome.addEventListener('input', () => validateTesto('cognome', 2, 'cognomeError'));

    inputs.email.addEventListener('blur', validateEmail); // L'AJAX scatta al BLUR (quando si esce dal campo)
    inputs.email.addEventListener('input', () => {
        FormUtils.clearError(inputs.email, 'emailError');
        state.email = false; // Disabilita il form mentre digita
        checkFormValidity();
    });

    inputs.telefono.addEventListener('blur', validatePhone);
    inputs.telefono.addEventListener('input', validatePhone);

    inputs.password.addEventListener('blur', validatePassword);
    inputs.password.addEventListener('input', validatePassword);

    inputs.conferma.addEventListener('blur', validateConferma);
    inputs.conferma.addEventListener('input', validateConferma);

    // --- INVIO FORM ---
    form.addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!Object.values(state).every(val => val === true)) return;

        registerBtn.disabled = true;
        registerBtn.querySelector('.btn-text').textContent = 'Registrazione...';

        try {
            const response = await fetch('registrazione', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    nome: inputs.nome.value.trim(),
                    cognome: inputs.cognome.value.trim(),
                    email: inputs.email.value.trim(),
                    prefisso: document.getElementById('prefisso').value,
                    telefono: inputs.telefono.value.trim(),
                    password: inputs.password.value
                })
            });

            const data = await response.json();

            if (data.success) {
                document.getElementById('successOverlay').classList.remove('hidden');
                setTimeout(() => window.location.href = data.redirectUrl, 2000);
            } else {
                if (window.showToast) window.showToast(data.message, "error");
                registerBtn.disabled = false;
                registerBtn.querySelector('.btn-text').textContent = 'Registrati';
            }
        } catch (error) {
            console.error("Errore Fetch:", error);
            if (window.showToast) window.showToast("Errore di connessione.", "error");
            registerBtn.disabled = false;
            registerBtn.querySelector('.btn-text').textContent = 'Registrati';
        }
    });
});