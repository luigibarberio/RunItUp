package it.runitup.control.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.runitup.model.bean.Prodotto;
import it.runitup.model.bean.VarianteProdotto;
import it.runitup.model.dao.ProdottoDAO;
import it.runitup.model.dao.VarianteDAO;

@WebServlet("/admin/prodotto/varianti")
public class AdminVariantiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final VarianteDAO varianteDAO = new VarianteDAO();
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();

    // doGet: Recupera tutte le varianti di un prodotto specifico e le inoltra alla JSP per la visualizzazione.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idProdottoStr = request.getParameter("idProdotto");

        if (idProdottoStr == null || idProdottoStr.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/admin/prodotti");
            return;
        }

        try {
            int idProdotto = Integer.parseInt(idProdottoStr);
            
            // Recupero del prodotto per verificare che esista
            Prodotto prodotto = prodottoDAO.getProdottoById(idProdotto);
            if (prodotto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Prodotto non trovato.");
                return;
            }

            // Recupero delle varianti associate al prodotto
            List<VarianteProdotto> listaVarianti = varianteDAO.getVariantiByProdotto(idProdotto);

            request.setAttribute("prodotto", prodotto);
            request.setAttribute("varianti", listaVarianti);

            request.getRequestDispatcher("/WEB-INF/view/admin/admin_varianti.jsp").forward(request, response);

        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID prodotto non valido.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento delle varianti.");
        }
    }

    // doPost: Gestisce le operazioni di inserimento, modifica ed eliminazione delle varianti tramite richieste AJAX con payload JSON.
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        try {
            // Lettura del payload JSON dalla richiesta
            BufferedReader reader = request.getReader();
            JsonObject jsonBody = new Gson().fromJson(reader, JsonObject.class);

            if (jsonBody == null || !jsonBody.has("action")) {
                inviaErrore(response, jsonResponse, "Richiesta non valida: azione mancante.");
                return;
            }

            String action = jsonBody.get("action").getAsString();

            // Switch per determinare l'azione da eseguire: insert, update o delete
            switch (action) {
                case "insert":
                    inserisciVariante(jsonBody, jsonResponse);
                    break;
                case "update":
                    modificaVariante(jsonBody, jsonResponse);
                    break;
                case "delete":
                    eliminaVariante(jsonBody, jsonResponse);
                    break;
                default:
                    inviaErrore(response, jsonResponse, "Azione non riconosciuta.");
                    return;
            }

            // Invio della risposta JSON al client
            response.getWriter().write(new Gson().toJson(jsonResponse));

        } catch (Exception e) {
            e.printStackTrace();
            inviaErrore(response, jsonResponse, "Errore interno del server: " + e.getMessage());
        }
    }

    // Metodo helper per inserire una nuova variante nel database
    private void inserisciVariante(JsonObject jsonBody, JsonObject jsonResponse) throws Exception {
        int idProdotto = jsonBody.get("idProdotto").getAsInt();
        
        VarianteProdotto v = new VarianteProdotto();
        Prodotto p = new Prodotto();
        p.setIdProdotto(idProdotto);
        v.setProdotto(p);
        
        v.setColore(jsonBody.get("colore").getAsString().trim());
        v.setTaglia(jsonBody.get("taglia").getAsString().trim());
        v.setPrezzo(new BigDecimal(jsonBody.get("prezzo").getAsString()));
        v.setQuantitaMagazzino(jsonBody.get("quantitaMagazzino").getAsInt());

        int nuovoId = varianteDAO.insertVariante(v);
        if (nuovoId > 0) {
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Variante aggiunta con successo.");
            jsonResponse.addProperty("idVariante", nuovoId); // Fondamentale per far funzionare l'eliminazione inline successiva!
        } else {
            throw new Exception("Impossibile salvare la variante nel database.");
        }
    }

    // Metodo helper per modificare una variante esistente nel database
    private void modificaVariante(JsonObject jsonBody, JsonObject jsonResponse) throws Exception {
        VarianteProdotto v = new VarianteProdotto();
        v.setIdVariante(jsonBody.get("idVariante").getAsInt());
        v.setColore(jsonBody.get("colore").getAsString().trim());
        v.setTaglia(jsonBody.get("taglia").getAsString().trim());
        v.setPrezzo(new BigDecimal(jsonBody.get("prezzo").getAsString()));
        v.setQuantitaMagazzino(jsonBody.get("quantitaMagazzino").getAsInt());

        boolean esito = varianteDAO.updateVariante(v);
        if (esito) {
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Variante modificata con successo.");
        } else {
            throw new Exception("Nessuna variante trovata con l'ID specificato.");
        }
    }

    // Metodo helper per eliminare una variante esistente nel database
    private void eliminaVariante(JsonObject jsonBody, JsonObject jsonResponse) throws Exception {
        int idVariante = jsonBody.get("idVariante").getAsInt();
        
        boolean esito = varianteDAO.deleteVariante(idVariante);
        if (esito) {
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Variante eliminata con successo.");
        } else {
            throw new Exception("Impossibile eliminare la variante. Potrebbe non esistere.");
        }
    }

    // Metodo helper per inviare un messaggio di errore in formato JSON al client
    private void inviaErrore(HttpServletResponse response, JsonObject jsonResponse, String messaggio) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("message", messaggio);
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
}