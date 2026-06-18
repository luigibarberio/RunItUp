package it.runitup.control;

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

import it.runitup.control.service.CarrelloManager;
import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.VarianteDAO;
import it.runitup.model.dto.VarianteDTO;
import it.runitup.model.dto.VoceCarrelloDTO;

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
            // 1. Inizializziamo il manager unificato e il DAO dei prodotti per le promozioni
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            
            // 2. Recuperiamo la lista grezza delle voci (valida sia per loggati che ospiti)
            List<VoceCarrello> vociGrezze = cartManager.getVociCarrello();
            
            // 3. Creiamo la lista destinata ad ospitare i pacchetti DTO completi
            List<VoceCarrelloDTO> vociDTO = new java.util.ArrayList<>();
            
            // 4. Calcoliamo il totale economico basandoci sul prezzo effettivo di vendita
            BigDecimal totaleCarrello = BigDecimal.ZERO;
            
            for (VoceCarrello voce : vociGrezze) {
                // Ricaviamo la variante arricchita con lo sconto in tempo reale dal DB
                VarianteDTO varianteDTO = varianteDAO.getVarianteConPromozione(voce.getVariante());
                
                // Creiamo il DTO della riga iniettando i due componenti
                VoceCarrelloDTO voceDTO = new VoceCarrelloDTO(voce, varianteDTO);
                vociDTO.add(voceDTO);
                
                // Calcolo del subtotale della riga: prezzoEffettivo (scontato se in promo) * quantita
                BigDecimal prezzoVendita = varianteDTO.getPrezzoEffettivo();
                BigDecimal quantita = new BigDecimal(voce.getQuantita());
                
                totaleCarrello = totaleCarrello.add(prezzoVendita.multiply(quantita));
            }
            
            // 5. Passiamo i DTO e il totale ricalcolato alla View
            request.setAttribute("vociCarrello", vociDTO);
            request.setAttribute("totaleCarrello", totaleCarrello);
            
            // 6. Forward alla JSP sicura sotto WEB-INF
            request.getRequestDispatcher("/WEB-INF/view/carrello.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento del carrello promozionale.");
        }
    }

    // Il metodo doPost ora gestisce sia l'aggiunta che la rimozione di articoli, restituendo sempre un JSON alla Fetch
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 1. Prepariamo una variabile per tracciare il numero totale di pezzi
        int totalePezzi = 0;

        try {
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = new Gson().fromJson(reader, JsonObject.class);
            
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            
            // 2. Leggiamo il totale pezzi iniziale prima di fare modifiche (come punto di partenza sicuro)
            totalePezzi = cartManager.getTotalePezzi();

            if (jsonBody == null || !jsonBody.has("idVariante")) {
                inviaRisposta(response, false, "Dati del carrello non validi.", totalePezzi);
                return;
            }
            
            int idVariante = jsonBody.get("idVariante").getAsInt();
            
            // Controllo Azione: Rimozione Esplicita o Aggiornamento Quantità?
            if (jsonBody.has("action") && "remove".equalsIgnoreCase(jsonBody.get("action").getAsString())) {
                
                // AZIONE A: Rimozione totale dell'articolo
                boolean rimosso = cartManager.rimuoviVoce(idVariante);
                if (rimosso) {
                    // 3. SE LA MODIFICA RIESCE: Ricalcoliamo il nuovo totale pezzi dal manager
                    totalePezzi = cartManager.getTotalePezzi();
                    // 4. Lo salviamo subito in sessione per i futuri caricamenti di pagina (Sincrono)
                    request.getSession().setAttribute("cartBadgeCount", totalePezzi);
                    
                    inviaRisposta(response, true, "Prodotto rimosso dal carrello.", totalePezzi);
                } else {
                    inviaRisposta(response, false, "Impossibile rimuovere il prodotto.", totalePezzi);
                }
                
            } else {
                
                // AZIONE B: Variazione di quantità (Pulsanti "+" e "-" o Aggiunta da scheda)
                int delta = jsonBody.has("delta") ? jsonBody.get("delta").getAsInt() : 1;
                
                boolean esito = cartManager.aggiornaQuantita(idVariante, delta);
                if (esito) {
                    // 3. SE LA MODIFICA RIESCE: Ricalcoliamo il nuovo totale pezzi dal manager
                    totalePezzi = cartManager.getTotalePezzi();
                    // 4. Lo salviamo subito in sessione per i futuri caricamenti di pagina (Sincrono)
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

    // Abbiamo aggiunto il parametro "int cartCount" alla firma del metodo helper
    private void inviaRisposta(HttpServletResponse response, boolean success, String message, int cartCount) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", message);
        
        // 5. Inseriamo il numero aggiornato nel JSON finale diretto alla Fetch (Asincrono)
        jsonResponse.addProperty("cartCount", cartCount); 
        
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
    
}