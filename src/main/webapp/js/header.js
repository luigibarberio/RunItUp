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
});