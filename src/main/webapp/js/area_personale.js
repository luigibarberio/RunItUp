document.addEventListener('DOMContentLoaded', () => {
    
    const btnLogout = document.getElementById('btnLogout');
    
    if (btnLogout) {
        btnLogout.addEventListener('click', async (e) => {
            e.preventDefault();
            
            // Disabilitiamo il pulsante temporaneamente per prevenire doppi click compulsivi
            btnLogout.disabled = true;
            const originalText = btnLogout.textContent;
            btnLogout.textContent = "Disconnessione...";
            
            try {
                // Eseguiamo la chiamata asincrona all'endpoint di logout tramite metodo POST
                const response = await fetch(`logout`, {
                    method: 'POST',
                    headers: {
                        'X-Requested-With': 'XMLHttpRequest'
                    }
                });
                
                if (!response.ok) {
                    throw new Error(`Errore HTTP: ${response.status}`);
                }
                
                const data = await response.json();
                
                if (data.success) {
                    // 1. Intercettiamo l'overlay centralizzato
                    const overlay = document.getElementById('successOverlay');
                    const title = document.getElementById('overlayTitle');
                    const text = document.getElementById('overlayText');
                    
                    // 2. Personalizziamo dinamicamente i testi interni dell'overlay
                    if (title) title.textContent = "Chiusura Sessione";
                    if (text) text.textContent = "Disconnessione riuscita. Grazie per aver visitato RunItUp!";
                    
                    // 3. Mostriamo l'overlay rimuovendo la classe helper 'hidden'
                    if (overlay) {
                        overlay.classList.remove('hidden');
                    }
                    
                    // 4. Eseguiamo il reindirizzamento guidato dopo 2 secondi (tempo dell'animazione CSS)
                    setTimeout(() => {
                        window.location.href = data.redirectUrl;
                    }, 2000);
                    
                } else {
                    // Se il backend segnala un problema (es. sessione già invalida)
                    if (window.showToast) {
                        window.showToast(data.message || "Impossibile completare la disconnessione.", "error");
                    }
                    btnLogout.disabled = false;
                    btnLogout.textContent = originalText;
                }
                
            } catch (error) {
                console.error("Errore riscontrato durante la fetch di logout:", error);
                if (window.showToast) {
                    window.showToast("Errore di comunicazione col server durante il logout.", "error");
                }
                btnLogout.disabled = false;
                btnLogout.textContent = originalText;
            }
        });
    }
});