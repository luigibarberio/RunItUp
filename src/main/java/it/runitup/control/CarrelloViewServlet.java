package it.runitup.control;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.runitup.control.service.CarrelloManager;
import it.runitup.model.bean.VoceCarrello;
import it.runitup.model.dao.CarrelloDAO;
import it.runitup.model.dao.ProdottoDAO;
import it.runitup.model.dao.VarianteDAO;
import it.runitup.model.dto.VarianteDTO;

@WebServlet("/carrello")
public class CarrelloViewServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final CarrelloDAO carrelloDAO = new CarrelloDAO();
    private final VarianteDAO varianteDAO = new VarianteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            // 1. Inizializziamo il manager unificato e il DAO dei prodotti per le promozioni
            CarrelloManager cartManager = new CarrelloManager(request, carrelloDAO, varianteDAO);
            ProdottoDAO prodottoDAO = new ProdottoDAO();
            
            // 2. Recuperiamo la lista grezza delle voci (valida sia per loggati che ospiti)
            List<VoceCarrello> vociGrezze = cartManager.getVociCarrello();
            
            // 3. Creiamo la lista destinata ad ospitare i pacchetti DTO completi
            List<it.runitup.model.dto.VoceCarrelloDTO> vociDTO = new java.util.ArrayList<>();
            
            // 4. Calcoliamo il totale economico basandoci sul prezzo effettivo di vendita
            BigDecimal totaleCarrello = BigDecimal.ZERO;
            
            for (VoceCarrello voce : vociGrezze) {
                // Ricaviamo la variante arricchita con lo sconto in tempo reale dal DB
                VarianteDTO varianteDTO = prodottoDAO.getVarianteConPromozione(voce.getVariante());
                
                // Creiamo il DTO della riga iniettando i due componenti
                it.runitup.model.dto.VoceCarrelloDTO voceDTO = new it.runitup.model.dto.VoceCarrelloDTO(voce, varianteDTO);
                vociDTO.add(voceDTO);
                
                // Calcolo del subtotale della riga: prezzoEffettivo (scontato se in promo) * quantita
                BigDecimal prezzoVendita = varianteDTO.getPrezzoEffettivo();
                BigDecimal quantita = new BigDecimal(voce.getQuantita());
                
                totaleCarrello = totaleCarrello.add(prezzoVendita.multiply(quantita));
            }
            
            // 5. Passiamo i DTO e il totale ricalcolato alla View
            request.setAttribute("vociCarrello", vociDTO);
            request.setAttribute("totaleCarrello", totaleCarrello);
            
            // 6. Forward alla JSP sicura sotto WEB-INF
            request.getRequestDispatcher("/WEB-INF/view/carrello.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel caricamento del carrello promozionale.");
        }
    }
}