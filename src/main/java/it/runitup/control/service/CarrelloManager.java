package it.runitup.control.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import it.runitup.model.bean.Utente;
import it.runitup.model.bean.VarianteProdotto;
import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.VarianteDAO;

public class CarrelloManager {
    
    private final HttpSession session;
    private final Utente utenteLoggato;
    private final CarrelloDAO carrelloDAO;
    private final VarianteDAO varianteDAO;

    public CarrelloManager(HttpServletRequest request, CarrelloDAO carrelloDAO, VarianteDAO varianteDAO) {
        this.session = request.getSession();
        this.utenteLoggato = (Utente) session.getAttribute("utente");
        this.carrelloDAO = carrelloDAO;
        this.varianteDAO = varianteDAO;
    }

    public boolean isLoggato() {
        return utenteLoggato != null;
    }

    // Metodo unificato per aggiungere o rimuovere articoli dal carrello dato un delta (positivo o negativo)
    public boolean aggiornaQuantita(int idVariante, int delta) throws SQLException {
        // Carichiamo lo stato in tempo reale dal DB per l'hard-check sulle quantità
        VarianteProdotto variante = varianteDAO.getVarianteById(idVariante);
        if (variante == null) return false;

        int quantitaAttuale = getQuantitaAttuale(idVariante);
        int nuovaQuantita = quantitaAttuale + delta;

        // 1. Controllo Overselling: impedisci di aggiungere più di quanto disponibile
        if (nuovaQuantita > variante.getQuantitaMagazzino()) {
            return false;
        }

        // 2. Esecuzione della logica a seconda dello stato di Login
        if (isLoggato()) {
            return aggiornaSuDB(idVariante, nuovaQuantita);
        } else {
            return aggiornaSuSessione(variante, nuovaQuantita);
        }
    }

    // Ottiene quanti pezzi di quella variante sono già presenti nel carrello
    private int getQuantitaAttuale(int idVariante) throws SQLException {
        if (isLoggato()) {
            int idCarrello = carrelloDAO.getOrCreateCarrelloByUtente(utenteLoggato.getIdUtente()).getIdCarrello();
            return carrelloDAO.getQuantitaVoce(idCarrello, idVariante);
        } else {
            List<VoceCarrello> carrelloOspite = getCarrelloOspite();
            for (VoceCarrello voce : carrelloOspite) {
                if (voce.getVariante().getIdVariante() == idVariante) {
                    return voce.getQuantita();
                }
            }
        }
        return 0;
    }

    // Logica di persistenza su Database (senza sporcarsi con SQL)
    private boolean aggiornaSuDB(int idVariante, int nuovaQuantita) throws SQLException {
        int idCarrello = carrelloDAO.getOrCreateCarrelloByUtente(utenteLoggato.getIdUtente()).getIdCarrello();

        if (nuovaQuantita <= 0) {
            // RIMOZIONE: Delega al DAO
            carrelloDAO.rimuoviVoce(idCarrello, idVariante);
        } else {
            // INSERIMENTO/AGGIORNAMENTO: Delega al DAO
            carrelloDAO.impostaQuantitaVoce(idCarrello, idVariante, nuovaQuantita);
        }
        
        return true;
    }

    // Logica di persistenza su Sessione RAM
    private boolean aggiornaSuSessione(VarianteProdotto variante, int nuovaQuantita) {
        List<VoceCarrello> carrelloOspite = getCarrelloOspite();

        if (nuovaQuantita <= 0) {
            // RIMOZIONE DALLA LISTA
            carrelloOspite.removeIf(voce -> voce.getVariante().getIdVariante() == variante.getIdVariante());
        } else {
            boolean trovato = false;
            for (VoceCarrello voce : carrelloOspite) {
                if (voce.getVariante().getIdVariante() == variante.getIdVariante()) {
                    voce.setQuantita(nuovaQuantita);
                    trovato = true;
                    break;
                }
            }
            if (!trovato) {
                carrelloOspite.add(new VoceCarrello(0, null, variante, nuovaQuantita));
            }
        }
        session.setAttribute("carrello_ospite", carrelloOspite);
        return true;
    }

    @SuppressWarnings("unchecked")
    private List<VoceCarrello> getCarrelloOspite() {
        List<VoceCarrello> carrello = (List<VoceCarrello>) session.getAttribute("carrello_ospite");
        if (carrello == null) {
            carrello = new ArrayList<>();
            session.setAttribute("carrello_ospite", carrello);
        }
        return carrello;
    }

    // Metodo per unire il carrello ospite con quello reale al momento del login
    public void mergeCarrello() throws SQLException {
        // Se per qualche motivo il metodo viene chiamato senza un utente loggato, interrompiamo subito
        if (utenteLoggato == null) return;

        // 1. Recuperiamo le voci salvate in RAM dall'ospite
        List<VoceCarrello> carrelloOspite = getCarrelloOspite();
        if (carrelloOspite.isEmpty()) return; // Se il carrello ospite è vuoto, non c'è nulla da unire

        // 2. Otteniamo l'ID del carrello reale dell'utente nel DB
        int idCarrello = carrelloDAO.getOrCreateCarrelloByUtente(utenteLoggato.getIdUtente()).getIdCarrello();

        // 3. Iteriamo su ogni voce del carrello ospite
        for (VoceCarrello voceOspite : carrelloOspite) {
            int idVariante = voceOspite.getVariante().getIdVariante();
            int quantitaSessione = voceOspite.getQuantita();

            // Recuperiamo la disponibilità effettiva in magazzino in tempo reale (Single Source of Truth)
            VarianteProdotto variante = varianteDAO.getVarianteById(idVariante);
            if (variante == null) continue; // Se il prodotto fosse stato eliminato nel frattempo, lo saltiamo

            int quantitaMagazzino = variante.getQuantitaMagazzino();

            // Recuperiamo quanti pezzi di questa variante l'utente ha già nel suo carrello su DB
            int quantitaSuDB = carrelloDAO.getQuantitaVoce(idCarrello, idVariante);

            // Logica del Tetto Massimo: calcoliamo la somma potenziale
            int nuovaQuantita = quantitaSuDB + quantitaSessione;
            
            // Se la somma supera il magazzino, la quantità finale viene limitata al massimo disponibile
            if (nuovaQuantita > quantitaMagazzino) {
                nuovaQuantita = quantitaMagazzino;
            }

            // Se la quantità calcolata è valida e differisce da quella già presente sul DB, salviamola
            if (nuovaQuantita > 0 && nuovaQuantita != quantitaSuDB) {
                carrelloDAO.impostaQuantitaVoce(idCarrello, idVariante, nuovaQuantita);
            }
        }

        // 4. Pulizia automatica della sessione come concordato
        session.removeAttribute("carrello_ospite");
    }

    // Metodo per ottenere la lista completa delle voci del carrello, indipendentemente dallo stato di login (utile per la visualizzazione del carrello)
    public List<VoceCarrello> getVociCarrello() throws SQLException {
        if (isLoggato()) {
            int idCarrello = carrelloDAO.getOrCreateCarrelloByUtente(utenteLoggato.getIdUtente()).getIdCarrello();
            return carrelloDAO.getVociByCarrello(idCarrello);
        } else {
            return getCarrelloOspite();
        }
    }

    // Metodo per rimuovere completamente una voce dal carrello (utilizzato ad esempio per il pulsante "Rimuovi" nella pagina del carrello)
    public boolean rimuoviVoce(int idVariante) throws SQLException {
        if (isLoggato()) {
            int idCarrello = carrelloDAO.getOrCreateCarrelloByUtente(utenteLoggato.getIdUtente()).getIdCarrello();
            carrelloDAO.rimuoviVoce(idCarrello, idVariante);
            return true;
        } else {
            List<VoceCarrello> carrelloOspite = getCarrelloOspite();
            boolean rimosso = carrelloOspite.removeIf(voce -> voce.getVariante().getIdVariante() == idVariante);
            if (rimosso) {
                session.setAttribute("carrello_ospite", carrelloOspite);
            }
            return rimosso;
        }
    }
    
}
