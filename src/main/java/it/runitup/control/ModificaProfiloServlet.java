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
        
        // L'AuthFilter garantisce che l'utente sia loggato.
        // Se ne approfitta per aggiornare i dati dell'utente in sessione con quelli più freschi dal DB, così da evitare discrepanze tra sessione e DB        
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

        request.getRequestDispatcher("/WEB-INF/view/modifica_profilo.jsp").forward(request, response);
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
            // 1. Parsing del corpo della richiesta JSON inviata dalla Fetch
            JsonObject jsonRequest = gson.fromJson(request.getReader(), JsonObject.class);
            
            String nome = jsonRequest.has("nome") ? jsonRequest.get("nome").getAsString().trim() : "";
            String cognome = jsonRequest.has("cognome") ? jsonRequest.get("cognome").getAsString().trim() : "";
            
            // Scomponiamo l'estrazione in accordo con la struttura simmetrica di RegistrazioneServlet
            String prefisso = jsonRequest.has("prefisso") ? jsonRequest.get("prefisso").getAsString().trim() : "";
            String numeroTelefono = jsonRequest.has("telefono") ? jsonRequest.get("telefono").getAsString().trim() : "";
            
            String passwordAttuale = jsonRequest.has("passwordAttuale") ? jsonRequest.get("passwordAttuale").getAsString() : "";
            String nuovaPassword = jsonRequest.has("nuovaPassword") ? jsonRequest.get("nuovaPassword").getAsString() : "";
            String confermaPassword = jsonRequest.has("confermaPassword") ? jsonRequest.get("confermaPassword").getAsString() : "";

            // TODO Centralizzare le regex di validazione in una classe di utilità condivisa, così da evitare duplicazione tra Servlet
            // --- 2. VALIDAZIONE ANAGRAFICA LATO SERVER ---
            if (nome.isEmpty() || nome.length() < 2 || !nome.matches("^[a-zA-ZÀ-ÿ\\s']{2,50}$")) {
                sendError(response, jsonResponse, "Nome non valido. Usa solo lettere (min 2 caratteri).");
                return;
            }

            if (cognome.isEmpty() || cognome.length() < 2 || !cognome.matches("^[a-zA-ZÀ-ÿ\\s']{2,50}$")) {
                sendError(response, jsonResponse, "Cognome non valido. Usa solo lettere (min 2 caratteri).");
                return;
            }

            // --- VALIDAZIONE DEL TELEFONO COMPOSITO ---
            String telefonoDaSalvare = null;
            
            // Se l'utente ha inserito dei caratteri nel campo del numero di telefono
            if (!numeroTelefono.isEmpty()) {
                // 1. Validazione del prefisso (anche se è una select, verifichiamo la Regex per sicurezza)
                if (prefisso.isEmpty() || !prefisso.matches("^\\+[1-9]\\d{1,3}$")) {
                    sendError(response, jsonResponse, "Prefisso internazionale non valido.");
                    return;
                }
                
                // 2. Validazione del numero
                if (!numeroTelefono.matches("^(3\\d{9}|0\\d{6,10})$")) {
                    sendError(response, jsonResponse, "Numero di telefono non valido. Inserisci un numero reale.");
                    return;
                }
                
                // 3. Validazione dell'unicità del numero di telefono (combinazione prefisso + numero)
                String telefonoCompleto = prefisso + " " + numeroTelefono;
                Utente utenteConTelefono = utenteDAO.doRetrieveByTelefono(telefonoCompleto);
                if (utenteConTelefono != null && (utenteConTelefono.getIdUtente() != utenteLoggato.getIdUtente())) {
                    sendError(response, jsonResponse, "Numero di telefono già registrato.");
                    return;
                }

                // Se tutti i controlli sono validi, creiamo la stringa con lo spazio per il DB
                telefonoDaSalvare = telefonoCompleto;
            }
            // Se numeroTelefono è vuoto, telefonoDaSalvare rimane null (coerente con il DB nullable)

            // 3. Recuperiamo l'utente dal DB per i controlli di sicurezza
            Utente utenteDb = utenteDAO.doRetrieveById(utenteLoggato.getIdUtente());
            if (utenteDb == null) {
                sendError(response, jsonResponse, "Impossibile completare l'operazione: utente non trovato.");
                return;
            }

            // Verifichiamo se l'utente desidera cambiare la password
            boolean passwordModificata = !nuovaPassword.isEmpty();

            // --- 4. GESTIONE CAMBIO PASSWORD ---
            if (passwordModificata) {
                if (passwordAttuale.isEmpty()) {
                    sendError(response, jsonResponse, "Inserisci la tua password attuale per confermare le modifiche.");
                    return;
                }

                // Controllo di corrispondenza dell'hash tramite BCrypt
                if (!BCrypt.checkpw(passwordAttuale, utenteDb.getPasswordHash())) {
                    sendError(response, jsonResponse, "La password attuale inserita non è corretta.");
                    return;
                }

                // Verifica uguaglianza formale dei due nuovi campi
                if (!nuovaPassword.equals(confermaPassword)) {
                    sendError(response, jsonResponse, "La nuova password e la password di conferma non coincidono.");
                    return;
                }

                // Verifica robustezza minima (es. almeno 8 caratteri)
                if (nuovaPassword.length() < 8) {
                    sendError(response, jsonResponse, "La nuova password deve contenere almeno 8 caratteri.");
                    return;
                }

                // Generiamo il nuovo hash sicuro
                String nuovoHash = BCrypt.hashpw(nuovaPassword, BCrypt.gensalt());
                utenteDb.setPasswordHash(nuovoHash);
                
            }

            // --- 5. AGGIORNAMENTO DATI ANAGRAFICI ---
            utenteDb.setNome(nome);
            utenteDb.setCognome(cognome);
            utenteDb.setTelefono(telefonoDaSalvare); // Iniettiamo la stringa formattata o null
            
            if (passwordModificata) {
                utenteDAO.doUpdateCompleto(utenteDb);
            } else {
                utenteDAO.doUpdateAnagrafica(utenteDb);
            }

            // --- 6. ALLINEAMENTO SESSIONE ---
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

        // Output del JSON finale verso il client
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