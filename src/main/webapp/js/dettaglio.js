document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('product-variants');
    if (!container) return;

    const varianti = JSON.parse(container.getAttribute('data-varianti'));
    
    const colSel = document.getElementById('coloreSelect');
    const tagSel = document.getElementById('tagliaSelect');
    const btnAggiungi = document.getElementById('btnAggiungi');
    const priceContainer = document.getElementById('price-container');
    
    // Recuperiamo il div per l'avviso delle scorte dalla JSP
    const stockWarning = document.getElementById('stock-warning'); 

    // POPOLAMENTO STATICO (Fatto una sola volta all'avvio)
    const coloriUnici = [...new Set(varianti.map(v => v.variante.colore))];
    coloriUnici.forEach(colore => {
        colSel.appendChild(new Option(colore, colore));
    });

    // Ordiniamo le taglie per comodità visiva e le inseriamo
    const taglieUniche = [...new Set(varianti.map(v => v.variante.taglia))].sort();
    taglieUniche.forEach(taglia => {
        tagSel.appendChild(new Option(taglia, taglia));
    });

    // LOGICA DI CONTROLLO SEMPLIFICATA
    function aggiornaUI() {
        const coloreScelto = colSel.value;
        const tagliaScelta = tagSel.value;

        // Reset visivo di default ad ogni cambio (nascondiamo l'avviso e ripristiniamo il testo)
        if (stockWarning) stockWarning.style.display = 'none';
        btnAggiungi.textContent = 'AGGIUNGI AL CARRELLO';

        // Gestione visiva "grigio" per le taglie non disponibili
        if (coloreScelto) {
            const taglieDisponibili = varianti
                .filter(v => v.variante.colore === coloreScelto && v.variante.quantitaMagazzino > 0)
                .map(v => v.variante.taglia);

            Array.from(tagSel.options).forEach(opt => {
                if (opt.value !== "") {
                    opt.disabled = !taglieDisponibili.includes(opt.value);
                }
            });
        } else {
            // Se nessun colore è scelto, tutte le taglie sono formalmente cliccabili
            Array.from(tagSel.options).forEach(opt => opt.disabled = false);
        }

        // Controllo Prezzo / Disponibilità
        const match = varianti.find(v => v.variante.colore === coloreScelto && v.variante.taglia === tagliaScelta);

        // Se esiste la combinazione selezionata
        if (match) {
            const prezzoListino = match.variante.prezzo;
            const prezzoScontato = match.prezzoScontato;
            
            // 1. Stampa del prezzo corretto
            if (match.inPromozione && prezzoScontato != null) {
                priceContainer.innerHTML = `
                    <span class="current-price" style="color: var(--promozioni);">€ ${prezzoScontato.toFixed(2)}</span>
                    <span class="old-price">€ ${prezzoListino.toFixed(2)}</span>
                `;
            } else {
                priceContainer.innerHTML = `<span class="current-price">€ ${prezzoListino.toFixed(2)}</span>`;
            }
            
            // Logica di controllo disponibilità e messaggi dinamici sulla base della quantità in magazzino
            const quantita = match.variante.quantitaMagazzino;
            
            if (quantita === 0) {
                // Caso in cui la variante esiste, ma la giacenza è 0 (Esaurito)
                btnAggiungi.disabled = true;
                btnAggiungi.textContent = 'ESAURITO';
                if (stockWarning) {
                    stockWarning.innerHTML = `🚫 Purtroppo questa combinazione è attualmente esaurita.`;
                    stockWarning.style.display = 'block';
                    stockWarning.className = 'stock-warning out-of-stock';
                }
            } else if (quantita > 0 && quantita < 5) {
                // Caso Scarcity: Meno di 5 pezzi disponibili
                btnAggiungi.disabled = false;
                if (stockWarning) {
                    stockWarning.innerHTML = `🔥 Affrettati! Ne restano solo <strong>${quantita}</strong> disponibili.`;
                    stockWarning.style.display = 'block';
                    stockWarning.className = 'stock-warning low-stock';
                }
            } else {
                // Caso Normale: Più di 5 pezzi (nessun avviso)
                btnAggiungi.disabled = false;
            }
        } 
        else {
            // Se ha selezionato entrambi ma non c'è proprio il match
            if (coloreScelto && tagliaScelta) {
                priceContainer.innerHTML = `<span class="current-price" style="color: #999;">Non disponibile</span>`;
            } else {
                // Manca ancora una selezione
                priceContainer.innerHTML = `<span class="current-price">Seleziona una variante</span>`;
            }
            btnAggiungi.disabled = true;
        }
    }

    // LISTENERS
    colSel.addEventListener('change', aggiornaUI);
    tagSel.addEventListener('change', aggiornaUI);

    // CHIAMATA FETCH JSON
    btnAggiungi.addEventListener('click', async () => {
        const match = varianti.find(v => v.variante.colore === colSel.value && v.variante.taglia === tagSel.value);
        if (!match) return;

        btnAggiungi.disabled = true;

        try {
            const response = await fetch('carrello', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ idVariante: match.variante.idVariante, delta: 1 })
            });

            const data = await response.json();

            if (data.success) {
                if (window.showToast) window.showToast("Prodotto aggiunto al carrello!", "success");
                
                // Aggiorniamo il badge del carrello leggendo il nuovo totale dal JSON
                if (window.updateCartBadge && data.cartCount !== undefined) {
                    window.updateCartBadge(data.cartCount);
                }
            } else {
                if (window.showToast) window.showToast(data.message || "Impossibile aggiungere il prodotto.", "error");
            }
        } catch (error) {
            console.error('Errore Fetch:', error);
            if (window.showToast) window.showToast("Errore di connessione.", "error");
        } finally {
            btnAggiungi.disabled = false;
        }
    });
});