package it.runitup.filter;

import java.io.IOException;

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

// Filtro di autenticazione per proteggere le pagine riservate agli utenti loggati
@WebFilter({"/checkout", "/area_personale", "/area_personale/*", "/ordini", "/ordini/*", "/stampa_fattura/*", "/fattura_pdf/*"})
public class AuthFilter implements Filter {

    public void init(FilterConfig fConfig) throws ServletException {
        // Nessuna inizializzazione particolare richiesta
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        // Cast delle richieste e risposte a HttpServletRequest e HttpServletResponse per poter accedere a metodi specifici
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        // Si controlla se l'utente è loggato verificando la presenza dell'attributo "utenteLoggato" nella sessione
        boolean isLoggedIn = (session != null && session.getAttribute("utenteLoggato") != null);

        if (isLoggedIn) {
            // Se l'utente è loggato, si prosegue con la catena di filtri o con la servlet richiesta
            chain.doFilter(request, response);
        } else {
            // L'utente non è loggato, quindi salviamo l'URL di destinazione e reindirizziamo alla pagina di login
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            
            String urlDiDestinazione = requestURI;
            if (queryString != null) {
                urlDiDestinazione += "?" + queryString;
            }

            // Creiamo una nuova sessione se non esiste e salviamo l'URL di destinazione per poter reindirizzare l'utente dopo il login
            HttpSession newSession = httpRequest.getSession(true);
            newSession.setAttribute("redirectDopoLogin", urlDiDestinazione);

            // Reindirizziamo l'utente alla pagina di login con un parametro per indicare che l'accesso è stato negato
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirected=true");
        }
    }

    public void destroy() {
        // Nessuna risorsa da rilasciare
    }
}