// Gestione dell'aggiunta, modifica e cancellazione delle varianti di prodotto (Admin)

document.addEventListener('DOMContentLoaded', () => {
    
    // --- AGGIUNTA NUOVA VARIANTE (CREA) ---
    const addForm = document.getElementById('addVariantForm');
    
    addForm.addEventListener('submit', async (e) => {
        e.preventDefault(); // Blocchiamo il refresh della pagina

        const btnAdd = document.getElementById('btnAddSubmit');
        const originalBtnText = btnAdd.textContent;

        // Recuperiamo i valori dal form orizzontale
        const idProdotto = document.getElementById('add_idProdotto').value;
        const colore = document.getElementById('add_colore').value.trim();
        const taglia = document.getElementById('add_taglia').value.trim();
        const prezzo = document.getElementById('add_prezzo').value;
        const quantita = document.getElementById('add_quantita').value;

        // Validazione base (Il tag "required" dell'HTML copre già la maggior parte dei casi)
        if (!colore || !taglia || !prezzo || !quantita) {
            if(window.showToast) window.showToast("Compila tutti i campi prima di aggiungere.", "error");
            return;
        }

        try {
            btnAdd.textContent = "⏳";
            btnAdd.disabled = true;

            // Spariamo il JSON alla Servlet
            const response = await fetch(window.location.pathname, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    action: 'insert',
                    idProdotto: parseInt(idProdotto),
                    colore: colore,
                    taglia: taglia,
                    prezzo: parseFloat(prezzo),
                    quantitaMagazzino: parseInt(quantita)
                })
            });

            const data = await response.json();

            if (data.success) {
                if(window.showToast) window.showToast(data.message, "success");
                
                // Nascondiamo il messaggio "Nessuna variante" se era visibile
                const emptyBox = document.getElementById('emptyStateBox');
                if (emptyBox) emptyBox.style.display = 'none';

                // Cloniamo il <template> HTML5 per generare la nuova card
                const template = document.getElementById('variant-card-template').innerHTML;
                const newCardHTML = template
                    .replace(/{ID}/g, data.idVariante)
                    .replace(/{COLORE}/g, colore)
                    .replace(/{TAGLIA}/g, taglia)
                    .replace(/{PREZZO}/g, parseFloat(prezzo).toFixed(2))
                    .replace(/{QUANTITA}/g, quantita);

                // Inseriamo la nuova card in cima alla griglia
                document.getElementById('variantsGrid').insertAdjacentHTML('afterbegin', newCardHTML);
                
                // Attiviamo l'animazione di discesa CSS
                document.getElementById(`variante-${data.idVariante}`).classList.add('new-variant-anim');

                // Resettiamo il form per un nuovo inserimento
                addForm.reset();

            } else {
                if(window.showToast) window.showToast(data.message, "error");
            }

        } catch (error) {
            console.error("Errore Fetch (Aggiunta):", error);
            if(window.showToast) window.showToast("Impossibile comunicare col server.", "error");
        } finally {
            btnAdd.textContent = originalBtnText;
            btnAdd.disabled = false;
        }
    });

});


// ============================================================================
// FUNZIONI GLOBALI (Richiamate dagli attributi onclick="" nell'HTML)
// ============================================================================

// Toggle tra modalità di visualizzazione e modifica per una variante specifica
window.toggleEditMode = function(idVariante, isEdit) {
    const card = document.getElementById(`variante-${idVariante}`);
    if (!card) return;

    const viewMode = card.querySelector('.variant-view-mode');
    const editMode = card.querySelector('.variant-edit-mode');

    if (isEdit) {
        viewMode.classList.add('hidden');
        editMode.classList.remove('hidden');
    } else {
        viewMode.classList.remove('hidden');
        editMode.classList.add('hidden');
    }
};

/**
 * 3. AGGIORNAMENTO INLINE (MODIFICA)
 */
window.updateVariante = async function(idVariante) {
    const card = document.getElementById(`variante-${idVariante}`);
    
    // Leggiamo i nuovi valori inseriti dall'utente negli input
    const nuovoColore = card.querySelector('.edit-colore').value.trim();
    const nuovaTaglia = card.querySelector('.edit-taglia').value.trim();
    const nuovoPrezzo = card.querySelector('.edit-prezzo').value;
    const nuovaQuantita = card.querySelector('.edit-quantita').value;

    const btnSave = card.querySelector('.btn-save');
    const originalText = btnSave.innerHTML;

    try {
        btnSave.innerHTML = "⏳";
        btnSave.disabled = true;

        const response = await fetch(window.location.pathname, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                action: 'update',
                idVariante: parseInt(idVariante),
                colore: nuovoColore,
                taglia: nuovaTaglia,
                prezzo: parseFloat(nuovoPrezzo),
                quantitaMagazzino: parseInt(nuovaQuantita)
            })
        });

        const data = await response.json();

        if (data.success) {
            if(window.showToast) window.showToast(data.message, "success");
            
            // L'aggiornamento ha avuto successo nel DB, aggiorniamo i testi statici dell'interfaccia!
            card.querySelector('.v-colore').textContent = nuovoColore;
            card.querySelector('.v-taglia').textContent = nuovaTaglia;
            card.querySelector('.v-prezzo').textContent = parseFloat(nuovoPrezzo).toFixed(2);
            card.querySelector('.v-quantita').textContent = nuovaQuantita;

            // Chiudiamo la modalità di modifica ritornando in lettura
            toggleEditMode(idVariante, false);
        } else {
            if(window.showToast) window.showToast(data.message, "error");
        }

    } catch (error) {
        console.error("Errore Fetch (Modifica):", error);
        if(window.showToast) window.showToast("Errore durante l'aggiornamento.", "error");
    } finally {
        btnSave.innerHTML = originalText;
        btnSave.disabled = false;
    }
};

/**
 * 4. ELIMINAZIONE ASINCRONA
 */
window.deleteVariante = async function(idVariante) {
    // Requisito OBBLIGATORIO: Chiedere sempre conferma per le cancellazioni
    if (!confirm('Sei sicuro di voler eliminare questa specifica taglia/colore?')) {
        return;
    }

    const card = document.getElementById(`variante-${idVariante}`);
    const btnDelete = card.querySelector('.btn-delete');
    const originalText = btnDelete.innerHTML;

    try {
        btnDelete.innerHTML = "⏳";
        btnDelete.disabled = true;

        const response = await fetch(window.location.pathname, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                action: 'delete',
                idVariante: parseInt(idVariante)
            })
        });

        const data = await response.json();

        if (data.success) {
            if(window.showToast) window.showToast(data.message, "success");
            
            // Effetto dissolvenza prima della rimozione
            card.classList.add('fade-out');
            
            setTimeout(() => {
                card.remove(); // Distrugge l'elemento HTML
                
                // Se abbiamo eliminato l'ultima variante, ricreiamo e mostriamo lo stato vuoto
                if (document.querySelectorAll('.variant-card').length === 0) {
                    let emptyBox = document.getElementById('emptyStateBox');
                    if (!emptyBox) {
                        emptyBox = document.createElement('div');
                        emptyBox.id = 'emptyStateBox';
                        emptyBox.className = 'empty-variants';
                        emptyBox.innerHTML = '<p>Nessuna variante inserita. Aggiungi la prima taglia dal pannello superiore!</p>';
                        document.getElementById('variantsGrid').appendChild(emptyBox);
                    } else {
                        emptyBox.style.display = 'block';
                    }
                }
            }, 300);

        } else {
            if(window.showToast) window.showToast(data.message, "error");
            btnDelete.innerHTML = originalText;
            btnDelete.disabled = false;
        }

    } catch (error) {
        console.error("Errore Fetch (Eliminazione):", error);
        if(window.showToast) window.showToast("Errore di comunicazione col server.", "error");
        btnDelete.innerHTML = originalText;
        btnDelete.disabled = false;
    }
};