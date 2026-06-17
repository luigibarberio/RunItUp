package it.runitup.control;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/images/prodotti")
public class ImmagineProdottoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idParam = request.getParameter("id");
        
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID immagine mancante");
            return;
        }

        // Costruiamo il path System-Agnostic basato sulla Home dell'utente
        String uploadRoot = System.getProperty("user.home") + File.separator + "runitup_media" + File.separator + "prodotti";
        
        // Creiamo il riferimento al file esatto (es. prodotto_5.png)
        File imageFile = new File(uploadRoot + File.separator + "prodotto_" + idParam + ".png");

        // Se il file non esiste sul disco, restituiamo un errore 404 (Not Found).
        // Questo dirà al browser di usare l'attributo 'onerror' della JSP (il placeholder).
        if (!imageFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // Indichiamo al browser che gli stiamo inviando un'immagine JPEG
        response.setContentType("image/jpeg");
        
        // Ottimizzazione Java 11: Files.copy copia l'intero file nello stream di risposta in modo iper-efficiente
        Files.copy(imageFile.toPath(), response.getOutputStream());
    }
}