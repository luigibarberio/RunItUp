package it.runitup.control.checkout;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.VarianteDAO;
import it.runitup.model.dto.VarianteDTO;
import it.runitup.model.dto.VoceCarrelloDTO;
import it.runitup.service.CarrelloManager;

@WebServlet("/carrello")
public class CarrelloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final CarrelloDAO carrelloDAO = new CarrelloDAO();
    private final VarianteDAO varianteDAO = new VarianteDAO();

    // Il metodo doGet ora si occupa di caricare le voci del carrello con i prezzi scontati in tempo reale e calcolare il totale aggiornato
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Inizializzazione del CarrelloManager con le DAO necessarie
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            
            // Recupero delle voci del carrello grezze dalla sessione o dal database
            List<VoceCarrello> vociGrezze = cartManager.getVociCarrello();
            
            // Creazione di una lista di DTO per le voci del carrello, che includeranno i prezzi scontati in tempo reale
            List<VoceCarrelloDTO> vociDTO = new java.util.ArrayList<>();
            
            // Calcolo del totale del carrello aggiornato con i prezzi scontati
            BigDecimal totaleCarrello = BigDecimal.ZERO;
            
            for (VoceCarrello voce : vociGrezze) {
                // Recupero della variante con eventuale promozione applicata
                VarianteDTO varianteDTO = varianteDAO.getVarianteConPromozione(voce.getVariante());
                
                // Creazione del DTO della voce del carrello con la variante aggiornata
                VoceCarrelloDTO voceDTO = new VoceCarrelloDTO(voce, varianteDTO);
                vociDTO.add(voceDTO);
                
                // Calcolo del totale del carrello considerando il prezzo effettivo della variante e la quantità
                BigDecimal prezzoVendita = varianteDTO.getPrezzoEffettivo();
                BigDecimal quantita = new BigDecimal(voce.getQuantita());
                
                totaleCarrello = totaleCarrello.add(prezzoVendita.multiply(quantita));
            }
            
            // Impostazione degli attributi della richiesta per la JSP
            request.setAttribute("vociCarrello", vociDTO);
            request.setAttribute("totaleCarrello", totaleCarrello);
                    
            // Inoltro alla JSP del carrello con i dati aggiornati
            request.getRequestDispatcher("/WEB-INF/view/checkout/carrello.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento del carrello promozionale.");
        }
    }

    // Il metodo doPost gestisce le richieste AJAX per modificare le quantità o rimuovere voci dal carrello, restituendo un JSON con l'esito e il numero aggiornato di pezzi
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Variabile per tenere traccia del numero totale di pezzi nel carrello dopo le modifiche
        int totalePezzi = 0;

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = new Gson().fromJson(reader, JsonObject.class);
            
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            
            // Calcolo iniziale del totale pezzi prima di qualsiasi modifica
            totalePezzi = cartManager.getTotalePezzi();

            if (jsonBody == null || !jsonBody.has("idVariante")) {
                inviaRisposta(response, false, "Dati del carrello non validi.", totalePezzi);
                return;
            }
            
            int idVariante = jsonBody.get("idVariante").getAsInt();
            
            // Controllo se l'azione è di rimozione o di aggiornamento della quantità
            if (jsonBody.has("action") && "remove".equalsIgnoreCase(jsonBody.get("action").getAsString())) {
                
                // AZIONE A: Rimozione della voce dal carrello
                boolean rimosso = cartManager.rimuoviVoce(idVariante);
                if (rimosso) {
                    // Se la rimozione riesce, ricalcoliamo il nuovo totale pezzi dal manager
                    totalePezzi = cartManager.getTotalePezzi();
                    // Aggiorniamo subito il numero di pezzi in sessione per i futuri caricamenti di pagina (Sincrono)
                    request.getSession().setAttribute("cartBadgeCount", totalePezzi);
                    
                    inviaRisposta(response, true, "Prodotto rimosso dal carrello.", totalePezzi);
                } else {
                    inviaRisposta(response, false, "Impossibile rimuovere il prodotto.", totalePezzi);
                }
                
            } else {
                
                // AZIONE B: Aggiornamento della quantità della voce nel carrello (pulsanti "+" e "-")
                int delta = jsonBody.has("delta") ? jsonBody.get("delta").getAsInt() : 1;
                
                boolean esito = cartManager.aggiornaQuantita(idVariante, delta);
                if (esito) {
                    // Se l'aggiornamento riesce, ricalcoliamo il nuovo totale pezzi dal manager
                    totalePezzi = cartManager.getTotalePezzi();
                    // Aggiorniamo subito il numero di pezzi in sessione per i futuri caricamenti di pagina (Sincrono)
                    request.getSession().setAttribute("cartBadgeCount", totalePezzi);
                    
                    inviaRisposta(response, true, "Quantità aggiornata nel carrello.", totalePezzi);
                } else {
                    inviaRisposta(response, false, "Quantità richiesta non disponibile in magazzino.", totalePezzi);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            inviaRisposta(response, false, "Errore interno durante la modifica del carrello.", totalePezzi);
        }
    }

    // Metodo di utilità per inviare una risposta JSON al client con l'esito dell'operazione e il numero aggiornato di pezzi nel carrello
    private void inviaRisposta(HttpServletResponse response, boolean success, String message, int cartCount) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", message);

        // Numero totale di pezzi nel carrello dopo l'operazione, utile per aggiornare il badge del carrello in tempo reale
        jsonResponse.addProperty("cartCount", cartCount); 
        
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
    
}