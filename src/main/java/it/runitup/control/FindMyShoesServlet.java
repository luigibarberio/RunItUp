package it.runitup.control;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import it.runitup.model.bean.GenereScarpa;
import it.runitup.model.bean.TipoAppoggio;
import it.runitup.model.bean.TipoPasso;
import it.runitup.model.bean.TipoTerreno;
import it.runitup.model.dao.ProdottoDAO;
import it.runitup.model.dto.ProdottoDTO;
import it.runitup.model.filter.FiltroCatalogo;

@WebServlet("/findmyshoes")
public class FindMyShoesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Riutilizziamo il DAO del catalogo senza scrivere nuovo codice SQL!
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Leggiamo i parametri dalla Query String (?genere=...&terreno=...)
        String genereParam = request.getParameter("genere");
        String terrenoParam = request.getParameter("terreno");
        String appoggioParam = request.getParameter("appoggio");
        String passoParam = request.getParameter("passo");

        // --- CASO A: L'utente apre la pagina senza filtri (Inizio del Quiz) ---
        if (genereParam == null && terrenoParam == null && appoggioParam == null && passoParam == null) {
            request.getRequestDispatcher("/WEB-INF/view/findmyshoes.jsp").forward(request, response);
            return;
        }

        // --- CASO B: L'utente ha completato il quiz (Calcolo Risultati) ---
        FiltroCatalogo filtri = new FiltroCatalogo();

        try {
            // "ANY" è un valore speciale che useremo nell'HTML per "Nessuno in particolare"
            // Se non è "ANY", parsiamo la stringa nella rispettiva Enum e l'aggiungiamo alla lista dei filtri
            
            if (genereParam != null && !genereParam.trim().isEmpty() && !genereParam.equals("ANY")) {
                filtri.setGeneri(Arrays.asList(GenereScarpa.valueOf(genereParam.toUpperCase())));
            }

            if (terrenoParam != null && !terrenoParam.trim().isEmpty() && !terrenoParam.equals("ANY")) {
                filtri.setTipiTerreno(Arrays.asList(TipoTerreno.valueOf(terrenoParam.toUpperCase())));
            }

            if (appoggioParam != null && !appoggioParam.trim().isEmpty() && !appoggioParam.equals("ANY")) {
                filtri.setTipiAppoggio(Arrays.asList(TipoAppoggio.valueOf(appoggioParam.toUpperCase())));
            }

            if (passoParam != null && !passoParam.trim().isEmpty() && !passoParam.equals("ANY")) {
                filtri.setTipiPasso(Arrays.asList(TipoPasso.valueOf(passoParam.toUpperCase())));
            }

            // 2. Facciamo lavorare il DAO con i filtri appena costruiti
            List<ProdottoDTO> prodottiConsigliati = prodottoDAO.doRetrieveAllFiltered(filtri);

            // 3. Passiamo i risultati alla JSP dedicata
            request.setAttribute("prodottiConsigliati", prodottiConsigliati);
            request.getRequestDispatcher("/WEB-INF/view/findmyshoes_results.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            // Protezione: se qualcuno si diverte a manomettere l'URL (es. ?genere=ALIENO)
            // il valueOf() dell'Enum lancerà questa eccezione. Noi rispondiamo con un 400 Bad Request.
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "I parametri inseriti non sono validi.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del database durante la ricerca.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Le ricerche devono essere condivisibili (es. inviare il link a un amico). 
        // Quindi forziamo tutto in GET. Se dovesse arrivare un POST, lo dirottiamo.
        doGet(request, response);
    }
}