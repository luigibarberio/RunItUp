/**
 * RUNITUP - Logica Client-Side Gestione Ordini (Admin)
 * Gestisce l'intercettazione degli errori di filtro dal backend e la UX dei bottoni.
 */

document.addEventListener('DOMContentLoaded', () => {
    
    // =====================================================================
    // 1. LETTURA DEGLI ERRORI DAL BACKEND (Toast Ibrido)
    // =====================================================================
    // Cerchiamo l'input hidden generato dalla JSP se c'è stato un errore (es. Cliente non trovato)
    const backendErrorInput = document.getElementById('backendErrorMsg');
    
    if (backendErrorInput && backendErrorInput.value !== '') {
        if (window.showToast) {
            window.showToast(backendErrorInput.value, "error");
        }
    }

    // =====================================================================
    // 2. UX DEL FORM DI FILTRAGGIO (Feedback visivo)
    // =====================================================================
    const filterForm = document.querySelector('.admin-filter-form');
    if (filterForm) {
        filterForm.addEventListener('submit', () => {
            const btnFilter = filterForm.querySelector('.btn-filter');
            if (btnFilter) {
                // Evitiamo doppi click accidentali e diamo un feedback visivo
                btnFilter.innerHTML = "⏳ Filtraggio...";
                btnFilter.style.pointerEvents = "none";
                btnFilter.style.opacity = "0.8";
            }
        });
    }

    // =====================================================================
    // 3. CAMBIO STATO ORDINE ASINCRONO (AJAX Fetch)
    // =====================================================================
    const statusSelects = document.querySelectorAll('.status-select');
    
    statusSelects.forEach(select => {
        select.addEventListener('change', async function() {
            const idOrdine = this.getAttribute('data-id');
            const nuovoStato = this.value;
            const statoOriginale = this.getAttribute('data-original');
            
            // Disabilitiamo temporaneamente la select durante il caricamento
            this.disabled = true;

            try {
                const response = await fetch('ordini', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        idOrdine: idOrdine,
                        nuovoStato: nuovoStato
                    })
                });

                const data = await response.json();

                if (data.success) {
                    if (window.showToast) window.showToast(data.message, "success");
                    // Aggiorniamo lo stato originale in caso di futuri cambiamenti
                    this.setAttribute('data-original', nuovoStato);
                } else {
                    if (window.showToast) window.showToast(data.message, "error");
                    // In caso di errore, rimettiamo la select al valore originale
                    this.value = statoOriginale;
                }
            } catch (error) {
                console.error("Errore Fetch:", error);
                if (window.showToast) window.showToast("Errore di rete durante l'aggiornamento.", "error");
                this.value = statoOriginale;
            } finally {
                // Riabilitiamo la select
                this.disabled = false;
            }
        });
    });

});