package it.runitup.model.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import it.runitup.model.bean.VarianteProdotto;

public class VarianteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private VarianteProdotto variante;
    private BigDecimal prezzoScontato; // Prezzo finale calcolato dal DAO
    private boolean inPromozione; // True se c'è una promo attiva

    public VarianteDTO() {
        super();
    }

    // --- Getter e Setter ---
    public VarianteProdotto getVariante() { return variante; }
    public void setVariante(VarianteProdotto variante) { this.variante = variante; }

    public BigDecimal getPrezzoScontato() { return prezzoScontato; }
    public void setPrezzoScontato(BigDecimal prezzoScontato) { this.prezzoScontato = prezzoScontato; }

    public boolean isInPromozione() { return inPromozione; }
    public void setInPromozione(boolean inPromozione) { this.inPromozione = inPromozione; }
    
    // Metodo di utilità per ottenere il prezzo effettivo da visualizzare, considerando eventuali promozioni (per evitare logica di business nelle JSP)
    public BigDecimal getPrezzoEffettivo() {
        return (inPromozione && prezzoScontato != null) ? prezzoScontato : variante.getPrezzo();
    }
}