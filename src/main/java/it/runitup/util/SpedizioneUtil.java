package it.runitup.util;

import java.math.BigDecimal;

public class SpedizioneUtil {
    
    // Costanti per la logica di calcolo della spedizione
    public static final BigDecimal SOGLIA_SPEDIZIONE_GRATUITA = new BigDecimal("100.00");
    public static final BigDecimal COSTO_SPEDIZIONE_BASE = new BigDecimal("3.99");

    // Metodo statico per calcolare il costo di spedizione in base al totale del carrello
    public static BigDecimal calcolaCostoSpedizione(BigDecimal totaleCarrello) {
        // Se il totale del carrello è null, significa che il carrello è fisicamente vuoto, per cui la spedizione è gratuita (nessun ordine da spedire)
        if (totaleCarrello == null) {
            return BigDecimal.ZERO;
        }
        
        // Se il totale del carrello è maggiore o uguale alla soglia di spedizione gratuita, allora la spedizione è gratuita
        if (totaleCarrello.compareTo(SOGLIA_SPEDIZIONE_GRATUITA) >= 0) {
            return BigDecimal.ZERO;
        }
        
        // Altrimenti, si applica il costo di spedizione base
        return COSTO_SPEDIZIONE_BASE;
    }
}