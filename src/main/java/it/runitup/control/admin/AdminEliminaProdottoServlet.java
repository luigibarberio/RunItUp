package it.runitup.control.admin;

import java.io.BufferedReader;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.runitup.model.dao.ProdottoDAO;

// Servlet per l'eliminazione di un prodotto dal catalogo. Riceve una richiesta POST contenente l'ID del prodotto da eliminare in formato JSON.
@WebServlet("/admin/prodotto/elimina")
public class AdminEliminaProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Configuriamo la risposta per essere un JSON puro
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        try {
            // 1. Estrazione del payload JSON dalla Request
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = new Gson().fromJson(reader, JsonObject.class);

            // Validazione del parametro
            if (jsonBody == null || !jsonBody.has("idProdotto")) {
                inviaErrore(response, jsonResponse, "Richiesta non valida: ID prodotto mancante.");
                return;
            }

            int idProdotto = jsonBody.get("idProdotto").getAsInt();

            // 2. Eliminazione sul Database
            boolean esito = prodottoDAO.deleteProdotto(idProdotto);
            
            if (esito) {
                // Successo: prepariamo il JSON verde per il frontend
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Il prodotto è stato eliminato definitivamente dal catalogo.");
            } else {
                inviaErrore(response, jsonResponse, "Impossibile eliminare il prodotto. Potrebbe essere già stato rimosso.");
                return;
            }

            // 3. Invio della risposta al Client
            response.getWriter().write(new Gson().toJson(jsonResponse));

        } catch (Exception e) {
            e.printStackTrace();
            inviaErrore(response, jsonResponse, "Si è verificato un errore critico lato server.");
        }
    }

    // Metodo di utilità per inviare un messaggio di errore in formato JSON
    private void inviaErrore(HttpServletResponse response, JsonObject jsonResponse, String messaggio) throws IOException {
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", messaggio);
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
}