/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author wijde
 */

import dao.ProduitDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import model.Produit;

public class GestionProduitsPanel extends JPanel {
    private final ProduitDAO produitDAO;
    private final DefaultTableModel tableModel;
    private final JTable tableProduits;

    public GestionProduitsPanel() throws SQLException {
        this.produitDAO = new ProduitDAO();
        this.tableModel = new DefaultTableModel(
            new Object[]{"ID", "Référence", "Nom", "Prix", "Stock"}, 0);
        this.tableProduits = new JTable(tableModel);

        initUI();
        chargerProduits();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Barre d'outils
        JToolBar toolBar = new JToolBar();
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnModifier = new JButton("Modifier");
        JButton btnSupprimer = new JButton("Supprimer");

        btnAjouter.addActionListener(this::ajouterProduit);
        btnModifier.addActionListener(this::modifierProduit);
        btnSupprimer.addActionListener(this::supprimerProduit);

        toolBar.add(btnAjouter);
        toolBar.add(btnModifier);
        toolBar.add(btnSupprimer);

        // Tableau
        tableProduits.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(tableProduits);

        add(toolBar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void chargerProduits() {
        try {
            tableModel.setRowCount(0);
            List<Produit> produits = produitDAO.findAll();

            for (Produit p : produits) {
                tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getReference(),
                    p.getNom(),
                    p.getPrix() + " €",
                    p.getStock()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des produits: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterProduit(ActionEvent e) {
        JDialog dialog = new JDialog((Frame)SwingUtilities.getWindowAncestor(this), "Nouveau Produit", true);
        dialog.setSize(400, 300);
        dialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        JTextField txtRef = new JTextField();
        JTextField txtNom = new JTextField();
        JTextField txtPrix = new JTextField();
        JTextField txtStock = new JTextField();
        JTextArea txtDesc = new JTextArea();

        formPanel.add(new JLabel("Référence:"));
        formPanel.add(txtRef);
        formPanel.add(new JLabel("Nom:"));
        formPanel.add(txtNom);
        formPanel.add(new JLabel("Prix:"));
        formPanel.add(txtPrix);
        formPanel.add(new JLabel("Stock:"));
        formPanel.add(txtStock);
        formPanel.add(new JLabel("Description:"));
        formPanel.add(new JScrollPane(txtDesc));

        JButton btnValider = new JButton("Valider");
        btnValider.addActionListener(ev -> {
            try {
                Produit p = new Produit(
                    txtRef.getText(),
                    txtNom.getText(),
                    new BigDecimal(txtPrix.getText()),
                    Integer.parseInt(txtStock.getText())
                );
                p.setDescription(txtDesc.getText());
                produitDAO.create(p);
                chargerProduits();
                dialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog,
                    "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(btnValider, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void modifierProduit(ActionEvent e) {
        int selectedRow = tableProduits.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un produit",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        // Implémentation similaire à ajouterProduit mais avec pré-remplissage
    }

    private void supprimerProduit(ActionEvent e) {
        int selectedRow = tableProduits.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un produit",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) tableModel.getValueAt(selectedRow, 0);
        String nom = (String) tableModel.getValueAt(selectedRow, 2);

        int confirm = JOptionPane.showConfirmDialog(this,
            "Supprimer le produit " + nom + " ?",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                produitDAO.delete(id);
                chargerProduits();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de la suppression: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
