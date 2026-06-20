package it.runitup.control.checkout;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

import it.runitup.model.bean.Coupon;
import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.CouponDAO;
import it.runitup.model.dao.VarianteDAO;
import it.runitup.model.dto.VarianteDTO;
import it.runitup.service.CarrelloManager;
import it.runitup.util.SpedizioneUtil;

@WebServlet("/valida-coupon")
public class ValidaCouponServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final CouponDAO couponDAO = new CouponDAO();
    private final CarrelloDAO carrelloDAO = new CarrelloDAO();
    private final VarianteDAO varianteDAO = new VarianteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        String codice = request.getParameter("codice");
        if (codice == null || codice.trim().isEmpty()) {
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Codice coupon non specificato.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try {
            // 1. Recupero del coupon (Il DB controlla già se esiste ed è attivo/valido temporalmente)
            Coupon coupon = couponDAO.getCouponByCodice(codice.trim().toUpperCase());
            if (coupon == null) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Coupon non valido o scaduto.");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            // 2. Recupero del subtotale attuale dei prodotti dal carrello dell'utente
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            List<VoceCarrello> vociGrezze = cartManager.getVociCarrello();
            
            if (vociGrezze == null || vociGrezze.isEmpty()) {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Il carrello è vuoto.");
                response.getWriter().write(jsonResponse.toString());
                return;
            }

            BigDecimal totaleProdotti = BigDecimal.ZERO;
            for (VoceCarrello voce : vociGrezze) {
                VarianteDTO vDTO = varianteDAO.getVarianteConPromozione(voce.getVariante());
                BigDecimal costoRiga = vDTO.getPrezzoEffettivo().multiply(new BigDecimal(voce.getQuantita()));
                totaleProdotti = totaleProdotti.add(costoRiga);
            }

            // 3. Applicazione delle regole matematiche concordate (Scenario A + Cap a 0 euro)
            BigDecimal valoreScontoCalcolato = BigDecimal.ZERO;
            String visualizzazioneSconto = "";

            if (coupon.getTipoSconto().name().equals("PERCENTUALE")) {
                // Sconto percentuale: subtotale * (valore / 100)
                BigDecimal percentuale = coupon.getValoreSconto().divide(new BigDecimal("100"), 4, java.math.RoundingMode.HALF_UP);
                valoreScontoCalcolato = totaleProdotti.multiply(percentuale).setScale(2, java.math.RoundingMode.HALF_UP);
                visualizzazioneSconto = coupon.getValoreSconto().stripTrailingZeros().toPlainString() + "%";
            } else {
                // Sconto fisso
                valoreScontoCalcolato = coupon.getValoreSconto();
                visualizzazioneSconto = "€ " + coupon.getValoreSconto().setScale(2).toString();
            }

            // Cap a 0€: Lo sconto non può superare il valore dei prodotti
            if (valoreScontoCalcolato.compareTo(totaleProdotti) > 0) {
                valoreScontoCalcolato = totaleProdotti;
            }

            // Sottraiamo lo sconto dai prodotti
            BigDecimal totaleProdottiScontato = totaleProdotti.subtract(valoreScontoCalcolato);

            // Calcolo spedizione basandoci sul subtotale dei prodotti post-sconto
            BigDecimal costoSpedizione = SpedizioneUtil.calcolaCostoSpedizione(totaleProdottiScontato);
            BigDecimal nuovoTotaleFinale = totaleProdottiScontato.add(costoSpedizione);

            // Risposta JSON per la fetch client-side
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("scontoRilevato", visualizzazioneSconto);

            // Formattazione del valore dello sconto per la visualizzazione (2 decimali, separatore decimale a virgola)
            jsonResponse.addProperty("importoSconto", String.format(java.util.Locale.US, "%.2f", valoreScontoCalcolato));
            jsonResponse.addProperty("nuovoTotale", String.format(java.util.Locale.US, "%.2f", nuovoTotaleFinale));
            
            // Includiamo anche il costo di spedizione, così da permettere al client di aggiornare dinamicamente anche quello se necessario (es: se il coupon ha fatto scendere il totale sotto la soglia per la spedizione gratuita)
            jsonResponse.addProperty("costoSpedizione", String.format(java.util.Locale.US, "%.2f", costoSpedizione));
            
        } catch (Exception e) {
            e.printStackTrace();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Errore del server durante la verifica del codice.");
        }

        response.getWriter().write(jsonResponse.toString());
    }
}