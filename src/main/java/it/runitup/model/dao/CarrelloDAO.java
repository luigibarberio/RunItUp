package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.bean.Brand;
import it.runitup.model.bean.Prodotto;
import it.runitup.model.bean.Utente;
import it.runitup.model.bean.VarianteProdotto;
import it.runitup.model.bean.Carrello;

import it.runitup.util.ConnectionPool;

public class CarrelloDAO {
        
    // Metodo per ottenere tutte le voci di un carrello specifico
    public List<VoceCarrello> getVociByCarrello(int idCarrello) throws SQLException {
        List<VoceCarrello> voci = new ArrayList<>();
        
        // Query SQL per ottenere tutte le voci del carrello con i dettagli delle varianti e dei prodotti
        String query = "SELECT vc.id_voce, vc.quantita, " +
                       "vp.id_variante, vp.colore, vp.taglia, vp.prezzo AS prezzo_variante, vp.quantita_magazzino, " +
                       "p.id_prodotto, p.nome AS nome_prodotto, " +
                       "b.id_brand, b.nome_brand " +
                       "FROM voce_carrello vc " +
                       "JOIN variante_prodotto vp ON vc.id_variante = vp.id_variante " +
                       "JOIN prodotto p ON vp.id_prodotto = p.id_prodotto " +
                       "JOIN brand b ON p.id_brand = b.id_brand " +
                       "WHERE vc.id_carrello = ?";

        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idCarrello);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    // 1. Mappatura del Brand
                    Brand brand = new Brand();
                    brand.setIdBrand(rs.getInt("id_brand"));
                    brand.setNomeBrand(rs.getString("nome_brand"));

                    // 2. Mappatura del Prodotto
                    Prodotto prodotto = new Prodotto();
                    prodotto.setIdProdotto(rs.getInt("id_prodotto"));
                    prodotto.setNome(rs.getString("nome_prodotto"));
                    prodotto.setBrand(brand);

                    // 3. Mappatura della Variante
                    VarianteProdotto variante = new VarianteProdotto();
                    variante.setIdVariante(rs.getInt("id_variante"));
                    variante.setColore(rs.getString("colore"));
                    variante.setTaglia(rs.getString("taglia"));
                    variante.setPrezzo(rs.getBigDecimal("prezzo_variante"));
                    variante.setQuantitaMagazzino(rs.getInt("quantita_magazzino"));
                    variante.setProdotto(prodotto);

                    // 4. Creazione fittizia del Carrello (ci basta l'ID per coerenza del Bean)
                    Carrello carrello = new Carrello();
                    carrello.setIdCarrello(idCarrello);

                    // 5. Mappatura finale della VoceCarrello
                    VoceCarrello voce = new VoceCarrello();
                    voce.setIdVoce(rs.getInt("id_voce"));
                    voce.setQuantita(rs.getInt("quantita"));
                    voce.setVariante(variante);
                    voce.setCarrello(carrello);

                    // Aggiunta alla lista
                    voci.add(voce);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        return voci;
    }

    // Metodo per aggiungere una variante al carrello dell'utente (con Hard-Check sulle quantità)
    public boolean aggiungiVariante(int idUtente, int idVariante, int quantitaAggiunta) throws SQLException {
        
        // Ottenimento o creazione del carrello dell'utente
        int idCarrello = getOrCreateCarrelloByUtente(idUtente).getIdCarrello();

        Connection con = ConnectionPool.getConnection();
        try {
            
            // Controllo della quantità disponibile in magazzino per la variante specifica
            int quantitaMagazzino = 0;
            String magazzinoQuery = "SELECT quantita_magazzino FROM variante_prodotto WHERE id_variante = ?";
            try (PreparedStatement psMag = con.prepareStatement(magazzinoQuery)) {
                psMag.setInt(1, idVariante);
                try (ResultSet rsMag = psMag.executeQuery()) {
                    if (rsMag.next()) {
                        quantitaMagazzino = rsMag.getInt("quantita_magazzino");
                    } else {
                        return false; // Variante non esistente
                    }
                }
            }

            // Controllo della quantità già presente nel carrello per la variante specifica
            int quantitaInCarrello = 0;
            String checkQuery = "SELECT quantita FROM voce_carrello WHERE id_carrello = ? AND id_variante = ?";
            try (PreparedStatement psCheck = con.prepareStatement(checkQuery)) {
                psCheck.setInt(1, idCarrello);
                psCheck.setInt(2, idVariante);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        quantitaInCarrello = rsCheck.getInt("quantita");
                    }
                }
            }

            // Verifica se l'aggiunta della quantità richiesta supera la quantità disponibile in magazzino
            if (quantitaInCarrello + quantitaAggiunta > quantitaMagazzino) {
                return false; // Blocco: superata la quantità in magazzino
            }

            // Se la voce esiste già nel carrello, si aggiorna la quantità; altrimenti, si inserisce una nuova voce
            if (quantitaInCarrello > 0) {
                String updateQuery = "UPDATE voce_carrello SET quantita = quantita + ? WHERE id_carrello = ? AND id_variante = ?";
                try (PreparedStatement psUpdate = con.prepareStatement(updateQuery)) {
                    psUpdate.setInt(1, quantitaAggiunta);
                    psUpdate.setInt(2, idCarrello);
                    psUpdate.setInt(3, idVariante);
                    psUpdate.executeUpdate();
                }
            } else {
                String insertQuery = "INSERT INTO voce_carrello (id_carrello, id_variante, quantita) VALUES (?, ?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(insertQuery)) {
                    psInsert.setInt(1, idCarrello);
                    psInsert.setInt(2, idVariante);
                    psInsert.setInt(3, quantitaAggiunta);
                    psInsert.executeUpdate();
                }
            }
            
            return true; // Operazione completata con successo
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
    }

    // Metodo per ottenere o creare un carrello associato a un utente specifico
    public Carrello getOrCreateCarrelloByUtente(int idUtente) throws SQLException {
        Carrello carrello = null;
        
        // Si tenta di recuperare il carrello esistente per l'utente
        String selectQuery = "SELECT id_carrello FROM carrello WHERE id_utente = ?";
        
        Connection con = ConnectionPool.getConnection();
        try {
            try (PreparedStatement psSelect = con.prepareStatement(selectQuery)) {
                psSelect.setInt(1, idUtente);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (rs.next()) {
                        carrello = new Carrello();
                        carrello.setIdCarrello(rs.getInt("id_carrello"));
                        
                        Utente u = new Utente();
                        u.setIdUtente(idUtente);
                        carrello.setUtente(u);
                        
                        return carrello; // Se esiste già, lo restituiamo subito
                    }
                }
            }

            // Se non esiste, creiamo un nuovo carrello per l'utente
            String insertQuery = "INSERT INTO carrello (id_utente) VALUES (?)";
            
            // Si utilizza RETURN_GENERATED_KEYS per ottenere l'ID del nuovo carrello appena creato
            try (PreparedStatement psInsert = con.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, idUtente);
                int affectedRows = psInsert.executeUpdate();
                
                // Se l'inserimento ha avuto successo, recuperiamo l'ID generato e creiamo il bean Carrello
                if (affectedRows > 0) {
                    try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                        if (rsKeys.next()) {
                            carrello = new Carrello();
                            carrello.setIdCarrello(rsKeys.getInt(1)); // Recuperiamo l'ID generato
                            
                            Utente u = new Utente();
                            u.setIdUtente(idUtente);
                            carrello.setUtente(u);
                        }
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        // Restituiamo il carrello appena creato o quello esistente
        return carrello;
    }
    
    // Metodo per ottenere la quantità di una voce specifica nel carrello
    public int getQuantitaVoce(int idCarrello, int idVariante) throws SQLException {
        String query = "SELECT quantita FROM voce_carrello WHERE id_carrello = ? AND id_variante = ?";
        
        Connection con = ConnectionPool.getConnection();

        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idCarrello);
            ps.setInt(2, idVariante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("quantita");
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return 0;
    }

    // Metodo per rimuovere una voce specifica dal carrello
    public void rimuoviVoce(int idCarrello, int idVariante) throws SQLException {
        String query = "DELETE FROM voce_carrello WHERE id_carrello = ? AND id_variante = ?";
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idCarrello);
            ps.setInt(2, idVariante);
            ps.executeUpdate();
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
    }

    // Metodo per impostare forzatamente la quantità di una voce nel carrello
    public void impostaQuantitaVoce(int idCarrello, int idVariante, int nuovaQuantita) throws SQLException {
        String checkQuery = "SELECT id_voce FROM voce_carrello WHERE id_carrello = ? AND id_variante = ?";
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement psCheck = con.prepareStatement(checkQuery)) {
            psCheck.setInt(1, idCarrello);
            psCheck.setInt(2, idVariante);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next()) {
                    String updateQuery = "UPDATE voce_carrello SET quantita = ? WHERE id_carrello = ? AND id_variante = ?";
                    try (PreparedStatement psUp = con.prepareStatement(updateQuery)) {
                        psUp.setInt(1, nuovaQuantita);
                        psUp.setInt(2, idCarrello);
                        psUp.setInt(3, idVariante);
                        psUp.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO voce_carrello (id_carrello, id_variante, quantita) VALUES (?, ?, ?)";
                    try (PreparedStatement psIn = con.prepareStatement(insertQuery)) {
                        psIn.setInt(1, idCarrello);
                        psIn.setInt(2, idVariante);
                        psIn.setInt(3, nuovaQuantita);
                        psIn.executeUpdate();
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
    }

}
