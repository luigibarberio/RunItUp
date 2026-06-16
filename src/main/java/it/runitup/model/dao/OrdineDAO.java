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
import it.runitup.util.ConnectionPool;

import java.time.LocalDateTime;

public class OrdineDAO {

    // Questo metodo salva un ordine completo, i dettagli e aggiorna il magazzino in un'unica transazione
    public int salvaOrdineCompleto(Ordine ordine, List<VoceCarrelloDTO> voci, int idUtente) throws SQLException {
        int idOrdineGenerato = -1;

        // Query necessarie per la transazione
        String insertOrdine = "INSERT INTO ordine (id_utente, id_coupon, totale_imponibile, totale_iva, totale_pagato, costo_spedizione, nome_e_cognome_destinatario, indirizzo_spedizione, citta, cap, metodo_pagamento, stato) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS metodo_pagamento), CAST(? AS stato_ordine))";
                              
        String insertDettaglio = "INSERT INTO dettaglio_ordine (id_ordine, id_variante, nome_prodotto_storico, colore_storico, taglia_storica, quantita_acquistata, prezzo_unitario_imponibile, aliquota_iva, prezzo_unitario_con_iva) " +
                                 "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                 
        String updateMagazzino = "UPDATE variante_prodotto SET quantita_magazzino = quantita_magazzino - ? " +
                                 "WHERE id_variante = ? AND quantita_magazzino >= ?";
                                 
        String svuotaCarrello = "DELETE FROM voce_carrello WHERE id_carrello = (SELECT id_carrello FROM carrello WHERE id_utente = ?)";

                              
        Connection con = ConnectionPool.getConnection();
        try {
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
                psOrd.setBigDecimal(6, ordine.getCostoSpedizione());
                
                // Dati di spedizione e pagamento
                psOrd.setString(7, ordine.getNome_e_cognome_destinatario());
                psOrd.setString(8, ordine.getIndirizzoSpedizione());
                psOrd.setString(9, ordine.getCitta());
                psOrd.setString(10, ordine.getCap());
                psOrd.setString(11, ordine.getMetodoPagamento()); // Passiamo la stringa "CARTA", "PAYPAL", ecc. Postgres farà il cast
                psOrd.setString(12, ordine.getStato().name()); 
                
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

    // Recupera tutti gli ordini di un utente, ordinati per data decrescente. Utile per la pagina "I miei ordini"
    public List<Ordine> getOrdiniByUtente(int idUtente) throws SQLException {
        List<Ordine> lista = new ArrayList<>();
        
        // LEFT JOIN necessaria: estraiamo il coupon solo se l'utente l'ha utilizzato
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

    // Recupera un ordine specifico con i dettagli del coupon se presente. Utile per la pagina di conferma ordine o per la fattura.
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

    // Recupera tutti i dettagli di un ordine specifico, utile per la fattura o la visualizzazione completa dell'ordine
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

    // Metodo di utilità per mappare un ResultSet in un oggetto Ordine, incluso il coupon se presente
    private Ordine mappaOrdineDaResultSet(ResultSet rs) throws SQLException {
        Ordine ord = new Ordine();
        ord.setIdOrdine(rs.getInt("id_ordine"));
        
        // Impostiamo solo l'ID Utente. Il controllo d'accesso si farà in Servlet.
        it.runitup.model.bean.Utente u = new it.runitup.model.bean.Utente();
        u.setIdUtente(rs.getInt("id_utente"));
        ord.setUtente(u);

        // Se l'ordine ha un coupon associato, lo mappiamo.
        int idCoupon = rs.getInt("id_coupon");
        if (!rs.wasNull()) {
            Coupon cp = new Coupon();
            cp.setIdCoupon(idCoupon);
            cp.setCodice(rs.getString("codice"));
            cp.setValoreSconto(rs.getBigDecimal("valore_sconto"));
            cp.setTipoSconto(it.runitup.model.bean.TipoScontoCoupon.valueOf(rs.getString("tipo_sconto")));
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
        ord.setStato(it.runitup.model.bean.StatoOrdine.valueOf(rs.getString("stato")));

        return ord;
    }

}