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

});