package it.runitup.filter;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.runitup.model.bean.Ordine;
import it.runitup.model.bean.Utente;
import it.runitup.model.dao.OrdineDAO;

// Questo filtro si applica alle rotte in cui si cerca di accedere a uno specifico ordine
@WebFilter({"/ordini" ,"/ordini/*", "/stampa_fattura/*", "/fattura_pdf/*"})
public class OrderOwnerFilter implements Filter {

    private OrdineDAO ordineDAO;

    public void init(FilterConfig fConfig) throws ServletException {
        ordineDAO = new OrdineDAO();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Recuperiamo l'ID dalla query string (?id=...)
        String idParam = httpRequest.getParameter("id");

        // Se non c'è un ID (es. l'utente sta solo visitando la lista /ordini), lasciamo passare.
        if (idParam == null || idParam.trim().isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        // Recuperiamo l'utente dalla sessione (L'AuthFilter dovrebbe averlo già garantito)
        HttpSession session = httpRequest.getSession(false);
        Utente utenteLoggato = (session != null) ? (Utente) session.getAttribute("utenteLoggato") : null;

        if (utenteLoggato == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione scaduta o utente non loggato.");
            return;
        }

        try {
            int idOrdine = Integer.parseInt(idParam.trim());
            Ordine ordine = ordineDAO.getOrdineById(idOrdine);

            // 1. L'ordine esiste?
            if (ordine == null) {
                httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Ordine non trovato.");
                return;
            }

            // 2. Controllo IDOR: L'utente loggato è il proprietario o è un admin?
            if (!utenteLoggato.isAdmin() && ordine.getUtente().getIdUtente() != utenteLoggato.getIdUtente()) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: non sei il proprietario di questo documento.");
                return;
            }

            // Passiamo l'ordine validato alla Servlet tramite attributo della request, così evitiamo di doverlo recuperare di nuovo in Servlet.
            httpRequest.setAttribute("ordineValidato", ordine);

            // Lasciamo proseguire la richiesta verso la Servlet
            chain.doFilter(request, response);

        } catch (NumberFormatException e) {
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato ID ordine non valido.");
        } catch (SQLException e) {
            e.printStackTrace();
            httpResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel database durante la validazione dell'ordine.");
        }
    }

    public void destroy() {}
}