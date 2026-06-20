document.addEventListener('DOMContentLoaded', () => {

    // Controllo dei messaggi di ritorno (Redirect dal server)
    if (window.serverFeedback) {
        if (window.serverFeedback === "updated") {
            window.showToast("Prodotto aggiornato con successo!", "success");
        } else if (window.serverFeedback === "created") {
            window.showToast("Nuovo prodotto inserito a catalogo!", "success");
        }
    }

    // Gestione Asincrona Eliminazione Prodotto
    const deleteButtons = document.querySelectorAll('.btn-delete');

    deleteButtons.forEach(btn => {
        btn.addEventListener('click', async function() {
            const idProdotto = this.getAttribute('data-id');
            const card = document.getElementById(`card-prodotto-${idProdotto}`);

            // Conferme di Cancellazione
            const isConfirmed = confirm(`Sei sicuro di voler eliminare definitivamente il prodotto #${idProdotto} dal catalogo?\nQuesta operazione non può essere annullata.`);
            
            if (!isConfirmed) {
                return; // L'admin ha annullato, non facciamo nulla
            }

            try {
                // Modifichiamo visivamente il bottone durante l'attesa
                const originalText = this.innerHTML;
                this.innerHTML = "⏳ Eliminazione...";
                this.disabled = true;

                // Fetch API con JSON
                const response = await fetch('prodotto/elimina', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ idProdotto: parseInt(idProdotto) })
                });

                const data = await response.json();

                if (data.success) {
                    window.showToast(data.message, "success");
                    
                    // Effetto grafico di rimozione fluida senza ricaricare la pagina
                    card.classList.add('fade-out');
                    setTimeout(() => {
                        card.remove();
                        // Se era l'ultimo prodotto, ricarichiamo la pagina per mostrare lo stato vuoto
                        if (document.querySelectorAll('.admin-product-card').length === 0) {
                            window.location.reload();
                        }
                    }, 400);

                } else {
                    window.showToast(data.message, "error");
                    this.innerHTML = originalText;
                    this.disabled = false;
                }

            } catch (error) {
                console.error("Errore Fetch Eliminazione:", error);
                window.showToast("Impossibile comunicare con il server.", "error");
                this.innerHTML = originalText;
                this.disabled = false;
            }
        });
    });

});