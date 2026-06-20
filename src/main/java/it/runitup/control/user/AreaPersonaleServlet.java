package it.runitup.control.user;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.runitup.model.bean.Utente;
import it.runitup.model.dao.UtenteDAO;

@WebServlet("/area_personale")
public class AreaPersonaleServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final UtenteDAO utenteDAO = new UtenteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recuperiamo la sessione e l'utente loggato dalla sessione, garantita dalla servlet di autenticazione.
        HttpSession session = request.getSession(false);
        Utente utenteInSessione = (Utente) session.getAttribute("utenteLoggato");

        try {
            // Recupero dell'utente aggiornato dal database per garantire che le informazioni siano sempre aggiornate.
            Utente utenteAggiornato = utenteDAO.doRetrieveById(utenteInSessione.getIdUtente());

            if (utenteAggiornato != null) {
                // Aggiorniamo l'utente loggato nella sessione con le informazioni più recenti dal database.
                session.setAttribute("utenteLoggato", utenteAggiornato);
                
            } else {
                // Se l'utente non esiste più nel database (ad esempio, se è stato eliminato), invalidiamo la sessione e reindirizziamo alla pagina di login con un messaggio di errore.
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?error=account_deleted");
                return;
            }

            // Inoltriamo la richiesta alla JSP dell'area personale, che mostrerà le informazioni aggiornate dell'utente.
            request.getRequestDispatcher("/WEB-INF/view/user/area_personale.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento del profilo utente.");
        }
    }

    // doPost: Reindirizza le richieste POST alla gestione GET per la visualizzazione dell'area personale
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}