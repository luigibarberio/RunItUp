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

    // --- FUNZIONI DI VALIDAZIONE SNELLITE CON FORMUTILS ---
    const validateTesto = (campo, minLen, errorId) => {
        // 1. Controllo vuoto centralizzato
        if (!FormUtils.checkText(campo, errorId, 'Campo obbligatorio.')) {
            state[campo] = false;
        } 
        // 2. Controllo manuale aggiuntivo per la lunghezza minima
        else if (inputs[campo].value.trim().length < minLen) {
            FormUtils.showError(inputs[campo], errorId, `Minimo ${minLen} caratteri.`);
            state[campo] = false;
        } 
        else {
            state[campo] = true;
        }
        checkFormValidity();
    };

    const validateEmail = async () => {
        state.email = false; 
        checkFormValidity();

        // Controlli base con FormUtils (Vuoto e Regex)
        if (!FormUtils.checkText('email', 'emailError', 'L\'email è obbligatoria.')) return;
        if (!FormUtils.checkRegex('email', 'emailError', FormUtils.emailRegex, 'Formato email non valido.')) return;

        // ==========================================
        // PUNTO D'ESAME: Controllo AJAX Email Live
        // ==========================================
        try {
            const response = await fetch(`check-email?email=${encodeURIComponent(inputs.email.value.trim())}`);
            const data = await response.json();

            if (data.esiste) {
                FormUtils.showError(inputs.email, 'emailError', 'Questa email è già registrata.');
            } else {
                FormUtils.clearError(inputs.email, 'emailError');
                state.email = true; // Email perfetta e libera!
            }
        } catch (e) {
            console.error("Errore verifica email", e);
            state.email = true; // Permettiamo l'invio per non bloccare se il check fallisce
        }
        checkFormValidity();
    };

    const validatePhone = () => {
        const val = inputs.telefono.value.trim();
        if (val === "") {
            // Se è vuoto (ed è opzionale), puliamo l'errore e diamo il via libera
            FormUtils.clearError(inputs.telefono, 'telefonoError');
            state.telefono = true;
        } else {
            // Se c'è scritto qualcosa, controlliamo che sia un numero valido
            state.telefono = FormUtils.checkRegex('telefono', 'telefonoError', FormUtils.phoneRegex, 'Formato numero non valido.');
        }
        checkFormValidity();
    };

    const validatePassword = () => {
        if (!FormUtils.checkText('password', 'passwordError', 'La password è obbligatoria.')) {
            state.password = false;
        } else if (inputs.password.value.length < 8) {
            FormUtils.showError(inputs.password, 'passwordError', 'Minimo 8 caratteri.');
            state.password = false;
        } else {
            state.password = true;
        }
        validateConferma(); // Ricontrolla la conferma se cambio la pass principale
    };

    const validateConferma = () => {
        const p1 = inputs.password.value;
        const p2 = inputs.conferma.value;

        if (!FormUtils.checkText('confermaPassword', 'confermaPasswordError', 'Conferma la password.')) {
            state.conferma = false;
        } else if (p1 !== p2) {
            FormUtils.showError(inputs.conferma, 'confermaPasswordError', 'Le password non coincidono.');
            state.conferma = false;
        } else {
            FormUtils.clearError(inputs.conferma, 'confermaPasswordError');
            state.conferma = true;
        }
        checkFormValidity();
    };

    // --- EVENT LISTENERS (BLUR e INPUT) ---
    inputs.nome.addEventListener('blur', () => validateTesto('nome', 2, 'nomeError'));
    inputs.nome.addEventListener('input', () => validateTesto('nome', 2, 'nomeError'));

    inputs.cognome.addEventListener('blur', () => validateTesto('cognome', 2, 'cognomeError'));
    inputs.cognome.addEventListener('input', () => validateTesto('cognome', 2, 'cognomeError'));

    inputs.email.addEventListener('blur', validateEmail); 
    inputs.email.addEventListener('input', () => {
        FormUtils.clearError(inputs.email, 'emailError');
        state.email = false; // Disabilita il form mentre digita per forzare il re-check
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
        registerBtn.querySelector('.btn-text').textContent = 'Registrazione in corso...';

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