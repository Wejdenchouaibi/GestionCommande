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
import model.Commande;
import model.LigneCommande;

public class CommandeDAO {
    private static final String INSERT_SQL = "INSERT INTO commandes (numero_commande, client_id, date_commande, statut) VALUES (?, ?, ?, ?)";
    private static final String INSERT_LIGNE_SQL = "INSERT INTO lignes_commande (commande_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT c.*, lc.*, p.* FROM commandes c " +
            "JOIN lignes_commande lc ON c.id = lc.commande_id " +
            "JOIN produits p ON lc.produit_id = p.id " +
            "WHERE c.id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM commandes ORDER BY date_commande DESC";
    private static final String UPDATE_SQL = "UPDATE commandes SET statut = ? WHERE id = ?";
    private static final String GENERATE_NUMERO_SQL = "SELECT CONCAT('CMD', DATE_FORMAT(NOW(), '%Y%m'), LPAD(IFNULL(MAX(SUBSTRING(numero_commande, 12)), 0) + 1, 4) FROM commandes " +
            "WHERE numero_commande LIKE CONCAT('CMD', DATE_FORMAT(NOW(), '%Y%m'), '%')";

    public void create(Commande commande) throws SQLException {
        Connection conn = null;
        PreparedStatement stmtCommande = null;
        PreparedStatement stmtLigne = null;
        
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Générer le numéro de commande
            String numeroCommande = generateNumeroCommande(conn);
            commande.setNumeroCommande(numeroCommande);
            
            // Insertion de la commande
            stmtCommande = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS);
            stmtCommande.setString(1, commande.getNumeroCommande());
            stmtCommande.setInt(2, commande.getClient().getId());
            stmtCommande.setTimestamp(3, Timestamp.valueOf(commande.getDateCommande()));
            stmtCommande.setString(4, commande.getStatut());
            
            stmtCommande.executeUpdate();
            
            // Récupération de l'ID généré
            try (ResultSet rs = stmtCommande.getGeneratedKeys()) {
                if (rs.next()) {
                    commande.setId(rs.getInt(1));
                }
            }
            
            // Insertion des lignes de commande
            stmtLigne = conn.prepareStatement(INSERT_LIGNE_SQL);
            for (LigneCommande ligne : commande.getLignes()) {
                stmtLigne.setInt(1, commande.getId());
                stmtLigne.setInt(2, ligne.getProduit().getId());
                stmtLigne.setInt(3, ligne.getQuantite());
                stmtLigne.setBigDecimal(4, ligne.getPrixUnitaire());
                stmtLigne.addBatch();
            }
            stmtLigne.executeBatch();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (stmtLigne != null) stmtLigne.close();
            if (stmtCommande != null) stmtCommande.close();
            if (conn != null) DatabaseConnection.closeConnection(conn);
        }
    }

    public Commande findById(int id) throws SQLException {
        Commande commande = null;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (commande == null) {
                        commande = new Commande();
                        commande.setId(rs.getInt("c.id"));
                        commande.setNumeroCommande(rs.getString("c.numero_commande"));
                        commande.setDateCommande(rs.getTimestamp("c.date_commande").toLocalDateTime());
                        commande.setStatut(rs.getString("c.statut"));
                        // Note: Vous devrez injecter le Client via ClientDAO
                    }
                    
                    // Ajouter les lignes de commande
                    LigneCommande ligne = new LigneCommande();
                    ligne.setId(rs.getInt("lc.id"));
                    ligne.setQuantite(rs.getInt("lc.quantite"));
                    ligne.setPrixUnitaire(rs.getBigDecimal("lc.prix_unitaire"));
                    // Note: Vous devrez injecter le Produit via ProduitDAO
                    
                    commande.ajouterLigne(ligne.getProduit(), ligne.getQuantite());
                }
            }
        }
        return commande;
    }

    public List<Commande> findAll() throws SQLException {
        List<Commande> commandes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            while (rs.next()) {
                Commande commande = new Commande();
                commande.setId(rs.getInt("id"));
                commande.setNumeroCommande(rs.getString("numero_commande"));
                commande.setDateCommande(rs.getTimestamp("date_commande").toLocalDateTime());
                commande.setStatut(rs.getString("statut"));
                // Note: Vous devrez injecter le Client via ClientDAO
                
                commandes.add(commande);
            }
        }
        return commandes;
    }

    public void updateStatut(int commandeId, String statut) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            stmt.setString(1, statut);
            stmt.setInt(2, commandeId);
            stmt.executeUpdate();
        }
    }

    private String generateNumeroCommande(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(GENERATE_NUMERO_SQL)) {
            
            if (rs.next()) {
                return rs.getString(1) + rs.getString(2);
            }
        }
        throw new SQLException("Impossible de générer le numéro de commande");
    }
}