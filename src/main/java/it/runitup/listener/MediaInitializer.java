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

@WebListener
public class MediaInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        
        // 1. Definiamo dove le immagini dovrebbero trovarsi sul PC host
        String userHome = System.getProperty("user.home");
        File mediaDir = new File(userHome + File.separator + "runitup_media" + File.separator + "prodotti");
        
        // Se la cartella non esiste, la creiamo
        if (!mediaDir.exists()) {
            mediaDir.mkdirs();
        }

        // 2. Chiediamo a Tomcat la lista dei file contenuti nel nostro "seed" interno al WAR
        String seedPath = "/WEB-INF/resources/images/seeds/"; // Cartella dentro il WAR dove sono presenti le immagini di seed
        Set<String> seedFiles = context.getResourcePaths(seedPath);
        
        if (seedFiles != null) {
            for (String seedFilePath : seedFiles) {
                // Estraiamo il nome del file dall'intero path (es. da "/WEB-INF/resources/images/seeds/prodotto_5.png" si ottiene "prodotto_5.png")
                String fileName = seedFilePath.substring(seedFilePath.lastIndexOf("/") + 1);
                
                // File di destinazione sul computer host
                File destFile = new File(mediaDir, fileName);
                
                // 3. Se l'immagine non è già presente sul computer host, la copiamo dal WAR!
                if (!destFile.exists()) {
                    try (InputStream is = context.getResourceAsStream(seedFilePath)) {
                        if (is != null) {
                            Files.copy(is, destFile.toPath());
                            context.log("Copiata immagine di seed: " + fileName + " in " + destFile.getAbsolutePath());
                        }
                    } catch (FileAlreadyExistsException faee) {
                        // Non andiamo a sovrascrivere il contenuto del file system per una questione di sicurezza
                        context.log("File già esistente: " + fileName);
                    } catch (Exception e) {
                        context.log("Errore durante la copia dell'immagine di seed: " + fileName, e);
                    }
                }
            }
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Alla chiusura del server non dobbiamo fare nulla di speciale
    }
}