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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

import it.runitup.control.service.CarrelloManager;
import it.runitup.model.bean.Utente;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.UtenteDAO;
import it.runitup.model.dao.VarianteDAO;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UtenteDAO utenteDAO = new UtenteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // Controlliamo se l'utente è già loggato. Se sì, lo reindirizziamo alla Home per evitare che acceda nuovamente alla pagina di login.
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute("utenteLoggato") != null) {
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }

        // Logica proattiva per salvare l'URL di destinazione prima del login, così da poter reindirizzare l'utente dopo il login
        if (session == null || session.getAttribute("redirectDopoLogin") == null) {
            
            // Recuperiamo l'header "Referer" che indica la pagina da cui l'utente proveniva prima di arrivare alla pagina di login
            String referer = request.getHeader("Referer");

            // Evitiamo di salvare l'URL di login o registrazione come destinazione per evitare di creare loop infiniti
            if (referer != null && !referer.contains("/login") && !referer.contains("/registrazione")) {
                
                // Creiamo la sessione se non esiste e salviamo l'URL di destinazione
                if (session == null) {
                    session = request.getSession(true);
                }
                session.setAttribute("redirectDopoLogin", referer);
            }
        }

        // Mostriamo la pagina di login
        request.getRequestDispatcher("/WEB-INF/view/login.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        JsonObject jsonRequest = gson.fromJson(request.getReader(), JsonObject.class);
        
        
        Map<String, Object> jsonResponse = new HashMap<>();
        
        try {
            // NORMALIZZAZIONE: Email sempre in lowercase
            String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString().trim().toLowerCase() : "";
            String password = jsonRequest.has("password") ? jsonRequest.get("password").getAsString() : "";
            
            if (email.isEmpty() || email.length() > 100 || password.isEmpty() || password.length() > 50) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.put("success", false);
                jsonResponse.put("message", "Credenziali non valide."); // Messaggio generico per non dare indizi ad un attaccante
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }
            
            Utente utente = utenteDAO.doRetrieveByEmail(email);

            // Verifica esistenza utente E verifica password con BCrypt
            if (utente != null && BCrypt.checkpw(password, utente.getPasswordHash())) {
                
                HttpSession session = request.getSession(true);
                session.setAttribute("utenteLoggato", utente);

                // Al momento del login, uniamo il carrello ospite con quello reale dell'utente loggato
                CarrelloDAO carrelloDAO = new CarrelloDAO();
                VarianteDAO varianteDAO = new VarianteDAO();
                CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
                
                try {
                    cartManager.mergeCarrello();
                } catch (SQLException e) {
                    // Se c'è un errore durante la fusione dei carrelli, logghiamo l'errore ma non blocchiamo il login
                    e.printStackTrace(); 
                }
                
                // Cattura l'eventuale URL di destinazione salvato dal filtro di autenticazione (AuthFilter) prima del login
                String redirectDopoLogin = (String) session.getAttribute("redirectDopoLogin");
                
                if (redirectDopoLogin != null) {
                    // Se esiste un URL di destinazione, lo include nella risposta JSON per permettere al client di fare un redirect dinamico dopo il login
                    jsonResponse.put("redirectUrl", redirectDopoLogin);
                    // Pulisce l'attributo dalla sessione per evitare redirect indesiderati in futuro
                    session.removeAttribute("redirectDopoLogin"); 
                } else {
                    // Comportamento standard: andiamo alla Home
                    jsonResponse.put("redirectUrl", request.getContextPath() + "/"); 
                }


                jsonResponse.put("success", true);
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