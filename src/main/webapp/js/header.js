document.addEventListener('DOMContentLoaded', () => {
    let lastScrollTop = 0;
    const header = document.querySelector('.main-header');
    
    if (!header) return; // Sicurezza: se l'header non c'è, ferma lo script

    window.addEventListener('scroll', () => {
        // Calcola la posizione attuale (supporto cross-browser)
        let scrollTop = window.pageYOffset || document.documentElement.scrollTop;

        // Se scendiamo E abbiamo superato l'altezza dell'header stesso
        if (scrollTop > lastScrollTop && scrollTop > header.offsetHeight) {
            header.classList.add('header-hidden'); // Nascondi
        } else {
            // Se saliamo
            header.classList.remove('header-hidden'); // Mostra
        }

        // Evita valori negativi su iOS (effetto bounce)
        lastScrollTop = scrollTop <= 0 ? 0 : scrollTop; 
    });


    // --- LOGICA AJAX SUGGERIMENTI RICERCA ---
    const searchInput = document.getElementById('searchInput');
    const searchSuggestions = document.getElementById('searchSuggestions');
    let debounceTimer;

    if (searchInput && searchSuggestions) {
        // Estraiamo in modo dinamico il contextPath dal data-attribute della JSP
        const contextPath = searchSuggestions.getAttribute('data-context');

        searchInput.addEventListener('input', () => {
            // Annulla il timer precedente (Debounce)
            clearTimeout(debounceTimer);
            const query = searchInput.value.trim();

            // Se l'utente svuota la barra o digita un solo carattere, chiudi la tendina
            if (query.length < 2) {
                searchSuggestions.classList.add('hidden');
                searchSuggestions.innerHTML = '';
                return;
            }

            // Aspettiamo 300ms prima di colpire il server
            debounceTimer = setTimeout(async () => {
                try {
                    const response = await fetch(`${contextPath}/suggerimenti?q=${encodeURIComponent(query)}`);
                    
                    if (!response.ok) throw new Error("Errore rete");
                    
                    const data = await response.json();
                    searchSuggestions.innerHTML = ''; // Svuotiamo i vecchi risultati

                    if (data.length > 0) {
                        data.forEach(item => {
                            // Creiamo l'elemento div per ogni scarpa suggerita
                            const div = document.createElement('div');
                            div.className = 'suggestion-item';
                            // Strutturiamo il suggerimento con Brand in grassetto e nome prodotto
                            div.innerHTML = `<strong>${item.brand}</strong> ${item.nome}`;
                            
                            // Se clicchi sul suggerimento, vai direttamente alla pagina del prodotto!
                            div.addEventListener('click', () => {
                                window.location.href = `${contextPath}/dettaglio?id=${item.id}`;
                            });
                            
                            searchSuggestions.appendChild(div);
                        });
                    } else {
                        // Nessun risultato
                        const div = document.createElement('div');
                        div.className = 'suggestion-empty';
                        div.textContent = 'Nessun prodotto trovato.';
                        searchSuggestions.appendChild(div);
                    }

                    // Mostriamo la tendina
                    searchSuggestions.classList.remove('hidden');

                } catch (err) {
                    console.error("Errore fetch suggerimenti:", err);
                }
            }, 300);
        });

        // Sicurezza: se l'utente clicca in un punto qualsiasi della pagina fuori dalla tendina, la chiudiamo
        document.addEventListener('click', (e) => {
            if (!searchInput.contains(e.target) && !searchSuggestions.contains(e.target)) {
                searchSuggestions.classList.add('hidden');
            }
        });
        
        // Se l'utente clicca di nuovo sull'input e c'è già testo, riapre la tendina
        searchInput.addEventListener('focus', () => {
            if (searchInput.value.trim().length >= 2 && searchSuggestions.innerHTML.trim() !== '') {
                searchSuggestions.classList.remove('hidden');
            }
        });
    }
});