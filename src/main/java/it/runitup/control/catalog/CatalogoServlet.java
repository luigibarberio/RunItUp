package it.runitup.control.catalog;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
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

@WebServlet("/catalogo")
public class CatalogoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Istanziamo il nostro contenitore per i criteri di ricerca
        FiltroCatalogo filtri = new FiltroCatalogo();

        // Estrazione dei parametri di ricerca testuale dalla query string
        String queryRicerca = request.getParameter("q");
        if (queryRicerca != null && !queryRicerca.trim().isEmpty()) {
            filtri.setRicercaTestuale(queryRicerca.trim());
        }

        // Estrazione e parsing dei filtri multi-selezione (brand, genere, taglie, colori, tipoTerreno, tipoAppoggio, tipoPasso) dalla query string
        String[] brandParams = request.getParameterValues("brand");
        if (brandParams != null && brandParams.length > 0) {
            List<Integer> brandIds = new ArrayList<>();
            for (String idStr : brandParams) {
                try {
                    if (idStr != null && !idStr.trim().isEmpty()) {
                        brandIds.add(Integer.parseInt(idStr.trim()));
                    }
                } catch (NumberFormatException e) {
                    // Se qualcuno manomette l'URL scrivendo caratteri al posto di ID numerici, lo ignoriamo
                }
            }
            if (!brandIds.isEmpty()) {
                filtri.setBrandIds(brandIds);
            }
        }

        // Estrazione e parsing dei filtri multi-selezione per il genere (Enum GenereScarpa) dalla query string
        String[] genereParams = request.getParameterValues("genere");
        if (genereParams != null && genereParams.length > 0) {
            List<GenereScarpa> generi = new ArrayList<>();
            for (String genStr : genereParams) {
                try {
                    if (genStr != null && !genStr.trim().isEmpty()) {
                        generi.add(GenereScarpa.valueOf(genStr.trim().toUpperCase()));
                    }
                } catch (IllegalArgumentException e) {
                    // Se il valore testuale non corrisponde all'Enum, viene ignorato in sicurezza
                }
            }
            if (!generi.isEmpty()) {
                filtri.setGeneri(generi);
            }
        }

        // Estrazione dei filtri di prezzo (minimo e massimo) dalla query string
        String prezzoMinParam = request.getParameter("prezzoMin");
        if (prezzoMinParam != null && !prezzoMinParam.trim().isEmpty()) {
            try {
                filtri.setPrezzoMinimo(new BigDecimal(prezzoMinParam.trim()));
            } catch (NumberFormatException e) {
                // Input non valido, ignorato
            }
        }

        String prezzoMaxParam = request.getParameter("prezzoMax");
        if (prezzoMaxParam != null && !prezzoMaxParam.trim().isEmpty()) {
            try {
                filtri.setPrezzoMassimo(new BigDecimal(prezzoMaxParam.trim()));
            } catch (NumberFormatException e) {
                // Input non valido, ignorato
            }
        }

        // Estrazione dei filtri booleani (inEvidenza, inOfferta, nuoviArrivi) dalla query string
        filtri.setInEvidenza("true".equals(request.getParameter("inEvidenza")));
        filtri.setInOfferta("true".equals(request.getParameter("inOfferta")));
        filtri.setNuoviArrivi("true".equals(request.getParameter("nuoviArrivi")));

        // Estrazione dei filtri multi-selezione per taglie, colori, tipoTerreno, tipoAppoggio e tipoPasso dalla query string
        String[] taglieParams = request.getParameterValues("taglie");
        if (taglieParams != null && taglieParams.length > 0) {
            filtri.setTaglie(java.util.Arrays.asList(taglieParams));
        }

        String[] coloriParams = request.getParameterValues("colori");
        if (coloriParams != null && coloriParams.length > 0) {
            filtri.setColori(java.util.Arrays.asList(coloriParams));
        }

        String[] tipoTerreno = request.getParameterValues("tipoTerreno");
        if (tipoTerreno != null && tipoTerreno.length > 0) {
            List<TipoTerreno> tipiTerreno = new ArrayList<>();
            for (String tipoStr : tipoTerreno) {
                try {
                    if (tipoStr != null && !tipoStr.trim().isEmpty()) {
                        tipiTerreno.add(TipoTerreno.valueOf(tipoStr.trim().toUpperCase()));
                    }
                } catch (IllegalArgumentException e) {
                    // Ignorato se il valore non è valido
                }
            }
            filtri.setTipiTerreno(tipiTerreno);
        }

        String[] tipoAppoggio = request.getParameterValues("tipoAppoggio");
        if (tipoAppoggio != null && tipoAppoggio.length > 0) {
            List<TipoAppoggio> tipiAppoggio = new ArrayList<>();
            for (String tipoStr : tipoAppoggio) {
                try {
                    if (tipoStr != null && !tipoStr.trim().isEmpty()) {
                        tipiAppoggio.add(TipoAppoggio.valueOf(tipoStr.trim().toUpperCase()));
                    }
                } catch (IllegalArgumentException e) {
                    // Ignorato se il valore non è valido
                }
            }
            filtri.setTipiAppoggio(tipiAppoggio);

        }

        String[] tipoPasso = request.getParameterValues("tipoPasso");
        if (tipoPasso != null && tipoPasso.length > 0) {
            List<TipoPasso> tipiPasso = new ArrayList<>();
            for (String tipoStr : tipoPasso) {
                try {
                    if (tipoStr != null && !tipoStr.trim().isEmpty()) {
                        tipiPasso.add(TipoPasso.valueOf(tipoStr.trim().toUpperCase()));
                    }
                } catch (IllegalArgumentException e) {
                    // Ignorato se il valore non è valido
                }
            }
            filtri.setTipiPasso(tipiPasso);
        }

        String ordina = request.getParameter("ordina");
        if (ordina != null && !ordina.trim().isEmpty()) {
            filtri.setOrdinamento(ordina.trim());
        }

        // Gestione della paginazione: recuperiamo il parametro "page" dalla query string, se presente, altrimenti impostiamo la pagina corrente a 1
        int paginaCorrente = 1;
        String pageParam = request.getParameter("page");
        if (pageParam != null && !pageParam.trim().isEmpty()) {
            try {
                int parsedPage = Integer.parseInt(pageParam.trim());
                if (parsedPage > 0) {
                    paginaCorrente = parsedPage;
                }
            } catch (NumberFormatException e) {
                // In caso di errore ricade sulla pagina 1 di default
            }
        }

        // Recuperiamo il limite e calcoliamo quanti elementi saltare (offset)
        int limit = filtri.getLimit();
        int offset = (paginaCorrente - 1) * limit;

        // Impostiamo il valore di limit a limit + 1 per verificare se esiste una pagina successiva
        filtri.setLimit(limit + 1);
        filtri.setOffset(offset);

        // Filtri per popolare la sidebar con i brand, taglie e colori disponibili in base ai prodotti filtrati (quindi non filtrati da brand/taglie/colori)
        FiltroCatalogo filtriSidebar = new FiltroCatalogo();
        filtriSidebar.setRicercaTestuale(filtri.getRicercaTestuale());
        filtriSidebar.setGeneri(filtri.getGeneri());
        filtriSidebar.setPrezzoMinimo(filtri.getPrezzoMinimo());
        filtriSidebar.setPrezzoMassimo(filtri.getPrezzoMassimo());
        filtriSidebar.setInEvidenza(filtri.getInEvidenza());
        filtriSidebar.setInOfferta(filtri.getInOfferta());
        filtriSidebar.setNuoviArrivi(filtri.getNuoviArrivi());
        filtriSidebar.setTipiTerreno(filtri.getTipiTerreno());
        filtriSidebar.setTipiAppoggio(filtri.getTipiAppoggio());
        filtriSidebar.setTipiPasso(filtri.getTipiPasso());

        // Recuperiamo i prodotti filtrati dal database e li inoltriamo alla JSP per la visualizzazione
        try {
            // Eseguiamo le query dinamica passando l'oggetto filtro configurato
            List<ProdottoDTO> prodotti = prodottoDAO.doRetrieveAllFiltered(filtri);
            List<ProdottoDTO> prodottiSidebar = prodottoDAO.doRetrieveAllFiltered(filtriSidebar);
            
            // Determiniamo se esiste una pagina successiva: se la lista restituita ha più elementi del limite, significa che c'è un'altra pagina
            boolean isPaginaSuccessiva = prodotti.size() > limit;
            if (isPaginaSuccessiva) {
                prodotti.remove(prodotti.size() - 1); // Rimuoviamo l'elemento extra usato per il controllo
                request.setAttribute("isPaginaSuccessiva", true);
            } else {
                request.setAttribute("isPaginaSuccessiva", false);
            }

            // Flag per la JSP: se true, mostrerà il link alla pagina successiva, altrimenti lo disabiliterà o lo nasconderà
            request.setAttribute("isPaginaSuccessiva", isPaginaSuccessiva);

            // Registriamo la lista nella request affinché le JSP possano leggerla
            request.setAttribute("prodottiCatalogo", prodotti);

            // Registriamo anche la lista per la sidebar
            request.setAttribute("prodottiSidebar", prodottiSidebar);

            // Passiamo l'oggetto filtri alla JSP per mantenere i valori dei filtri selezionati dall'utente
            request.setAttribute("filtriCorrenti", filtri);

            // Passiamo la pagina corrente alla JSP per la gestione della paginazione
            request.setAttribute("paginaCorrente", paginaCorrente);

            // Richiesta standard: carichiamo l'intera infrastruttura del catalogo
            request.getRequestDispatcher("/WEB-INF/view/catalog/catalogo.jsp")
                    .forward(request, response);
        

        } catch (Exception e) {
            e.printStackTrace();
            // In caso di errore, inviamo un messaggio di errore generico al client e logghiamo l'eccezione
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Si è verificato un errore durante il caricamento del catalogo prodotti.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Il catalogo lavora solo in lettura dati (idempotente), quindi deviamo eventuali POST inattese sul doGet
        doGet(request, response);
    }
}