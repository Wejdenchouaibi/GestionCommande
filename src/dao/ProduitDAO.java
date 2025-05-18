/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

/**
 *
 * @author wijde
 */


import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Produit;

public class ProduitDAO {
    private static final String INSERT_SQL = "INSERT INTO produits (reference, nom, description, prix, stock) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM produits WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM produits ORDER BY nom";
    private static final String UPDATE_SQL = "UPDATE produits SET reference = ?, nom = ?, description = ?, prix = ?, stock = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM produits WHERE id = ?";
    private static final String UPDATE_STOCK_SQL = "UPDATE produits SET stock = stock - ? WHERE id = ? AND stock >= ?";

    public void create(Produit produit) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, produit.getReference());
            stmt.setString(2, produit.getNom());
            stmt.setString(3, produit.getDescription());
            stmt.setBigDecimal(4, produit.getPrix());
            stmt.setInt(5, produit.getStock());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    produit.setId(rs.getInt(1));
                }
            }
        }
    }

    public Produit findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduit(rs);
                }
            }
        }
        return null;
    }

    public List<Produit> findAll() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            while (rs.next()) {
                produits.add(mapProduit(rs));
            }
        }
        return produits;
    }

    public void update(Produit produit) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, produit.getReference());
            stmt.setString(2, produit.getNom());
            stmt.setString(3, produit.getDescription());
            stmt.setBigDecimal(4, produit.getPrix());
            stmt.setInt(5, produit.getStock());
            stmt.setInt(6, produit.getId());
            
            stmt.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(DELETE_SQL)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public boolean decrementerStock(int produitId, int quantite) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_STOCK_SQL)) {
            
            stmt.setInt(1, quantite);
            stmt.setInt(2, produitId);
            stmt.setInt(3, quantite);
            
            return stmt.executeUpdate() > 0;
        }
    }

    private Produit mapProduit(ResultSet rs) throws SQLException {
        Produit produit = new Produit();
        produit.setId(rs.getInt("id"));
        produit.setReference(rs.getString("reference"));
        produit.setNom(rs.getString("nom"));
        produit.setDescription(rs.getString("description"));
        produit.setPrix(rs.getBigDecimal("prix"));
        produit.setStock(rs.getInt("stock"));
        return produit;
    }
}

