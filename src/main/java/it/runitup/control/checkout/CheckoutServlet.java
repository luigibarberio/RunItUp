package it.runitup.control.checkout;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import it.runitup.model.bean.Coupon;
import it.runitup.model.bean.Ordine;
import it.runitup.model.bean.StatoOrdine;
import it.runitup.model.bean.TipoMetodoDiPagamento;
import it.runitup.model.bean.Utente;
import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.CouponDAO;
import it.runitup.model.dao.VarianteDAO;
import it.runitup.model.dto.VarianteDTO;
import it.runitup.model.dto.VoceCarrelloDTO;
import it.runitup.service.CarrelloManager;
import it.runitup.util.SpedizioneUtil;
import it.runitup.model.dao.OrdineDAO;
import it.runitup.model.dao.UtenteDAO;

@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final CarrelloDAO carrelloDAO = new CarrelloDAO();
    private final VarianteDAO varianteDAO = new VarianteDAO();
    private final OrdineDAO ordineDAO = new OrdineDAO();
    // doGet: Carica le voci del carrello con i prezzi scontati in tempo reale e calcola il totale aggiornato
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            List<VoceCarrello> vociGrezze = cartManager.getVociCarrello();
            
            // Se l'utente cerca di forzare l'URL /checkout ma ha il carrello vuoto, lo rispediamo al carrello
            if (vociGrezze == null || vociGrezze.isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/carrello");
                return;
            }

            // Creazione di una lista di DTO per le voci del carrello, che includeranno i prezzi scontati in tempo reale
            List<VoceCarrelloDTO> vociDTO = new ArrayList<>();
            BigDecimal totaleProdotti = BigDecimal.ZERO;
            
            for (VoceCarrello voce : vociGrezze) {
                VarianteDTO vDTO = varianteDAO.getVarianteConPromozione(voce.getVariante());
                vociDTO.add(new VoceCarrelloDTO(voce, vDTO));
                
                BigDecimal costoRiga = vDTO.getPrezzoEffettivo().multiply(new BigDecimal(voce.getQuantita()));
                totaleProdotti = totaleProdotti.add(costoRiga);
            }
            
            // Calcolo del costo di spedizione e del totale finale
            BigDecimal costoSpedizione = SpedizioneUtil.calcolaCostoSpedizione(totaleProdotti);
            BigDecimal totaleFinale = totaleProdotti.add(costoSpedizione);
            
            request.setAttribute("vociCheckout", vociDTO);
            request.setAttribute("totaleProdotti", totaleProdotti);
            request.setAttribute("costoSpedizione", costoSpedizione);
            request.setAttribute("totaleFinale", totaleFinale);
            
            request.getRequestDispatcher("/WEB-INF/view/checkout/checkout.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore durante il caricamento del checkout.");
        }
    }

    // doPost: Riceve i dati JSON, convalida anagrafica, ricalcola i totali (e l'eventuale coupon), scorpora l'IVA e salva sul database
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();
        com.google.gson.Gson gson = new com.google.gson.Gson();

        HttpSession session = request.getSession(false);
        Utente utenteLoggato = (Utente) session.getAttribute("utenteLoggato");
        UtenteDAO utenteDAO = new UtenteDAO();
        
        try {
            // Le informazioni dell'utente verranno ottenute tramite chiamata al DB usando l'ID utente memorizzato in sessione, per evitare manomissioni dei dati lato client
            Utente utente = utenteDAO.doRetrieveById(utenteLoggato.getIdUtente());

            // Parsing del corpo della richiesta JSON per ottenere i dati di spedizione e pagamento
            java.io.BufferedReader reader = request.getReader();
            JsonObject jsonBody = gson.fromJson(reader, JsonObject.class);

            String nome = jsonBody.has("nome") ? jsonBody.get("nome").getAsString().trim() : "";
            String cognome = jsonBody.has("cognome") ? jsonBody.get("cognome").getAsString().trim() : "";
            String indirizzo = jsonBody.has("indirizzo") ? jsonBody.get("indirizzo").getAsString().trim() : "";
            String citta = jsonBody.has("citta") ? jsonBody.get("citta").getAsString().trim() : "";
            String cap = jsonBody.has("cap") ? jsonBody.get("cap").getAsString().trim() : "";
            String metodoPagamentoStr = jsonBody.has("metodoPagamento") ? jsonBody.get("metodoPagamento").getAsString().trim() : "";
            String codiceCoupon = jsonBody.has("codiceCoupon") ? jsonBody.get("codiceCoupon").getAsString().trim().toUpperCase() : "";

            // Validazione lato server dei dati di spedizione e pagamento ricevuti dal client
            if (nome.isEmpty() || nome.length() > 50 ||
                cognome.isEmpty() || cognome.length() > 50 ||
                indirizzo.isEmpty() || indirizzo.length() > 100 ||
                citta.isEmpty() || citta.length() > 50 ||
                cap.length() != 5 || !cap.matches("\\d{5}") ||
                metodoPagamentoStr.isEmpty() || metodoPagamentoStr.length() > 20 ||
                codiceCoupon.length() > 20) {

                inviaRisposta(response, jsonResponse, false, "Dati di spedizione non conformi o incompleti.");
                return;
            }

            // Validazione del metodo di pagamento ricevuto dal client
            TipoMetodoDiPagamento metodoPagamento;
            try {
                metodoPagamento = TipoMetodoDiPagamento.valueOf(metodoPagamentoStr);
            } catch (IllegalArgumentException e) {
                inviaRisposta(response, jsonResponse, false, "Il metodo di pagamento inviato non è supportato.");
                return;
            }

            // Ricalcolo del carrello e dei totali, considerando eventuali coupon, per garantire che il client non possa manipolare i prezzi o le quantità
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            List<VoceCarrello> vociGrezze = cartManager.getVociCarrello();
            
            if (vociGrezze == null || vociGrezze.isEmpty()) {
                inviaRisposta(response, jsonResponse, false, "Il carrello risulta vuoto. Impossibile procedere.");
                return;
            }

            List<VoceCarrelloDTO> vociDTO = new ArrayList<>();
            BigDecimal totaleProdotti = BigDecimal.ZERO;
            
            for (VoceCarrello voce : vociGrezze) {
                VarianteDTO vDTO = varianteDAO.getVarianteConPromozione(voce.getVariante());
                vociDTO.add(new VoceCarrelloDTO(voce, vDTO));
                BigDecimal subtot = vDTO.getPrezzoEffettivo().multiply(new BigDecimal(voce.getQuantita()));
                totaleProdotti = totaleProdotti.add(subtot);
            }

            // Se il totale prodotti è zero o negativo, non permettiamo di procedere con l'ordine
            if (totaleProdotti.compareTo(BigDecimal.ZERO) <= 0) {
                inviaRisposta(response, jsonResponse, false, "Il totale del carrello non è valido. Impossibile procedere.");
                return;
            }
            Coupon couponApplicato = null;
            BigDecimal valoreSconto = BigDecimal.ZERO;

            // Se l'utente ha inserito un codice coupon, verifichiamo la sua validità e calcoliamo lo sconto applicabile
            if (!codiceCoupon.isEmpty()) {
                CouponDAO cDAO = new CouponDAO();
                couponApplicato = cDAO.getCouponByCodice(codiceCoupon);
                
                // Se l'utente ha forzato una stringa coupon fasulla modificando il client-side
                if (couponApplicato == null) {
                    inviaRisposta(response, jsonResponse, false, "Il codice promozionale inserito non è più valido.");
                    return;
                }

                // Calcolo del valore dello sconto in base al tipo di coupon (percentuale o fisso)
                if (couponApplicato.getTipoSconto().name().equals("PERCENTUALE")) {
                    BigDecimal pct = couponApplicato.getValoreSconto().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                    valoreSconto = totaleProdotti.multiply(pct).setScale(2, RoundingMode.HALF_UP);
                } else {
                    valoreSconto = couponApplicato.getValoreSconto();
                }

                // Cap di sicurezza a 0 euro
                if (valoreSconto.compareTo(totaleProdotti) > 0) {
                    valoreSconto = totaleProdotti;
                }
            }

            // Calcolo del totale prodotti scontato
            BigDecimal totaleProdottiScontato = totaleProdotti.subtract(valoreSconto);

            // Calcolo spedizione sul parziale merci rimasto
            BigDecimal costoSpedizione = SpedizioneUtil.calcolaCostoSpedizione(totaleProdottiScontato);
            BigDecimal totalePagato = totaleProdottiScontato.add(costoSpedizione);

            // Scorporo dell'IVA dal totale pagato, considerando un'aliquota del 22%
            BigDecimal aliquotaDivisore = new BigDecimal("1.22");
            BigDecimal totaleImponibile = totalePagato.divide(aliquotaDivisore, 2, RoundingMode.HALF_UP);
            BigDecimal totaleIva = totalePagato.subtract(totaleImponibile);

            // Creazione dell'oggetto Ordine con tutti i dati necessari per il salvataggio nel database
            Ordine ordine = new Ordine();
            ordine.setUtente(utente);
            ordine.setCoupon(couponApplicato); // Se applicato conterrà il Bean, altrimenti null (gestito da OrdineDAO)
            ordine.setDataOrdine(LocalDateTime.now());
            
            ordine.setTotaleImponibile(totaleImponibile);
            ordine.setTotaleIva(totaleIva);
            ordine.setTotalePagato(totalePagato);
            ordine.setCostoSpedizione(costoSpedizione);
            
            ordine.setNome_e_cognome_destinatario(nome + " " + cognome);
            ordine.setIndirizzoSpedizione(indirizzo);
            ordine.setCitta(citta);
            ordine.setCap(cap);
            ordine.setMetodoPagamento(metodoPagamento.name());
            ordine.setStato(StatoOrdine.IN_LAVORAZIONE);

            // Salvataggio dell'ordine completo nel database, comprese le voci del carrello e l'eventuale coupon
            int idOrdineGenerato = ordineDAO.salvaOrdineCompleto(ordine, vociDTO, utente.getIdUtente());

            if (idOrdineGenerato > 0) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("redirectUrl", request.getContextPath() + "/ordini?id=" + idOrdineGenerato);

                // Si aggiorna il cartBadgeCount in sessione a 0, poiché il carrello è stato svuotato
                session.setAttribute("cartBadgeCount", 0);

                response.getWriter().write(gson.toJson(jsonResponse));
            } else {
                inviaRisposta(response, jsonResponse, false, "Scorte esaurite per uno o più articoli selezionati. Ordine annullato.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            inviaRisposta(response, jsonResponse, false, "Si è verificato un errore imprevisto nell'elaborazione del pagamento.");
        }
    }

    // Metodo di utilità per inviare una risposta JSON al client con l'esito dell'operazione e un messaggio
    private void inviaRisposta(HttpServletResponse response, JsonObject jsonResponse, boolean success, String message) throws IOException {
        jsonResponse.addProperty("success", success);
        jsonResponse.addProperty("message", message);
        response.getWriter().write(new Gson().toJson(jsonResponse));
    }
}