package it.runitup.control.checkout;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.runitup.model.bean.Ordine;
import it.runitup.model.bean.DettaglioOrdine;
import it.runitup.model.dao.OrdineDAO;

// Servlet dedicata alla generazione della fattura in formato PDF (o meglio, una pagina ottimizzata per la stampa)
@WebServlet("/stampa_fattura")
public class StampaFatturaServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final OrdineDAO ordineDAO = new OrdineDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Recupero dell'ordine validato dal filtro OrderOwnerFilter, che ha già verificato la proprietà dell'ordine e lo ha messo in request
        Ordine ordine = (Ordine) request.getAttribute("ordineValidato");

        if (ordine == null) {
            // Se l'ordine non è stato validato, inviamo un errore 400 (Bad Request)
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ordine non validato.");
            return;
        }

        try {
            // Recupero dei dettagli dell'ordine per la generazione della fattura
            List<DettaglioOrdine> dettagli = ordineDAO.getDettagliByOrdine(ordine.getIdOrdine());

            request.setAttribute("ordine", ordine);
            request.setAttribute("dettagliOrdini", dettagli);

            // Inoltro alla JSP dedicata alla stampa della fattura
            request.getRequestDispatcher("/WEB-INF/view/order/stampa_fattura.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nella generazione del documento fiscale.");
        }
    }

    // doPost: Reindirizza le richieste POST alla gestione GET per la visualizzazione della fattura
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}