package it.runitup.model.bean;

import java.io.Serializable;

public class Brand implements Serializable {

    private static final long serialVersionUID = 1L;

    private int idBrand;
    private String nomeBrand;

    public Brand() {
        super();
    }

    public Brand(int idBrand, String nomeBrand) {
        this.idBrand = idBrand;
        this.nomeBrand = nomeBrand;
    }

    // --- Getter e Setter ---
    public int getIdBrand() { return idBrand; }
    public void setIdBrand(int idBrand) { this.idBrand = idBrand; }

    public String getNomeBrand() { return nomeBrand; }
    public void setNomeBrand(String nomeBrand) { this.nomeBrand = nomeBrand; }

    @Override
    public String toString() {
        return "Brand [idBrand=" + idBrand + ", nomeBrand=" + nomeBrand + "]";
    }
}