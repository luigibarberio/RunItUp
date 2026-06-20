package it.runitup.control.catalog;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import it.runitup.model.bean.Prodotto;
import it.runitup.model.dao.ProdottoDAO;

@WebServlet("/suggerimenti")
public class SuggerimentiRicercaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Impostazione del tipo di contenuto della risposta come JSON e della codifica dei caratteri
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String q = request.getParameter("q");
        
        // Restituzione di un array JSON vuoto se la query è nulla o troppo corta (meno di 2 caratteri)
        if (q == null || q.trim().length() < 2) {
            response.getWriter().write("[]");
            return;
        }

        try {
            List<Prodotto> risultati = prodottoDAO.doRetrieveSuggerimenti(q.trim());
            
            // Creazione di un array JSON per contenere i suggerimenti dei prodotti
            JsonArray jsonArray = new JsonArray();
            for (Prodotto p : risultati) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", p.getIdProdotto());
                obj.addProperty("nome", p.getNome());
                obj.addProperty("brand", p.getBrand() != null ? p.getBrand().getNomeBrand() : "");
                jsonArray.add(obj);
            }

            // Invio della risposta JSON al client con i suggerimenti dei prodotti
            response.getWriter().write(gson.toJson(jsonArray));

        } catch (Exception e) {
            // Gestione degli errori generici, come problemi di connessione al database o eccezioni non previste
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("[]");
        }
    }
}