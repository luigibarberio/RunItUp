/* ==========================================================================
   SCRIPT PAGINA CATALOGO (catalogo.js)
   ========================================================================== */

/**
 * Pulisce i campi vuoti (es. i range di prezzo) prima di inviare il form.
 * Disabilitando l'input, il browser evita di inserire parametri inutili (es. &prezzoMin=) nell'URL.
 */
function pulisciCampiVuoti() {
    const form = document.getElementById('filtriForm');
    if (!form) return;

    // Seleziona gli input numerici
    const inputs = form.querySelectorAll('input[type="number"]');
    inputs.forEach(input => {
        if (input.value.trim() === '') {
            input.disabled = true; 
        }
    });
}

/**
 * Invia il form dei filtri attivando prima la pulizia dei campi vuoti.
 * Viene chiamata, ad esempio, al cambio della select dell'ordinamento.
 */
function submitFiltri() {
    pulisciCampiVuoti();
    document.getElementById('filtriForm').submit();
}

/**
 * Apre e chiude la sidebar dei filtri sulla visualizzazione Mobile.
 * Blocca anche lo scroll del corpo della pagina quando il menù è aperto.
 */
function toggleFiltri() {
    const sidebar = document.getElementById('sidebarFiltri');
    if (!sidebar) return;

    sidebar.classList.toggle('open');
    
    // Blocchiamo lo scroll della pagina in background quando il cassetto è aperto
    if (sidebar.classList.contains('open')) {
        document.body.style.overflow = 'hidden';
    } else {
        document.body.style.overflow = '';
    }
}