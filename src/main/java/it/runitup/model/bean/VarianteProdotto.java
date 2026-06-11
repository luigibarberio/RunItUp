package it.runitup.model.bean;

import java.io.Serializable;
import java.math.BigDecimal;

public class VarianteProdotto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idVariante;
    
    // Prodotto associato alla specifica variante
    private Prodotto prodotto; 
    
    private String colore;
    private String taglia;
    private BigDecimal prezzo;
    private int quantitaMagazzino;

    public VarianteProdotto() {
        super();
    }

    public VarianteProdotto(int idVariante, Prodotto prodotto, String colore, String taglia, BigDecimal prezzo, int quantitaMagazzino) {
        this.idVariante = idVariante;
        this.prodotto = prodotto;
        this.colore = colore;
        this.taglia = taglia;
        this.prezzo = prezzo;
        this.quantitaMagazzino = quantitaMagazzino;
    }

    // --- Getter e Setter ---
    public int getIdVariante() { return idVariante; }
    public void setIdVariante(int idVariante) { this.idVariante = idVariante; }

    public Prodotto getProdotto() { return prodotto; }
    public void setProdotto(Prodotto prodotto) { this.prodotto = prodotto; }

    public String getColore() { return colore; }
    public void setColore(String colore) { this.colore = colore; }

    public String getTaglia() { return taglia; }
    public void setTaglia(String taglia) { this.taglia = taglia; }

    public BigDecimal getPrezzo() { return prezzo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }

    public int getQuantitaMagazzino() { return quantitaMagazzino; }
    public void setQuantitaMagazzino(int quantitaMagazzino) { this.quantitaMagazzino = quantitaMagazzino; }

    @Override
    public String toString() {
        return "VarianteProdotto [idVariante=" + idVariante + ", prodotto=" + (prodotto != null ? prodotto.getNome() : "null") 
               + ", colore=" + colore + ", taglia=" + taglia + ", prezzo=" + prezzo + "]";
    }
}