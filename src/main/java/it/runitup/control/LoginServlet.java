package it.runitup.control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mindrot.jbcrypt.BCrypt;
import com.google.gson.Gson;

import it.runitup.model.bean.Utente;
import it.runitup.model.dao.UtenteDAO;

// Servlet per gestire le richieste di login degli utenti
@WebServlet(name = "LoginServlet", value = "/login")
public class LoginServlet extends HttpServlet {

    // Gson per convertire le risposte in formato JSON
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Si imposta il tipo di risposta JSON e la codifica dei caratteri
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Parametri dalla richiesta di login
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        // Mappa che conterrà i dati della risposta JSON
        Map<String, String> jsonResponse = new HashMap<>();
        
        // Si verifica che i campi email e password siano presenti e non vuoti
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST); // Status 400
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Campi obbligatori mancanti.");
            
            // Si scrive la risposta JSON nel corpo della risposta
            response.getWriter().write(gson.toJson(jsonResponse));
            return;
        }
        
        UtenteDAO utenteDAO = new UtenteDAO();
        
        try {
            Utente utenteTrovato = utenteDAO.doRetrieveByEmail(email);
            
            if (utenteTrovato != null && BCrypt.checkpw(password, utenteTrovato.getPasswordHash())) {
                
                // Login riuscito, si crea la sessione e si salva l'utente loggato
                HttpSession session = request.getSession();
                session.setAttribute("utente", utenteTrovato);
                
                // TODO: Gestire il merge del carrello del Guest nel Database 
                // non appena avremo implementato la logica del Carrello.
                
                jsonResponse.put("status", "success");
                jsonResponse.put("message", "Login effettuato con successo.");
                
                response.getWriter().write(gson.toJson(jsonResponse));
                
            } else {
                // Login fallito: email non trovata o password errata
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Status 401
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "Email o password errati.");
                response.getWriter().write(gson.toJson(jsonResponse));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // Status 500
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Errore interno del server, riprova più tardi.");
            
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        // Verifica se l'utente è già loggato controllando la sessione
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("utente") != null) {
            // Se è loggato, reindirizza alla pagina principale
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // Se non è loggato, inoltra la richiesta alla view nascosta
        request.getRequestDispatcher("/WEB-INF/view/login.jsp").forward(request, response);
    }
}