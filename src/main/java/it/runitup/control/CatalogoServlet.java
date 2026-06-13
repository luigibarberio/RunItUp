package it.runitup.control;

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

        // 1. ESTRAZIONE E PARSING DEI FILTRI MULTI-SELEZIONE (BRAND)
        String[] brandParams = request.getParameterValues("brand");
        if (brandParams != null && brandParams.length > 0) {
            List<Integer> brandIds = new ArrayList<>();
            for (String idStr : brandParams) {
                try {
                    if (idStr != null && !idStr.trim().isEmpty()) {
                        brandIds.add(Integer.parseInt(idStr.trim()));
                    }
                } catch (NumberFormatException e) {
                    // Protezione: se qualcuno manomette l'URL scrivendo caratteri al posto di ID numerici, lo ignoriamo
                }
            }
            if (!brandIds.isEmpty()) {
                filtri.setBrandIds(brandIds);
            }
        }

        // 2. ESTRAZIONE E PARSING DEI FILTRI MULTI-SELEZIONE ENUM (GENERE)
        String[] genereParams = request.getParameterValues("genere");
        if (genereParams != null && genereParams.length > 0) {
            List<GenereScarpa> generi = new ArrayList<>();
            for (String genStr : genereParams) {
                try {
                    if (genStr != null && !genStr.trim().isEmpty()) {
                        generi.add(GenereScarpa.valueOf(genStr.trim().toUpperCase()));
                    }
                } catch (IllegalArgumentException e) {
                    // Protezione: se il valore testuale non corrisponde all'Enum, viene ignorato in sicurezza
                }
            }
            if (!generi.isEmpty()) {
                filtri.setGeneri(generi);
            }
        }

        // 3. ESTRAZIONE FILTRI DEI RANGE DI PREZZO (BigDecimal)
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

        // 4. ESTRAZIONE FILTRI BOOLEANI (CHECKBOX)
        filtri.setInEvidenza("true".equals(request.getParameter("inEvidenza")));
        filtri.setInOfferta("true".equals(request.getParameter("inOfferta")));
        filtri.setNuoviArrivi("true".equals(request.getParameter("nuoviArrivi")));

        // 5. ESTRAZIONE FILTRI TESTUALI AVANZATI ED ORDINAMENTO
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

        // 6. GESTIONE DELLA PAGINAZIONE (CALCOLO DI LIMIT ED OFFSET)
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

        // 7. INVOCAZIONE DEL DAO ED INVIO ALLA VIEW CORRETTA
        try {
            // Eseguiamo la query dinamica passando l'oggetto filtro configurato
            List<ProdottoDTO> prodotti = prodottoDAO.doRetrieveAllFiltered(filtri);
            
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

            // Chiamata standard: carichiamo l'intera infrastruttura della pagina del catalogo
            // Passiamo i filtri correnti alla JSP per mantenere lo stato dei filtri selezionati
            request.setAttribute("filtriCorrenti", filtri);

            // Passiamo la pagina corrente alla JSP per la gestione della paginazione
            request.setAttribute("paginaCorrente", paginaCorrente);

            // Richiesta standard: carichiamo l'intera infrastruttura del catalogo
            request.getRequestDispatcher("/WEB-INF/view/catalogo.jsp")
                    .forward(request, response);
        

        } catch (Exception e) {
            e.printStackTrace();
            // In caso di anomalie gravi sul DB o sul server, rispondiamo con un codice di errore HTTP 500
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