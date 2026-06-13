package it.runitup.model.dao;

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
import it.runitup.util.ConnectionPool;

public class VarianteDAO {

    // Metodo per ottenere una variante specifica tramite il suo ID
    public VarianteProdotto getVarianteById(int idVariante) throws SQLException {
        VarianteProdotto variante = null;
        
        // Query con JOIN per estrarre Variante, Prodotto e Brand in un colpo solo
        String query = "SELECT vp.id_variante, vp.colore, vp.taglia, vp.prezzo, vp.quantita_magazzino, " +
                       "p.id_prodotto, p.nome AS nome_prodotto, p.descrizione, p.immagine_copertina, " +
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
                    prodotto.setImmagineCopertina(rs.getBytes("immagine_copertina"));
                    
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
}