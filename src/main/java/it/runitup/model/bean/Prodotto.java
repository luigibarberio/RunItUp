package it.runitup.model.bean;

import java.io.Serializable;
import java.sql.Timestamp;

public class Prodotto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idProdotto;
    private String nome;
    private String descrizione;

    private Brand brand;
    
    private byte[] immagineCopertina;
    private GenereScarpa genere;
    private TipoTerreno tipoTerreno;
    private TipoAppoggio tipoAppoggio;
    private TipoPasso tipoPasso;

    private Timestamp dataInserimento;
    private boolean inEvidenza;
    private int totaleVendite;

    public Prodotto() {
        super();
    }

    public Prodotto(int idProdotto, String nome, String descrizione, Brand brand, byte[] immagineCopertina,
                    GenereScarpa genere, TipoTerreno tipoTerreno, TipoAppoggio tipoAppoggio, TipoPasso tipoPasso,
                    Timestamp dataInserimento, boolean inEvidenza, int totaleVendite) {
        this.idProdotto = idProdotto;
        this.nome = nome;
        this.descrizione = descrizione;
        this.brand = brand;
        this.immagineCopertina = immagineCopertina;
        this.genere = genere;
        this.tipoTerreno = tipoTerreno;
        this.tipoAppoggio = tipoAppoggio;
        this.tipoPasso = tipoPasso;
        this.dataInserimento = dataInserimento;
        this.inEvidenza = inEvidenza;
        this.totaleVendite = totaleVendite;
    }

    // --- Getters e Setters ---
    public int getIdProdotto() { return idProdotto; }
    public void setIdProdotto(int idProdotto) { this.idProdotto = idProdotto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }

    public byte[] getImmagineCopertina() { return immagineCopertina; }
    public void setImmagineCopertina(byte[] immagineCopertina) { this.immagineCopertina = immagineCopertina; }

    public GenereScarpa getGenere() { return genere; }
    public void setGenere(GenereScarpa genere) { this.genere = genere; }

    public TipoTerreno getTipoTerreno() { return tipoTerreno; }
    public void setTipoTerreno(TipoTerreno tipoTerreno) { this.tipoTerreno = tipoTerreno; }

    public TipoAppoggio getTipoAppoggio() { return tipoAppoggio; }
    public void setTipoAppoggio(TipoAppoggio tipoAppoggio) { this.tipoAppoggio = tipoAppoggio; }

    public TipoPasso getTipoPasso() { return tipoPasso; }
    public void setTipoPasso(TipoPasso tipoPasso) { this.tipoPasso = tipoPasso; }

    public Timestamp getDataInserimento() { return dataInserimento; }
    public void setDataInserimento(Timestamp dataInserimento) { this.dataInserimento = dataInserimento; }

    public boolean isInEvidenza() { return inEvidenza; }
    public void setInEvidenza(boolean inEvidenza) { this.inEvidenza = inEvidenza; }

    public int getTotaleVendite() { return totaleVendite; }
    public void setTotaleVendite(int totaleVendite) { this.totaleVendite = totaleVendite; }

    @Override
    public String toString() {
        return "Prodotto [idProdotto=" + idProdotto + ", nome=" + nome + ", brand=" + (brand != null ? brand.getNomeBrand() : "null") + "]";
    }
}