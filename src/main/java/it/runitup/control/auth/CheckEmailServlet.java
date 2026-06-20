package it.runitup.control.auth;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.runitup.model.dao.UtenteDAO;

@WebServlet("/check-email")
public class CheckEmailServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UtenteDAO utenteDAO = new UtenteDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String email = request.getParameter("email");
        JsonObject jsonResponse = new JsonObject();
        
        if (email == null || email.trim().isEmpty()) {
            jsonResponse.addProperty("esiste", false);
        } else {
            try {
                // Se la query restituisce un utente, l'email esiste già
                boolean esiste = (utenteDAO.doRetrieveByEmail(email.toLowerCase()) != null);
                jsonResponse.addProperty("esiste", esiste);
            } catch (Exception e) {
                jsonResponse.addProperty("esiste", false); // In caso di errore DB, non blocchiamo il front-end, lo fermerà la RegistrazioneServlet
            }
        }
        
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
}