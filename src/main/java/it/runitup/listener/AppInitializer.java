package it.runitup.listener;

import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import it.runitup.util.ConnectionPool;

// Inizializzatore dell'applicazione che configura il pool di connessioni al database all'avvio dell'applicazione.
@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        // Parametro di configurazione per il pool size ottenuto dal web.xml (con conversione da String a int e fallback a 5 se non specificato)
        String poolSizeParam = sce.getServletContext().getInitParameter("poolSize");
        int poolSize = (poolSizeParam != null) ? Integer.parseInt(poolSizeParam) : 5;
        
        try {
            ConnectionPool.init(poolSize);
            System.out.println("[AppInitializer] ConnectionPool inizializzato con pool size: " + poolSize);
        } catch (SQLException e) {
            System.err.println("[AppInitializer] Impossibile inizializzare il ConnectionPool");
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ConnectionPool.shutdown();
        System.out.println("[AppInitializer] ConnectionPool chiuso correttamente");
    }
}