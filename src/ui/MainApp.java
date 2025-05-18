/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author wijde
 */


import javax.swing.*;
import java.sql.SQLException;

public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                JFrame frame = new JFrame("Gestion de Commandes");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1024, 768);
                frame.setLocationRelativeTo(null);
                
                JTabbedPane onglets = new JTabbedPane();
                onglets.addTab("Produits", new GestionProduitsPanel());
                onglets.addTab("Commandes", new GestionCommandesPanel());
                
                frame.add(onglets);
                frame.setVisible(true);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "Erreur de connexion à la base de données: " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}