package it.runitup.model.bean;

import java.io.Serializable;
import java.math.BigDecimal;

public class DettaglioOrdine implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idDettaglio;
    private Ordine ordine;
    private VarianteProdotto variante; // Può essere null nel DB se eliminata fisicamente
    
    // Dati storici congelati (Snapshot)
    private String nomeProdottoStorico;
    private String coloreStorico;
    private String tagliaStorica;
    
    // Dati dell'acquisto
    private int quantitaAcquistata;
    private BigDecimal prezzoUnitarioImponibile;
    private BigDecimal aliquotaIva;
    private BigDecimal prezzoUnitarioConIva;

    public DettaglioOrdine() {
        super();
    }

    public DettaglioOrdine(int idDettaglio, Ordine ordine, VarianteProdotto variante, String nomeProdottoStorico,
                           String coloreStorico, String tagliaStorica, int quantitaAcquistata,
                           BigDecimal prezzoUnitarioImponibile, BigDecimal aliquotaIva, BigDecimal prezzoUnitarioConIva) {
        this.idDettaglio = idDettaglio;
        this.ordine = ordine;
        this.variante = variante;
        this.nomeProdottoStorico = nomeProdottoStorico;
        this.coloreStorico = coloreStorico;
        this.tagliaStorica = tagliaStorica;
        this.quantitaAcquistata = quantitaAcquistata;
        this.prezzoUnitarioImponibile = prezzoUnitarioImponibile;
        this.aliquotaIva = aliquotaIva;
        this.prezzoUnitarioConIva = prezzoUnitarioConIva;
    }

    // --- Getter e Setter ---
    public int getIdDettaglio() { return idDettaglio; }
    public void setIdDettaglio(int idDettaglio) { this.idDettaglio = idDettaglio; }

    public Ordine getOrdine() { return ordine; }
    public void setOrdine(Ordine ordine) { this.ordine = ordine; }

    public VarianteProdotto getVariante() { return variante; }
    public void setVariante(VarianteProdotto variante) { this.variante = variante; }

    public String getNomeProdottoStorico() { return nomeProdottoStorico; }
    public void setNomeProdottoStorico(String nomeProdottoStorico) { this.nomeProdottoStorico = nomeProdottoStorico; }

    public String getColoreStorico() { return coloreStorico; }
    public void setColoreStorico(String coloreStorico) { this.coloreStorico = coloreStorico; }

    public String getTagliaStorica() { return tagliaStorica; }
    public void setTagliaStorica(String tagliaStorica) { this.tagliaStorica = tagliaStorica; }

    public int getQuantitaAcquistata() { return quantitaAcquistata; }
    public void setQuantitaAcquistata(int quantitaAcquistata) { this.quantitaAcquistata = quantitaAcquistata; }

    public BigDecimal getPrezzoUnitarioImponibile() { return prezzoUnitarioImponibile; }
    public void setPrezzoUnitarioImponibile(BigDecimal prezzoUnitarioImponibile) { this.prezzoUnitarioImponibile = prezzoUnitarioImponibile; }

    public BigDecimal getAliquotaIva() { return aliquotaIva; }
    public void setAliquotaIva(BigDecimal aliquotaIva) { this.aliquotaIva = aliquotaIva; }

    public BigDecimal getPrezzoUnitarioConIva() { return prezzoUnitarioConIva; }
    public void setPrezzoUnitarioConIva(BigDecimal prezzoUnitarioConIva) { this.prezzoUnitarioConIva = prezzoUnitarioConIva; }

    @Override
    public String toString() {
        return "DettaglioOrdine [idDettaglio=" + idDettaglio + ", nomeProdottoStorico=" + nomeProdottoStorico + ", quantita=" + quantitaAcquistata + "]";
    }
}