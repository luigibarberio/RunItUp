package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import it.runitup.model.bean.Ordine;
import it.runitup.model.dto.VoceCarrelloDTO;
import it.runitup.util.ConnectionPool;

public class OrdineDAO {

    // Questo metodo salva un ordine completo, i dettagli e aggiorna il magazzino in un'unica transazione
    public int salvaOrdineCompleto(Ordine ordine, List<VoceCarrelloDTO> voci, int idUtente) throws SQLException {
        Connection con = null;
        int idOrdineGenerato = -1;

        // Query necessarie per la transazione
        String insertOrdine = "INSERT INTO ordine (id_utente, id_coupon, totale_imponibile, totale_iva, totale_pagato, nome_e_cognome_destinatario, indirizzo_spedizione, citta, cap, metodo_pagamento, stato) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS metodo_pagamento), CAST(? AS stato_ordine))";
                              
        String insertDettaglio = "INSERT INTO dettaglio_ordine (id_ordine, id_variante, nome_prodotto_storico, colore_storico, taglia_storica, quantita_acquistata, prezzo_unitario_imponibile, aliquota_iva, prezzo_unitario_con_iva) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                 
        String updateMagazzino = "UPDATE variante_prodotto SET quantita_magazzino = quantita_magazzino - ? " +
                                 "WHERE id_variante = ? AND quantita_magazzino >= ?";
                                 
        String svuotaCarrello = "DELETE FROM voce_carrello WHERE id_carrello = (SELECT id_carrello FROM carrello WHERE id_utente = ?)";

                              
        try {
            con = ConnectionPool.getConnection();
            con.setAutoCommit(false); // Inizio transazione

            // 1. INSERIMENTO ORDINE PRINCIPALE
            try (PreparedStatement psOrd = con.prepareStatement(insertOrdine, Statement.RETURN_GENERATED_KEYS)) {
                psOrd.setInt(1, ordine.getUtente().getIdUtente());
                
                if (ordine.getCoupon() != null) {
                    psOrd.setInt(2, ordine.getCoupon().getIdCoupon());
                } else {
                    psOrd.setNull(2, java.sql.Types.INTEGER);
                }
                
                psOrd.setBigDecimal(3, ordine.getTotaleImponibile());
                psOrd.setBigDecimal(4, ordine.getTotaleIva());
                psOrd.setBigDecimal(5, ordine.getTotalePagato());
                
                // NUOVI CAMPI INSERITI
                psOrd.setString(6, ordine.getNome_e_cognome_destinatario());
                psOrd.setString(7, ordine.getIndirizzoSpedizione());
                psOrd.setString(8, ordine.getCitta());
                psOrd.setString(9, ordine.getCap());
                psOrd.setString(10, ordine.getMetodoPagamento()); // Passiamo la stringa "CARTA", "PAYPAL", ecc. Postgres farà il cast
                psOrd.setString(11, ordine.getStato().name()); 
                
                psOrd.executeUpdate();
                                
                // Recuperiamo l'ID univoco assegnato dal database all'ordine
                try (ResultSet rsKeys = psOrd.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idOrdineGenerato = rsKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione ordine fallita, nessun ID generato.");
                    }
                }
            }

            // 2. ITERAZIONE SULLE VOCI DEL CARRELLO (Dettagli e Magazzino)
            try (PreparedStatement psDet = con.prepareStatement(insertDettaglio);
                 PreparedStatement psMag = con.prepareStatement(updateMagazzino)) {
                
                for (VoceCarrelloDTO voceDTO : voci) {
                    int idVariante = voceDTO.getVarianteDTO().getVariante().getIdVariante();
                    int qta = voceDTO.getVoceCarrello().getQuantita();
                    
                    // 2a. Sottrazione Magazzino con Hard-Check Concorrenziale
                    psMag.setInt(1, qta);
                    psMag.setInt(2, idVariante);
                    psMag.setInt(3, qta); // Verifica: Il magazzino DEVE essere >= della quantità richiesta!
                    
                    int righeAggiornate = psMag.executeUpdate();
                    
                    if (righeAggiornate == 0) {
                        // Se righeAggiornate è 0, significa che la scarpa è finita un attimo prima del click.
                        // Lanciamo un'eccezione che farà scattare il ROLLBACK
                        throw new SQLException("STOCK_ESAURITO_" + idVariante); 
                    }
                    
                    // 2b. Inserimento Dettaglio (Snapshot storico)
                    psDet.setInt(1, idOrdineGenerato);
                    psDet.setInt(2, idVariante);
                    psDet.setString(3, voceDTO.getVarianteDTO().getVariante().getProdotto().getNome());
                    psDet.setString(4, voceDTO.getVarianteDTO().getVariante().getColore());
                    psDet.setString(5, voceDTO.getVarianteDTO().getVariante().getTaglia());
                    psDet.setInt(6, qta);
                    
                    // Calcoli IVA del singolo prodotto (Scorporo 22%)
                    java.math.BigDecimal prezzoIvato = voceDTO.getVarianteDTO().getPrezzoEffettivo();
                    java.math.BigDecimal aliquota = new java.math.BigDecimal("0.22");
                    java.math.BigDecimal divisore = new java.math.BigDecimal("1.22");
                    java.math.BigDecimal prezzoImponibile = prezzoIvato.divide(divisore, 2, java.math.RoundingMode.HALF_UP);
                    
                    psDet.setBigDecimal(7, prezzoImponibile);
                    psDet.setBigDecimal(8, aliquota);
                    psDet.setBigDecimal(9, prezzoIvato);
                    
                    psDet.executeUpdate();
                }
            }

            // 3. SVUOTAMENTO DEL CARRELLO DELL'UTENTE
            try (PreparedStatement psCart = con.prepareStatement(svuotaCarrello)) {
                psCart.setInt(1, idUtente);
                psCart.executeUpdate();
            }

            // SE ARRIVIAMO QUI SENZA ERRORI, CONFERMIAMO TUTTO SUL DB
            con.commit();
            return idOrdineGenerato;

        } catch (SQLException e) {
            // IN CASO DI ERRORE, ANNULLIAMO TUTTE LE OPERAZIONI FATTE FINORA!
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            // Se l'errore è dovuto allo stock, restituiamo un codice speciale per dirlo alla Servlet
            if (e.getMessage() != null && e.getMessage().startsWith("STOCK_ESAURITO")) {
                return -1; 
            }
            throw e; // Rilanciamo l'eccezione per errori di DB "veri"
            
        } finally {
            // Ripristiniamo l'autocommit di default e rilasciamo la connessione
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) { e.printStackTrace(); }
                ConnectionPool.releaseConnection(con);
            }
        }
    }
}