package it.runitup.control;

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
            // --- CASISTICA A: Dettaglio Ordine ---
            if (idParam != null && !idParam.trim().isEmpty()) {
                
                // L'oggetto Ordine ce lo ha appena impacchettato e validato il Filtro!
                Ordine ordine = (Ordine) request.getAttribute("ordineValidato");
                
                // Estraiamo solo i dettagli commerciali
                List<DettaglioOrdine> dettagli = ordineDAO.getDettagliByOrdine(ordine.getIdOrdine());

                request.setAttribute("ordine", ordine); // Non serve rimetterlo se la JSP usa "ordineGiaValidato", ma lo rimettiamo col nome standard per comodità
                request.setAttribute("dettagliOrdini", dettagli);

                request.getRequestDispatcher("/WEB-INF/view/dettaglio_ordine.jsp").forward(request, response);
                return;
            }
            // --- CASISTICA B: Cronologia Completa ---
            else {
                List<Ordine> cronologiaOrdini = ordineDAO.getOrdiniByUtente(utenteLoggato.getIdUtente());
                request.setAttribute("cronologiaOrdini", cronologiaOrdini);
                request.getRequestDispatcher("/WEB-INF/view/cronologia_ordini.jsp").forward(request, response);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il recupero dei dati degli ordini.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Le consultazioni dello storico avvengono solo in GET. Rimandiamo al doGet in caso di chiamate errate.
        doGet(request, response);
    }
}