package it.runitup.control.checkout;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.runitup.model.bean.Ordine;
import it.runitup.model.bean.DettaglioOrdine;
import it.runitup.model.bean.Utente;
import it.runitup.model.dao.OrdineDAO;

@WebServlet("/ordini")
public class OrdiniServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final OrdineDAO ordineDAO = new OrdineDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        Utente utenteLoggato = (Utente) session.getAttribute("utenteLoggato");

        String idParam = request.getParameter("id");

        try {
            // Caso A: Visualizzazione Dettaglio Ordine Specifico
            if (idParam != null && !idParam.trim().isEmpty()) {
                
                // Recupero dell'ordine specifico dal filtro OrderOwnerFilter, che ha già validato la proprietà dell'ordine
                Ordine ordine = (Ordine) request.getAttribute("ordineValidato");
                
                // Recupero dei dettagli dell'ordine per la visualizzazione
                List<DettaglioOrdine> dettagli = ordineDAO.getDettagliByOrdine(ordine.getIdOrdine());

                request.setAttribute("ordine", ordine); // Non serve rimetterlo se la JSP usa "ordineGiaValidato", ma lo rimettiamo col nome standard per comodità
                request.setAttribute("dettagliOrdini", dettagli);

                request.getRequestDispatcher("/WEB-INF/view/order/dettaglio_ordine.jsp").forward(request, response);
                return;
            }
            // Caso B: Visualizzazione Cronologia Ordini dell'Utente
            else {
                List<Ordine> cronologiaOrdini = ordineDAO.getOrdiniByUtente(utenteLoggato.getIdUtente());
                request.setAttribute("cronologiaOrdini", cronologiaOrdini);
                request.getRequestDispatcher("/WEB-INF/view/order/cronologia_ordini.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei dati degli ordini.");
        }
    }

    // doPost: Reindirizza le richieste POST alla gestione GET per la visualizzazione degli ordini
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}