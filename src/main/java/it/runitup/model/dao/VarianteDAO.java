package it.runitup.model.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import it.runitup.model.bean.Brand;
import it.runitup.model.bean.GenereScarpa;
import it.runitup.model.bean.Prodotto;
import it.runitup.model.bean.TipoAppoggio;
import it.runitup.model.bean.TipoPasso;
import it.runitup.model.bean.TipoTerreno;
import it.runitup.model.bean.VarianteProdotto;
import it.runitup.model.dto.VarianteDTO;
import it.runitup.util.ConnectionPool;

public class VarianteDAO {

    // Metodo per ottenere una variante specifica tramite il suo ID
    public VarianteProdotto getVarianteById(int idVariante) throws SQLException {
        VarianteProdotto variante = null;
        
        // Query con JOIN per estrarre Variante, Prodotto e Brand in un colpo solo
        String query = "SELECT vp.id_variante, vp.colore, vp.taglia, vp.prezzo, vp.quantita_magazzino, " +
                       "p.id_prodotto, p.nome AS nome_prodotto, p.descrizione, " +
                       "p.genere, p.tipo_terreno, p.tipo_appoggio, p.tipo_passo, " +
                       "p.data_inserimento, p.in_evidenza, p.totale_vendite, " +
                       "b.id_brand, b.nome_brand " +
                       "FROM variante_prodotto vp " +
                       "JOIN prodotto p ON vp.id_prodotto = p.id_prodotto " +
                       "JOIN brand b ON p.id_brand = b.id_brand " +
                       "WHERE vp.id_variante = ?";

        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idVariante);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // 1. Mappatura Brand
                    Brand brand = new Brand();
                    brand.setIdBrand(rs.getInt("id_brand"));
                    brand.setNomeBrand(rs.getString("nome_brand"));

                    // 2. Mappatura Prodotto
                    Prodotto prodotto = new Prodotto();
                    prodotto.setIdProdotto(rs.getInt("id_prodotto"));
                    prodotto.setNome(rs.getString("nome_prodotto"));
                    prodotto.setDescrizione(rs.getString("descrizione"));
                    
                    // Gestione Enums (con controlli di sicurezza)
                    if (rs.getString("genere") != null)
                        prodotto.setGenere(GenereScarpa.valueOf(rs.getString("genere")));
                    if (rs.getString("tipo_terreno") != null)
                        prodotto.setTipoTerreno(TipoTerreno.valueOf(rs.getString("tipo_terreno")));
                    if (rs.getString("tipo_appoggio") != null)
                        prodotto.setTipoAppoggio(TipoAppoggio.valueOf(rs.getString("tipo_appoggio")));
                    if (rs.getString("tipo_passo") != null)
                        prodotto.setTipoPasso(TipoPasso.valueOf(rs.getString("tipo_passo")));
                    
                    prodotto.setDataInserimento(rs.getTimestamp("data_inserimento"));
                    prodotto.setInEvidenza(rs.getBoolean("in_evidenza"));
                    prodotto.setTotaleVendite(rs.getInt("totale_vendite"));
                    prodotto.setBrand(brand);

                    // 3. Mappatura VarianteProdotto
                    variante = new VarianteProdotto();
                    variante.setIdVariante(rs.getInt("id_variante"));
                    variante.setColore(rs.getString("colore"));
                    variante.setTaglia(rs.getString("taglia"));
                    variante.setPrezzo(rs.getBigDecimal("prezzo"));
                    variante.setQuantitaMagazzino(rs.getInt("quantita_magazzino"));
                    variante.setProdotto(prodotto);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        return variante;
    }

    // Recupera una variante specifica con i dettagli del prodotto e del brand, calcolando il prezzo scontato se è attiva una promozione
    public VarianteDTO getVarianteConPromozione(VarianteProdotto variante) throws SQLException {
        VarianteDTO vDto = new VarianteDTO();
        vDto.setVariante(variante);
        vDto.setInPromozione(false);

        if (variante == null) return vDto;

        // Query perfettamente allineata alla tabella ponte variante_promozione
        // Seleziona la percentuale controllando che il timestamp attuale sia nel range di validità
        String query = "SELECT pr.percentuale " +
                       "FROM variante_promozione vpr " +
                       "JOIN promozione pr ON vpr.id_promozione = pr.id_promozione " +
                       "WHERE vpr.id_variante = ? " +
                       "  AND CURRENT_TIMESTAMP BETWEEN pr.data_inizio AND pr.data_fine";
        
                       
        Connection con = ConnectionPool.getConnection();
        
        try (PreparedStatement ps = con.prepareStatement(query)) {
            
            ps.setInt(1, variante.getIdVariante());
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal percentuale = rs.getBigDecimal("percentuale");
                    
                    if (percentuale != null && percentuale.compareTo(BigDecimal.ZERO) > 0) {
                        vDto.setInPromozione(true);
                        
                        // Calcolo prezzo scontato: Prezzo - (Prezzo * percentuale / 100)
                        BigDecimal cento = new BigDecimal("100");
                        BigDecimal sconto = variante.getPrezzo().multiply(percentuale.divide(cento, 4, java.math.RoundingMode.HALF_UP));
                        vDto.setPrezzoScontato(variante.getPrezzo().subtract(sconto));
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return vDto;
    }


    // =========================================================================
    // METODI CRUD PER L'AREA ADMIN (AJAX/FETCH)
    // =========================================================================

    /**
     * Recupera tutte le varianti di uno specifico prodotto (per la tabella Admin).
     */
    public java.util.List<VarianteProdotto> getVariantiByProdotto(int idProdotto) throws SQLException {
        java.util.List<VarianteProdotto> list = new java.util.ArrayList<>();
        String query = "SELECT id_variante, id_prodotto, colore, taglia, prezzo, quantita_magazzino " +
                       "FROM variante_prodotto WHERE id_prodotto = ? ORDER BY colore, taglia";
                       
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VarianteProdotto v = new VarianteProdotto();
                    v.setIdVariante(rs.getInt("id_variante"));
                    
                    // Creiamo un Prodotto "fittizio" con solo l'ID per mantenere il riferimento
                    Prodotto p = new Prodotto();
                    p.setIdProdotto(rs.getInt("id_prodotto"));
                    v.setProdotto(p);
                    
                    v.setColore(rs.getString("colore"));
                    v.setTaglia(rs.getString("taglia"));
                    v.setPrezzo(rs.getBigDecimal("prezzo"));
                    v.setQuantitaMagazzino(rs.getInt("quantita_magazzino"));
                    
                    list.add(v);
                }
            }
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
        return list;
    }

    /**
     * Inserisce una nuova variante e restituisce l'ID autogenerato.
     */
    public int insertVariante(VarianteProdotto v) throws SQLException {
        String query = "INSERT INTO variante_prodotto (id_prodotto, colore, taglia, prezzo, quantita_magazzino) VALUES (?, ?, ?, ?, ?)";
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, v.getProdotto().getIdProdotto());
            ps.setString(2, v.getColore());
            ps.setString(3, v.getTaglia());
            ps.setBigDecimal(4, v.getPrezzo());
            ps.setInt(5, v.getQuantitaMagazzino());
            
            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
            return -1;
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
    }

    /**
     * Aggiorna una variante esistente (Modifica Inline).
     */
    public boolean updateVariante(VarianteProdotto v) throws SQLException {
        String query = "UPDATE variante_prodotto SET colore = ?, taglia = ?, prezzo = ?, quantita_magazzino = ? WHERE id_variante = ?";
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, v.getColore());
            ps.setString(2, v.getTaglia());
            ps.setBigDecimal(3, v.getPrezzo());
            ps.setInt(4, v.getQuantitaMagazzino());
            ps.setInt(5, v.getIdVariante());
            
            return ps.executeUpdate() > 0;
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
    }

    /**
     * Elimina fisicamente una variante.
     */
    public boolean deleteVariante(int idVariante) throws SQLException {
        String query = "DELETE FROM variante_prodotto WHERE id_variante = ?";
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idVariante);
            return ps.executeUpdate() > 0;
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
    }
}