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
import it.runitup.model.dao.ProdottoDAO;
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
            ProdottoDAO prodottoDAO = new ProdottoDAO();
            
            // 2. Recuperiamo la lista grezza delle voci (valida sia per loggati che ospiti)
            List<VoceCarrello> vociGrezze = cartManager.getVociCarrello();
            
            // 3. Creiamo la lista destinata ad ospitare i pacchetti DTO completi
            List<VoceCarrelloDTO> vociDTO = new java.util.ArrayList<>();
            
            // 4. Calcoliamo il totale economico basandoci sul prezzo effettivo di vendita
            BigDecimal totaleCarrello = BigDecimal.ZERO;
            
            for (VoceCarrello voce : vociGrezze) {
                // Ricaviamo la variante arricchita con lo sconto in tempo reale dal DB
                VarianteDTO varianteDTO = prodottoDAO.getVarianteConPromozione(voce.getVariante());
                
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

        try {
            // 1. Lettura del body JSON dalla Fetch
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = new Gson().fromJson(reader, JsonObject.class);
            
            if (jsonBody == null || !jsonBody.has("idVariante")) {
                inviaRisposta(response, false, "Dati del carrello non validi.");
                return;
            }
            
            int idVariante = jsonBody.get("idVariante").getAsInt();
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            
            // 2. Controllo Azione: Rimozione Esplicita o Aggiornamento Quantità?
            if (jsonBody.has("action") && "remove".equalsIgnoreCase(jsonBody.get("action").getAsString())) {
                
                // AZIONE A: Rimozione totale dell'articolo (Pulsante "Rimuovi")
                boolean rimosso = cartManager.rimuoviVoce(idVariante);
                if (rimosso) {
                    inviaRisposta(response, true, "Prodotto rimosso dal carrello.");
                } else {
                    inviaRisposta(response, false, "Impossibile rimuovere il prodotto.");
                }
                
            } else {
                
                // AZIONE B: Variazione di quantità (Pulsanti "+" e "-" o Aggiunta da scheda prodotto)
                // Se non viene specificato un delta, assumiamo +1
                int delta = jsonBody.has("delta") ? jsonBody.get("delta").getAsInt() : 1;
                
                boolean esito = cartManager.aggiornaQuantita(idVariante, delta);
                if (esito) {
                    inviaRisposta(response, true, "Quantità aggiornata nel carrello.");
                } else {
                    inviaRisposta(response, false, "Quantità richiesta non disponibile in magazzino.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            inviaRisposta(response, false, "Errore interno durante la modifica del carrello.");
        }
    }

    private void inviaRisposta(HttpServletResponse response, boolean success, String message) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", message);
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
}