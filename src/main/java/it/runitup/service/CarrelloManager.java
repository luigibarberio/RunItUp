package it.runitup.service;

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
        this.utenteLoggato = (Utente) session.getAttribute("utenteLoggato");
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

        // Se la nuova quantità è negativa o supera la disponibilità in magazzino, si blocca l'operazione
        if (nuovaQuantita > variante.getQuantitaMagazzino() || nuovaQuantita < 0) {
            return false;
        }

        // Se l'utente è loggato, si aggiorna direttamente il carrello nel DB, altrimenti lo si aggiornia in sessione
        if (isLoggato()) {
            return aggiornaSuDB(idVariante, nuovaQuantita);
        } else {
            return aggiornaSuSessione(variante, nuovaQuantita);
        }
    }

    // Metodo per ottenere la quantità attuale di una variante nel carrello, indipendentemente dallo stato di login (utile per il controllo delle quantità)
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

    // Metodo per aggiornare la quantità di una variante nel carrello del database, restituendo true se l'operazione è avvenuta con successo
    private boolean aggiornaSuDB(int idVariante, int nuovaQuantita) throws SQLException {
        int idCarrello = carrelloDAO.getOrCreateCarrelloByUtente(utenteLoggato.getIdUtente()).getIdCarrello();

        // Se la nuova quantità è zero o negativa (anche se questo caso non dovrebbe verificarsi), rimuoviamo la voce dal carrello, altrimenti aggiorniamo la quantità
        if (nuovaQuantita <= 0) {
            carrelloDAO.rimuoviVoce(idCarrello, idVariante);
        } else {
            carrelloDAO.impostaQuantitaVoce(idCarrello, idVariante, nuovaQuantita);
        }
        
        return true;
    }

    // Metodo per aggiornare la quantità di una variante nel carrello della sessione (per utenti non loggati), restituendo true se l'operazione è avvenuta con successo
    private boolean aggiornaSuSessione(VarianteProdotto variante, int nuovaQuantita) {
        List<VoceCarrello> carrelloOspite = getCarrelloOspite();

        if (nuovaQuantita <= 0) {
            // Rimuoviamo la voce dal carrello se la quantità è zero o negativa (anche se quest'ultimo caso non dovrebbe verificarsi)
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

        // Si ottiene il carrello ospite dalla sessione (che potrebbe essere vuoto se l'utente non aveva aggiunto nulla prima del login)
        List<VoceCarrello> carrelloOspite = getCarrelloOspite();
        if (carrelloOspite.isEmpty()) return; // Se il carrello ospite è vuoto, non c'è nulla da unire

        // Si ottiene o crea il carrello reale dell'utente loggato dal database, per avere un ID carrello valido su cui operare
        int idCarrello = carrelloDAO.getOrCreateCarrelloByUtente(utenteLoggato.getIdUtente()).getIdCarrello();

        // Per ogni voce nel carrello ospite, si tenta di aggiungerla al carrello reale, rispettando la logica del "tetto massimo" basato sulla disponibilità in magazzino
        for (VoceCarrello voceOspite : carrelloOspite) {
            int idVariante = voceOspite.getVariante().getIdVariante();
            int quantitaSessione = voceOspite.getQuantita();

            // Si recupera la variante dal database per ottenere la quantità attuale in magazzino e applicare la logica del tetto massimo, evitando di superare la disponibilità reale del prodotto (in caso di aggiornamenti concorrenti del magazzino)
            VarianteProdotto variante = varianteDAO.getVarianteById(idVariante);
            if (variante == null) continue; // Se il prodotto fosse stato eliminato nel frattempo, lo saltiamo

            int quantitaMagazzino = variante.getQuantitaMagazzino();
            
            // Calcolo della quantità attuale sul DB per questa variante, per poterla sommare con quella della sessione
            int quantitaSuDB = carrelloDAO.getQuantitaVoce(idCarrello, idVariante);

            int nuovaQuantita = quantitaSuDB + quantitaSessione;
            
            // Se la somma supera il magazzino, la quantità finale viene limitata al massimo disponibile
            if (nuovaQuantita > quantitaMagazzino) {
                nuovaQuantita = quantitaMagazzino;
            }

            // Se la quantità calcolata è valida e differisce da quella già presente sul DB, salviamola
            if (nuovaQuantita > 0 && nuovaQuantita != quantitaSuDB) {
                carrelloDAO.impostaQuantitaVoce(idCarrello, idVariante, nuovaQuantita);

                // Aggiornamento del totale pezzi in sessione per il badge del carrello
                int totalePezzi = getTotalePezzi();
                session.setAttribute("cartBadgeCount", totalePezzi);

            }
        }

        // Dopo aver unito i carrelli, si rimuove il carrello ospite dalla sessione
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

    // Metodo per calcolare il totale dei pezzi nel carrello, indipendentemente dallo stato di login (utile per il badge del carrello)
    public int getTotalePezzi() throws SQLException {
        int totale = 0;
        List<VoceCarrello> voci = getVociCarrello();
        for (VoceCarrello voce : voci) {
            totale += voce.getQuantita();
        }
        return totale;
    }

}
