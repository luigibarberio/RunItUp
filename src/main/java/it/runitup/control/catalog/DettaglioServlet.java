package it.runitup.control.catalog;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.runitup.model.bean.Prodotto;
import it.runitup.model.dao.ProdottoDAO;
import it.runitup.model.dto.VarianteDTO;

@WebServlet("/dettaglio")
public class DettaglioServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/catalogo");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            
            // 1. Recupero il Prodotto (con il suo Brand già caricato dal DAO)
            Prodotto prodotto = prodottoDAO.doRetrieveById(id);
            
            if (prodotto == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Prodotto non trovato");
                return;
            }

            // 2. Recupero le varianti (con prezzi calcolati e promozioni)
            List<VarianteDTO> varianti = prodottoDAO.getVariantiDTOByProdottoId(id);

            // 3. Passaggio alla JSP
            request.setAttribute("prodotto", prodotto);
            request.setAttribute("varianti", varianti);

            request.getRequestDispatcher("/WEB-INF/view/catalog/dettaglio.jsp")
                   .forward(request, response);

        } catch (NumberFormatException | SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento del prodotto");
        }
    }
}