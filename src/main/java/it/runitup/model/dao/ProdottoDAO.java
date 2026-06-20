package it.runitup.model.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.runitup.model.bean.TipoTerreno;
import it.runitup.model.bean.VarianteProdotto;
import it.runitup.model.bean.TipoAppoggio;
import it.runitup.model.bean.TipoPasso;
import it.runitup.model.bean.Brand;
import it.runitup.model.bean.GenereScarpa;
import it.runitup.model.bean.Prodotto;
import it.runitup.model.dto.ProdottoDTO;
import it.runitup.model.dto.VarianteDTO;
import it.runitup.model.filter.FiltroCatalogo;
import it.runitup.util.ConnectionPool;

public class ProdottoDAO {

    public List<ProdottoDTO> doRetrieveAllFiltered(FiltroCatalogo filtri) throws SQLException {
        List<ProdottoDTO> prodotti = new ArrayList<>();
        List<Object> parametri = new ArrayList<>();

        if (filtri == null) {
            filtri = new FiltroCatalogo(); // Fallback sui default se l'oggetto è null
        }

        // Costruzione dinamica della query SQL con JOIN per ottenere tutte le informazioni necessarie in un'unica chiamata al DB, inclusi i calcoli per il prezzo scontato e la variabilità dei prezzi tra le varianti
        StringBuilder query = new StringBuilder(
            "SELECT p.*, b.nome_brand, " +
            // Calcolo del prezzo scontato
            "MIN(CASE " +
            "   WHEN pr.id_promozione IS NOT NULL " +
            "   THEN v.prezzo * (1 - (pr.percentuale / 100.0)) " +
            "   ELSE v.prezzo " +
            "END) AS prezzo_effettivo_minimo, " +
            // Calcolo del prezzo scontato MASSIMO per valutare la variabilità
            "MAX(CASE " +
            "   WHEN pr.id_promozione IS NOT NULL " +
            "   THEN v.prezzo * (1 - (pr.percentuale / 100.0)) " +
            "   ELSE v.prezzo " +
            "END) AS prezzo_effettivo_massimo, " +
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

        // Applicazione dinamica dei filtri in base ai criteri specificati nell'oggetto FiltroCatalogo, con gestione sicura dei parametri per prevenire SQL Injection e supporto per filtri complessi come multi-selezione e ricerche testuali avanzate
        if (filtri.getRicercaTestuale() != null && !filtri.getRicercaTestuale().isEmpty()) {
            // Usiamo ILIKE (PostgreSQL) per una ricerca case-insensitive
            query.append(" AND (p.nome ILIKE ? OR b.nome_brand ILIKE ?)");
            // Aggiungiamo i % per far sì che trovi la parola in qualsiasi punto della stringa
            String searchPattern = "%" + filtri.getRicercaTestuale() + "%";
            parametri.add(searchPattern);
            parametri.add(searchPattern);
        }

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

        // Chiusura della query con GROUP BY per aggregare correttamente i risultati dopo i JOIN e i calcoli, e per permettere l'uso di funzioni di aggregazione come MIN, MAX e BOOL_OR sui prezzi e sulle promozioni, mantenendo comunque la possibilità di filtrare sui prezzi calcolati tramite HAVING
        query.append(" GROUP BY p.id_prodotto, b.id_brand, b.nome_brand");

        // Filtro sui prezzi calcolati (prezzo scontato minimo) usando HAVING, che permette di filtrare sui risultati aggregati dopo il GROUP BY. Gestiamo sia il prezzo minimo che il prezzo massimo, e anche la possibilità di filtrare solo i prodotti in offerta
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

        // Ordinamento dinamico in base alla scelta dell'utente, con valori predefiniti per garantire un ordinamento coerente anche se l'utente non specifica nulla
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

        // Paginazione sicura con LIMIT e OFFSET, sempre alla fine della query
        query.append(" LIMIT ? OFFSET ?");
        parametri.add(filtri.getLimit());
        parametri.add(filtri.getOffset());

        // Esecuzione della query con PreparedStatement, passando tutti i parametri ottenuti dall'oggetto FiltroCatalogo
        Connection con = ConnectionPool.getConnection();
        try {

            try (PreparedStatement ps = con.prepareStatement(query.toString())) {

                for (int i = 0; i < parametri.size(); i++) {
                    ps.setObject(i + 1, parametri.get(i));
                }

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        // Mappatura del prodotto con tutte le informazioni necessarie per la visualizzazione in catalogo
                        Prodotto p = new Prodotto();
                        p.setIdProdotto(rs.getInt("id_prodotto"));
                        p.setNome(rs.getString("nome"));
                        p.setDescrizione(rs.getString("descrizione"));
                        
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

                        // Mapping dell'oggetto Brand
                        Brand b = new Brand();
                        b.setIdBrand(rs.getInt("id_brand"));
                        b.setNomeBrand(rs.getString("nome_brand"));
                        p.setBrand(b);

                        // Creazione del DTO per il prodotto, necessario per includere i campi calcolati che non fanno parte della tabella prodotto
                        ProdottoDTO dto = new ProdottoDTO();
                        dto.setProdotto(p);

                        BigDecimal prezzoMinimo = rs.getBigDecimal("prezzo_effettivo_minimo");
                        BigDecimal prezzoMassimo = rs.getBigDecimal("prezzo_effettivo_massimo");
                        
                        dto.setPrezzoMinimo(prezzoMinimo);
                        dto.setPrezzoOriginale(rs.getBigDecimal("prezzo_listino_minimo"));
                        dto.setScontato(rs.getBoolean("ha_promozione_attiva"));

                        // Calcolo della variabilità dei prezzi tra le varianti
                        if (prezzoMinimo != null && prezzoMassimo != null) {
                            // Se il prezzo massimo è maggiore del prezzo minimo, allora ci sono varianti con prezzi diversi, quindi ha prezzi variabili
                            dto.setHaPrezziVariabili(prezzoMassimo.compareTo(prezzoMinimo) > 0);
                        } else {
                            dto.setHaPrezziVariabili(false);
                        }

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

                        // Aggiunta del DTO alla lista dei risultati
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

    // Metodo per recuperare un prodotto specifico con tutte le sue informazioni
    public Prodotto doRetrieveById(int idProdotto) throws SQLException {
        Prodotto prodotto = null;
        
        // Join con brand per recuperare l'oggetto Brand associato
        String query = "SELECT p.*, b.nome_brand " +
                       "FROM prodotto p " +
                       "JOIN brand b ON p.id_brand = b.id_brand " +
                       "WHERE p.id_prodotto = ?";

        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idProdotto);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    prodotto = new Prodotto();
                    prodotto.setIdProdotto(rs.getInt("id_prodotto"));
                    prodotto.setNome(rs.getString("nome"));
                    prodotto.setDescrizione(rs.getString("descrizione"));
                    
                    // Mappatura degli enum, con controllo per evitare NullPointerException se il campo è null
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

                    // Mapping dell'oggetto Brand
                    Brand brand = new Brand();
                    brand.setIdBrand(rs.getInt("id_brand"));
                    brand.setNomeBrand(rs.getString("nome_brand"));
                    prodotto.setBrand(brand);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }

        return prodotto;
    }

    // Metodo per recuperare tutte le varianti di un prodotto specifico
    public List<VarianteDTO> getVariantiDTOByProdottoId(int idProdotto) throws SQLException {
        List<VarianteDTO> listaVarianti = new ArrayList<>();
        
        String query = "SELECT v.*, pr.percentuale " +
                    "FROM variante_prodotto v " +
                    "LEFT JOIN variante_promozione vp ON v.id_variante = vp.id_variante " +
                    "LEFT JOIN promozione pr ON vp.id_promozione = pr.id_promozione " +
                    "    AND CURRENT_TIMESTAMP BETWEEN pr.data_inizio AND pr.data_fine " +
                    "WHERE v.id_prodotto = ? AND v.quantita_magazzino > 0";

        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    VarianteProdotto v = new VarianteProdotto();
                    v.setIdVariante(rs.getInt("id_variante"));
                    v.setColore(rs.getString("colore"));
                    v.setTaglia(rs.getString("taglia"));
                    v.setPrezzo(rs.getBigDecimal("prezzo"));
                    v.setQuantitaMagazzino(rs.getInt("quantita_magazzino"));

                    VarianteDTO vDto = new VarianteDTO();
                    vDto.setVariante(v);
                    
                    BigDecimal percentuale = rs.getBigDecimal("percentuale");
                    if (percentuale != null) {
                        vDto.setInPromozione(true);
                        // Calcolo prezzo scontato: Prezzo * (1 - percentuale/100)
                        BigDecimal sconto = v.getPrezzo().multiply(percentuale.divide(new BigDecimal("100")));
                        vDto.setPrezzoScontato(v.getPrezzo().subtract(sconto));
                    } else {
                        vDto.setInPromozione(false);
                    }
                    
                    listaVarianti.add(vDto);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return listaVarianti;
    }

    // Metodo per recuperare una lista veloce di prodotti per i suggerimenti della barra di ricerca AJAX
    public List<Prodotto> doRetrieveSuggerimenti(String queryTestuale) throws SQLException {
        List<Prodotto> suggerimenti = new ArrayList<>();
        
        // Estraiamo solo i dati strettamente necessari per il menu a tendina
        String query = "SELECT p.id_prodotto, p.nome, b.nome_brand " +
                   "FROM prodotto p " +
                   "JOIN brand b ON p.id_brand = b.id_brand " +
                   "WHERE CONCAT(b.nome_brand, ' ', p.nome) ILIKE ? " +
                   "OR CONCAT(p.nome, ' ', b.nome_brand) ILIKE ? " +
                   "LIMIT 5";

        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            // Aggiungiamo i % per cercare la sottostringa in qualsiasi posizione
            String pattern = "%" + queryTestuale + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Prodotto p = new Prodotto();
                    p.setIdProdotto(rs.getInt("id_prodotto"));
                    p.setNome(rs.getString("nome"));
                    
                    Brand b = new Brand();
                    b.setNomeBrand(rs.getString("nome_brand"));
                    p.setBrand(b);
                    
                    suggerimenti.add(p);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        return suggerimenti;
    }

    // Metodo per recuperare un prodotto completo con tutte le sue informazioni, utilizzato principalmente per la pagina di dettaglio prodotto dell'area admin
    public Prodotto getProdottoById(int idProdotto) throws SQLException {
        Prodotto p = null;
        String query = "SELECT p.*, b.nome_brand FROM prodotto p JOIN brand b ON p.id_brand = b.id_brand WHERE p.id_prodotto = ?";
        
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idProdotto);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    p = new Prodotto();
                    p.setIdProdotto(rs.getInt("id_prodotto"));
                    p.setNome(rs.getString("nome"));
                    p.setDescrizione(rs.getString("descrizione"));
                    
                    Brand b = new Brand();
                    b.setIdBrand(rs.getInt("id_brand"));
                    b.setNomeBrand(rs.getString("nome_brand"));
                    p.setBrand(b);
                    
                    p.setGenere(GenereScarpa.valueOf(rs.getString("genere")));
                    p.setTipoTerreno(TipoTerreno.valueOf(rs.getString("tipo_terreno")));
                    p.setTipoAppoggio(TipoAppoggio.valueOf(rs.getString("tipo_appoggio")));
                    p.setTipoPasso(TipoPasso.valueOf(rs.getString("tipo_passo")));
                    p.setInEvidenza(rs.getBoolean("in_evidenza"));
                    p.setDataInserimento(rs.getTimestamp("data_inserimento"));
                    p.setTotaleVendite(rs.getInt("totale_vendite"));
                }
            }
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
        return p;
    }

    // Metodo per inserire un nuovo prodotto nel database, restituendo l'ID generato del nuovo prodotto
    public int insertProdotto(Prodotto p) throws SQLException {
        String query = "INSERT INTO prodotto (nome, descrizione, id_brand, genere, tipo_terreno, tipo_appoggio, tipo_passo, in_evidenza) " +
                       "VALUES (?, ?, ?, CAST(? AS genere_scarpa), CAST(? AS tipo_terreno), CAST(? AS tipo_appoggio), CAST(? AS tipo_passo), ?)";
        
        Connection con = ConnectionPool.getConnection();
        // Utilizzo di RETURN_GENERATED_KEYS per ottenere l'ID del prodotto appena inserito, che è necessario per poter poi inserire le varianti associate a quel prodotto
        try (PreparedStatement ps = con.prepareStatement(query, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescrizione());
            ps.setInt(3, p.getBrand().getIdBrand());
            ps.setString(4, p.getGenere().name());
            ps.setString(5, p.getTipoTerreno().name());
            ps.setString(6, p.getTipoAppoggio().name());
            ps.setString(7, p.getTipoPasso().name());
            ps.setBoolean(8, p.isInEvidenza());

            int affectedRows = ps.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1); // Ritorna il nuovo ID
                    }
                }
            }
            return -1;
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
    }

    // Metodo per aggiornare un prodotto esistente nel database, restituendo true se l'aggiornamento è avvenuto con successo
    public boolean updateProdotto(Prodotto p) throws SQLException {
        String query = "UPDATE prodotto SET nome = ?, descrizione = ?, id_brand = ?, " +
                       "genere = CAST(? AS genere_scarpa), tipo_terreno = CAST(? AS tipo_terreno), " +
                       "tipo_appoggio = CAST(? AS tipo_appoggio), tipo_passo = CAST(? AS tipo_passo), " +
                       "in_evidenza = ? WHERE id_prodotto = ?";
                       
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, p.getNome());
            ps.setString(2, p.getDescrizione());
            ps.setInt(3, p.getBrand().getIdBrand());
            ps.setString(4, p.getGenere().name());
            ps.setString(5, p.getTipoTerreno().name());
            ps.setString(6, p.getTipoAppoggio().name());
            ps.setString(7, p.getTipoPasso().name());
            ps.setBoolean(8, p.isInEvidenza());
            ps.setInt(9, p.getIdProdotto());

            return ps.executeUpdate() > 0;
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
    }

    // Metodo per eliminare un prodotto dal database, restituendo true se l'eliminazione è avvenuta con successo
    public boolean deleteProdotto(int idProdotto) throws SQLException {
        String query = "DELETE FROM prodotto WHERE id_prodotto = ?";
        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, idProdotto);
            return ps.executeUpdate() > 0;
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }
    }

    // Metodo per recuperare tutti i prodotti (utile per l'area admin)
    public List<ProdottoDTO> doRetrieveAllAdmin() throws SQLException {
        List<ProdottoDTO> lista = new ArrayList<>();

        // LEFT JOIN per includere anche i prodotti senza varianti
        String query = 
            "SELECT p.*, b.nome_brand, " +
            "       MIN(v.prezzo) AS prezzo_minimo " +
            "FROM prodotto p " +
            "JOIN brand b ON p.id_brand = b.id_brand " +
            "LEFT JOIN variante_prodotto v ON p.id_prodotto = v.id_prodotto " +
            "GROUP BY p.id_prodotto, p.nome, p.descrizione, p.id_brand, p.genere, " +
            "         p.tipo_terreno, p.tipo_appoggio, p.tipo_passo, p.in_evidenza, " +
            "         p.data_inserimento, p.totale_vendite, b.nome_brand " +
            "ORDER BY p.id_prodotto DESC"; // I più recenti in alto

        Connection con = ConnectionPool.getConnection();
        try (PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProdottoDTO dto = new ProdottoDTO();

                // Mapping del prodotto con tutte le sue informazioni
                Prodotto p = new Prodotto();
                p.setIdProdotto(rs.getInt("id_prodotto"));
                p.setNome(rs.getString("nome"));
                p.setDescrizione(rs.getString("descrizione"));

                Brand b = new Brand();
                b.setIdBrand(rs.getInt("id_brand"));
                b.setNomeBrand(rs.getString("nome_brand"));
                p.setBrand(b);

                p.setGenere(GenereScarpa.valueOf(rs.getString("genere")));
                p.setTipoTerreno(TipoTerreno.valueOf(rs.getString("tipo_terreno")));
                p.setTipoAppoggio(TipoAppoggio.valueOf(rs.getString("tipo_appoggio")));
                p.setTipoPasso(TipoPasso.valueOf(rs.getString("tipo_passo")));
                p.setInEvidenza(rs.getBoolean("in_evidenza"));
                p.setDataInserimento(rs.getTimestamp("data_inserimento"));
                p.setTotaleVendite(rs.getInt("totale_vendite"));

                dto.setProdotto(p);

                // Il prezzo minimo potrebbe essere null se il prodotto non ha varianti, quindi gestiamo questo caso
                dto.setPrezzoMinimo(rs.getBigDecimal("prezzo_minimo"));

                lista.add(dto);
            }
        } finally {
            if (con != null) ConnectionPool.releaseConnection(con);
        }

        return lista;
    }
}