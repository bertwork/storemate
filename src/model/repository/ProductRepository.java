package model.repository;

import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductRepository {

    private static final String SELECT_ALL = """
            SELECT p.id, p.product_name, p.price, p.image_path, c.category_name
            FROM products p
            JOIN categories c ON c.id = p.category_id
            ORDER BY p.category_id, p.id
            """;

    public List<Product> findAll() throws SQLException {
        List<Product> products = new ArrayList<>();

        try (Connection con = Database.connect();
             PreparedStatement st = con.prepareStatement(SELECT_ALL);
             ResultSet rs = st.executeQuery()) {

            while (rs.next()) {
                products.add(new Product(
                        rs.getInt("id"),
                        rs.getString("product_name"),
                        rs.getString("category_name"),
                        rs.getBigDecimal("price"),
                        rs.getString("image_path"))); // null for now
            }
        }
        return products;
    }
}