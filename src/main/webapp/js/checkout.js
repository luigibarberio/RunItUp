// Logica client-side per la pagina di checkout: validazione form, gestione dinamica del metodo di pagamento, applicazione coupon e invio ordine
document.addEventListener('DOMContentLoaded', () => {
    
    const form = document.getElementById('checkoutForm');
    const paymentRadios = document.querySelectorAll('input[name="metodoPagamento"]');
    const creditCardFields = document.getElementById('creditCardFields');
    const btnInviaOrdine = document.getElementById('btnInviaOrdine');

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

    // Variabile per tenere traccia del coupon applicato. Ci servirà per evitare che si possa effettuare un ordine senza aver applicato un coupon valido, magari modificando il campo del coupon dopo averlo validato
    let couponConfermato = "";

    // --- GESTIONE DELLO STATO (Pattern usato in registrazione.js) ---
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

    // --- FUNZIONI DI VALIDAZIONE E AGGIORNAMENTO STATO ---
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

    // --- ASSEGNAZIONE EVENTI BLUR / INPUT ---
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


    // --- GESTIONE DINAMICA METODO PAGAMENTO ---
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

    // --- LOGICA COUPON (Fetch Asincrona) ---
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
                
                // Salviamo il coupon confermato
                couponConfermato = codice;

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

                    // Puliamo il campo di testo del coupon
                    inputs.codiceCoupon.value = "";
                }

                // Aggiornamento del totale complessivo dell'ordine (e il costo di spedizione se necessario)
                if (data.nuovoTotale) {
                    const totaleFormattato = parseFloat(data.nuovoTotale).toFixed(2).replace('.', ',');
                    document.getElementById('final-total-display').textContent = `€ ${totaleFormattato}`;
                    btnInviaOrdine.innerHTML = `Conferma e Paga (€ ${totaleFormattato})`;
                }

                // Aggiornamento del costo di spedizione se il coupon ha fatto scendere il totale sotto la soglia per la spedizione gratuita
                if (data.costoSpedizione !== undefined) {
                    const display = document.getElementById('shipping-cost-display');
                    const costoNumerico = parseFloat(data.costoSpedizione);
                    
                    if (costoNumerico === 0) {
                        display.textContent = "Gratis";
                        display.classList.add('free-shipping');
                    } else {
                        // Usa toLocaleString per formattare correttamente in base alla lingua
                        const formato = costoNumerico.toLocaleString('it-IT', { 
                            minimumFractionDigits: 2, 
                            maximumFractionDigits: 2 
                        });
                        display.textContent = `€ ${formato}`;
                        display.classList.remove('free-shipping');
                    }
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

    // Se l'utente modifica il campo del coupon dopo averlo confermato, avvisiamo che deve riapplicarlo. Non resettiamo subito couponConfermato per non fargli perdere lo sconto a schermo
    inputs.codiceCoupon.addEventListener('input', () => {
        if (couponConfermato !== "" && inputs.codiceCoupon.value.trim().toUpperCase() !== "") {
            couponMessage.textContent = "Codice del Coupon modificato. Ricordati di applicarlo!";
            couponMessage.style.color = 'var(--testo-secondario)';
        } else if (inputs.codiceCoupon.value.trim() === "") {
            couponMessage.textContent = ""; // Se cancella il campo, togliamo il messaggio di avviso
        }
    });

    // Listener sulla pressione del tasto Enter nel campo del coupon per simulare il click sul bottone "Applica" e evitare l'invio del form
    inputs.codiceCoupon.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault(); // Blocca l'invio del checkout form
            document.getElementById('btnApplicaCoupon').click(); // Simula il click sul tasto Applica
        }
    });

    // --- INVIO FORM (Nessuna validazione extra richiesta) ---
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
            codiceCoupon: couponConfermato // Passiamo il coupon al backend
        };

        let btnInviaOrdineOriginalText = btnInviaOrdine.textContent; // Salviamo il testo originale del bottone per poterlo ripristinare in caso di errore
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
                btnInviaOrdine.textContent = btnInviaOrdineOriginalText; // Si ripristina il testo originale del bottone
            }

        } catch (error) {
            console.error("Errore Fetch:", error);
            if (window.showToast) window.showToast("Errore di comunicazione col server.", "error");
            btnInviaOrdine.disabled = false;
            btnInviaOrdine.textContent = btnInviaOrdineOriginalText; // Si ripristina il testo originale del bottone
        }
    });
});