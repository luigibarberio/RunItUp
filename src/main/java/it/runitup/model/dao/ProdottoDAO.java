package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.runitup.model.bean.TipoTerreno;
import it.runitup.model.bean.TipoAppoggio;
import it.runitup.model.bean.TipoPasso;
import it.runitup.model.bean.Brand;
import it.runitup.model.bean.GenereScarpa;
import it.runitup.model.bean.Prodotto;
import it.runitup.model.dto.ProdottoDTO;
import it.runitup.model.filter.FiltroCatalogo;
import it.runitup.util.ConnectionPool;

public class ProdottoDAO {

    public List<ProdottoDTO> doRetrieveAllFiltered(FiltroCatalogo filtri) throws SQLException {
        List<ProdottoDTO> prodotti = new ArrayList<>();
        List<Object> parametri = new ArrayList<>();

        if (filtri == null) {
            filtri = new FiltroCatalogo(); // Fallback sui default se l'oggetto è null
        }

        // 1. QUERY BASE CON CALCOLO DINAMICO DELLO SCONTO (PostgreSQL)
        // Usiamo un LEFT JOIN con la tabella promozione basandoci sulla data corrente (CURRENT_TIMESTAMP)
        StringBuilder query = new StringBuilder(
            "SELECT p.*, b.nome_brand, " +
            // Calcolo del prezzo scontato
            "MIN(CASE " +
            "   WHEN pr.id_promozione IS NOT NULL " +
            "   THEN v.prezzo * (1 - (pr.percentuale / 100.0)) " +
            "   ELSE v.prezzo " +
            "END) AS prezzo_effettivo_minimo, " +
            // Prezzo originale base
            "MIN(v.prezzo) AS prezzo_listino_minimo, " +
            // Flag booleano: se c'è una promo unita, allora è in offerta
            "BOOL_OR(CASE " +
            "   WHEN pr.id_promozione IS NOT NULL THEN true ELSE false " +
            "END) AS ha_promozione_attiva ," +
            "STRING_AGG(DISTINCT v.colore, ',' ORDER BY v.colore ASC) AS colori_disponibili, " +
            "STRING_AGG(DISTINCT v.taglia, ',' ORDER BY v.taglia ASC) AS taglie_disponibili " +
            "FROM prodotto p " +
            "JOIN brand b ON p.id_brand = b.id_brand " +
            "JOIN variante_prodotto v ON p.id_prodotto = v.id_prodotto " +
            // 1. Passiamo per la tabella ponte
            "LEFT JOIN variante_promozione vp ON v.id_variante = vp.id_variante " +
            // 2. Uniamo la promozione SOLO SE è attualmente valida (Manteniamo l'alias 'pr')
            "LEFT JOIN promozione pr ON vp.id_promozione = pr.id_promozione " +
            "   AND CURRENT_TIMESTAMP BETWEEN pr.data_inizio AND pr.data_fine " +
            "WHERE v.quantita_magazzino > 0 " // Escludiamo varianti esaurite
        );

        // 2. COSTRUZIONE DINAMICA DELLE CLAUSOLE WHERE
        // Filtro Brand (Multi-selezione)
        if (filtri.getBrandIds() != null && !filtri.getBrandIds().isEmpty()) {
            query.append(" AND p.id_brand IN (?");
            parametri.add(filtri.getBrandIds().get(0));
            for (int i = 1; i < filtri.getBrandIds().size(); i++) {
                query.append(",?");
                parametri.add(filtri.getBrandIds().get(i));
            }
            query.append(")");
        }

        // Filtro Genere (Multi-selezione Enum)
        if (filtri.getGeneri() != null && !filtri.getGeneri().isEmpty()) {
            query.append(" AND p.genere IN (?::genere_scarpa");
            parametri.add(filtri.getGeneri().get(0).toString());
            for (int i = 1; i < filtri.getGeneri().size(); i++) {
                query.append(",?::genere_scarpa");
                parametri.add(filtri.getGeneri().get(i).toString());
            }
            query.append(")");
        }

        // Filtro Booleano: In Evidenza
        if (filtri.getInEvidenza() != null && filtri.getInEvidenza()) {
            query.append(" AND p.in_evidenza = ?");
            parametri.add(true);
        }

        // Filtro Booleano: Nuovi Arrivi (Prodotti inseriti negli ultimi 14 giorni)
        if (filtri.getNuoviArrivi() != null && filtri.getNuoviArrivi()) {
            query.append(" AND p.data_inserimento >= CURRENT_TIMESTAMP - INTERVAL '14 days'");
        }

        // Filtro Booleano: Taglia
        if (filtri.getTaglie() != null && !filtri.getTaglie().isEmpty()) {
            query.append(" AND v.taglia IN (?");
            parametri.add(filtri.getTaglie().get(0));
            for (int i = 1; i < filtri.getTaglie().size(); i++) {
                query.append(",?");
                parametri.add(filtri.getTaglie().get(i));
            }
            query.append(")");
        }

        // Filtro Booleano: Colore
        if (filtri.getColori() != null && !filtri.getColori().isEmpty()) {
            query.append(" AND v.colore IN (?");
            parametri.add(filtri.getColori().get(0));
            for (int i = 1; i < filtri.getColori().size(); i++) {
                query.append(",?");
                parametri.add(filtri.getColori().get(i));
            }
            query.append(")");
        }

        // Filtro Testuale Avanzato: Tipo Terreno
        if (filtri.getTipiTerreno() != null && !filtri.getTipiTerreno().isEmpty()) {
            query.append(" AND p.tipo_terreno IN (?::tipo_terreno");
            parametri.add(filtri.getTipiTerreno().get(0).toString());
            for (int i = 1; i < filtri.getTipiTerreno().size(); i++) {
                query.append(",?::tipo_terreno");
                parametri.add(filtri.getTipiTerreno().get(i).toString());
            }
            query.append(")");
        }

        // Filtro Testuale Avanzato: Tipo Appoggio
        if (filtri.getTipiAppoggio() != null && !filtri.getTipiAppoggio().isEmpty()) {
            query.append(" AND p.tipo_appoggio IN (?::tipo_appoggio");
            parametri.add(filtri.getTipiAppoggio().get(0).toString());
            for (int i = 1; i < filtri.getTipiAppoggio().size(); i++) {
                query.append(",?::tipo_appoggio");
                parametri.add(filtri.getTipiAppoggio().get(i).toString());
            }
            query.append(")");
        }

        // Filtro Testuale Avanzato: Tipo Passo
        if (filtri.getTipiPasso() != null && !filtri.getTipiPasso().isEmpty()) {
            query.append(" AND p.tipo_passo IN (?::tipo_passo");
            parametri.add(filtri.getTipiPasso().get(0).toString());
            for (int i = 1; i < filtri.getTipiPasso().size(); i++) {
                query.append(",?::tipo_passo");
                parametri.add(filtri.getTipiPasso().get(i).toString());
            }
            query.append(")");
        }

        // 3. CHIUSURA FILTRI E RAGGRUPPAMENTO (PostgreSQL Strict Rules)
        query.append(" GROUP BY p.id_prodotto, b.id_brand, b.nome_brand");

        // 4. APPLICAZIONE FILTRO AD AVANZATO SUL PREZZO CALCOLATO (Tramite CLAUSOLA HAVING)
        // Essendo prezzi aggregati e calcolati con formule (sconti), non possiamo usare WHERE, serve HAVING.
        boolean hasHaving = false;
        if (filtri.getPrezzoMinimo() != null) {
            query.append(" HAVING MIN(CASE WHEN pr.id_promozione IS NOT NULL AND CURRENT_TIMESTAMP BETWEEN pr.data_inizio AND pr.data_fine THEN v.prezzo * (1 - (pr.percentuale / 100.0)) ELSE v.prezzo END) >= ?");
            parametri.add(filtri.getPrezzoMinimo());
            hasHaving = true;
        }
        if (filtri.getPrezzoMassimo() != null) {
            if (hasHaving) {
                query.append(" AND");
            } else {
                query.append(" HAVING");
                hasHaving = true;
            }
            query.append(" MIN(CASE WHEN pr.id_promozione IS NOT NULL AND CURRENT_TIMESTAMP BETWEEN pr.data_inizio AND pr.data_fine THEN v.prezzo * (1 - (pr.percentuale / 100.0)) ELSE v.prezzo END) <= ?");
            parametri.add(filtri.getPrezzoMassimo());
        }

        // Filtro Booleano: In Offerta (Richiede che almeno una variante abbia una promo attiva)
        if (filtri.getInOfferta() != null && filtri.getInOfferta()) {
            if (hasHaving) {
                query.append(" AND");
            } else {
                query.append(" HAVING");
            }
            query.append(" BOOL_OR(CASE WHEN pr.id_promozione IS NOT NULL AND CURRENT_TIMESTAMP BETWEEN pr.data_inizio AND pr.data_fine THEN true ELSE false END) = true");
        }

        // 5. ORDINAMENTO (Controllato per impedire SQL Injection strutturali)
        String order = filtri.getOrdinamento();
        if (order != null) {
            switch (order) {
                case "prezzo_asc": query.append(" ORDER BY prezzo_effettivo_minimo ASC"); break;
                case "prezzo_desc": query.append(" ORDER BY prezzo_effettivo_minimo DESC"); break;
                case "recenti": query.append(" ORDER BY p.data_inserimento DESC"); break;
                case "vendite": query.append(" ORDER BY p.totale_vendite DESC"); break;
                default: query.append(" ORDER BY p.in_evidenza DESC, p.totale_vendite DESC"); break;
            }
        } else {
            query.append(" ORDER BY p.in_evidenza DESC, p.totale_vendite DESC");
        }

        // 6. PAGINAZIONE (LIMIT E OFFSET)
        query.append(" LIMIT ? OFFSET ?");
        parametri.add(filtri.getLimit());
        parametri.add(filtri.getOffset());

        // 7. APERTURA CONNESSIONE ED ESECUZIONE
        Connection con = ConnectionPool.getConnection();
        try {

            try (PreparedStatement ps = con.prepareStatement(query.toString())) {

                // Riversiamo i parametri tipizzati in RAM dentro il PreparedStatement
                for (int i = 0; i < parametri.size(); i++) {
                    ps.setObject(i + 1, parametri.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        // MAPPING DEL BEAN PRODOTTO STANDARD
                        Prodotto p = new Prodotto();
                        p.setIdProdotto(rs.getInt("id_prodotto"));
                        p.setNome(rs.getString("nome"));
                        p.setDescrizione(rs.getString("descrizione"));
                        p.setImmagineCopertina(rs.getBytes("immagine_copertina"));
                        
                        if (rs.getString("genere") != null) {
                            p.setGenere(GenereScarpa.valueOf(rs.getString("genere")));
                        }
                        
                        if (rs.getString("tipo_terreno") != null) {
                            p.setTipoTerreno(TipoTerreno.valueOf(rs.getString("tipo_terreno")));
                        }

                        if (rs.getString("tipo_appoggio") != null) {
                            p.setTipoAppoggio(TipoAppoggio.valueOf(rs.getString("tipo_appoggio")));
                        }

                        if (rs.getString("tipo_passo") != null) {
                            p.setTipoPasso(TipoPasso.valueOf(rs.getString("tipo_passo")));
                        }

                        p.setDataInserimento(rs.getTimestamp("data_inserimento"));
                        p.setInEvidenza(rs.getBoolean("in_evidenza"));
                        p.setTotaleVendite(rs.getInt("totale_vendite"));

                        // MAPPING DEL BRAND (Grazie alla INNER JOIN b.*)
                        Brand b = new Brand();
                        b.setIdBrand(rs.getInt("id_brand"));
                        b.setNomeBrand(rs.getString("nome_brand"));
                        p.setBrand(b);

                        // IMPACHETTAMENTO DEL DTO PER IL TRASPORTO VERSO LA VIEW
                        ProdottoDTO dto = new ProdottoDTO();
                        dto.setProdotto(p);
                        dto.setPrezzoMinimo(rs.getBigDecimal("prezzo_effettivo_minimo"));
                        dto.setPrezzoOriginale(rs.getBigDecimal("prezzo_listino_minimo"));
                        dto.setScontato(rs.getBoolean("ha_promozione_attiva"));

                        // Calcolo booleano dinamico della novità (ultimi 14 giorni)
                        long quattordiciGiorniInMs = 14L * 24 * 60 * 60 * 1000;
                        long oraInMs = System.currentTimeMillis();
                        if (p.getDataInserimento() != null) {
                            long dataInserimentoInMs = p.getDataInserimento().getTime();
                            dto.setNuovo((oraInMs - dataInserimentoInMs) <= quattordiciGiorniInMs);
                        } else {
                            dto.setNuovo(false);
                        }

                        // Mapping dei colori e taglie disponibili (Stringhe aggregate da SQL)
                        String coloriStr = rs.getString("colori_disponibili");
                        if (coloriStr != null && !coloriStr.isEmpty()) {
                            dto.setColoriDisponibili(coloriStr.contains(",") ? List.of(coloriStr.split(",")) : List.of(coloriStr));
                        } else {
                            dto.setColoriDisponibili(new ArrayList<>());
                        }

                        String taglieStr = rs.getString("taglie_disponibili");
                        if (taglieStr != null && !taglieStr.isEmpty()) {
                            dto.setTaglieDisponibili(taglieStr.contains(",") ? List.of(taglieStr.split(",")) : List.of(taglieStr));
                        } else {
                            dto.setTaglieDisponibili(new ArrayList<>());
                        }

                        prodotti.add(dto);
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return prodotti;
    }
}