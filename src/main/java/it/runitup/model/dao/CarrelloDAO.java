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
        
        // Query con JOIN per recuperare in un colpo solo Voce, Variante, Prodotto e Brand
        String query = "SELECT vc.id_voce, vc.quantita, " +
                       "vp.id_variante, vp.colore, vp.taglia, vp.prezzo AS prezzo_variante, vp.quantita_magazzino, " +
                       "p.id_prodotto, p.nome AS nome_prodotto, p.immagine_copertina, " +
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
                    prodotto.setImmagineCopertina(rs.getBytes("immagine_copertina"));
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
        
        // 1. Otteniamo l'ID del carrello (se non esiste, il metodo lo crea al volo)
        int idCarrello = getOrCreateCarrelloByUtente(idUtente).getIdCarrello();

        try (Connection con = ConnectionPool.getConnection()) {
            
            // 2. Recuperiamo la giacenza attuale in magazzino
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

            // 3. Controlliamo se la variante è già nel carrello e quanta ce n'è
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

            // 4. HARD-CHECK: Verificata la disponibilità totale
            if (quantitaInCarrello + quantitaAggiunta > quantitaMagazzino) {
                return false; // Blocco: superata la quantità in magazzino
            }

            // 5. Inserimento o Aggiornamento sicuro
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
        }
    }

    // Metodo per ottenere o creare un carrello associato a un utente specifico
    public Carrello getOrCreateCarrelloByUtente(int idUtente) throws SQLException {
        Carrello carrello = null;
        
        // 1. Proviamo a cercare il carrello esistente
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
                        
                        return carrello; // Trovato, lo restituiamo e usciamo
                    }
                }
            }

            // 2. Se arriviamo qui, il carrello non esiste. Lo creiamo.
            String insertQuery = "INSERT INTO carrello (id_utente) VALUES (?)";
            
            // Usiamo RETURN_GENERATED_KEYS per farci restituire subito l'ID appena creato dal DB
            try (PreparedStatement psInsert = con.prepareStatement(insertQuery, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psInsert.setInt(1, idUtente);
                int affectedRows = psInsert.executeUpdate();
                
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
        
        return carrello;
    }
    
}
