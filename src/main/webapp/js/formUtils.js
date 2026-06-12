// Namespace globale per le utility dei form
window.FormUtils = {
    // Espressioni regolari riutilizzabili
    emailRegex: /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/,
    phoneRegex: /^(3\d{9}|0\d{6,10})$/,

    // Mostra l'errore graficamente aggiungendo la classe e il testo
    showError: function(inputElement, errorElementId, message) {
        const errorElement = document.getElementById(errorElementId);
        if (inputElement) inputElement.classList.add('error');
        if (errorElement) errorElement.textContent = message;
    },

    // Pulisce l'errore graficamente rimuovendo la classe e il testo
    clearError: function(inputElement, errorElementId) {
        const errorElement = document.getElementById(errorElementId);
        if (inputElement) inputElement.classList.remove('error');
        if (errorElement) errorElement.textContent = '';
    }
};