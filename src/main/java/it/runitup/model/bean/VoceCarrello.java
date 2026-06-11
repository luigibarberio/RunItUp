package it.runitup.model.bean;

import java.io.Serializable;

public class VoceCarrello implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idVoce;
    private Carrello carrello;
    private VarianteProdotto variante; // La scarpa specifica (colore/taglia)
    private int quantita;

    public VoceCarrello() {
        super();
    }

    public VoceCarrello(int idVoce, Carrello carrello, VarianteProdotto variante, int quantita) {
        this.idVoce = idVoce;
        this.carrello = carrello;
        this.variante = variante;
        this.quantita = quantita;
    }

    // --- Getter e Setter ---
    public int getIdVoce() { return idVoce; }
    public void setIdVoce(int idVoce) { this.idVoce = idVoce; }

    public Carrello getCarrello() { return carrello; }
    public void setCarrello(Carrello carrello) { this.carrello = carrello; }

    public VarianteProdotto getVariante() { return variante; }
    public void setVariante(VarianteProdotto variante) { this.variante = variante; }

    public int getQuantita() { return quantita; }
    public void setQuantita(int quantita) { this.quantita = quantita; }

    @Override
    public String toString() {
        return "VoceCarrello [idVoce=" + idVoce + ", variante=" + (variante != null ? variante.getIdVariante() : "null") + ", quantita=" + quantita + "]";
    }
}