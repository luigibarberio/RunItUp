package it.runitup.model.bean;

import java.io.Serializable;

public class Utente implements Serializable {

    private static final long serialVersionUID = 1L;

    // Attributi
    private int idUtente;
    private String nome;
    private String cognome;
    private String email;
    private String passwordHash;
    private String telefono;
    private boolean admin;

    // Costruttore vuoto obbligatorio
    public Utente() {
        super();
    }

    // Costruttore con parametri
    public Utente(int idUtente, String nome, String cognome, String email, String passwordHash, String telefono, boolean admin) {
        this.idUtente = idUtente;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.passwordHash = passwordHash;
        this.telefono = telefono;
        this.admin = admin;
    }

    // --- Getter e Setter ---
    public int getIdUtente() { return idUtente; }

    public void setIdUtente(int idUtente) { this.idUtente = idUtente; }

    public String getNome() { return nome; }

    public void setNome(String nome) { this.nome = nome; }

    public String getCognome() { return cognome; }

    public void setCognome(String cognome) { this.cognome = cognome; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }

    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getTelefono() { return telefono; }

    public void setTelefono(String telefono) { this.telefono = telefono; }

    public boolean isAdmin() { return admin; }

    public void setAdmin(boolean admin) { this.admin = admin; }

    @Override
    public String toString() {
        return "Utente [idUtente=" + idUtente + ", nome=" + nome + ", cognome=" + cognome + ", email=" + email
                + ", admin=" + admin + "]";
    }
}