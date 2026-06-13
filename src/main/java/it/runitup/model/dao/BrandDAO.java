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

    // Qui in futuro potremo aggiungere i metodi CRUD per l'Area Admin (create, update, delete brand)

}