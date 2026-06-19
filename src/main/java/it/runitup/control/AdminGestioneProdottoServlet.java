package it.runitup.control;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import it.runitup.model.bean.Brand;
import it.runitup.model.bean.GenereScarpa;
import it.runitup.model.bean.Prodotto;
import it.runitup.model.bean.TipoAppoggio;
import it.runitup.model.bean.TipoPasso;
import it.runitup.model.bean.TipoTerreno;
import it.runitup.model.dao.BrandDAO;
import it.runitup.model.dao.ProdottoDAO;

@WebServlet("/admin/prodotto/gestione")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class AdminGestioneProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private final ProdottoDAO prodottoDAO = new ProdottoDAO();
    private final BrandDAO brandDAO = new BrandDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        
        try {
            // REQUISITO: Carichiamo la lista dei brand per la datalist HTML5
            request.setAttribute("listaBrand", brandDAO.doRetrieveAll());

            if (idParam != null && !idParam.trim().isEmpty()) {
                int idProdotto = Integer.parseInt(idParam.trim());
                Prodotto prodottoEsistente = prodottoDAO.getProdottoById(idProdotto);
                
                if (prodottoEsistente != null) {
                    request.setAttribute("prodottoForm", prodottoEsistente);
                    request.setAttribute("modalita", "MODIFICA");
                } else {
                    request.setAttribute("modalita", "INSERIMENTO");
                }
            } else {
                request.setAttribute("modalita", "INSERIMENTO");
            }
            
            request.getRequestDispatcher("/WEB-INF/view/admin_prodotto_form.jsp").forward(request, response);
            
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID prodotto non valido.");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore nel recupero dati prodotto.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        
        String idParam = request.getParameter("idProdotto");
        String nome = request.getParameter("nome");
        String descrizione = request.getParameter("descrizione");
        String nomeBrandParam = request.getParameter("nomeBrand");
        String genereParam = request.getParameter("genere");
        String terrenoParam = request.getParameter("tipoTerreno");
        String appoggioParam = request.getParameter("tipoAppoggio");
        String passoParam = request.getParameter("tipoPasso");
        boolean inEvidenza = request.getParameter("inEvidenza") != null;

        boolean isModifica = (idParam != null && !idParam.trim().isEmpty());

        // 1. RICOSTRUIAMO SUBITO IL BEAN CON I DATI INVIATI DALL'UTENTE
        // Così se la validazione fallisce, glieli rimandiamo indietro senza svuotare il form!
        Prodotto prodotto = new Prodotto();
        prodotto.setNome(nome != null ? nome.trim() : "");
        prodotto.setDescrizione(descrizione != null ? descrizione.trim() : "");
        prodotto.setInEvidenza(inEvidenza);
        
        Brand fallbackBrand = new Brand();
        fallbackBrand.setNomeBrand(nomeBrandParam != null ? nomeBrandParam.trim() : "");
        prodotto.setBrand(fallbackBrand);

        if (genereParam != null) prodotto.setGenere(GenereScarpa.valueOf(genereParam.toUpperCase()));
        if (terrenoParam != null) prodotto.setTipoTerreno(TipoTerreno.valueOf(terrenoParam.toUpperCase()));
        if (appoggioParam != null) prodotto.setTipoAppoggio(TipoAppoggio.valueOf(appoggioParam.toUpperCase()));
        if (passoParam != null) prodotto.setTipoPasso(TipoPasso.valueOf(passoParam.toUpperCase()));
        
        if (isModifica) {
            prodotto.setIdProdotto(Integer.parseInt(idParam.trim()));
        }

        // 2. VALIDAZIONE LATO SERVER (Il Paracadute)
        if (nome == null || nome.trim().isEmpty() || nomeBrandParam == null || nomeBrandParam.trim().isEmpty()) {
            gestisciErroreERitornaAlForm(request, response, prodotto, isModifica, "I campi Nome e Brand sono obbligatori.");
            return;
        }

        try {
            // 3. GESTIONE UPSERT DEL BRAND (Cerca o Crea)
            Brand brand = brandDAO.doRetrieveByNome(nomeBrandParam.trim());
            
            if (brand == null) {
                brand = new Brand();
                brand.setNomeBrand(nomeBrandParam.trim());
                int nuovoIdBrand = brandDAO.doSave(brand);
                
                if (nuovoIdBrand <= 0) {
                    throw new SQLException("Impossibile creare il nuovo Brand.");
                }
                brand.setIdBrand(nuovoIdBrand);
            }
            prodotto.setBrand(brand);

            // 4. SALVATAGGIO PRODOTTO
            int idProdottoFinal = -1;

            if (isModifica) {
                idProdottoFinal = prodotto.getIdProdotto();
                boolean esitoUpdate = prodottoDAO.updateProdotto(prodotto);
                if (!esitoUpdate) throw new SQLException("L'aggiornamento del prodotto è fallito.");
            } else {
                idProdottoFinal = prodottoDAO.insertProdotto(prodotto);
                if (idProdottoFinal == -1) throw new SQLException("L'inserimento del prodotto è fallito.");
            }

            // 5. GESTIONE UPLOAD IMMAGINI
            String uploadPath = System.getProperty("user.home") + File.separator + "runitup_media" + File.separator + "prodotti";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) uploadDir.mkdirs(); 

            salvaImmagineFisica(request.getPart("immagine_0"), uploadPath, idProdottoFinal, 0);
            salvaImmagineFisica(request.getPart("immagine_1"), uploadPath, idProdottoFinal, 1);

            // 6. REDIRECT AL SUCCESSO
            if (isModifica) {
                response.sendRedirect(request.getContextPath() + "/admin/prodotti?msg=updated");
            } else {
                response.sendRedirect(request.getContextPath() + "/admin/prodotto/varianti?idProdotto=" + idProdottoFinal + "&msg=created");
            }

        } catch (Exception e) {
            e.printStackTrace();
            gestisciErroreERitornaAlForm(request, response, prodotto, isModifica, "Errore critico durante il salvataggio: " + e.getMessage());
        }
    }

    /**
     * HELPER METHOD: Ricarica la pagina JSP preservando i dati digitati dall'utente (UX Avanzata)
     */
    private void gestisciErroreERitornaAlForm(HttpServletRequest request, HttpServletResponse response, Prodotto prodotto, boolean isModifica, String errorMessage) throws ServletException, IOException {
        request.setAttribute("erroreMessage", errorMessage);
        request.setAttribute("prodottoForm", prodotto);
        request.setAttribute("modalita", isModifica ? "MODIFICA" : "INSERIMENTO");
        
        try {
            request.setAttribute("listaBrand", brandDAO.doRetrieveAll());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        request.getRequestDispatcher("/WEB-INF/view/admin_prodotto_form.jsp").forward(request, response);
    }

    private void salvaImmagineFisica(Part part, String uploadPath, int idProdotto, int index) throws IOException {
        if (part != null && part.getSize() > 0) {
            String fileName = "prodotto_" + idProdotto + "_" + index + ".png";
            File fileDestinazione = new File(uploadPath + File.separator + fileName);
            try (InputStream input = part.getInputStream()) {
                Files.copy(input, fileDestinazione.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }
}