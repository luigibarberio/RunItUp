package it.runitup.model.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

public class Coupon implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idCoupon;
    private String codice;
    private BigDecimal valoreSconto;
    private TipoScontoCoupon tipoSconto;
    private LocalDate dataInizio;
    private LocalDate dataScadenza; // Può essere null

    public Coupon() {
        super();
    }

    public Coupon(int idCoupon, String codice, BigDecimal valoreSconto, TipoScontoCoupon tipoSconto,
                  LocalDate dataInizio, LocalDate dataScadenza) {
        this.idCoupon = idCoupon;
        this.codice = codice;
        this.valoreSconto = valoreSconto;
        this.tipoSconto = tipoSconto;
        this.dataInizio = dataInizio;
        this.dataScadenza = dataScadenza;
    }

    // --- Getter e Setter ---
    public int getIdCoupon() { return idCoupon; }
    public void setIdCoupon(int idCoupon) { this.idCoupon = idCoupon; }

    public String getCodice() { return codice; }
    public void setCodice(String codice) { this.codice = codice; }

    public BigDecimal getValoreSconto() { return valoreSconto; }
    public void setValoreSconto(BigDecimal valoreSconto) { this.valoreSconto = valoreSconto; }

    public TipoScontoCoupon getTipoSconto() { return tipoSconto; }
    public void setTipoSconto(TipoScontoCoupon tipoSconto) { this.tipoSconto = tipoSconto; }

    public LocalDate getDataInizio() { return dataInizio; }
    public void setDataInizio(LocalDate dataInizio) { this.dataInizio = dataInizio; }

    public LocalDate getDataScadenza() { return dataScadenza; }
    public void setDataScadenza(LocalDate dataScadenza) { this.dataScadenza = dataScadenza; }

    @Override
    public String toString() {
        return "Coupon [idCoupon=" + idCoupon + ", codice=" + codice + ", valoreSconto=" + valoreSconto
                + ", tipoSconto=" + tipoSconto + ", dataInizio=" + dataInizio + ", dataScadenza=" + dataScadenza + "]";
    }
}