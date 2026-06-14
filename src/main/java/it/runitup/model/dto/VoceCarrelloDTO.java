package it.runitup.model.dto;

import java.io.Serializable;
import it.runitup.model.bean.VoceCarrello;

public class VoceCarrelloDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private VoceCarrello voceCarrello;
    private VarianteDTO varianteDTO;

    public VoceCarrelloDTO() {
        super();
    }

    public VoceCarrelloDTO(VoceCarrello voceCarrello, VarianteDTO varianteDTO) {
        this.voceCarrello = voceCarrello;
        this.varianteDTO = varianteDTO;
    }

    // --- Getter e Setter ---
    public VoceCarrello getVoceCarrello() { 
        return voceCarrello; 
    }
    public void setVoceCarrello(VoceCarrello voceCarrello) { 
        this.voceCarrello = voceCarrello; 
    }

    public VarianteDTO getVarianteDTO() { 
        return varianteDTO; 
    }
    public void setVarianteDTO(VarianteDTO varianteDTO) { 
        this.varianteDTO = varianteDTO; 
    }
}