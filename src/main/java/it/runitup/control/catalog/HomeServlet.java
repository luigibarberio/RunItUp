package it.runitup.control.catalog;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.runitup.model.bean.Coupon;
import it.runitup.model.dao.CouponDAO;
import it.runitup.model.dao.ProdottoDAO;
import it.runitup.model.dto.ProdottoDTO;
import it.runitup.model.filter.FiltroCatalogo;

@WebServlet("")
public class HomeServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();
    private final CouponDAO couponDAO = new CouponDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // Query per recuperare le informazioni necessarie per la home page, suddivise in sezioni:
            // Sezione nuovi arrivi
            FiltroCatalogo filtroNuovi = new FiltroCatalogo();
            filtroNuovi.setNuoviArrivi(true);
            filtroNuovi.setLimit(8); 
            List<ProdottoDTO> nuoviArrivi = prodottoDAO.doRetrieveAllFiltered(filtroNuovi);

            // Sezione prodotti in evidenza
            FiltroCatalogo filtroEvidenza = new FiltroCatalogo();
            filtroEvidenza.setInEvidenza(true);
            filtroEvidenza.setLimit(8);
            List<ProdottoDTO> inEvidenza = prodottoDAO.doRetrieveAllFiltered(filtroEvidenza);

            // Sezione prodotti in offerta
            FiltroCatalogo filtroOfferta = new FiltroCatalogo();
            filtroOfferta.setInOfferta(true);
            filtroOfferta.setLimit(4);
            List<ProdottoDTO> inOfferta = prodottoDAO.doRetrieveAllFiltered(filtroOfferta);

            // Sezione coupon attivi
            List<Coupon> couponAttivi = couponDAO.getCouponAttivi();
            List<Coupon> couponRandom = new ArrayList<>();
            
            if (couponAttivi != null && !couponAttivi.isEmpty()) {
                // Estrazione casuale di 2 coupon dalla lista dei coupon attivi
                Collections.shuffle(couponAttivi);
                
                // Limitiamo il numero di coupon selezionati a 2 o al numero totale disponibile se inferiore
                int limiteCoupon = Math.min(2, couponAttivi.size());
                for (int i = 0; i < limiteCoupon; i++) {
                    couponRandom.add(couponAttivi.get(i));
                }
            }

            // Impostazione degli attributi della richiesta per la JSP della home page
            request.setAttribute("prodottiNuoviArrivi", nuoviArrivi);
            request.setAttribute("prodottiInEvidenza", inEvidenza);
            request.setAttribute("prodottiInOfferta", inOfferta);
            request.setAttribute("couponHome", couponRandom);

            // Inoltro della richiesta alla JSP della home page
            request.getRequestDispatcher("/WEB-INF/view/home.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            // Gestione degli errori generici, come problemi di connessione al database o eccezioni non previste
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Si è verificato un errore imprevisto durante il caricamento della pagina principale.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Reindirizzamento della richiesta POST alla gestione della richiesta GET per mantenere la logica di caricamento della home page
        doGet(request, response);
    }
}