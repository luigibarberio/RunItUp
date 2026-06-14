package it.runitup.control;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.runitup.control.service.CarrelloManager;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.VarianteDAO;

@WebServlet("/modifica_carrello")
public class CarrelloFetchServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final CarrelloDAO carrelloDAO = new CarrelloDAO();
    private final VarianteDAO varianteDAO = new VarianteDAO();

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