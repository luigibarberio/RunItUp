package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.runitup.model.bean.Utente;
import it.runitup.util.ConnectionPool; // Assicurati che il package del pool sia corretto

public class UtenteDAO {

    // Metodo per autenticare un utente tramite email (Login)
    public Utente doRetrieveByEmail(String email) throws SQLException {
        
        Utente utente = null;
        
        // Query SQL con parametri (PreparedStatement) per evitare SQL Injection
        String query = "SELECT * FROM utente WHERE email = ?";
        
        Connection con = ConnectionPool.getConnection();

        try{
            // Il costrutto try-with-resources chiude in automatico le risorse (Connection e Statement)
            try (PreparedStatement ps = con.prepareStatement(query)) {
                
                ps.setString(1, email);
                
                // Esecuzione della query e gestione del ResultSet
                try (ResultSet rs = ps.executeQuery()) {
                    
                    // Se c'è almeno una riga (l'utente esiste ed è unico)
                    if (rs.next()) {
                        utente = new Utente();
                        utente.setIdUtente(rs.getInt("id_utente"));
                        utente.setNome(rs.getString("nome"));
                        utente.setCognome(rs.getString("cognome"));
                        utente.setEmail(rs.getString("email"));
                        utente.setPasswordHash(rs.getString("password_hash"));
                        utente.setTelefono(rs.getString("telefono"));
                        utente.setAdmin(rs.getBoolean("is_admin"));
                    }
                }
            } 
        // L'eccezione SQLException viene "lanciata" (throws) al chiamante (la Servlet)
        // Si chiude la connessione nel blocco finally per garantire che venga sempre rilasciata al pool, anche in caso di errori
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        return utente;
    }

    // Metodo per recuperare un utente tramite ID (Utile per la sincronizzazione dei dati dell'Area Personale e per il Pannello Admin)
    public Utente doRetrieveById(int idUtente) throws SQLException {
        Utente utente = null;
        String query = "SELECT * FROM utente WHERE id_utente = ?";
        
        Connection con = ConnectionPool.getConnection();
        
        try {
            // Il try-with-resources chiude automaticamente PreparedStatement e ResultSet
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setInt(1, idUtente);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        utente = new Utente();
                        utente.setIdUtente(rs.getInt("id_utente"));
                        utente.setNome(rs.getString("nome"));
                        utente.setCognome(rs.getString("cognome"));
                        utente.setEmail(rs.getString("email"));
                        utente.setPasswordHash(rs.getString("password_hash"));
                        utente.setTelefono(rs.getString("telefono"));
                        utente.setAdmin(rs.getBoolean("is_admin"));
                    }
                }
            }
        // Rilasciamo sempre la connessione al pool, garantendo la robustezza del server
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        return utente;
    }

    // Metodo per recuperare un utente tramite numero di telefono, utile per evitare duplicazioni di contatti
    public Utente doRetrieveByTelefono(String telefono) throws SQLException {
        Utente utente = null;
        String query = "SELECT * FROM utente WHERE telefono = ?";
        
        Connection con = ConnectionPool.getConnection();
        
        try {
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, telefono);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        utente = new Utente();
                        utente.setIdUtente(rs.getInt("id_utente"));
                        utente.setNome(rs.getString("nome"));
                        utente.setCognome(rs.getString("cognome"));
                        utente.setEmail(rs.getString("email"));
                        utente.setPasswordHash(rs.getString("password_hash"));
                        utente.setTelefono(rs.getString("telefono"));
                        utente.setAdmin(rs.getBoolean("is_admin"));
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        return utente;
    }

    // Metodo per salvare un nuovo utente nel database (Registrazione)
    public void doSave(Utente utente) throws SQLException {
        
        // Query SQL con parametri (PreparedStatement) per evitare SQL Injection
        String query = "INSERT INTO utente (nome, cognome, email, password_hash, telefono, is_admin) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            
            // Prendiamo i dati dall'oggetto Java e li infiliamo nei '?'
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            ps.setString(3, utente.getEmail());
            ps.setString(4, utente.getPasswordHash());
            ps.setString(5, utente.getTelefono());
            ps.setBoolean(6, utente.isAdmin()); // Di default un nuovo utente non è admin
            
            // Esecuzione dell'INSERT
            ps.executeUpdate();
        }finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
    }

    // Metodo per aggiornare i dati anagrafici di un utente
    public void doUpdateAnagrafica(Utente utente) throws SQLException {
        String query = "UPDATE utente SET nome = ?, cognome = ?, telefono = ? WHERE id_utente = ?";
        
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            
            // Gestione coerente del telefono (nullable nel database)
            if (utente.getTelefono() != null && !utente.getTelefono().trim().isEmpty()) {
                ps.setString(3, utente.getTelefono());
            } else {
                ps.setNull(3, java.sql.Types.VARCHAR);
            }
            
            ps.setInt(4, utente.getIdUtente());
            
            ps.executeUpdate();
            
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
    }

    // Metodo per aggiornare l'hash della password di un utente
    public void doUpdateCompleto(Utente utente) throws SQLException {
        String query = "UPDATE utente SET nome = ?, cognome = ?, telefono = ?, password_hash = ? WHERE id_utente = ?";
        
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setString(1, utente.getNome());
            ps.setString(2, utente.getCognome());
            ps.setString(3, utente.getTelefono());
            ps.setString(4, utente.getPasswordHash());
            ps.setInt(5, utente.getIdUtente());
            
            ps.executeUpdate();
            
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
    }    

}