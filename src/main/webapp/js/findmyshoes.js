/**
 * RUNITUP - Logica Client-Side Shoe Finder
 * Gestisce l'avanzamento degli step del quiz, la validazione e la barra di progresso.
 */

document.addEventListener('DOMContentLoaded', () => {
    
    const steps = document.querySelectorAll('.quiz-step');
    const progressBar = document.getElementById('quizProgress');
    const totalSteps = steps.length;
    let currentStepIndex = 0; // Partiamo dallo Step 1 (indice 0)

    // =====================================================================
    // 1. FUNZIONE CENTRALE DI AGGIORNAMENTO UI
    // =====================================================================
    const updateQuizUI = () => {
        // Mostriamo solo lo step corrente, nascondendo gli altri
        steps.forEach((step, index) => {
            if (index === currentStepIndex) {
                step.classList.add('active-step');
            } else {
                step.classList.remove('active-step');
            }
        });

        // Aggiorniamo dinamicamente la barra di progresso verde
        if (progressBar) {
            const progressPercentage = ((currentStepIndex + 1) / totalSteps) * 100;
            progressBar.style.width = `${progressPercentage}%`;
        }
    };

    // =====================================================================
    // 2. GESTIONE BOTTONI "CONTINUA" (Avanzamento Manuale)
    // =====================================================================
    const nextButtons = document.querySelectorAll('.btn-next');
    nextButtons.forEach((btn) => {
        btn.addEventListener('click', () => {
            
            // Verifichiamo che l'utente abbia effettivamente selezionato un radio button nello step corrente
            const currentStepEl = steps[currentStepIndex];
            const checkedRadio = currentStepEl.querySelector('input[type="radio"]:checked');

            if (!checkedRadio) {
                // Se non ha scelto nulla, mostriamo il Toast di errore!
                if (window.showToast) {
                    window.showToast("Per favore, seleziona un'opzione prima di continuare.", "error");
                }
                return; // Blocchiamo l'avanzamento
            }

            // Se tutto è ok, andiamo avanti
            if (currentStepIndex < totalSteps - 1) {
                currentStepIndex++;
                updateQuizUI();
            }
        });
    });

    // =====================================================================
    // 3. GESTIONE BOTTONI "INDIETRO"
    // =====================================================================
    const prevButtons = document.querySelectorAll('.btn-prev');
    prevButtons.forEach((btn) => {
        btn.addEventListener('click', () => {
            if (currentStepIndex > 0) {
                currentStepIndex--;
                updateQuizUI();
            }
        });
    });

    // =====================================================================
    // 4. UX PREMIUM: AUTO-AVANZAMENTO AL CLICK SULLA CARD
    // =====================================================================
    const allRadioInputs = document.querySelectorAll('.quiz-option input[type="radio"]');
    allRadioInputs.forEach((radio) => {
        radio.addEventListener('change', () => {
            
            // Quando un'opzione cambia stato (viene selezionata), 
            // aspettiamo 400 millisecondi per far vedere l'animazione azzurra del CSS...
            setTimeout(() => {
                // ...e poi avanziamo automaticamente (se non siamo già all'ultima domanda)
                if (currentStepIndex < totalSteps - 1) {
                    currentStepIndex++;
                    updateQuizUI();
                }
            }, 400); 

        });
    });

    // Inizializzazione al caricamento della pagina
    updateQuizUI();
});