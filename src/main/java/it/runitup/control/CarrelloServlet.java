package it.runitup.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.runitup.model.bean.Utente;
import it.runitup.model.bean.VarianteProdotto;
import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.VarianteDAO;

@WebServlet("/aggiungi_al_carrello")
public class CarrelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final CarrelloDAO carrelloDAO = new CarrelloDAO();

    // DAO per ottenere i dettagli di una variante (prezzo, disponibilità, nome prodotto, brand, ecc.)
    private final VarianteDAO varianteDAO = new VarianteDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Impostiamo la risposta per essere letta dalla fetch() in JS
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // 1. Lettura del body JSON dalla richiesta Fetch
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = new Gson().fromJson(reader, JsonObject.class);
            
            if (jsonBody == null || !jsonBody.has("idVariante")) {
                inviaRisposta(response, false, "Dati non validi.");
                return;
            }
            
            int idVariante = jsonBody.get("idVariante").getAsInt();
            int quantitaAggiunta = 1; // Dalla pagina prodotto si aggiunge sempre 1 pezzo alla volta

            // 2. Soft-Check: Il prodotto esiste e ha almeno 1 pezzo disponibile?
            VarianteProdotto variante = varianteDAO.getVarianteById(idVariante);
            if (variante == null || variante.getQuantitaMagazzino() < quantitaAggiunta) {
                inviaRisposta(response, false, "Spiacenti, l'articolo è esaurito.");
                return;
            }

            // 3. Smistamento Logica (Ospite vs Loggato)
            HttpSession session = request.getSession();
            Utente utenteLoggato = (Utente) session.getAttribute("utente");

            if (utenteLoggato != null) {
                // --- UTENTE LOGGATO: Lavoriamo sul Database ---
                // Il DAO fa tutto: controlla se esiste, incrementa o inserisce.
                // TODO Implementare controllo di quantità massima disponibile nel DAO per evitare overselling.
                carrelloDAO.aggiungiVariante(utenteLoggato.getIdUtente(), idVariante, quantitaAggiunta);
                
            } else {
                // --- UTENTE OSPITE: Lavoriamo in RAM (Sessione) ---
                @SuppressWarnings("unchecked")
                List<VoceCarrello> carrelloOspite = (List<VoceCarrello>) session.getAttribute("carrello_ospite");
                
                if (carrelloOspite == null) {
                    carrelloOspite = new ArrayList<>();
                }

                boolean trovata = false;
                for (VoceCarrello voce : carrelloOspite) {
                    if (voce.getVariante().getIdVariante() == idVariante) {
                        // Ulteriore Soft-check: l'utente ha già X nel carrello, supererebbe il magazzino aggiungendone 1?
                        if (voce.getQuantita() + quantitaAggiunta > variante.getQuantitaMagazzino()) {
                            inviaRisposta(response, false, "Hai raggiunto la quantità massima disponibile per questo articolo.");
                            return;
                        }
                        // Se c'è già, incrementiamo
                        voce.setQuantita(voce.getQuantita() + quantitaAggiunta);
                        trovata = true;
                        break;
                    }
                }

                if (!trovata) {
                    // Non c'era, creiamo una nuova voce in RAM (ID 0 perché non è nel DB)
                    VoceCarrello nuovaVoce = new VoceCarrello(0, null, variante, quantitaAggiunta);
                    carrelloOspite.add(nuovaVoce);
                }

                // Salviamo l'aggiornamento in sessione
                session.setAttribute("carrello_ospite", carrelloOspite);
            }

            // 4. Risposta di Successo
            inviaRisposta(response, true, "Prodotto aggiunto al carrello!");

        } catch (Exception e) {
            e.printStackTrace();
            inviaRisposta(response, false, "Errore interno del server.");
        }
    }

    // Metodo di utilità per formattare la risposta JSON in modo pulito
    private void inviaRisposta(HttpServletResponse response, boolean success, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", message);
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
}