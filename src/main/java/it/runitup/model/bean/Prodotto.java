package it.runitup.model.bean;

import java.io.Serializable;

public class Prodotto implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idProdotto;
    private String nome;
    private String descrizione;

    private Brand brand;
    
    private String immagineCopertina;
    private GenereScarpa genere;
    private String tipoTerreno;
    private String tipoAppoggio;
    private String tipoPasso;

    public Prodotto() {
        super();
    }

    public Prodotto(int idProdotto, String nome, String descrizione, Brand brand, String immagineCopertina,
                    GenereScarpa genere, String tipoTerreno, String tipoAppoggio, String tipoPasso) {
        this.idProdotto = idProdotto;
        this.nome = nome;
        this.descrizione = descrizione;
        this.brand = brand;
        this.immagineCopertina = immagineCopertina;
        this.genere = genere;
        this.tipoTerreno = tipoTerreno;
        this.tipoAppoggio = tipoAppoggio;
        this.tipoPasso = tipoPasso;
    }

    // --- Getter e Setter ---
    public int getIdProdotto() { return idProdotto; }
    public void setIdProdotto(int idProdotto) { this.idProdotto = idProdotto; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getDescrizione() { return descrizione; }
    public void setDescrizione(String descrizione) { this.descrizione = descrizione; }

    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }

    public String getImmagineCopertina() { return immagineCopertina; }
    public void setImmagineCopertina(String immagineCopertina) { this.immagineCopertina = immagineCopertina; }

    public GenereScarpa getGenere() { return genere; }
    public void setGenere(GenereScarpa genere) { this.genere = genere; }

    public String getTipoTerreno() { return tipoTerreno; }
    public void setTipoTerreno(String tipoTerreno) { this.tipoTerreno = tipoTerreno; }

    public String getTipoAppoggio() { return tipoAppoggio; }
    public void setTipoAppoggio(String tipoAppoggio) { this.tipoAppoggio = tipoAppoggio; }

    public String getTipoPasso() { return tipoPasso; }
    public void setTipoPasso(String tipoPasso) { this.tipoPasso = tipoPasso; }

    @Override
    public String toString() {
        return "Prodotto [idProdotto=" + idProdotto + ", nome=" + nome + ", brand=" + (brand != null ? brand.getNomeBrand() : "null") + "]";
    }
}