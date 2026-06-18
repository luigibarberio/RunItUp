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

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riutilizziamo Gson per la conformità con il resto del Front Controller (LoginServlet)
    private final Gson gson = new Gson();

    // Endpoint Sincrono
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate(); // Distrugge la sessione corrente
        }
        
        // Si verifica se esiste un url di referrer per reindirizzare l'utente alla pagina precedente al logout, se esiste
        String referrer = request.getHeader("Referer");
        if (referrer != null && !referrer.contains("/logout")) { // Evitiamo di creare un loop di logout
            response.sendRedirect(referrer);
            return;
        } else {        
            // Se non c'è un referrer valido, reindirizziamo alla home page
            response.sendRedirect(request.getContextPath() + "/");
            return;
        }
    }

    // Endpoint Asincrono: Intercetta la richiesta POST inviata dal file JS "logout.js" quando l'utente clicca sul bottone "Logout" nella dashboard
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Impostiamo i corretti header per una risposta JSON in UTF-8
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            // Recuperiamo la sessione corrente senza crearne una nuova (false)
            HttpSession session = request.getSession(false);
            
            if (session != null) {
                // Il metodo invalidate pulisce la mappa della sessione e disconnette l'utente
                session.invalidate();
            }

            // Prepariamo l'oggetto di successo mappato sulle aspettative del file JS
            jsonResponse.put("success", true);
            jsonResponse.put("redirectUrl", request.getContextPath() + "/");
            jsonResponse.put("message", "Disconnessione completata con successo.");

        } catch (Exception e) {
            e.printStackTrace();
            // In caso di errore anomalo del server, rispondiamo con un codice 500
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Si è verificato un errore interno durante il logout.");
        }

        // Scriviamo il payload JSON direttamente sul body della risposta HTTP
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}