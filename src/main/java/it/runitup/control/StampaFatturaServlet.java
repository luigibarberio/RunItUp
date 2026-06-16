package it.runitup.control;

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
        
        // Se arriviamo qui, il Filtro ha già verificato tutto. Se mancava l'ID, il filtro ci ha fermato.
        // Recuperiamo direttamente il "pacco dono" dal Filtro.
        Ordine ordine = (Ordine) request.getAttribute("ordineValidato");

        if (ordine == null) {
            // Fallback estremo se qualcosa va storto nel passaggio
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Ordine non validato.");
            return;
        }

        try {
            // Dobbiamo solo caricare i prodotti acquistati
            List<DettaglioOrdine> dettagli = ordineDAO.getDettagliByOrdine(ordine.getIdOrdine());

            request.setAttribute("ordine", ordine);
            request.setAttribute("dettagliOrdini", dettagli);

            // Forward alla View dedicata alla Stampa (la JSP finta-A4 che abbiamo creato prima)
            request.getRequestDispatcher("/WEB-INF/view/stampa_fattura.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nella generazione del documento fiscale.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Nessuna modifica di stato viene fatta qui, inoltriamo alla GET
        doGet(request, response);
    }
}