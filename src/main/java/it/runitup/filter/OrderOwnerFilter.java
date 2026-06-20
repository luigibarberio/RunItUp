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

// Filtro per proteggere le pagine relative agli ordini, assicurandosi che solo il proprietario dell'ordine o un admin possa accedervi.
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

        // Recupero dell'ID dell'ordine dalla query string o dai parametri della richiesta
        String idParam = httpRequest.getParameter("id");

        // Se non c'è un ID (es. l'utente sta solo visitando la lista /ordini), per cui si lascia proseguire la richiesta senza ulteriori controlli
        if (idParam == null || idParam.trim().isEmpty()) {
            chain.doFilter(request, response);
            return;
        }

        // Recupero dell'utente loggato dalla sessione (si assume che l'utente sia già autenticato grazie al filtro AuthFilter)
        HttpSession session = httpRequest.getSession(false);
        Utente utenteLoggato = (session != null) ? (Utente) session.getAttribute("utenteLoggato") : null;

        if (utenteLoggato == null) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sessione scaduta o utente non loggato.");
            return;
        }

        try {
            int idOrdine = Integer.parseInt(idParam.trim());
            Ordine ordine = ordineDAO.getOrdineById(idOrdine);

            // Controllo sull'esistenza dell'ordine, in caso contrario viene restituito un errore 404 (Not Found)
            if (ordine == null) {
                httpResponse.sendError(HttpServletResponse.SC_NOT_FOUND, "Ordine non trovato.");
                return;
            }

            // Controllo se l'utente loggato è un admin o il proprietario dell'ordine
            if (!utenteLoggato.isAdmin() && ordine.getUtente().getIdUtente() != utenteLoggato.getIdUtente()) {
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: non sei il proprietario di questo documento.");
                return;
            }

            // Se l'utente è autorizzato, si può procedere con la richiesta, eventualmente aggiungendo l'ordine come attributo della richiesta per un uso successivo nella Servlet o JSP
            httpRequest.setAttribute("ordineValidato", ordine);

            // Si prosegue con la catena di filtri o con la servlet richiesta
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