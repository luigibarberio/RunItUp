package it.runitup.listener;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.FileAlreadyExistsException;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

// Inizializzatore dell'applicazione che copia le immagini di seed dal WAR al filesystem del computer host all'avvio dell'applicazione.
@WebListener
public class MediaInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        // Si crea la cartella "runitup_media/prodotti" nella home directory dell'utente se non esiste già
        String userHome = System.getProperty("user.home");
        File mediaDir = new File(userHome + File.separator + "runitup_media" + File.separator + "prodotti");
        
        // Se la cartella non esiste, la creiamo
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }

        // Copia delle immagini di seed dal WAR al filesystem del computer host
        String seedPath = "/WEB-INF/resources/images/seeds/"; // Cartella dentro il WAR dove sono presenti le immagini di seed
        Set<String> seedFiles = context.getResourcePaths(seedPath);
        
        if (seedFiles != null) {
            for (String seedFilePath : seedFiles) {
                // Estrazione del nome del file dall'intero percorso (ad esempio, da "/WEB-INF/resources/images/seeds/prodotto_1_0.png" si ottiene "prodotto_1_0.png")
                String fileName = seedFilePath.substring(seedFilePath.lastIndexOf("/") + 1);
                
                // Creazione del percorso di destinazione sul filesystem del computer host
                File destFile = new File(mediaDir, fileName);
                
                // Se il file non esiste già, lo copiamo dal WAR al filesystem del computer host
                if (!destFile.exists()) {
                    try (InputStream is = context.getResourceAsStream(seedFilePath)) {
                        if (is != null) {
                            Files.copy(is, destFile.toPath());
                            context.log("Copiata immagine di seed: " + fileName + " in " + destFile.getAbsolutePath());
                        }
                    } catch (FileAlreadyExistsException faee) {
                        // Questo caso non dovrebbe mai verificarsi grazie al controllo precedente, la gestione dell'eccezione è solo per notificare l'errore nel log
                        context.log("File già esistente: " + fileName);
                    } catch (Exception e) {
                        // Log dell'errore in caso di problemi durante la copia del file
                        context.log("Errore durante la copia dell'immagine di seed: " + fileName, e);
                    }
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Non è necessario fare nulla alla chiusura del contesto per questo listener
    }
}