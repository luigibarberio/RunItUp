// Funzione esposta globalmente per aggiornare il badge del carrello
window.updateCartBadge = function(newCount) {
    const badge = document.getElementById('cartBadge');
    if (!badge) return;

    if (newCount > 0) {
        badge.textContent = newCount;
        badge.classList.remove('hidden');
        
        // Animazione di "pop"
        badge.classList.add('bump');
        setTimeout(() => {
            badge.classList.remove('bump');
        }, 250);
    } else {
        badge.classList.add('hidden');
    }
};

document.addEventListener('DOMContentLoaded', () => {
    // ----------------======================================================
    // 1. SCROLL DETECT (Nascondi header allo scroll down)
    // ----------------======================================================
    let lastScrollTop = 0;
    const header = document.querySelector('.main-header');
    
    if (header) {
        window.addEventListener('scroll', () => {
            let scrollTop = window.pageYOffset || document.documentElement.scrollTop;

            if (scrollTop > lastScrollTop && scrollTop > header.offsetHeight) {
                header.classList.add('header-hidden'); 
            } else {
                header.classList.remove('header-hidden'); 
            }
            lastScrollTop = scrollTop <= 0 ? 0 : scrollTop; 
        });
    }

    // ----------------======================================================
    // 2. TOGGLE OFF-CANVAS MENU MOBILE (Drawer Destro)
    // ----------------======================================================
    const menuOpenBtn = document.getElementById('mobileMenuOpenBtn');
    const menuCloseBtn = document.getElementById('mobileMenuCloseBtn');
    const drawer = document.getElementById('mobileNavigationDrawer');
    const overlay = document.getElementById('mobileDrawerOverlay');

    function openDrawer() {
        if(drawer && overlay) {
            drawer.classList.add('open');
            overlay.classList.add('open');
            document.body.style.overflow = 'hidden'; // Blocca lo scorrimento della pagina sotto
        }
    }

    function closeDrawer() {
        if(drawer && overlay) {
            drawer.classList.remove('open');
            overlay.classList.remove('open');
            document.body.style.overflow = '';
        }
    }

    if (menuOpenBtn) menuOpenBtn.addEventListener('click', openDrawer);
    if (menuCloseBtn) menuCloseBtn.addEventListener('click', closeDrawer);
    if (overlay) overlay.addEventListener('click', closeDrawer);

    // ----------------======================================================
    // 3. MOBILE SEARCH BAR OVERLAY (Dropdown dall'alto)
    // ----------------======================================================
    const searchOpenBtn = document.getElementById('mobileSearchOpenBtn');
    const searchCloseBtn = document.getElementById('mobileSearchCloseBtn');
    const searchOverlay = document.getElementById('mobileSearchOverlay');
    const mobileInput = document.getElementById('mobileSearchInput');

    if (searchOpenBtn && searchOverlay) {
        searchOpenBtn.addEventListener('click', () => {
            searchOverlay.classList.add('open');
            if (mobileInput) mobileInput.focus(); // Mette subito il focus sulla tastiera
        });
    }

    if (searchCloseBtn && searchOverlay) {
        searchCloseBtn.addEventListener('click', () => {
            searchOverlay.classList.remove('open');
            if (mobileInput) mobileInput.value = '';
        });
    }

    // ----------------======================================================
    // 4. LOGICA AJAX SUGGERIMENTI RICERCA (Preservata e Blindata)
    // --------------------------------======================================

    function setupAjaxSearch(inputId, suggestionsId) {
        const searchInput = document.getElementById(inputId);
        const searchSuggestions = document.getElementById(suggestionsId);
        let debounceTimer;

        if (searchInput && searchSuggestions) {
            const contextPath = searchSuggestions.getAttribute('data-context');

            searchInput.addEventListener('input', () => {
                clearTimeout(debounceTimer);
                const query = searchInput.value.trim();

                if (query.length < 2) {
                    searchSuggestions.classList.add('hidden');
                    searchSuggestions.innerHTML = '';
                    return;
                }

                debounceTimer = setTimeout(async () => {
                    try {
                        const response = await fetch(`${contextPath}/suggerimenti?q=${encodeURIComponent(query)}`);
                        if (!response.ok) throw new Error("Errore di rete");
                        
                        const data = await response.json();
                        searchSuggestions.innerHTML = ''; 

                        if (data.length > 0) {
                            data.forEach(item => {
                                const div = document.createElement('div');
                                div.className = 'suggestion-item';
                                div.innerHTML = `<strong>${item.brand}</strong> ${item.nome}`;
                                
                                div.addEventListener('click', () => {
                                    window.location.href = `${contextPath}/dettaglio?id=${item.id}`;
                                });
                                searchSuggestions.appendChild(div);
                            });
                        } else {
                            const div = document.createElement('div');
                            div.className = 'suggestion-empty';
                            div.textContent = 'Nessun prodotto trovato.';
                            searchSuggestions.appendChild(div);
                        }

                        searchSuggestions.classList.remove('hidden');

                    } catch (err) {
                        console.error("Errore fetch suggerimenti:", err);
                    }
                }, 300);
            });

            // Chiudi i suggerimenti se l'utente clicca fuori dall'input o dai suggerimenti
            document.addEventListener('click', (e) => {
                if (!searchInput.contains(e.target) && !searchSuggestions.contains(e.target)) {
                    searchSuggestions.classList.add('hidden');
                }
            });
            
            // Mostra i suggerimenti se l'input è già popolato e l'utente fa focus
            searchInput.addEventListener('focus', () => {
                if (searchInput.value.trim().length >= 2 && searchSuggestions.innerHTML.trim() !== '') {
                    searchSuggestions.classList.remove('hidden');
                }
            });
        }
    }

    // Inizializza la logica AJAX per entrambi gli input di ricerca
    setupAjaxSearch('desktopSearchInput', 'desktopSearchSuggestions');
    setupAjaxSearch('mobileSearchInput', 'mobileSearchSuggestions');

});