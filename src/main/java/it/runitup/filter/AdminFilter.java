package it.runitup.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.runitup.model.bean.Utente;

// Filtro per proteggere le pagine dell'area amministrativa, assicurandosi che solo gli utenti con ruolo admin possano accedervi.
@WebFilter({"/admin", "/admin/*"})
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        
        Utente utente = null;
        if (session != null) {
            utente = (Utente) session.getAttribute("utenteLoggato");
        }

        // Si controlla se l'utente è loggato e ha il ruolo di admin
        if (utente != null && utente.isAdmin()) {

            String path = httpRequest.getServletPath();

            // Determina quale pagina dell'area admin è attiva per evidenziare il menu laterale (funzionalità di UI/UX)
            String activePage = "prodotti"; // default
            if (path.contains("/admin/ordini")) {
                activePage = "ordini";
            }

            // Imposta l'attributo "activePage" nella richiesta per essere utilizzato nella JSP dell'area admin
            httpRequest.setAttribute("activePage", activePage);

            // L'utente è un admin, quindi si prosegue con la catena di filtri o con la servlet richiesta
            chain.doFilter(request, response);
        } else {
            // Tentativo di accesso abusivo, se l'utente non è loggato, reindirizza alla pagina di login, altrimenti si restituirà un errore 403 (Forbidden)
            if (utente == null) {
                // L'utente non è loggato, quindi salviamo l'URL di destinazione e reindirizziamo alla pagina di login
                String requestURI = httpRequest.getRequestURI();
                String queryString = httpRequest.getQueryString();
                
                String urlDiDestinazione = requestURI;
                if (queryString != null) {
                    urlDiDestinazione += "?" + queryString;
                }

                HttpSession newSession = httpRequest.getSession(true);
                newSession.setAttribute("redirectDopoLogin", urlDiDestinazione);

                // Reindirizza alla pagina di login con un parametro per indicare che l'accesso è stato negato
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirected=true");
            }else {
                // L'utente è loggato ma non ha il ruolo di admin, quindi restituiamo un errore 403 (Forbidden)
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: area riservata agli amministratori.");
                return;
            }
        }
    }
}