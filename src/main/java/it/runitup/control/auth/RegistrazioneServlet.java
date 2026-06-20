package it.runitup.control.auth;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.mindrot.jbcrypt.BCrypt;

import it.runitup.model.bean.Utente;
import it.runitup.model.dao.UtenteDAO;

@WebServlet("/registrazione")
public class RegistrazioneServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private UtenteDAO utenteDAO = new UtenteDAO();

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/view/auth/registrazione.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Gson gson = new Gson();
        JsonObject jsonRequest = gson.fromJson(request.getReader(), JsonObject.class);
        Map<String, Object> jsonResponse = new HashMap<>();

        try {
            
            // Si ottengono i parametri dalla richiesta JSON e si effettuano normalizzazione e trimming
            String nome = jsonRequest.has("nome") ? jsonRequest.get("nome").getAsString().trim() : "";
            String cognome = jsonRequest.has("cognome") ? jsonRequest.get("cognome").getAsString().trim() : "";
            String email = jsonRequest.has("email") ? jsonRequest.get("email").getAsString().trim().toLowerCase() : "";
            String plainPassword = jsonRequest.has("password") ? jsonRequest.get("password").getAsString() : "";
            
            String prefisso = jsonRequest.has("prefisso") ? jsonRequest.get("prefisso").getAsString().trim() : "";
            String numero = jsonRequest.has("telefono") ? jsonRequest.get("telefono").getAsString().trim() : "";

            // Validazione dei parametri
            String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
            String phoneRegex = "^(3\\d{9}|0\\d{6,10})$";
            String prefissoRegex = "^\\+[1-9]\\d{1,3}$";

            if (nome.length() < 2 || nome.length() > 50 || 
                cognome.length() < 2 || cognome.length() > 50) {
                sendError(response, jsonResponse, gson, "Nome e cognome devono avere tra 2 e 50 caratteri.");
                return;
            }
            if (email.length() > 100 || !email.matches(emailRegex)) {
                sendError(response, jsonResponse, gson, "Formato email non valido o troppo lungo.");
                return;
            }
            if (plainPassword.length() < 8 || plainPassword.length() > 50) {
                sendError(response, jsonResponse, gson, "La password deve essere compresa tra 8 e 50 caratteri.");
                return;
            }
            if (!prefisso.isEmpty() && !prefisso.matches(prefissoRegex)) {
                sendError(response, jsonResponse, gson, "Formato prefisso non valido.");
                return;
            }
            if (!numero.isEmpty() && (numero.length() > 15 || !numero.matches(phoneRegex))) {
                sendError(response, jsonResponse, gson, "Formato telefono non valido.");
                return;
            }

            // Doppio controllo di sicurezza lato server (non ci fidiamo solo di JS)
            if (utenteDAO.doRetrieveByEmail(email) != null) {
                sendError(response, jsonResponse, gson, "Email già registrata.");
                return;
            }

            // Controllo sull'esistenza del numero di telefono solo se l'utente ha inserito un numero
            if (!numero.isEmpty() && utenteDAO.doRetrieveByTelefono(prefisso + " " + numero) != null) {
                sendError(response, jsonResponse, gson, "Telefono già registrato.");
                return;
            }

            Utente nuovoUtente = new Utente();
            nuovoUtente.setNome(nome);
            nuovoUtente.setCognome(cognome);
            nuovoUtente.setEmail(email);
            
            // Hashing della password
            nuovoUtente.setPasswordHash(BCrypt.hashpw(plainPassword, BCrypt.gensalt()));

            // Gestione Telefono (Prefisso + Numero)
            if (!numero.isEmpty()) {
                nuovoUtente.setTelefono(prefisso + " " + numero);
            } else {
                nuovoUtente.setTelefono(null); // Nel DB è nullable
            }

            nuovoUtente.setAdmin(false);

            // Salvataggio
            utenteDAO.doSave(nuovoUtente);

            jsonResponse.put("success", true);
            jsonResponse.put("redirectUrl", request.getContextPath() + "/login");

        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Errore durante la registrazione.");
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private void sendError(HttpServletResponse response, Map<String, Object> jsonResponse, Gson gson, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        response.getWriter().write(gson.toJson(jsonResponse));
    }

}