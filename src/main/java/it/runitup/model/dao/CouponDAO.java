package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

import it.runitup.model.bean.Coupon;
import it.runitup.model.bean.TipoScontoCoupon;
import it.runitup.util.ConnectionPool;

public class CouponDAO {

    /**
     * Recupera un coupon dal database in base al codice alfanumerico inserito dall'utente.
     * La particolarità di questa query è che scarta in automatico a livello di RDBMS 
     * i coupon non ancora iniziati o già scaduti.
     * * @param codice Il codice promozionale (es. "RUN10")
     * @return L'oggetto Coupon precompilato se valido, null se inesistente o scaduto.
     */
    public Coupon getCouponByCodice(String codice) throws SQLException {
        Coupon coupon = null;
        
        // La funzione CURRENT_DATE di PostgreSQL restituisce la data odierna.
        // Gestiamo in sicurezza il fatto che data_scadenza possa essere NULL (coupon perenne).
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

                    Date sqlDataScadenza = rs.getDate("data_scadenza");
                    if (sqlDataScadenza != null) {
                        coupon.setDataScadenza(sqlDataScadenza.toLocalDate());
                    }
                }
            }
        } finally {
            if (con != null) {
                con.close();
            }
        }
        return coupon;
    }
}