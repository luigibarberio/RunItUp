package it.runitup.control;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.runitup.model.bean.Ordine;
import it.runitup.model.bean.Utente;
import it.runitup.model.dao.OrdineDAO;
import it.runitup.model.dao.UtenteDAO;

@WebServlet("/admin/ordini")
public class AdminOrdiniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final OrdineDAO ordineDAO = new OrdineDAO();
    private final UtenteDAO utenteDAO = new UtenteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Lettura dei parametri di filtro dalla query string (?dal=...&al=...&emailCliente=...)
        String dalStr = request.getParameter("dal");
        String alStr = request.getParameter("al");
        String emailCliente = request.getParameter("emailCliente");

        LocalDate dal = null;
        LocalDate al = null;
        Integer idCliente = null;

        try {
            // 2. Parsifichiamo le date (se l'admin le ha inserite)
            if (dalStr != null && !dalStr.trim().isEmpty()) {
                dal = LocalDate.parse(dalStr.trim());
            }
            if (alStr != null && !alStr.trim().isEmpty()) {
                al = LocalDate.parse(alStr.trim());
            }

            // 3. Risolviamo l'email del cliente nel suo ID reale (per la query in DB)
            if (emailCliente != null && !emailCliente.trim().isEmpty()) {
                Utente u = utenteDAO.doRetrieveByEmail(emailCliente.trim());
                if (u != null) {
                    idCliente = u.getIdUtente();
                } else {
                    // Se l'admin digita un'email che non esiste nel datalist,
                    // usiamo un ID fittizio (-1) per far sì che la query restituisca un risultato vuoto,
                    // e impostiamo un messaggio di errore soft.
                    idCliente = -1; 
                    request.setAttribute("erroreFiltro", "Nessun cliente trovato con l'email: " + emailCliente);
                }
            }

            // 4. Esecuzione delle Query tramite i DAO
            List<Utente> listaClienti = utenteDAO.doRetrieveAll(); // Serve per popolare il <datalist>
            List<Ordine> ordiniAdmin = ordineDAO.doRetrieveAllAdmin(dal, al, idCliente); // Risultato del filtro

            // 5. Setup degli attributi per la JSP
            request.setAttribute("listaClienti", listaClienti);
            request.setAttribute("ordiniAdmin", ordiniAdmin);
            
            // Rimandiamo indietro i parametri per mantenere compilato il form di filtro (UX)
            request.setAttribute("filtroDal", dalStr != null ? dalStr : "");
            request.setAttribute("filtroAl", alStr != null ? alStr : "");
            request.setAttribute("filtroEmail", emailCliente != null ? emailCliente : "");

            // 6. Forward alla View
            request.getRequestDispatcher("/WEB-INF/view/admin_ordini.jsp").forward(request, response);

        } catch (DateTimeParseException e) {
            // Caso limite: l'utente manomette a mano la query string nell'URL con formati strani (es. ?dal=ciao)
            request.setAttribute("erroreFiltro", "Formato data non valido.");
            ricaricaPaginaSenzaFiltri(request, response);
            
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore di connessione al database.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // La visualizzazione e i filtri sono eseguiti in GET. 
        // In futuro potremmo usare il POST qui per permettere all'admin di aggiornare 
        // lo STATO dell'ordine (es. da IN_LAVORAZIONE a SPEDITO) in modo tradizionale,
        // o in alternativa potremmo usare una chiamata Fetch asincrona JSON.
        doGet(request, response);
    }
    
    /**
     * Helper method per ricaricare la pagina allo stato base in caso di errori di parsing.
     */
    private void ricaricaPaginaSenzaFiltri(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("listaClienti", utenteDAO.doRetrieveAll());
            request.setAttribute("ordiniAdmin", ordineDAO.doRetrieveAllAdmin(null, null, null));
            request.getRequestDispatcher("/WEB-INF/view/admin_ordini.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore di connessione al database.");
        }
    }
}