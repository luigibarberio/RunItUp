package it.runitup.model.util;

import org.postgresql.ds.PGSimpleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ConnectionPool {

    private static int INITIAL_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 20;

    private static List<Connection> freeDbConnections;
    private static int currentPoolSize = 0;
    private static boolean initialized = false;
    
    private static PGSimpleDataSource pgDataSource;

    // Metodo di utilità per leggere variabili d'ambiente o System Properties con fallback a default
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null) {
            value = System.getProperty(key);
        }
        return (value != null && !value.trim().isEmpty()) ? value : defaultValue;
    }

    // Inizializza il pool con un numero specificato di connessioni (o default se <= 0)
    public static synchronized void init(int poolSize) throws SQLException {
        if (initialized) {
            return;
        }

        // Costruzione dinamica dell'URL JDBC
        String dbHost = getEnvOrDefault("DB_HOST", "localhost");
        String dbPort = getEnvOrDefault("DB_PORT", "5432");
        String dbName = getEnvOrDefault("DB_NAME", "runitup_db");
        String jdbcUrl = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

        // Inizializziazione del DataSource con i parametri letti da ambiente o di default
        pgDataSource = new PGSimpleDataSource();
        pgDataSource.setURL(getEnvOrDefault(jdbcUrl, "jdbc:postgresql://localhost:5432/runitup_db"));
        pgDataSource.setUser(getEnvOrDefault("DB_USER", "postgres"));
        pgDataSource.setPassword(getEnvOrDefault("DB_PASSWORD", "password"));

        // Inizializzazione del Pool
        freeDbConnections = new LinkedList<>();
        INITIAL_POOL_SIZE = (poolSize > 0) ? poolSize : INITIAL_POOL_SIZE;

        // Creazione delle connessioni iniziali
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            freeDbConnections.add(createDBConnection());
        }
        
        currentPoolSize = INITIAL_POOL_SIZE;
        initialized = true;
        
        System.out.println("[ConnectionPool] Inizializzato tramite DataSource. Connessioni attive: " + currentPoolSize);
    }

    // Crea una nuova connessione al DataSource
    private static Connection createDBConnection() throws SQLException {
        Connection connection = pgDataSource.getConnection();
        connection.setAutoCommit(true);
        return connection;
    }

    // Restituisce una connessione dal pool, creando una nuova se il pool è vuoto e non ha raggiunto il limite massimo
    public static synchronized Connection getConnection() throws SQLException {
        if (!initialized) {
            throw new SQLException("[ConnectionPool] Errore: chiama init() prima!");
        }

        if (freeDbConnections.isEmpty()) {
            if (currentPoolSize < MAX_POOL_SIZE) {
                currentPoolSize++;
                return createDBConnection(); // Chiede una nuova connessione al DataSource
            } else {
                throw new SQLException("[ConnectionPool] Errore: Pool esaurito.");
            }
        }
        
        Connection conn = freeDbConnections.remove(0);
        
        try {
            // Controlla se la connessione è ancora valida (timeout 1 secondo)
            if (!conn.isValid(1)) {
                conn.close();
                currentPoolSize--;
                return getConnection(); // Ricorsione
            }
        } catch (SQLException e) {
            currentPoolSize--;
            return getConnection();
        }
        
        return conn;
    }

    // Restituisce la connessione al pool al termine dell'utilizzo.
    public static synchronized void releaseConnection(Connection connection) {
        if (connection != null && initialized) {
            try {
                if (!connection.isClosed() && connection.isValid(1)) {
                    connection.setAutoCommit(true); // Reset di sicurezza
                    freeDbConnections.add(connection);
                } else {
                    currentPoolSize--;
                }
            } catch (SQLException e) {
                currentPoolSize--;
            }
        }
    }
}