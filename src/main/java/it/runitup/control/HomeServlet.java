package it.runitup.control;

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
            // 1. SEZIONE NUOVI ARRIVI: Impostiamo il filtro booleano e il limite a 8
            FiltroCatalogo filtroNuovi = new FiltroCatalogo();
            filtroNuovi.setNuoviArrivi(true);
            filtroNuovi.setLimit(8); 
            List<ProdottoDTO> nuoviArrivi = prodottoDAO.doRetrieveAllFiltered(filtroNuovi);

            // 2. SEZIONE PRODOTTI IN EVIDENZA: Impostiamo il flag inEvidenza e il limite a 8
            FiltroCatalogo filtroEvidenza = new FiltroCatalogo();
            filtroEvidenza.setInEvidenza(true);
            filtroEvidenza.setLimit(8);
            List<ProdottoDTO> inEvidenza = prodottoDAO.doRetrieveAllFiltered(filtroEvidenza);

            // 3. SEZIONE PRODOTTI IN PROMOZIONE: Impostiamo inOfferta e limitiamo a 4 elementi fisici
            FiltroCatalogo filtroOfferta = new FiltroCatalogo();
            filtroOfferta.setInOfferta(true);
            filtroOfferta.setLimit(4);
            List<ProdottoDTO> inOfferta = prodottoDAO.doRetrieveAllFiltered(filtroOfferta);

            // 4. SEZIONE COUPON: Logica di Business per l'estrazione casuale di 2 coupon attivi
            List<Coupon> couponAttivi = couponDAO.getCouponAttivi();
            List<Coupon> couponRandom = new ArrayList<>();
            
            if (couponAttivi != null && !couponAttivi.isEmpty()) {
                // Mescoliamo la lista direttamente in memoria RAM per garantire la casualità ad ogni refresh
                Collections.shuffle(couponAttivi);
                
                // Estraiamo in sicurezza al massimo 2 elementi (evitando IndexOutOfBounds se ce ne fossero meno)
                int limiteCoupon = Math.min(2, couponAttivi.size());
                for (int i = 0; i < limiteCoupon; i++) {
                    couponRandom.add(couponAttivi.get(i));
                }
            }

            // 5. INIEZIONE DEI DATI: Registriamo le liste come attributi della request per la View
            request.setAttribute("prodottiNuoviArrivi", nuoviArrivi);
            request.setAttribute("prodottiInEvidenza", inEvidenza);
            request.setAttribute("prodottiInOfferta", inOfferta);
            request.setAttribute("couponHome", couponRandom);

            // 6. VIEW FORWARD: Spediamo i pacchetti alla JSP della Home sotto WEB-INF
            request.getRequestDispatcher("/WEB-INF/view/home.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            // Rispetto rigoroso della Checklist: il server non perde mai il controllo in caso di errore DB
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Si è verificato un errore imprevisto durante il caricamento della pagina principale.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Trattandosi di una landing page informativa, deviamo le chiamate POST sul doGet
        doGet(request, response);
    }
}