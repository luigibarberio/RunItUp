package it.runitup.control.catalog;

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
    
    // DAO per l'accesso ai prodotti nel catalogo
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        // Recupero dei parametri di filtro dalla richiesta
        String genereParam = request.getParameter("genere");
        String terrenoParam = request.getParameter("terreno");
        String appoggioParam = request.getParameter("appoggio");
        String passoParam = request.getParameter("passo");

        // Caso A: L'utente non ha ancora completato il quiz (nessun parametro di filtro presente)
        if (genereParam == null && terrenoParam == null && appoggioParam == null && passoParam == null) {
            request.getRequestDispatcher("/WEB-INF/view/catalog/findmyshoes.jsp").forward(request, response);
            return;
        }

        // Caso B: L'utente ha completato il quiz e ha inviato i filtri
        FiltroCatalogo filtri = new FiltroCatalogo();

        try {
  
            // Costruzione dei filtri basati sui parametri ricevuti dalla richiesta, ignorando eventuali valori "ANY" che indicano nessun filtro per quel campo           
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

            // Recupero dei prodotti consigliati dal database in base ai filtri applicati
            List<ProdottoDTO> prodottiConsigliati = prodottoDAO.doRetrieveAllFiltered(filtri);

            // Impostazione degli attributi della richiesta per la JSP dei risultati
            request.setAttribute("prodottiConsigliati", prodottiConsigliati);
            request.getRequestDispatcher("/WEB-INF/view/catalog/findmyshoes_results.jsp").forward(request, response);

        } catch (IllegalArgumentException e) {
            // Gestione del caso in cui i parametri di filtro non corrispondano a valori validi degli enum
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "I parametri inseriti non sono validi.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore del database durante la ricerca.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Reindirizzamento della richiesta POST alla gestione della richiesta GET per mantenere la logica di filtraggio
        doGet(request, response);
    }
}