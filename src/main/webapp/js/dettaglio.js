document.addEventListener('DOMContentLoaded', () => {
    const container = document.getElementById('product-variants');
    if (!container) return;

    const varianti = JSON.parse(container.getAttribute('data-varianti'));
    
    const colSel = document.getElementById('coloreSelect');
    const tagSel = document.getElementById('tagliaSelect');
    const btnAggiungi = document.getElementById('btnAggiungi');
    const priceContainer = document.getElementById('price-container');

    // 1. POPOLAMENTO STATICO (Fatto una sola volta all'avvio)
    const coloriUnici = [...new Set(varianti.map(v => v.variante.colore))];
    coloriUnici.forEach(colore => {
        colSel.appendChild(new Option(colore, colore));
    });

    // Ordiniamo le taglie per comodità visiva e le inseriamo
    const taglieUniche = [...new Set(varianti.map(v => v.variante.taglia))].sort();
    taglieUniche.forEach(taglia => {
        tagSel.appendChild(new Option(taglia, taglia));
    });

    // 2. LOGICA DI CONTROLLO SEMPLIFICATA
    function aggiornaUI() {
        const coloreScelto = colSel.value;
        const tagliaScelta = tagSel.value;

        // A. Gestione visiva "grigio" per le taglie non disponibili
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

        // B. Controllo Prezzo / Disponibilità
        const match = varianti.find(v => v.variante.colore === coloreScelto && v.variante.taglia === tagliaScelta);

        // Se esiste la combinazione E c'è disponibilità in magazzino
        if (match && match.variante.quantitaMagazzino > 0) {
            const prezzoListino = match.variante.prezzo;
            const prezzoScontato = match.prezzoScontato;
            
            if (match.inPromozione && prezzoScontato != null) {
                priceContainer.innerHTML = `
                    <span class="current-price" style="color: var(--promozioni);">€ ${prezzoScontato.toFixed(2)}</span>
                    <span class="old-price">€ ${prezzoListino.toFixed(2)}</span>
                `;
            } else {
                priceContainer.innerHTML = `<span class="current-price">€ ${prezzoListino.toFixed(2)}</span>`;
            }
            btnAggiungi.disabled = false;
        } 
        else {
            // Se ha selezionato entrambi ma non c'è match/giacenza -> Non disponibile
            if (coloreScelto && tagliaScelta) {
                priceContainer.innerHTML = `<span class="current-price" style="color: #999;">Non disponibile</span>`;
            } else {
                // Manca ancora una selezione
                priceContainer.innerHTML = `<span class="current-price">Seleziona una variante</span>`;
            }
            btnAggiungi.disabled = true;
        }
    }

    // 3. LISTENERS (Senza nessun reset "aggressivo")
    colSel.addEventListener('change', aggiornaUI);
    tagSel.addEventListener('change', aggiornaUI);

    // 4. CHIAMATA FETCH JSON
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