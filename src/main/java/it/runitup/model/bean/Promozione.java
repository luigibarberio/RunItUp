package it.runitup.model.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Promozione implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idPromozione;
    private String nome;
    private BigDecimal percentuale;
    private LocalDateTime dataInizio;
    private LocalDateTime dataFine;

    public Promozione() {
        super();
    }

    public Promozione(int idPromozione, String nome, BigDecimal percentuale, LocalDateTime dataInizio, LocalDateTime dataFine) {
        this.idPromozione = idPromozione;
        this.nome = nome;
        this.percentuale = percentuale;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
    }

    // --- Getter e Setter ---
    public int getIdPromozione() { return idPromozione; }
    public void setIdPromozione(int idPromozione) { this.idPromozione = idPromozione; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public BigDecimal getPercentuale() { return percentuale; }
    public void setPercentuale(BigDecimal percentuale) { this.percentuale = percentuale; }

    public LocalDateTime getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDateTime dataInizio) { this.dataInizio = dataInizio; }

    public LocalDateTime getDataFine() { return dataFine; }
    public void setDataFine(LocalDateTime dataFine) { this.dataFine = dataFine; }

    @Override
    public String toString() {
        return "Promozione [idPromozione=" + idPromozione + ", nome=" + nome + ", percentuale=" + percentuale
                + ", dataInizio=" + dataInizio + ", dataFine=" + dataFine + "]";
    }
}