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
@WebFilter({"/checkout", "/area_personale/*", "/ordini/*", "/stampa_fattura/*", "/fattura_pdf/*"})
public class AuthFilter implements Filter {

    public void init(FilterConfig fConfig) throws ServletException {
        // Nessuna inizializzazione particolare richiesta
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        // Cast necessario perché Filter lavora a un livello più astratto dell'HTTP
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);

        // Controllo: L'utente è loggato?
        boolean isLoggedIn = (session != null && session.getAttribute("utenteLoggato") != null);

        if (isLoggedIn) {
            // Utente autorizzato: lascia procedere la richiesta verso la Servlet o JSP di destinazione
            chain.doFilter(request, response);
        } else {
            // 1. Catturiamo l'URL esatto che stava cercando di visitare (inclusi eventuali parametri, es: ?id=5)
            String requestURI = httpRequest.getRequestURI();
            String queryString = httpRequest.getQueryString();
            
            String urlDiDestinazione = requestURI;
            if (queryString != null) {
                urlDiDestinazione += "?" + queryString;
            }

            // 2. Salviamo questo URL nella sessione (creandola se non esiste)
            HttpSession newSession = httpRequest.getSession(true);
            newSession.setAttribute("redirectDopoLogin", urlDiDestinazione);

            // 3. Reindirizziamo l'utente alla pagina di login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?redirected=true");
        }
    }

    public void destroy() {
        // Pulizia risorse se necessario (non in questo caso)
    }
}