// Logica client-side per la pagina del catalogo: gestione dei filtri, ordinamento, e interazione con la sidebar dei filtri su mobile
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

// Funzione richiamata al submit del form dei filtri
function submitFiltri() {
    pulisciCampiVuoti();
    document.getElementById('filtriForm').submit();
}

// Funzione per il toggle della sidebar dei filtri su mobile
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