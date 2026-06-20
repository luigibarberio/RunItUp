package it.runitup.control.admin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.runitup.model.dao.ProdottoDAO;
import it.runitup.model.dto.ProdottoDTO;

// Servlet per la visualizzazione dell'elenco completo dei prodotti nel pannello di controllo admin. Riceve richieste GET per mostrare la tabella riassuntiva dei prodotti.
@WebServlet("/admin/prodotti")
public class AdminProdottiServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();

    // Gestione della richiesta GET per visualizzare l'elenco dei prodotti
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {

            // Recuperiamo l'elenco completo dei prodotti
            List<ProdottoDTO> elencoCompleto = prodottoDAO.doRetrieveAllAdmin();
            
            // Iniettiamo i dati come attributo per la JSP
            request.setAttribute("prodottiAdmin", elencoCompleto);
            
            // Intercettiamo eventuali messaggi di successo dai redirect (es. msg=updated o msg=created)
            String msg = request.getParameter("msg");
            if (msg != null && !msg.trim().isEmpty()) {
                request.setAttribute("feedbackMsg", msg.trim());
            }

            // Forward verso la vista di riepilogo del pannello di controllo
            request.getRequestDispatcher("/WEB-INF/view/admin/admin_prodotti.jsp").forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            // Rispetto rigoroso della Checklist: gestione dell'errore centralizzata
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Impossibile caricare l'elenco dei prodotti aziendali dal database.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Trattandosi di un pannello di sola consultazione tabellare, deviamo sul doGet
        doGet(request, response);
    }
}