package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import it.runitup.model.bean.Coupon;
import it.runitup.model.bean.DettaglioOrdine;
import it.runitup.model.bean.Ordine;
import it.runitup.model.bean.Prodotto;
import it.runitup.model.bean.VarianteProdotto;
import it.runitup.model.dto.VoceCarrelloDTO;
import it.runitup.model.bean.Utente;
import it.runitup.model.bean.TipoScontoCoupon;
import it.runitup.model.bean.StatoOrdine;
import it.runitup.util.ConnectionPool;

import java.time.LocalDateTime;

public class OrdineDAO {

    // Metodo per salvare un ordine completo, comprese le voci del carrello, aggiornando magazzino e vendite. Restituisce l'ID dell'ordine generato o -1 se lo stock è insufficiente.
    public int salvaOrdineCompleto(Ordine ordine, List<VoceCarrelloDTO> voci, int idUtente) throws SQLException {
        int idOrdineGenerato = -1;

        // Query necessarie per la transazione
        String insertOrdine = "INSERT INTO ordine (id_utente, id_coupon, totale_imponibile, totale_iva, totale_pagato, costo_spedizione, nome_e_cognome_destinatario, indirizzo_spedizione, citta, cap, metodo_pagamento, stato) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS metodo_pagamento), CAST(? AS stato_ordine))";
                              
        String insertDettaglio = "INSERT INTO dettaglio_ordine (id_ordine, id_variante, nome_prodotto_storico, colore_storico, taglia_storica, quantita_acquistata, prezzo_unitario_imponibile, aliquota_iva, prezzo_unitario_con_iva) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                 
        String updateMagazzino = "UPDATE variante_prodotto SET quantita_magazzino = quantita_magazzino - ? " +
                                 "WHERE id_variante = ? AND quantita_magazzino >= ?";
                                 
        String incrementaVendite = "UPDATE prodotto SET totale_vendite = totale_vendite + ? " +
                                   "WHERE id_prodotto = (SELECT id_prodotto FROM variante_prodotto WHERE id_variante = ?)";
                                 
        String svuotaCarrello = "DELETE FROM voce_carrello WHERE id_carrello = (SELECT id_carrello FROM carrello WHERE id_utente = ?)";

        Connection con = null;
        try {
            con = ConnectionPool.getConnection();
            con.setAutoCommit(false); // Inizio transazione

            // Inserimento dell'ordine principale e recupero dell'ID generato
            try (PreparedStatement psOrd = con.prepareStatement(insertOrdine, Statement.RETURN_GENERATED_KEYS)) {
                psOrd.setInt(1, ordine.getUtente().getIdUtente());
                
                if (ordine.getCoupon() != null) {
                    psOrd.setInt(2, ordine.getCoupon().getIdCoupon());
                } else {
                    psOrd.setNull(2, java.sql.Types.INTEGER);
                }
                
                // Calcolo dei totali e inserimento dei valori
                psOrd.setBigDecimal(3, ordine.getTotaleImponibile());
                psOrd.setBigDecimal(4, ordine.getTotaleIva());
                psOrd.setBigDecimal(5, ordine.getTotalePagato());
                psOrd.setBigDecimal(6, ordine.getCostoSpedizione());
                
                // Dati di spedizione e pagamento
                psOrd.setString(7, ordine.getNome_e_cognome_destinatario());
                psOrd.setString(8, ordine.getIndirizzoSpedizione());
                psOrd.setString(9, ordine.getCitta());
                psOrd.setString(10, ordine.getCap());
                psOrd.setString(11, ordine.getMetodoPagamento());
                psOrd.setString(12, ordine.getStato().name()); 
                
                psOrd.executeUpdate();
                                
                // Recupero dell'ID generato per l'ordine appena inserito
                try (ResultSet rsKeys = psOrd.getGeneratedKeys()) {
                    if (rsKeys.next()) {
                        idOrdineGenerato = rsKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione ordine fallita, nessun ID generato.");
                    }
                }
            }

            // Iterazione sulle voci del carrello per aggiornare magazzino, inserire dettagli e incrementare vendite
            try (PreparedStatement psDet = con.prepareStatement(insertDettaglio);
                 PreparedStatement psMag = con.prepareStatement(updateMagazzino);
                 PreparedStatement psVendite = con.prepareStatement(incrementaVendite)) {
                
                for (VoceCarrelloDTO voceDTO : voci) {
                    int idVariante = voceDTO.getVarianteDTO().getVariante().getIdVariante();
                    int qta = voceDTO.getVoceCarrello().getQuantita();
                    
                    // Aggiornamento magazzino: decremento della quantità disponibile, con controllo che non vada sotto zero
                    psMag.setInt(1, qta);
                    psMag.setInt(2, idVariante);
                    psMag.setInt(3, qta); 
                    
                    int righeAggiornate = psMag.executeUpdate();
                    
                    if (righeAggiornate == 0) {
                        throw new SQLException("STOCK_ESAURITO_" + idVariante); 
                    }
                    
                    // Inserimento del dettaglio dell'ordine con i dati storici del prodotto e della variante
                    psDet.setInt(1, idOrdineGenerato);
                    psDet.setInt(2, idVariante);
                    psDet.setString(3, voceDTO.getVarianteDTO().getVariante().getProdotto().getNome());
                    psDet.setString(4, voceDTO.getVarianteDTO().getVariante().getColore());
                    psDet.setString(5, voceDTO.getVarianteDTO().getVariante().getTaglia());
                    psDet.setInt(6, qta);
                    
                    java.math.BigDecimal prezzoIvato = voceDTO.getVarianteDTO().getPrezzoEffettivo();
                    java.math.BigDecimal aliquota = new java.math.BigDecimal("0.22");
                    java.math.BigDecimal divisore = new java.math.BigDecimal("1.22");
                    java.math.BigDecimal prezzoImponibile = prezzoIvato.divide(divisore, 2, java.math.RoundingMode.HALF_UP);
                    
                    psDet.setBigDecimal(7, prezzoImponibile);
                    psDet.setBigDecimal(8, aliquota);
                    psDet.setBigDecimal(9, prezzoIvato);
                    
                    psDet.executeUpdate();

                    // Incremento del totale vendite del prodotto associato alla variante acquistata
                    psVendite.setInt(1, qta);
                    psVendite.setInt(2, idVariante);
                    psVendite.executeUpdate();
                }
            }
            
            // Svuotamento del carrello dell'utente dopo il completamento dell'ordine
            try (PreparedStatement psCart = con.prepareStatement(svuotaCarrello)) {
                psCart.setInt(1, idUtente);
                psCart.executeUpdate();
            }

            // Commit della transazione se tutte le operazioni sono andate a buon fine
            con.commit();
            return idOrdineGenerato;

        } catch (SQLException e) {
            // Rollback della transazione in caso di errore, per garantire l'integrità dei dati
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            // Controllo specifico per lo stock esaurito: se l'eccezione contiene il messaggio "STOCK_ESAURITO", restituiamo -1 per indicare questo caso particolare
            if (e.getMessage() != null && e.getMessage().startsWith("STOCK_ESAURITO")) {
                return -1; 
            }
            throw e; // Rilancia l'eccezione per gestirla a un livello superiore
            
        } finally {
            // Rilascio della connessione e ripristino dell'auto-commit sulla connessione
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                } catch (SQLException e) { e.printStackTrace(); }
                ConnectionPool.releaseConnection(con);
            }
        }
    }

    // Metodo per recuperare tutti gli ordini di un utente, inclusi i dettagli del coupon se presente, ordinati per data decrescente. Utile per la visualizzazione della cronologia ordini di un utente.
    public List<Ordine> getOrdiniByUtente(int idUtente) throws SQLException {
        List<Ordine> lista = new ArrayList<>();
        
        // Query che recupera tutti gli ordini di un utente, includendo i dettagli del coupon se presente, e ordinati per data decrescente (LEFT JOIN con coupon per ottenere i dati del coupon se associato)
        String query = "SELECT o.*, c.codice, c.valore_sconto, c.tipo_sconto " +
                       "FROM ordine o " +
                       "LEFT JOIN coupon c ON o.id_coupon = c.id_coupon " +
                       "WHERE o.id_utente = ? " +
                       "ORDER BY o.data_ordine DESC";

        
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, idUtente);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mappaOrdineDaResultSet(rs));
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return lista;
    }

    // Metodo per recuperare un ordine specifico di un utente, includendo i dettagli del coupon se presente. Utile per la visualizzazione dei dettagli di un ordine specifico.
    public Ordine getOrdineById(int idOrdine) throws SQLException {
        String query = "SELECT o.*, c.codice, c.valore_sconto, c.tipo_sconto " +
                       "FROM ordine o " +
                       "LEFT JOIN coupon c ON o.id_coupon = c.id_coupon " +
                       "WHERE o.id_ordine = ?";

        Connection con = ConnectionPool.getConnection();
        
        try (PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, idOrdine);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mappaOrdineDaResultSet(rs);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return null;
    }

    // Metodo per recuperare i dettagli di un ordine specifico, inclusi i dati storici del prodotto e della variante al momento dell'acquisto. Utile per la visualizzazione dei dettagli dell'ordine.
    public List<DettaglioOrdine> getDettagliByOrdine(int idOrdine) throws SQLException {
        List<DettaglioOrdine> dettagli = new ArrayList<>();
        String query = "SELECT d.*, v.id_prodotto " +
                       "FROM dettaglio_ordine d " +
                       "LEFT JOIN variante_prodotto v ON d.id_variante = v.id_variante " +
                       "WHERE d.id_ordine = ?";

        
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, idOrdine);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DettaglioOrdine dett = new DettaglioOrdine();
                    dett.setIdDettaglio(rs.getInt("id_dettaglio"));
                    
                    // Assegnazione lazy dell'ordine
                    Ordine ord = new Ordine();
                    ord.setIdOrdine(idOrdine);
                    dett.setOrdine(ord);
                    
                    // Assegnazione lazy della variante (può essere null se la variante è stata rimossa dal DB)
                    int idVariante = rs.getInt("id_variante");
                    if (!rs.wasNull()) {
                        VarianteProdotto vp = new VarianteProdotto();
                        vp.setIdVariante(idVariante);

                        // Recuperiamo l'id_prodotto ottenuto dalla JOIN
                        int idProdotto = rs.getInt("id_prodotto");
                        if (!rs.wasNull()) {
                            Prodotto p = new Prodotto();
                            p.setIdProdotto(idProdotto);
                            vp.setProdotto(p); // ID PRODOTTO IDRATATO!
                        }

                        dett.setVariante(vp);
                    }

                    // Dati storici
                    dett.setNomeProdottoStorico(rs.getString("nome_prodotto_storico"));
                    dett.setColoreStorico(rs.getString("colore_storico"));
                    dett.setTagliaStorica(rs.getString("taglia_storica"));
                    dett.setQuantitaAcquistata(rs.getInt("quantita_acquistata"));
                    dett.setPrezzoUnitarioImponibile(rs.getBigDecimal("prezzo_unitario_imponibile"));
                    dett.setAliquotaIva(rs.getBigDecimal("aliquota_iva"));
                    dett.setPrezzoUnitarioConIva(rs.getBigDecimal("prezzo_unitario_con_iva"));
                    
                    dettagli.add(dett);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return dettagli;
    }

    // Metodo di utilità per mappare un ResultSet in un oggetto Ordine, inclusi l'utente e il coupon se presenti
    private Ordine mappaOrdineDaResultSet(ResultSet rs) throws SQLException {
        Ordine ord = new Ordine();
        ord.setIdOrdine(rs.getInt("id_ordine"));
        
        // Mappatura completa dell'utente associato all'ordine
        UtenteDAO utenteDAO = new UtenteDAO();
        Utente utente = utenteDAO.doRetrieveById(rs.getInt("id_utente"));
        ord.setUtente(utente);

        // Se l'ordine ha un coupon associato, lo mappiamo.
        int idCoupon = rs.getInt("id_coupon");
        if (!rs.wasNull()) {
            Coupon cp = new Coupon();
            cp.setIdCoupon(idCoupon);
            cp.setCodice(rs.getString("codice"));
            cp.setValoreSconto(rs.getBigDecimal("valore_sconto"));
            cp.setTipoSconto(TipoScontoCoupon.valueOf(rs.getString("tipo_sconto")));
            ord.setCoupon(cp);
        }

        // Conversione Timestamp (DB) in LocalDateTime (Java)
        java.sql.Timestamp ts = rs.getTimestamp("data_ordine");
        if (ts != null) {
            // Convertiamo il timestamp in LocalDateTime tenendo conto del fuso orario di Roma
            LocalDateTime ldt = ts.toInstant()
                                    .atZone(java.time.ZoneId.of("Europe/Rome"))
                                    .toLocalDateTime();
            ord.setDataOrdine(ldt);
        }

        // Totali e info spedizione
        ord.setTotaleImponibile(rs.getBigDecimal("totale_imponibile"));
        ord.setTotaleIva(rs.getBigDecimal("totale_iva"));
        ord.setTotalePagato(rs.getBigDecimal("totale_pagato"));
        ord.setCostoSpedizione(rs.getBigDecimal("costo_spedizione"));
        ord.setNome_e_cognome_destinatario(rs.getString("nome_e_cognome_destinatario"));
        ord.setIndirizzoSpedizione(rs.getString("indirizzo_spedizione"));
        ord.setCitta(rs.getString("citta"));
        ord.setCap(rs.getString("cap"));
        ord.setMetodoPagamento(rs.getString("metodo_pagamento"));
        
        // Enum di stato
        ord.setStato(StatoOrdine.valueOf(rs.getString("stato")));

        return ord;
    }

    // Metodo per recuperare tutti gli ordini, con filtri dinamici per data e cliente, ordinati per data decrescente. Utile per la dashboard admin.
    public List<Ordine> doRetrieveAllAdmin(java.time.LocalDate dal, java.time.LocalDate al, Integer idCliente) throws SQLException {
        List<Ordine> ordini = new ArrayList<>();
        
        // Costruiamo la query dinamicamente. La JOIN ci serve per avere subito i dati dell'utente.
        StringBuilder query = new StringBuilder(
            "SELECT o.*, u.nome, u.cognome, u.email " +
            "FROM ordine o " +
            "JOIN utente u ON o.id_utente = u.id_utente " +
            "WHERE 1=1 " // Trucco per accodare gli AND dinamicamente
        );

        if (dal != null) query.append("AND o.data_ordine >= ? ");
        if (al != null) query.append("AND o.data_ordine <= ? ");
        if (idCliente != null) query.append("AND o.id_utente = ? ");

        query.append("ORDER BY o.data_ordine DESC"); // I più recenti in alto

        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query.toString())) {
            
            int paramIndex = 1;
            
            if (dal != null) {
                // Convertiamo LocalDate in Timestamp (Inizio della giornata: 00:00:00)
                ps.setTimestamp(paramIndex++, java.sql.Timestamp.valueOf(dal.atStartOfDay()));
            }
            if (al != null) {
                // Convertiamo LocalDate in Timestamp (Fine della giornata: 23:59:59)
                ps.setTimestamp(paramIndex++, java.sql.Timestamp.valueOf(al.atTime(23, 59, 59)));
            }
            if (idCliente != null) {
                ps.setInt(paramIndex++, idCliente);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Ordine ord = new Ordine();
                    ord.setIdOrdine(rs.getInt("id_ordine"));
                    
                    // Popoliamo i dati dell'utente aggregati dalla JOIN
                    Utente u = new Utente();
                    u.setIdUtente(rs.getInt("id_utente"));
                    u.setNome(rs.getString("nome"));
                    u.setCognome(rs.getString("cognome"));
                    u.setEmail(rs.getString("email"));
                    ord.setUtente(u);

                    // Conversione Timestamp in LocalDateTime per data_ordine
                    java.sql.Timestamp ts = rs.getTimestamp("data_ordine");
                    if (ts != null) {
                        ord.setDataOrdine(ts.toInstant().atZone(java.time.ZoneId.of("Europe/Rome")).toLocalDateTime());
                    }

                    // Totali
                    ord.setTotaleImponibile(rs.getBigDecimal("totale_imponibile"));
                    ord.setTotaleIva(rs.getBigDecimal("totale_iva"));
                    ord.setTotalePagato(rs.getBigDecimal("totale_pagato"));
                    ord.setCostoSpedizione(rs.getBigDecimal("costo_spedizione"));
                    
                    // Dati Spedizione e Stato
                    ord.setNome_e_cognome_destinatario(rs.getString("nome_e_cognome_destinatario"));
                    ord.setIndirizzoSpedizione(rs.getString("indirizzo_spedizione"));
                    ord.setCitta(rs.getString("citta"));
                    ord.setCap(rs.getString("cap"));
                    ord.setMetodoPagamento(rs.getString("metodo_pagamento"));
                    ord.setStato(it.runitup.model.bean.StatoOrdine.valueOf(rs.getString("stato")));

                    ordini.add(ord);
                }
            }
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
        
        return ordini;
    }

    // Metodo per aggiornare solo lo stato dell'ordine
    public boolean updateStatoOrdine(int idOrdine, StatoOrdine nuovoStato) throws SQLException {
        
        String query = "UPDATE ordine SET stato = ?::stato_ordine WHERE id_ordine = ?";
        
        Connection con = null;
        PreparedStatement ps = null;
        
        try {
            con = ConnectionPool.getConnection();
            ps = con.prepareStatement(query);
            ps.setString(1, nuovoStato.name());
            ps.setInt(2, idOrdine);
            
            return ps.executeUpdate() > 0;
        } finally {
            if (ps != null) ps.close();
            if (con != null) ConnectionPool.releaseConnection(con);
        }
    }

}