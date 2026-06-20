package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.Date;

import it.runitup.model.bean.Coupon;
import it.runitup.model.bean.TipoScontoCoupon;
import it.runitup.util.ConnectionPool;

public class CouponDAO {

    // Metodo per ottenere un coupon attivo dal database tramite il suo codice, restituisce null se il coupon non esiste o non è valido.
    public Coupon getCouponByCodice(String codice) throws SQLException {
        Coupon coupon = null;
        
        // Query che recupera il coupon solo se è attivo (data_inizio <= oggi e (data_scadenza >= oggi o data_scadenza è NULL))
        String query = "SELECT id_coupon, codice, valore_sconto, tipo_sconto, data_inizio, data_scadenza " +
                       "FROM coupon " +
                       "WHERE codice = ? " +
                       "  AND data_inizio <= CURRENT_DATE " +
                       "  AND (data_scadenza IS NULL OR data_scadenza >= CURRENT_DATE)";


        Connection con = ConnectionPool.getConnection();
            
        try (PreparedStatement ps = con.prepareStatement(query)) {

            ps.setString(1, codice);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    coupon = new Coupon();
                    coupon.setIdCoupon(rs.getInt("id_coupon"));
                    coupon.setCodice(rs.getString("codice"));
                    coupon.setValoreSconto(rs.getBigDecimal("valore_sconto"));
                    
                    // Mappatura sicura dell'ENUM dal DB all'Enum Java
                    coupon.setTipoSconto(TipoScontoCoupon.valueOf(rs.getString("tipo_sconto")));
                    
                    // Conversione da java.sql.Date (Database) a java.time.LocalDate (Bean)
                    Date sqlDataInizio = rs.getDate("data_inizio");
                    if (sqlDataInizio != null) {
                        coupon.setDataInizio(sqlDataInizio.toLocalDate());
                    }

                    // Conversione sicura della data di scadenza, che può essere NULL
                    Date sqlDataScadenza = rs.getDate("data_scadenza");
                    if (sqlDataScadenza != null) {
                        coupon.setDataScadenza(sqlDataScadenza.toLocalDate());
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }

        // Restituisce il coupon trovato o null se non esiste o non è valido
        return coupon;
    }

    // Metodo per ottenere tutti i coupon attivi (non scaduti e già iniziati) dal database.
    public List<Coupon> getCouponAttivi() throws SQLException {
        List<Coupon> lista = new ArrayList<>();
        
        // Query che recupera tutti i coupon attivi (data_inizio <= oggi e (data_scadenza >= oggi o data_scadenza è NULL))
        String query = "SELECT id_coupon, codice, valore_sconto, tipo_sconto, data_inizio, data_scadenza " +
                       "FROM coupon " +
                       "WHERE data_inizio <= CURRENT_DATE " +
                       "  AND (data_scadenza IS NULL OR data_scadenza >= CURRENT_DATE)";
                       
        Connection con = ConnectionPool.getConnection();
        
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Coupon coupon = new Coupon();
                coupon.setIdCoupon(rs.getInt("id_coupon"));
                coupon.setCodice(rs.getString("codice"));
                coupon.setValoreSconto(rs.getBigDecimal("valore_sconto"));
                
                // Mappatura dell'enum
                coupon.setTipoSconto(TipoScontoCoupon.valueOf(rs.getString("tipo_sconto")));
                
                // Conversione sicura java.sql.Date -> java.time.LocalDate
                java.sql.Date sqlDataInizio = rs.getDate("data_inizio");
                if (sqlDataInizio != null) {
                    coupon.setDataInizio(sqlDataInizio.toLocalDate());
                }
                
                // Conversione sicura della data di scadenza, che può essere NULL
                java.sql.Date sqlDataScadenza = rs.getDate("data_scadenza");
                if (sqlDataScadenza != null) {
                    coupon.setDataScadenza(sqlDataScadenza.toLocalDate());
                }
                
                lista.add(coupon);
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return lista;
    }

}