/**
 * RUNITUP - Logica Client-Side del Checkout
 * Pattern a Stati per disabilitazione automatica bottone, 
 * gestione Coupon e inoltro asincrono.
 */
document.addEventListener('DOMContentLoaded', () => {
    
    const form = document.getElementById('checkoutForm');
    const paymentRadios = document.querySelectorAll('input[name="metodoPagamento"]');
    const creditCardFields = document.getElementById('creditCardFields');
    const btnInviaOrdine = document.getElementById('btnInviaOrdine');
    const originalBtnText = btnInviaOrdine.innerHTML; // Salviamo il testo col prezzo generato dalla JSP!

    // Mappa degli input
    const inputs = {
        nome: document.getElementById('nome'),
        cognome: document.getElementById('cognome'),
        indirizzo: document.getElementById('indirizzo'),
        citta: document.getElementById('citta'),
        cap: document.getElementById('cap'),
        numeroCarta: document.getElementById('numeroCarta'),
        scadenzaCarta: document.getElementById('scadenzaCarta'),
        cvvCarta: document.getElementById('cvvCarta'),
        codiceCoupon: document.getElementById('codiceCoupon')
    };

    // --- 1. GESTIONE DELLO STATO (Pattern usato in registrazione.js) ---
    // Nome e Cognome sono precompilati dalla JSP, quindi partono a true se non sono vuoti
    const state = {
        nome: inputs.nome.value.trim() !== '',
        cognome: inputs.cognome.value.trim() !== '',
        indirizzo: false,
        citta: false,
        cap: false,
        numeroCarta: false,
        scadenzaCarta: false,
        cvvCarta: false
    };

    // Attiva il bottone solo se i campi richiesti per il metodo scelto sono validi
    const checkFormValidity = () => {
        let isValid = state.nome && state.cognome && state.indirizzo && state.citta && state.cap;
        
        const metodoSelezionato = document.querySelector('input[name="metodoPagamento"]:checked').value;
        if (metodoSelezionato === 'CARTA') {
            isValid = isValid && state.numeroCarta && state.scadenzaCarta && state.cvvCarta;
        }

        btnInviaOrdine.disabled = !isValid;
    };

    // Chiamata iniziale per impostare il bottone al caricamento pagina
    checkFormValidity();

    // --- 2. FUNZIONI DI VALIDAZIONE E AGGIORNAMENTO STATO ---
    const validateTesto = (campo, errorId, msg) => {
        if (inputs[campo].value.trim() === '') {
            FormUtils.showError(inputs[campo], errorId, msg);
            state[campo] = false;
        } else {
            FormUtils.clearError(inputs[campo], errorId);
            state[campo] = true;
        }
        checkFormValidity();
    };

    const validateRegex = (campo, errorId, regex, msg) => {
        // Se il campo è invisibile (es. carta non selezionata), lo ignoriamo
        if (inputs[campo].closest('.hidden')) return;

        if (!regex.test(inputs[campo].value.trim())) {
            FormUtils.showError(inputs[campo], errorId, msg);
            state[campo] = false;
        } else {
            FormUtils.clearError(inputs[campo], errorId);
            state[campo] = true;
        }
        checkFormValidity();
    };

    // --- 3. ASSEGNAZIONE EVENTI BLUR / INPUT ---
    const setupLiveValidation = (campo, fn) => {
        inputs[campo].addEventListener('blur', fn);
        inputs[campo].addEventListener('input', () => {
            // Appena digita, togliamo il rosso ma forziamo lo stato a false finché non esce (blur)
            FormUtils.clearError(inputs[campo], campo + 'Error');
            state[campo] = false; 
            checkFormValidity();
        });
    };

    setupLiveValidation('nome', () => validateTesto('nome', 'nomeError', 'Campo obbligatorio.'));
    setupLiveValidation('cognome', () => validateTesto('cognome', 'cognomeError', 'Campo obbligatorio.'));
    setupLiveValidation('indirizzo', () => validateTesto('indirizzo', 'indirizzoError', 'Campo obbligatorio.'));
    setupLiveValidation('citta', () => validateTesto('citta', 'cittaError', 'Campo obbligatorio.'));
    
    setupLiveValidation('cap', () => validateRegex('cap', 'capError', FormUtils.capRegex, 'Inserisci 5 cifre.'));
    setupLiveValidation('numeroCarta', () => validateRegex('numeroCarta', 'numeroCartaError', FormUtils.ccRegex, 'Inserisci 16 cifre.'));
    setupLiveValidation('scadenzaCarta', () => validateRegex('scadenzaCarta', 'scadenzaCartaError', FormUtils.expRegex, 'Formato MM/AA.'));
    setupLiveValidation('cvvCarta', () => validateRegex('cvvCarta', 'cvvCartaError', FormUtils.cvvRegex, 'Codice di 3 o 4 cifre.'));


    // --- 4. GESTIONE DINAMICA METODO PAGAMENTO ---
    paymentRadios.forEach(radio => {
        radio.addEventListener('change', (e) => {
            if (e.target.value === 'CARTA') {
                creditCardFields.classList.remove('hidden');
            } else {
                creditCardFields.classList.add('hidden');
                FormUtils.clearError(inputs.numeroCarta, 'numeroCartaError');
                FormUtils.clearError(inputs.scadenzaCarta, 'scadenzaCartaError');
                FormUtils.clearError(inputs.cvvCarta, 'cvvCartaError');
            }
            // Importante: Ricalcola la validità perché i requisiti sono cambiati!
            checkFormValidity(); 
        });
    });

    // --- 5. LOGICA COUPON (Fetch Asincrona) ---
    const btnApplicaCoupon = document.getElementById('btnApplicaCoupon');
    const couponMessage = document.getElementById('couponMessage');

    btnApplicaCoupon.addEventListener('click', async () => {
        const codice = inputs.codiceCoupon.value.trim().toUpperCase();
        
        if (codice === '') {
            couponMessage.style.color = 'var(--promozioni)';
            couponMessage.textContent = 'Inserisci un codice prima di applicare.';
            return;
        }

        btnApplicaCoupon.disabled = true;
        btnApplicaCoupon.textContent = '...';

        try {
            const response = await fetch(`valida-coupon?codice=${encodeURIComponent(codice)}`);
            const data = await response.json();

            if (data.success) {
                couponMessage.style.color = 'var(--conferme)';
                couponMessage.textContent = `Coupon applicato! Sconto di ${data.scontoRilevato}`;
                
                // --- Aggiornamento Dinamico della Sidebar Costi ---
                const summaryCouponRow = document.getElementById('summary-coupon-row');
                const summaryCouponCode = document.getElementById('summary-coupon-code');
                const summaryCouponVal = document.getElementById('summary-coupon-val');

                if (summaryCouponRow && summaryCouponCode && summaryCouponVal) {
                    // Impostiamo il testo del codice inserito
                    summaryCouponCode.textContent = codice;
                    // Inseriamo il valore formattato dello sconto
                    const scontoFormattato = parseFloat(data.importoSconto || 0).toFixed(2).replace('.', ',');
                    summaryCouponVal.textContent = `-€ ${scontoFormattato}`;
                    
                    // Rendiamo visibile la riga dello sconto nella sidebar
                    summaryCouponRow.classList.remove('hidden');
                }

                // Aggiornamento del totale complessivo dell'ordine
                if (data.nuovoTotale) {
                    const totaleFormattato = parseFloat(data.nuovoTotale).toFixed(2).replace('.', ',');
                    document.getElementById('final-total-display').textContent = `€ ${totaleFormattato}`;
                    btnInviaOrdine.innerHTML = `Conferma e Paga (€ ${totaleFormattato})`;
                }
            } else {
                couponMessage.style.color = 'var(--promozioni)';
                couponMessage.textContent = data.message || 'Coupon non valido o scaduto.';
            }
        } catch (error) {
            console.error("Errore validazione coupon:", error);
            couponMessage.style.color = 'var(--promozioni)';
            couponMessage.textContent = 'Errore di connessione.';
        } finally {
            btnApplicaCoupon.disabled = false;
            btnApplicaCoupon.textContent = 'Applica';
        }
    });

    // --- 6. INVIO FORM (Nessuna validazione extra richiesta) ---
    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Se il bottone non è disabled, i dati sono già garantiti da checkFormValidity()
        const payload = {
            nome: inputs.nome.value.trim(),
            cognome: inputs.cognome.value.trim(),
            indirizzo: inputs.indirizzo.value.trim(),
            citta: inputs.citta.value.trim(),
            cap: inputs.cap.value.trim(),
            metodoPagamento: document.querySelector('input[name="metodoPagamento"]:checked').value,
            codiceCoupon: inputs.codiceCoupon.value.trim().toUpperCase() // Passiamo il coupon al backend
        };

        btnInviaOrdine.disabled = true;
        btnInviaOrdine.textContent = "Elaborazione in corso...";

        try {
            const response = await fetch('checkout', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            const data = await response.json();

            if (data.success) {
                const overlay = document.getElementById('successOverlay');
                if (overlay) overlay.classList.remove('hidden');

                setTimeout(() => {
                    window.location.href = data.redirectUrl || 'ordini'; 
                }, 2500);

            } else {
                if (window.showToast) window.showToast(data.message, "error");
                btnInviaOrdine.disabled = false;
                btnInviaOrdine.innerHTML = originalBtnText; // RIPRISTINA IL PREZZO PRECEDENTE!
            }

        } catch (error) {
            console.error("Errore Fetch:", error);
            if (window.showToast) window.showToast("Errore di comunicazione col server.", "error");
            btnInviaOrdine.disabled = false;
            btnInviaOrdine.innerHTML = originalBtnText; // RIPRISTINA IL PREZZO PRECEDENTE!
        }
    });
});