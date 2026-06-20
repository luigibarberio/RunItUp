// Validazione lato client per il form di creazione/modifica prodotto (Admin)
document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('productForm');
    
    // Verifichiamo la presenza del campo hidden generato dalla JSP
    const hiddenIdInput = document.getElementById('idProdotto');
    const isModifica = (hiddenIdInput !== null);

    const backendErrorInput = document.getElementById('backendErrorMsg');
    if (backendErrorInput && backendErrorInput.value !== '') {
        if (window.showToast) {
            window.showToast(backendErrorInput.value, "error");
        }
    }

    form.addEventListener('submit', (event) => {
        // Preveniamo il ricaricamento automatico per validare i dati lato client
        event.preventDefault();

        let isValid = true;

        // Validazione Testi (Obbligatori sempre)
        if (!window.FormUtils.checkText('nome', 'nomeError', 'Il nome del modello è obbligatorio.')) {
            isValid = false;
        }

        // Validazione Brand (Obbligatorio sempre)
        if (!window.FormUtils.checkText('nomeBrand', 'brandError', 'Digita o seleziona un brand.')) {
            isValid = false;
        }

        // Se tutto è valido, disabilitiamo il bottone e facciamo una submit nativa 
        // (necessaria per inviare correttamente i payload multipart/form-data)
        if (isValid) {
            const btnSalva = document.getElementById('btnSalva');
            btnSalva.textContent = "Salvataggio in corso...";
            btnSalva.disabled = true;
            
            form.submit();
        }
    });
});