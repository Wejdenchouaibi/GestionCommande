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
import model.Client;

public class ClientDAO {
    private static final String INSERT_SQL = "INSERT INTO clients (numero_client, nom, prenom, email, telephone, adresse) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SELECT_BY_ID_SQL = "SELECT * FROM clients WHERE id = ?";
    private static final String SELECT_ALL_SQL = "SELECT * FROM clients ORDER BY nom, prenom";
    private static final String UPDATE_SQL = "UPDATE clients SET numero_client = ?, nom = ?, prenom = ?, email = ?, telephone = ?, adresse = ? WHERE id = ?";
    private static final String DELETE_SQL = "DELETE FROM clients WHERE id = ?";
    private static final String SELECT_BY_NUMERO_SQL = "SELECT * FROM clients WHERE numero_client = ?";

    public void create(Client client) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            
            setClientParameters(stmt, client);
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    client.setId(rs.getInt(1));
                }
            }
        }
    }

    public Client findById(int id) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_ID_SQL)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapClient(rs);
                }
            }
        }
        return null;
    }

    public Client findByNumero(String numeroClient) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SELECT_BY_NUMERO_SQL)) {
            
            stmt.setString(1, numeroClient);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapClient(rs);
                }
            }
        }
        return null;
    }

    public List<Client> findAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SELECT_ALL_SQL)) {
            
            while (rs.next()) {
                clients.add(mapClient(rs));
            }
        }
        return clients;
    }

    public void update(Client client) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(UPDATE_SQL)) {
            
            setClientParameters(stmt, client);
            stmt.setInt(7, client.getId());
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

    private Client mapClient(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id"));
        client.setNumeroClient(rs.getString("numero_client"));
        client.setNom(rs.getString("nom"));
        client.setPrenom(rs.getString("prenom"));
        client.setEmail(rs.getString("email"));
        client.setTelephone(rs.getString("telephone"));
        client.setAdresse(rs.getString("adresse"));
        return client;
    }

    private void setClientParameters(PreparedStatement stmt, Client client) throws SQLException {
        stmt.setString(1, client.getNumeroClient());
        stmt.setString(2, client.getNom());
        stmt.setString(3, client.getPrenom());
        stmt.setString(4, client.getEmail());
        stmt.setString(5, client.getTelephone());
        stmt.setString(6, client.getAdresse());
    }

    // Génère un numéro client séquentiel (ex: CL20230001)
    public String generateNumeroClient() throws SQLException {
        String prefix = "CL" + java.time.LocalDate.now().getYear();
        String sql = "SELECT MAX(CAST(SUBSTRING(numero_client, 7) AS UNSIGNED)) FROM clients WHERE numero_client LIKE ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefix + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                int lastNum = rs.next() ? rs.getInt(1) : 0;
                return prefix + String.format("%04d", lastNum + 1);
            }
        }
    }
}
