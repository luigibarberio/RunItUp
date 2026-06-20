package it.runitup.model.dto;

import java.util.List;
import java.io.Serializable;
import java.math.BigDecimal;
import it.runitup.model.bean.Prodotto;

public class ProdottoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Prodotto di base con tutte le sue informazioni (ID, nome, descrizione, brand, categoria, ecc.)
    private Prodotto prodotto;

    // Campi aggiuntivi calcolati dal DAO per la visualizzazione
    private BigDecimal prezzoMinimo; // Prezzo minimo tra tutte le varianti, calcolato con eventuali sconti
    private BigDecimal prezzoOriginale; // Prezzo originale della variante a prezzo minimo (senza sconti)
    private boolean isNuovo;
    private boolean isScontato;
    private boolean haPrezziVariabili; // Indica se il prodotto ha varianti con prezzi diversi
    private List<String> coloriDisponibili; // Lista dei colori disponibili per il prodotto
    private List<String> taglieDisponibili; // Lista delle taglie disponibili per il prodotto

    public ProdottoDTO() {
        super();
    }

    // --- Getter e Setter ---
    public Prodotto getProdotto() { return prodotto; }
    public void setProdotto(Prodotto prodotto) { this.prodotto = prodotto; }

    public BigDecimal getPrezzoMinimo() { return prezzoMinimo; }
    public void setPrezzoMinimo(BigDecimal prezzoMinimo) { this.prezzoMinimo = prezzoMinimo; }

    public BigDecimal getPrezzoOriginale() { return prezzoOriginale; }
    public void setPrezzoOriginale(BigDecimal prezzoOriginale) { this.prezzoOriginale = prezzoOriginale; }

    public boolean isNuovo() { return isNuovo; }
    public void setNuovo(boolean isNuovo) { this.isNuovo = isNuovo; }

    public boolean isScontato() { return isScontato; }
    public void setScontato(boolean isScontato) { this.isScontato = isScontato; }

    public boolean isHaPrezziVariabili() { return haPrezziVariabili; }
    public void setHaPrezziVariabili(boolean haPrezziVariabili) { this.haPrezziVariabili = haPrezziVariabili; }

    public List<String> getColoriDisponibili() { return coloriDisponibili; }
    public void setColoriDisponibili(List<String> coloriDisponibili) { this.coloriDisponibili = coloriDisponibili; }

    public List<String> getTaglieDisponibili() { return taglieDisponibili; }
    public void setTaglieDisponibili(List<String> taglieDisponibili) { this.taglieDisponibili = taglieDisponibili; }

}