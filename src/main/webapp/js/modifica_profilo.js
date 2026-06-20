// Gestione del modulo di modifica profilo - Validazione, abilitazione pulsante e invio asincrono

document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('editProfileForm');
    const saveBtn = document.getElementById('saveBtn');
    const btnText = saveBtn.querySelector('.btn-text');
    
    // Riferimenti ai campi di input del modulo
    const inputs = {
        nome: document.getElementById('nome'),
        cognome: document.getElementById('cognome'),
        prefisso: document.getElementById('prefisso'),
        telefono: document.getElementById('telefono'),
        pwdAttuale: document.getElementById('passwordAttuale'),
        pwdNuova: document.getElementById('nuovaPassword'),
        pwdConferma: document.getElementById('confermaPassword')
    };

    // --- LOGICA DI ABILITAZIONE LIVE DEL PULSANTE "APPLICA MODIFICHE" ---
    const checkFormChanges = () => {
        const currentNome = inputs.nome.value.trim();
        const currentCognome = inputs.cognome.value.trim();
        const currentTelefono = inputs.telefono.value.trim();
        const currentPrefisso = inputs.prefisso.value;
        
        const pwdA = inputs.pwdAttuale.value;
        const pwdN = inputs.pwdNuova.value;
        const pwdC = inputs.pwdConferma.value;

        // I campi obbligatori di base non possono mai essere lasciati vuoti
        if (currentNome === '' || currentCognome === '') {
            saveBtn.disabled = true;
            return;
        }

        // Verifica se almeno un dato anagrafico si discosta dal valore iniziale inserito nel data-attribute
        const anagraficaModificata = (
            currentNome !== inputs.nome.dataset.initial ||
            currentCognome !== inputs.cognome.dataset.initial ||
            currentTelefono !== inputs.telefono.dataset.initial ||
            currentPrefisso !== inputs.prefisso.dataset.initial
        );

        // Verifica se l'utente sta provando a modificare la password
        let passwordModificata = false;
        const haToccatoPassword = (pwdA !== '' || pwdN !== '' || pwdC !== '');

        if (haToccatoPassword) {
            // Per abilitare validamente il form sul cambio password, tutti e tre i campi devono soddisfare i criteri formali minimi, altrimenti il pulsante viene forzatamente bloccato
            if (pwdA !== '' && pwdN.length >= 8 && pwdN === pwdC) {
                passwordModificata = true;
            } else {
                saveBtn.disabled = true;
                return;
            }
        }

        // Il pulsante si attiva solo se c'è una modifica reale all'anagrafica OPPURE una password correttamente compilata
        saveBtn.disabled = !(anagraficaModificata || passwordModificata);
    };

    // --- FUNZIONI DI VALIDAZIONE FORMALE (Eseguite al focus out / blur) ---
    const validateNome = () => {
        // Regex allineata al backend: accetta lettere, spazi, accenti e apostrofi
        return FormUtils.checkRegex('nome', 'nomeError', /^[a-zA-ZÀ-ÿ\s']{2,50}$/, "Nome non valido (solo lettere, min 2).");
    };

    const validateCognome = () => {
        return FormUtils.checkRegex('cognome', 'cognomeError', /^[a-zA-ZÀ-ÿ\s']{2,50}$/, "Cognome non valido (solo lettere, min 2).");
    };

    const validateTelefono = () => {
        const val = inputs.telefono.value.trim();
        if (val === '') {
            FormUtils.clearError(inputs.telefono, 'telefonoError');
            return true;
        }
        // Regex allineata al backend: numeri di cellulare italiani o fissi validi
        return FormUtils.checkRegex('telefono', 'telefonoError', /^(3\d{9}|0\d{6,10})$/, "Numero non valido. Inserisci un numero reale.");
    };

    const validatePasswordAttuale = () => {
        if (inputs.pwdNuova.value !== '' || inputs.pwdConferma.value !== '') {
            if (inputs.pwdAttuale.value === '') {
                FormUtils.showError(inputs.pwdAttuale, 'passwordAttualeError', "Inserisci la password attuale per confermare.");
                return false;
            }
        }
        FormUtils.clearError(inputs.pwdAttuale, 'passwordAttualeError');
        return true;
    };

    // Metodi per la validazione della nuova password e della conferma password
    const validateNuovaPassword = () => {
        const val = inputs.pwdNuova.value;
        if (val === '' && inputs.pwdConferma.value === '' && inputs.pwdAttuale.value === '') {
            FormUtils.clearError(inputs.pwdNuova, 'nuovaPasswordError');
            return true;
        }
        if (val.length < 8) {
            FormUtils.showError(inputs.pwdNuova, 'nuovaPasswordError', "La nuova password deve contenere almeno 8 caratteri.");
            return false;
        }
        FormUtils.clearError(inputs.pwdNuova, 'nuovaPasswordError');
        return true;
    };

    const validateConfermaPassword = () => {
        const valNuova = inputs.pwdNuova.value;
        const valConf = inputs.pwdConferma.value;
        if (valNuova === '' && valConf === '' && inputs.pwdAttuale.value === '') {
            FormUtils.clearError(inputs.pwdConferma, 'confermaPasswordError');
            return true;
        }
        if (valNuova !== valConf) {
            FormUtils.showError(inputs.pwdConferma, 'confermaPasswordError', "Le password non coincidono.");
            return false;
        }
        FormUtils.clearError(inputs.pwdConferma, 'confermaPasswordError');
        return true;
    };

    // Funzione di validazione globale che esegue tutte le singole validazioni e restituisce un booleano complessivo sulla validità del form (utilizzata prima dell'invio)
    const validateAll = () => {
        let valid = true;
        if (!validateNome()) valid = false;
        if (!validateCognome()) valid = false;
        if (!validateTelefono()) valid = false;
        if (!validatePasswordAttuale()) valid = false;
        if (!validateNuovaPassword()) valid = false;
        if (!validateConfermaPassword()) valid = false;
        return valid;
    };

    // --- Inserimento degli event listeners (Gestione Input/Blur Separata) ---
    
    // Per Nome
    inputs.nome.addEventListener('input', () => {
        FormUtils.clearError(inputs.nome, 'nomeError'); // Svuota immediatamente l'errore alla digitazione
        checkFormChanges();
    });
    inputs.nome.addEventListener('blur', validateNome);

    // Per Cognome
    inputs.cognome.addEventListener('input', () => {
        FormUtils.clearError(inputs.cognome, 'cognomeError');
        checkFormChanges();
    });
    inputs.cognome.addEventListener('blur', validateCognome);

    // Per Telefono e Prefisso
    inputs.telefono.addEventListener('input', () => {
        FormUtils.clearError(inputs.telefono, 'telefonoError');
        checkFormChanges();
    });
    inputs.telefono.addEventListener('blur', validateTelefono);
    inputs.prefisso.addEventListener('change', checkFormChanges);

    // Per i campi Password
    inputs.pwdAttuale.addEventListener('input', () => {
        FormUtils.clearError(inputs.pwdAttuale, 'passwordAttualeError');
        checkFormChanges();
    });
    inputs.pwdAttuale.addEventListener('blur', validatePasswordAttuale);

    inputs.pwdNuova.addEventListener('input', () => {
        FormUtils.clearError(inputs.pwdNuova, 'nuovaPasswordError');
        checkFormChanges();
    });
    inputs.pwdNuova.addEventListener('blur', validateNuovaPassword);

    inputs.pwdConferma.addEventListener('input', () => {
        FormUtils.clearError(inputs.pwdConferma, 'confermaPasswordError');
        checkFormChanges();
    });
    inputs.pwdConferma.addEventListener('blur', validateConfermaPassword);


    // --- Invio asincrono del payload (Fetch API) ---
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Controllo di sicurezza finale prima della fetch
        if (!validateAll()) return;

        saveBtn.disabled = true;
        const originalText = btnText.textContent;
        btnText.textContent = "Salvataggio...";

        // Creazione del Payload con prefisso e telefono rigorosamente separati
        const payload = {
            nome: inputs.nome.value.trim(),
            cognome: inputs.cognome.value.trim(),
            prefisso: inputs.prefisso.value,
            telefono: inputs.telefono.value.trim(),
            passwordAttuale: inputs.pwdAttuale.value,
            nuovaPassword: inputs.pwdNuova.value,
            confermaPassword: inputs.pwdConferma.value
        };

        try {
            // Utilizziamo la rotta relativa 'modifica' coerentemente con la posizione della JSP
            const response = await fetch('modifica', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (data.success) {
                // Rimuoviamo la classe 'hidden' dall'overlay per mostrare l'animazione di successo
                const overlay = document.getElementById('successOverlay');
                if (overlay) {
                    overlay.classList.remove('hidden');
                }
                
                // Reindirizzamento temporizzato guidato dall'URL di risposta del server
                setTimeout(() => {
                    window.location.href = data.redirectUrl;
                }, 1500);
            } else {
                // Errore restituito logicamente dal server (es: password attuale non coincidente)
                if (window.showToast) {
                    window.showToast(data.message, "error");
                }
                saveBtn.disabled = false;
                btnText.textContent = originalText;
            }
        } catch (error) {
            console.error("Errore riscontrato durante l'invio della fetch:", error);
            if (window.showToast) {
                window.showToast("Impossibile connettersi al server. Riprova più tardi.", "error");
            }
            saveBtn.disabled = false;
            btnText.textContent = originalText;
        }
    });
});