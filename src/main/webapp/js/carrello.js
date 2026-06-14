/**
 * RUNITUP - Logica Client-Side del Carrello
 * Gestisce l'aggiunta, la diminuzione e la rimozione degli articoli tramite Fetch asincrona,
 * supportando le promozioni e i calcoli in tempo reale.
 */

// Funzione richiamata dai pulsanti "+" e "-"
async function modificaQuantita(idVariante, delta) {
    const qtySpan = document.getElementById(`qty-${idVariante}`);
    const btnMinus = document.getElementById(`btn-minus-${idVariante}`);
    
    // Disabilitiamo i pulsanti temporaneamente per evitare spam di click (Race Conditions)
    disableControls(idVariante, true);

    try {
        // Fetch asincrona verso l'endpoint unificato
        const response = await fetch('carrello', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idVariante: idVariante, delta: delta })
        });

        const data = await response.json();

        if (data.success) {
            // 1. Aggiorniamo il valore testuale della quantità
            let nuovaQty = parseInt(qtySpan.textContent, 10) + delta;
            qtySpan.textContent = nuovaQty;

            // 2. Ricalcoliamo i subtotali di riga basandoci sui DTO
            const itemRow = document.getElementById(`item-${idVariante}`);
            
            // Estraiamo i nuovi data-attribute per le promozioni (col punto per i calcoli float)
            const prezzoEffettivo = parseFloat(itemRow.getAttribute('data-prezzo-effettivo'));
            const prezzoOriginale = parseFloat(itemRow.getAttribute('data-prezzo-originale'));
            const inPromo = itemRow.getAttribute('data-in-promo') === 'true';

            // A. Ricalcoliamo sempre il prezzo di vendita finale (Effettivo)
            const nuovoSubtotaleEffettivo = prezzoEffettivo * nuovaQty;
            document.getElementById(`subtot-${idVariante}`).textContent = nuovoSubtotaleEffettivo.toFixed(2).replace('.', ',');

            // B. Se è in promozione, ricalcoliamo dinamicamente anche il prezzo sbarrato (Originale)
            if (inPromo) {
                const nuovoSubtotaleOriginale = prezzoOriginale * nuovaQty;
                const spanVecchioPrezzo = document.getElementById(`old-subtot-${idVariante}`);
                if (spanVecchioPrezzo) {
                    spanVecchioPrezzo.textContent = nuovoSubtotaleOriginale.toFixed(2).replace('.', ',');
                }
            }

            // 3. Ricalcoliamo il totale complessivo del carrello e la spedizione
            aggiornaTotaleCarrelloCompleto();

            // 4. Gestione dinamica dello stato di disabilitazione del tasto "-"
            if (btnMinus) {
                btnMinus.disabled = (nuovaQty <= 1);
            }
        } else {
            // Se c'è un errore (es. magazzino esaurito), mostriamo il toast d'errore
            if (window.showToast) window.showToast(data.message, "error");
        }
    } catch (error) {
        console.error('Errore durante la modifica:', error);
        if (window.showToast) window.showToast("Errore di connessione col server.", "error");
    } finally {
        disableControls(idVariante, false);
    }
}

// Funzione richiamata dal bottone "Rimuovi"
async function rimuoviArticolo(idVariante) {
    if (!confirm("Sei sicuro di voler rimuovere questo articolo dal carrello?")) {
        return; 
    }

    try {
        const response = await fetch('carrello', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ idVariante: idVariante, action: 'remove' })
        });

        const data = await response.json();

        if (data.success) {
            // Rimuoviamo l'elemento riga dal DOM
            const riga = document.getElementById(`item-${idVariante}`);
            if (riga) riga.remove();
            
            if (window.showToast) window.showToast(data.message, "success");

            // Ricalcoliamo il totale generale
            aggiornaTotaleCarrelloCompleto();

            // Ricaricamento per mostrare il messaggio "Carrello Vuoto" se necessario
            const rimasti = document.querySelectorAll('.cart-item');
            if (rimasti.length === 0) {
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            }
        } else {
            if (window.showToast) window.showToast(data.message, "error");
        }
    } catch (error) {
        console.error('Errore durante la rimozione:', error);
        if (window.showToast) window.showToast("Errore di connessione col server.", "error");
    }
}

// Calcola la somma di tutti i subtotali (scontati) presenti a schermo per aggiornare il riepilogo
function aggiornaTotaleCarrelloCompleto() {
    let sommaProdotti = 0;
    const items = document.querySelectorAll('.cart-item');
    
    items.forEach(item => {
        const id = item.id.replace('item-', '');
        // Leggiamo sempre lo span "subtot-" che contiene il prezzo finale che l'utente pagherà
        const subtotText = document.getElementById(`subtot-${id}`).textContent.replace(',', '.');
        sommaProdotti += parseFloat(subtotText);
    });

    // Logica di calcolo Spedizione in tempo reale
    let costoSpedizione = 0;
    const shippingContainer = document.getElementById('shipping-container');

    if (sommaProdotti === 0) {
        costoSpedizione = 0;
        shippingContainer.innerHTML = '<span>€ 0,00</span>';
    } else if (sommaProdotti < 100.00) {
        costoSpedizione = 3.99;
        shippingContainer.innerHTML = '<span>€ 3,99</span>';
    } else {
        costoSpedizione = 0;
        shippingContainer.innerHTML = '<span class="free-shipping">Gratis</span>';
    }

    const totaleFinale = sommaProdotti + costoSpedizione;

    // Riformattiamo per la visualizzazione italiana (virgola come separatore decimale)
    document.getElementById('summary-subtotal').textContent = sommaProdotti.toFixed(2).replace('.', ',');
    document.getElementById('cart-total-val').textContent = totaleFinale.toFixed(2).replace('.', ',');
}

// Funzione helper per disabilitare i bottoni +/- durante le chiamate
function disableControls(idVariante, isFetching) {
    const btnPlus = document.getElementById(`btn-plus-${idVariante}`);
    const btnMinus = document.getElementById(`btn-minus-${idVariante}`);
    
    if (btnPlus) btnPlus.disabled = isFetching;
    
    if (!isFetching) {
        const qtySpan = document.getElementById(`qty-${idVariante}`);
        if (qtySpan) {
            const qty = parseInt(qtySpan.textContent, 10);
            if (btnMinus) btnMinus.disabled = (qty <= 1);
        }
    } else {
        if (btnMinus) btnMinus.disabled = true;
    }
}