package it.runitup.model.filter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import it.runitup.model.bean.GenereScarpa;
import it.runitup.model.bean.TipoAppoggio;
import it.runitup.model.bean.TipoPasso;
import it.runitup.model.bean.TipoTerreno;

// Classe che rappresenta i filtri applicati al catalogo, utilizzata per passare i parametri di filtraggio dal frontend al DAO
public class FiltroCatalogo implements Serializable {

    private static final long serialVersionUID = 1L;

    // --- Filtri Liste (Multi-selezione) ---
    private List<Integer> brandIds;
    private List<GenereScarpa> generi;

    // --- Filtri Numerici (Range di Prezzo) ---
    private BigDecimal prezzoMinimo;
    private BigDecimal prezzoMassimo;

    // --- Filtri Booleani (Checkbox) ---
    private Boolean inEvidenza;
    private Boolean inOfferta;
    private Boolean nuoviArrivi;

    private List<String> taglie;
    private List<String> colori;

    // --- Filtri per caratteristiche della scarpa (Sezioni specifiche della scarpa) ---
    private List<TipoTerreno> tipiTerreno;
    private List<TipoAppoggio> tipiAppoggio;
    private List<TipoPasso> tipiPasso;

    // --- Filtro di Ricerca (Campo di testo) ---
    private String ricercaTestuale;

    // --- Logiche di Visualizzazione ---
    private String ordinamento;
    private int limit; // Numero di prodotti per pagina di default
    private int offset;

    public FiltroCatalogo() {
        super();
        // Inizializzazione dei valori di default
        this.limit = 21;
        this.offset = 0;
    }

    // --- Getters e Setters ---
    public List<Integer> getBrandIds() { return brandIds; }
    public void setBrandIds(List<Integer> brandIds) { this.brandIds = brandIds; }

    public List<GenereScarpa> getGeneri() { return generi; }
    public void setGeneri(List<GenereScarpa> generi) { this.generi = generi; }

    public BigDecimal getPrezzoMinimo() { return prezzoMinimo; }
    public void setPrezzoMinimo(BigDecimal prezzoMinimo) { this.prezzoMinimo = prezzoMinimo; }

    public BigDecimal getPrezzoMassimo() { return prezzoMassimo; }
    public void setPrezzoMassimo(BigDecimal prezzoMassimo) { this.prezzoMassimo = prezzoMassimo; }

    public Boolean getInEvidenza() { return inEvidenza; }
    public void setInEvidenza(Boolean inEvidenza) { this.inEvidenza = inEvidenza; }

    public Boolean getInOfferta() { return inOfferta; }
    public void setInOfferta(Boolean inOfferta) { this.inOfferta = inOfferta; }

    public Boolean getNuoviArrivi() { return nuoviArrivi; }
    public void setNuoviArrivi(Boolean nuoviArrivi) { this.nuoviArrivi = nuoviArrivi; }

    public List<String> getTaglie() { return taglie; }
    public void setTaglie(List<String> taglie) { this.taglie = taglie; }

    public List<String> getColori() { return colori; }
    public void setColori(List<String> colori) { this.colori = colori; }

    public List<TipoTerreno> getTipiTerreno() { return tipiTerreno; }
    public void setTipiTerreno(List<TipoTerreno> tipiTerreno) { this.tipiTerreno = tipiTerreno; }

    public List<TipoAppoggio> getTipiAppoggio() { return tipiAppoggio; }
    public void setTipiAppoggio(List<TipoAppoggio> tipiAppoggio) { this.tipiAppoggio = tipiAppoggio; }

    public List<TipoPasso> getTipiPasso() { return tipiPasso; }
    public void setTipiPasso(List<TipoPasso> tipiPasso) { this.tipiPasso = tipiPasso; }

    public String getRicercaTestuale() { return ricercaTestuale; }
    public void setRicercaTestuale(String ricercaTestuale) { this.ricercaTestuale = ricercaTestuale; }

    public String getOrdinamento() { return ordinamento; }
    public void setOrdinamento(String ordinamento) { this.ordinamento = ordinamento; }

    public int getLimit() { return limit; }
    public void setLimit(int limit) { this.limit = limit; }

    public int getOffset() { return offset; }
    public void setOffset(int offset) { this.offset = offset; }
}