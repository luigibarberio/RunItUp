package it.runitup.model.bean;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Ordine implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idOrdine;
    private Utente utente;
    private Coupon coupon; // Può essere null
    private LocalDateTime dataOrdine;
    
    // Totali fiscali con precisione assoluta
    private BigDecimal totaleImponibile;
    private BigDecimal totaleIva;
    private BigDecimal totalePagato;
    
    private String nome_e_cognome_destinatario;
    private String indirizzoSpedizione;
    private String citta;
    private String cap;
    private String metodoPagamento;
    private StatoOrdine stato;

    public Ordine() {
        super();
    }

    public Ordine(int idOrdine, Utente utente, Coupon coupon, LocalDateTime dataOrdine, BigDecimal totaleImponibile,
                  BigDecimal totaleIva, BigDecimal totalePagato, String nome_e_cognome_destinatario, String indirizzoSpedizione, String citta, String cap,
                  String metodoPagamento, StatoOrdine stato) {
        this.idOrdine = idOrdine;
        this.utente = utente;
        this.coupon = coupon;
        this.dataOrdine = dataOrdine;
        this.totaleImponibile = totaleImponibile;
        this.totaleIva = totaleIva;
        this.totalePagato = totalePagato;
        this.nome_e_cognome_destinatario = nome_e_cognome_destinatario;
        this.indirizzoSpedizione = indirizzoSpedizione;
        this.citta = citta;
        this.cap = cap;
        this.metodoPagamento = metodoPagamento;
        this.stato = stato;
    }

    // --- Getter e Setter ---
    public int getIdOrdine() { return idOrdine; }
    public void setIdOrdine(int idOrdine) { this.idOrdine = idOrdine; }

    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { this.utente = utente; }

    public Coupon getCoupon() { return coupon; }
    public void setCoupon(Coupon coupon) { this.coupon = coupon; }

    public LocalDateTime getDataOrdine() { return dataOrdine; }
    public void setDataOrdine(LocalDateTime dataOrdine) { this.dataOrdine = dataOrdine; }

    public BigDecimal getTotaleImponibile() { return totaleImponibile; }
    public void setTotaleImponibile(BigDecimal totaleImponibile) { this.totaleImponibile = totaleImponibile; }

    public BigDecimal getTotaleIva() { return totaleIva; }
    public void setTotaleIva(BigDecimal totaleIva) { this.totaleIva = totaleIva; }

    public BigDecimal getTotalePagato() { return totalePagato; }
    public void setTotalePagato(BigDecimal totalePagato) { this.totalePagato = totalePagato; }

    public String getIndirizzoSpedizione() { return indirizzoSpedizione; }
    public void setIndirizzoSpedizione(String indirizzoSpedizione) { this.indirizzoSpedizione = indirizzoSpedizione; }

    public String getCitta() { return citta; }
    public void setCitta(String citta) { this.citta = citta; }

    public String getCap() { return cap; }
    public void setCap(String cap) { this.cap = cap; }

    public String getMetodoPagamento() { return metodoPagamento; }
    public void setMetodoPagamento(String metodoPagamento) { this.metodoPagamento = metodoPagamento; }

    public StatoOrdine getStato() { return stato; }
    public void setStato(StatoOrdine stato) { this.stato = stato; }

    public String getNome_e_cognome_destinatario() { return nome_e_cognome_destinatario; }
    public void setNome_e_cognome_destinatario(String nome_e_cognome_destinatario) { this.nome_e_cognome_destinatario = nome_e_cognome_destinatario; }

    @Override
    public String toString() {
        return "Ordine [idOrdine=" + idOrdine + ", utente=" + (utente != null ? utente.getEmail() : "null") + ", totalePagato=" + totalePagato + ", stato=" + stato + ", nome_e_cognome_destinatario=" + nome_e_cognome_destinatario + "]";
    }
}