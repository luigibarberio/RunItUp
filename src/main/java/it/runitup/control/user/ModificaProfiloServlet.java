package it.runitup.control.user;

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

import it.runitup.model.bean.Utente;
import it.runitup.model.dao.UtenteDAO;

@WebServlet("/area_personale/modifica")
public class ModificaProfiloServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final UtenteDAO utenteDAO = new UtenteDAO();
    private final Gson gson = new Gson();

    // Il metodo doGet serve a mostrare la pagina di modifica del profilo, con i dati precompilati
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recupero della sessione e dell'utente loggato dalla sessione, garantita dalla servlet di autenticazione.
        try {
            HttpSession session = request.getSession(false);
            Utente utenteLoggato = (Utente) session.getAttribute("utenteLoggato");
            Utente utenteDb = utenteDAO.doRetrieveById(utenteLoggato.getIdUtente());
            if (utenteDb != null) {
                session.setAttribute("utenteLoggato", utenteDb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento dei dati del profilo.");
            return;
        }

        request.getRequestDispatcher("/WEB-INF/view/user/modifica_profilo.jsp").forward(request, response);
    }

    // Il metodo doPost gestisce sia la modifica dell'anagrafica che il cambio password, con validazioni e controlli di sicurezza
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> jsonResponse = new HashMap<>();
        HttpSession session = request.getSession(false);
        Utente utenteLoggato = (Utente) session.getAttribute("utenteLoggato");

        try {
            // Recupero dei dati JSON dalla richiesta e parsing in JsonObject
            JsonObject jsonRequest = gson.fromJson(request.getReader(), JsonObject.class);
            
            String nome = jsonRequest.has("nome") ? jsonRequest.get("nome").getAsString().trim() : "";
            String cognome = jsonRequest.has("cognome") ? jsonRequest.get("cognome").getAsString().trim() : "";
            
            // Recupero del prefisso e del numero di telefono, con trimming e normalizzazione
            String prefisso = jsonRequest.has("prefisso") ? jsonRequest.get("prefisso").getAsString().trim() : "";
            String numeroTelefono = jsonRequest.has("telefono") ? jsonRequest.get("telefono").getAsString().trim() : "";
            
            // Recupero delle password per il cambio password, se l'utente desidera modificarla
            String passwordAttuale = jsonRequest.has("passwordAttuale") ? jsonRequest.get("passwordAttuale").getAsString() : "";
            String nuovaPassword = jsonRequest.has("nuovaPassword") ? jsonRequest.get("nuovaPassword").getAsString() : "";
            String confermaPassword = jsonRequest.has("confermaPassword") ? jsonRequest.get("confermaPassword").getAsString() : "";

            // Validazione dei campi anagrafici: nome e cognome devono essere non vuoti, avere almeno 2 caratteri e contenere solo lettere (anche accentate) e spazi
            if (nome.isEmpty() || nome.length() < 2 || !nome.matches("^[a-zA-ZÀ-ÿ\\s']{2,50}$")) {
                sendError(response, jsonResponse, "Nome non valido. Usa solo lettere (min 2 caratteri).");
                return;
            }

            if (cognome.isEmpty() || cognome.length() < 2 || !cognome.matches("^[a-zA-ZÀ-ÿ\\s']{2,50}$")) {
                sendError(response, jsonResponse, "Cognome non valido. Usa solo lettere (min 2 caratteri).");
                return;
            }

            // Validazione del numero di telefono solo se l'utente ha inserito un numero
            String telefonoDaSalvare = null;
            
            if (!numeroTelefono.isEmpty()) {
                // Validazione del prefisso (anche se è una select, verifichiamo la Regex per sicurezza)
                if (prefisso.isEmpty() || !prefisso.matches("^\\+[1-9]\\d{1,3}$")) {
                    sendError(response, jsonResponse, "Prefisso internazionale non valido.");
                    return;
                }
                
                // Validazione del numero di telefono (deve essere un numero reale, es. 3XXXXXXXXX o 0XXXXXXX)
                if (!numeroTelefono.matches("^(3\\d{9}|0\\d{6,10})$")) {
                    sendError(response, jsonResponse, "Numero di telefono non valido. Inserisci un numero reale.");
                    return;
                }
                
                // Validazione dell'unicità del numero di telefono (combinazione prefisso + numero)
                String telefonoCompleto = prefisso + " " + numeroTelefono;
                Utente utenteConTelefono = utenteDAO.doRetrieveByTelefono(telefonoCompleto);
                if (utenteConTelefono != null && (utenteConTelefono.getIdUtente() != utenteLoggato.getIdUtente())) {
                    sendError(response, jsonResponse, "Numero di telefono già registrato.");
                    return;
                }

                // Se tutte le validazioni passano, si salva il numero di telefono formattato
                telefonoDaSalvare = telefonoCompleto;
            } else {
                // Se il numero di telefono è vuoto, si salva come null nel database
                telefonoDaSalvare = null;
            }

            // Recupero dell'utente dal database per garantire che le informazioni siano aggiornate
            Utente utenteDb = utenteDAO.doRetrieveById(utenteLoggato.getIdUtente());
            if (utenteDb == null) {
                sendError(response, jsonResponse, "Impossibile completare l'operazione: utente non trovato.");
                return;
            }

            // Verifica se l'utente ha richiesto un cambio password: se il campo nuovaPassword non è vuoto, allora l'utente vuole cambiare la password
            boolean passwordModificata = !nuovaPassword.isEmpty();

            // Gestione del cambio password con controlli di sicurezza: verifica della password attuale, uguaglianza delle nuove password e robustezza minima
            if (passwordModificata) {
                if (passwordAttuale.isEmpty()) {
                    sendError(response, jsonResponse, "Inserisci la tua password attuale per confermare le modifiche.");
                    return;
                }

                // Verifica della password attuale confrontando l'hash salvato nel database con la password fornita dall'utente
                if (!BCrypt.checkpw(passwordAttuale, utenteDb.getPasswordHash())) {
                    sendError(response, jsonResponse, "La password attuale inserita non è corretta.");
                    return;
                }

                // Verifica che la nuova password e la conferma coincidano e che rispettino i requisiti di robustezza (almeno 8 caratteri)
                if (!nuovaPassword.equals(confermaPassword)) {
                    sendError(response, jsonResponse, "La nuova password e la password di conferma non coincidono.");
                    return;
                }
                if (nuovaPassword.length() < 8) {
                    sendError(response, jsonResponse, "La nuova password deve contenere almeno 8 caratteri.");
                    return;
                }

                // Se tutte le verifiche passano, si genera l'hash della nuova password e si aggiorna l'oggetto utente
                String nuovoHash = BCrypt.hashpw(nuovaPassword, BCrypt.gensalt());
                utenteDb.setPasswordHash(nuovoHash);
                
            }

            // Aggiornamento dei dati anagrafici dell'utente nel database: nome, cognome e telefono
            utenteDb.setNome(nome);
            utenteDb.setCognome(cognome);
            utenteDb.setTelefono(telefonoDaSalvare); // Iniettiamo la stringa formattata o null
            
            if (passwordModificata) {
                utenteDAO.doUpdateCompleto(utenteDb);
            } else {
                utenteDAO.doUpdateAnagrafica(utenteDb);
            }

            // Aggiornamento dell'utente loggato nella sessione con le informazioni più recenti dal database
            session.setAttribute("utenteLoggato", utenteDb);

            jsonResponse.put("success", true);
            jsonResponse.put("message", "Profilo aggiornato con successo!");
            jsonResponse.put("redirectUrl", request.getContextPath() + "/area_personale");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Errore nel database durante l'aggiornamento dei dati.");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("message", "Si è verificato un errore imprevisto sul server.");
        }

        // Output della risposta JSON al client, che conterrà lo stato dell'operazione e eventuali messaggi di errore o successo
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    // Metodo helper per interrompere il flusso inviando un errore formattato (400 Bad Request)
    private void sendError(HttpServletResponse response, Map<String, Object> jsonResponse, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        jsonResponse.put("success", false);
        jsonResponse.put("message", message);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}