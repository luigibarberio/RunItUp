package it.runitup.control.admin;

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
        
        // Estrazione dei parametri di filtro dalla query string
        String dalStr = request.getParameter("dal");
        String alStr = request.getParameter("al");
        String emailCliente = request.getParameter("emailCliente");

        LocalDate dal = null;
        LocalDate al = null;
        Integer idCliente = null;

        try {
            // Parsing delle date dai parametri della query string, se presenti
            if (dalStr != null && !dalStr.trim().isEmpty()) {
                dal = LocalDate.parse(dalStr.trim());
            }
            if (alStr != null && !alStr.trim().isEmpty()) {
                al = LocalDate.parse(alStr.trim());
            }

            // Se l'email del cliente è stata fornita, si recupera l'ID corrispondente nel database
            if (emailCliente != null && !emailCliente.trim().isEmpty()) {
                Utente u = utenteDAO.doRetrieveByEmail(emailCliente.trim());
                if (u != null) {
                    idCliente = u.getIdUtente();
                } else {
                    // Se non troviamo il cliente, impostiamo idCliente a -1 per indicare che non esiste
                    idCliente = -1; 
                    request.setAttribute("erroreFiltro", "Nessun cliente trovato con l'email: " + emailCliente);
                }
            }

            // Recupero della lista dei clienti e degli ordini filtrati dal database
            List<Utente> listaClienti = utenteDAO.doRetrieveAll(); // Serve per popolare il <datalist>
            List<Ordine> ordiniAdmin = ordineDAO.doRetrieveAllAdmin(dal, al, idCliente); // Risultato del filtro

            // Impostazione degli attributi della request per la JSP
            request.setAttribute("listaClienti", listaClienti);
            request.setAttribute("ordiniAdmin", ordiniAdmin);
            
            // Impostazione dei valori dei filtri per mantenere i campi del form precompilati
            request.setAttribute("filtroDal", dalStr != null ? dalStr : "");
            request.setAttribute("filtroAl", alStr != null ? alStr : "");
            request.setAttribute("filtroEmail", emailCliente != null ? emailCliente : "");

            // Forward alla View
            request.getRequestDispatcher("/WEB-INF/view/admin/admin_ordini.jsp").forward(request, response);

        } catch (DateTimeParseException e) {
            // Gestione dell'errore di parsing delle date
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
    
    // Metodo di utilità per ricaricare la pagina senza filtri in caso di errore
    private void ricaricaPaginaSenzaFiltri(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            request.setAttribute("listaClienti", utenteDAO.doRetrieveAll());
            request.setAttribute("ordiniAdmin", ordineDAO.doRetrieveAllAdmin(null, null, null));
            request.getRequestDispatcher("/WEB-INF/view/admin/admin_ordini.jsp").forward(request, response);
        } catch (SQLException ex) {
            ex.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore di connessione al database.");
        }
    }
}