package it.runitup.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

import it.runitup.model.bean.Utente;
import it.runitup.model.dao.UtenteDAO;
// TODO import it.runitup.model.bean.Carrello;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UtenteDAO utenteDAO = new UtenteDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/login.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        JsonObject jsonRequest = gson.fromJson(request.getReader(), JsonObject.class);
        
        // NORMALIZZAZIONE: Email sempre in lowercase
        String email = jsonRequest.get("email").getAsString().toLowerCase();
        String password = jsonRequest.get("password").getAsString();

        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            Utente utente = utenteDAO.doRetrieveByEmail(email);

            // Verifica esistenza utente E verifica password con BCrypt
            if (utente != null && BCrypt.checkpw(password, utente.getPasswordHash())) {
                
                HttpSession session = request.getSession(true);
                session.setAttribute("utenteLoggato", utente);

                // GESTIONE CARRELLO (Merge logic)
                // Carrello carrelloSessione = (Carrello) session.getAttribute("carrello");
                // TODO: CarrelloDAO carrelloDAO = new CarrelloDAO();
                // Carrello carrelloDB = carrelloDAO.doRetrieveByUtente(utente.getIdUtente());
                // carrelloSessione.merge(carrelloDB); 
                
                jsonResponse.put("success", true);
                jsonResponse.put("redirectUrl", request.getContextPath() + "/"); 
            } else {
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Email o password errata.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Errore durante il login.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}