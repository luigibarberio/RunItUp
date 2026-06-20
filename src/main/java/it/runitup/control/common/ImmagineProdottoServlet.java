package it.runitup.control.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Servlet per servire le immagini dei prodotti. Riceve richieste GET con parametri "id" (ID del prodotto) e "index" (indice dell'immagine, opzionale, default 0). Se l'immagine richiesta non esiste, restituisce un errore 404.
@WebServlet("/images/prodotti")
public class ImmagineProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        // Se non viene passato l'indice, assumiamo che si voglia la prima immagine (indice 0)
        String indexParam = request.getParameter("index");
        String index = (indexParam != null && !indexParam.trim().isEmpty()) ? indexParam.trim() : "0";
        
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID immagine mancante");
            return;
        }

        String uploadRoot = System.getProperty("user.home") + File.separator + "runitup_media" + File.separator + "prodotti";
        
        // Costruzione del percorso dell'immagine basata sull'ID del prodotto e sull'indice dell'immagine
        File imageFile = new File(uploadRoot + File.separator + "prodotto_" + idParam + "_" + index + ".png");

        // Se l'immagine specificata non esiste, proviamo a servire la prima immagine (indice 0) come fallback
        if (!imageFile.exists() && "1".equals(index)) {
            imageFile = new File(uploadRoot + File.separator + "prodotto_" + idParam + "_0.png");
        }

        // Se l'immagine non esiste ancora, restituiamo un errore 404
        if (!imageFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Impostiamo il tipo di contenuto della risposta come immagine PNG e copiamo il file dell'immagine nel flusso di output della risposta
        response.setContentType("image/png");
        
        Files.copy(imageFile.toPath(), response.getOutputStream());
    }
}