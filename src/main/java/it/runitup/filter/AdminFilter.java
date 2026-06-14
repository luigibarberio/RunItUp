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

// Protegge l'intera area amministrativa (Servlet e JSP mappate sotto /admin/)
@WebFilter("/admin/*")
public class AdminFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        
        Utente utente = null;
        if (session != null) {
            utente = (Utente) session.getAttribute("utente");
        }

        // Hard Check: L'utente deve esistere e il metodo isAdmin() deve restituire TRUE
        if (utente != null && utente.isAdmin()) {
            // Autorizzazione confermata, procedi verso la risorsa admin richiesta
            chain.doFilter(request, response);
        } else {
            // Tentativo di accesso abusivo, se l'utente non è loggato, reindirizza alla pagina di login, altrimenti si restituirà un errore 403 (Forbidden)
            if (utente == null) {
                // Salviamo l'URL richiesto per un eventuale redirect dopo il login
                String requestURI = httpRequest.getRequestURI();
                String queryString = httpRequest.getQueryString();
                
                String urlDiDestinazione = requestURI;
                if (queryString != null) {
                    urlDiDestinazione += "?" + queryString;
                }

                HttpSession newSession = httpRequest.getSession(true);
                newSession.setAttribute("redirectDopoLogin", urlDiDestinazione);

                // Reindirizziamo l'utente alla pagina di login
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
            }else {
                // L'utente è loggato ma non ha i permessi admin, quindi restituiamo un errore 403
                httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso negato: area riservata agli amministratori.");
                return;
            }
        }
    }
}