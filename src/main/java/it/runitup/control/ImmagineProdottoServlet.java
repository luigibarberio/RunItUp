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
        // Se non viene passato l'indice, assumiamo che si voglia la prima immagine (indice 0)
        String indexParam = request.getParameter("index");
        String index = (indexParam != null && !indexParam.trim().isEmpty()) ? indexParam.trim() : "0";
        
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "ID immagine mancante");
            return;
        }

        String uploadRoot = System.getProperty("user.home") + File.separator + "runitup_media" + File.separator + "prodotti";
        
        // Creiamo il riferimento al file richiesto (es. prodotto_5_1.png)
        File imageFile = new File(uploadRoot + File.separator + "prodotto_" + idParam + "_" + index + ".png");

        // Se l'immagine richiesta non esiste e l'indice è 1, proviamo a servire la primaria (indice 0)
        if (!imageFile.exists() && "1".equals(index)) {
            imageFile = new File(uploadRoot + File.separator + "prodotto_" + idParam + "_0.png");
        }

        // Se nemmeno la primaria esiste, allora diamo 404 (così la JSP usa il placeholder SVG)
        if (!imageFile.exists()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("image/png");
        
        Files.copy(imageFile.toPath(), response.getOutputStream());
    }
}