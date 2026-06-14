// Namespace globale per le utility dei form
window.FormUtils = {
    // Espressioni regolari riutilizzabili
    emailRegex: /^[a-zA-Z0-9_+&*-]+(?:\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\.)+[a-zA-Z]{2,7}$/,
    phoneRegex: /^(3\d{9}|0\d{6,10})$/,
    capRegex: /^\d{5}$/,
    ccRegex: /^\d{16}$/,
    expRegex: /^(0[1-9]|1[0-2])\/\d{2}$/,
    cvvRegex: /^\d{3,4}$/,

    // Mostra l'errore graficamente
    showError: function(inputElement, errorElementId, message) {
        const errorElement = document.getElementById(errorElementId);
        if (inputElement) inputElement.classList.add('error');
        if (errorElement) errorElement.textContent = message;
    },

    // Pulisce l'errore graficamente
    clearError: function(inputElement, errorElementId) {
        const errorElement = document.getElementById(errorElementId);
        if (inputElement) inputElement.classList.remove('error');
        if (errorElement) errorElement.textContent = '';
    },

    // Validazione centralizzata per stringhe vuote
    checkText: function(inputId, errorId, message) {
        const input = document.getElementById(inputId);
        if (!input) return true;
        
        // Salta la validazione se il campo è nascosto nel DOM (es. form dinamiche)
        if (input.closest && input.closest('.hidden')) return true;

        if (input.value.trim() === '') {
            this.showError(input, errorId, message);
            return false;
        } else {
            this.clearError(input, errorId);
            return true;
        }
    },

    // Validazione centralizzata con Regex
    checkRegex: function(inputId, errorId, regex, message) {
        const input = document.getElementById(inputId);
        if (!input) return true;

        if (input.closest && input.closest('.hidden')) return true;

        if (!regex.test(input.value.trim())) {
            this.showError(input, errorId, message);
            return false;
        } else {
            this.clearError(input, errorId);
            return true;
        }
    }
};