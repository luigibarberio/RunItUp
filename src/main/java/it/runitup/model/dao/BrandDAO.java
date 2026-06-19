package it.runitup.model.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import it.runitup.model.bean.Brand;
import it.runitup.util.ConnectionPool;

public class BrandDAO {

    // Metodo per recuperare tutti i brand presenti nel database, ordinati alfabeticamente
    public List<Brand> doRetrieveAll() throws SQLException {
        List<Brand> brands = new ArrayList<>();
        
        // Query ordinata alfabeticamente per garantire una buona UX nella sidebar
        String query = "SELECT id_brand, nome_brand FROM brand ORDER BY nome_brand ASC";

        Connection con = ConnectionPool.getConnection();
        
        try {
            // Costrutto try-with-resources per chiudere automaticamente PreparedStatement e ResultSet
            try (PreparedStatement ps = con.prepareStatement(query);
                ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Brand b = new Brand();
                    b.setIdBrand(rs.getInt("id_brand"));
                    b.setNomeBrand(rs.getString("nome_brand"));
                    
                    brands.add(b);
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        return brands;
    }

    // Metodo per recuperare un brand specifico dal database in base al nome
    public Brand doRetrieveByNome(String nomeBrand) throws SQLException {
        Brand brand = null;
        
        String query = "SELECT id_brand, nome_brand FROM brand WHERE LOWER(nome_brand) = LOWER(?)";
        
        Connection con = ConnectionPool.getConnection();
        
        try {
            try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setString(1, nomeBrand);
                
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        brand = new Brand();
                        brand.setIdBrand(rs.getInt("id_brand"));
                        brand.setNomeBrand(rs.getString("nome_brand"));
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
        
        return brand;
    }

    // Metodo per inserire un nuovo brand nel database (restituisce l'id del brand appena inserito)
    public int doSave(Brand brand) throws SQLException {
        String query = "INSERT INTO brand (nome_brand) VALUES (?)";
        
        Connection con = ConnectionPool.getConnection();

        try {
            try (PreparedStatement ps = con.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, brand.getNomeBrand());
                
                int affectedRows = ps.executeUpdate();
                
                if (affectedRows == 0) {
                    throw new SQLException("Creazione brand fallita, nessuna riga interessata.");
                }
                
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Creazione brand fallita, nessun ID ottenuto.");
                    }
                }
            }
        } finally {
            if (con != null) {
                ConnectionPool.releaseConnection(con);
            }
        }
    }

}