package it.runitup.model.bean;

import java.io.Serializable;

public class Carrello implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idCarrello;
    
    // Utente associato al carrello
    private Utente utente;

    public Carrello() {
        super();
    }

    public Carrello(int idCarrello, Utente utente) {
        this.idCarrello = idCarrello;
        this.utente = utente;
    }

    // --- Getter e Setter ---
    public int getIdCarrello() { return idCarrello; }
    public void setIdCarrello(int idCarrello) { this.idCarrello = idCarrello; }

    public Utente getUtente() { return utente; }
    public void setUtente(Utente utente) { this.utente = utente; }

    @Override
    public String toString() {
        return "Carrello [idCarrello=" + idCarrello + ", utente=" + (utente != null ? utente.getEmail() : "null") + "]";
    }
}