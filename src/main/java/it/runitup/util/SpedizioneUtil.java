package it.runitup.util;

import java.math.BigDecimal;

public class SpedizioneUtil {
    
    // Definiamo le costanti per evitare "Magic Numbers" nel codice
    public static final BigDecimal SOGLIA_SPEDIZIONE_GRATUITA = new BigDecimal("100.00");
    public static final BigDecimal COSTO_SPEDIZIONE_BASE = new BigDecimal("3.99");

    // Metodo statico per calcolare il costo di spedizione in base al totale del carrello
    public static BigDecimal calcolaCostoSpedizione(BigDecimal totaleCarrello) {
        // 1. Se il totaleCarrello è NULL, significa che il carrello è fisicamente vuoto.
        // In questo caso la spedizione è rigorosamente 0.
        if (totaleCarrello == null) {
            return BigDecimal.ZERO;
        }
        
        // 2. Se il carrello NON è vuoto (totaleCarrello != null) ed è >= 100€,
        // la spedizione è gratuita per premio fedeltà.
        if (totaleCarrello.compareTo(SOGLIA_SPEDIZIONE_GRATUITA) >= 0) {
            return BigDecimal.ZERO;
        }
        
        // 3. In tutti gli altri casi (carrello non vuoto ma inferiore a 100€, 
        // inclusi gli ordini particolari con totale prodotti a 0€) si applica il costo base.
        return COSTO_SPEDIZIONE_BASE;
    }
}