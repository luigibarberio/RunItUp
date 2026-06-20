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
        
        // 1. L'AuthFilter garantisce che l'utenteLoggato sia presente.
        HttpSession session = request.getSession(false);
        Utente utenteInSessione = (Utente) session.getAttribute("utenteLoggato");

        try {
            // 2. Recuperiamo i dati "freschi" dal DB 
            // in caso siano stati modificati da un altro dispositivo o dall'Admin.
            Utente utenteAggiornato = utenteDAO.doRetrieveById(utenteInSessione.getIdUtente());

            if (utenteAggiornato != null) {
                // 3. Sincronizziamo la sessione sovrascrivendo il vecchio oggetto
                session.setAttribute("utenteLoggato", utenteAggiornato);
                
            } else {
                // Caso limite (ma da gestire per il 30 e lode): l'account è stato cancellato dal DB mentre l'utente era loggato.
                session.invalidate();
                response.sendRedirect(request.getContextPath() + "/login?error=account_deleted");
                return;
            }

            // 4. Forward verso la View (La nostra dashboard a "scatole")
            request.getRequestDispatcher("/WEB-INF/view/user/area_personale.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento del profilo utente.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // La dashboard si apre in GET.
        doGet(request, response);
    }
}