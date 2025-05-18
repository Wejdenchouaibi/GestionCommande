/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

/**
 *
 * @author wijde
 */


import dao.ClientDAO;
import dao.CommandeDAO;
import dao.ProduitDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import model.Client;
import model.Commande;
import model.LigneCommande;
import model.Produit;

public class GestionCommandesPanel extends JPanel {
    private final CommandeDAO commandeDAO;
    private final ClientDAO clientDAO;
    private final ProduitDAO produitDAO;
    private final DefaultTableModel modelCommandes;
    private final DefaultTableModel modelPanier;
    private final JTable tableCommandes;
    private final JTable tablePanier;
    private final JComboBox<Client> comboClients;
    private final JComboBox<Produit> comboProduits;
    private final JLabel lblTotal;
    private final List<LigneCommande> panier = new ArrayList<>();

    public GestionCommandesPanel() throws SQLException {
        this.commandeDAO = new CommandeDAO();
        this.clientDAO = new ClientDAO();
        this.produitDAO = new ProduitDAO();
        this.modelCommandes = new DefaultTableModel(
            new Object[]{"N°", "Date", "Client", "Total", "Statut"}, 0);
        this.modelPanier = new DefaultTableModel(
            new Object[]{"Produit", "Prix", "Qté", "Total"}, 0);
        this.tableCommandes = new JTable(modelCommandes);
        this.tablePanier = new JTable(modelPanier);
        this.comboClients = new JComboBox<>();
        this.comboProduits = new JComboBox<>();
        this.lblTotal = new JLabel("Total: 0.00 €");

        initUI();
        chargerDonnees();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // Panel principal avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();

        // Onglet "Nouvelle Commande"
        JPanel panelNouvelleCommande = new JPanel(new BorderLayout());

        // Panel client et produits
        JPanel panelHaut = new JPanel(new GridLayout(1, 4, 5, 5));
        panelHaut.add(new JLabel("Client:"));
        panelHaut.add(comboClients);
        panelHaut.add(new JLabel("Produit:"));
        panelHaut.add(comboProduits);

        // Boutons panier
        JPanel panelBoutons = new JPanel();
        JButton btnAjouter = new JButton("Ajouter au panier");
        JButton btnSupprimer = new JButton("Supprimer du panier");
        JButton btnVider = new JButton("Vider le panier");
        JButton btnValider = new JButton("Valider la commande");

        btnAjouter.addActionListener(this::ajouterAuPanier);
        btnSupprimer.addActionListener(this::supprimerDuPanier);
        btnVider.addActionListener(this::viderPanier);
        btnValider.addActionListener(this::validerCommande);

        panelBoutons.add(btnAjouter);
        panelBoutons.add(btnSupprimer);
        panelBoutons.add(btnVider);
        panelBoutons.add(btnValider);

        // Panier
        JScrollPane scrollPanier = new JScrollPane(tablePanier);
        JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelTotal.add(lblTotal);

        panelNouvelleCommande.add(panelHaut, BorderLayout.NORTH);
        panelNouvelleCommande.add(scrollPanier, BorderLayout.CENTER);
        panelNouvelleCommande.add(panelBoutons, BorderLayout.SOUTH);
        panelNouvelleCommande.add(panelTotal, BorderLayout.SOUTH);

        // Onglet "Historique"
        JScrollPane scrollCommandes = new JScrollPane(tableCommandes);
        
        tabbedPane.addTab("Nouvelle Commande", panelNouvelleCommande);
        tabbedPane.addTab("Historique", scrollCommandes);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private void chargerDonnees() {
        try {
            // Charger les combobox
            comboClients.removeAllItems();
            for (Client c : clientDAO.findAll()) {
                comboClients.addItem(c);
            }

            comboProduits.removeAllItems();
            for (Produit p : produitDAO.findAll()) {
                comboProduits.addItem(p);
            }

            // Charger les commandes
            chargerCommandes();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerCommandes() {
        try {
            modelCommandes.setRowCount(0);
            List<Commande> commandes = commandeDAO.findAll();

            for (Commande c : commandes) {
                modelCommandes.addRow(new Object[]{
                    c.getNumeroCommande(),
                    c.getDateCommande().toString(),
                    c.getClient().toString(),
                    c.getTotal() + " €",
                    c.getStatut()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Erreur lors du chargement des commandes: " + e.getMessage(),
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterAuPanier(ActionEvent e) {
        Produit produit = (Produit) comboProduits.getSelectedItem();
        if (produit == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un produit",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String quantiteStr = JOptionPane.showInputDialog(this,
            "Quantité pour " + produit.getNom() + ":",
            "1");

        try {
            int quantite = Integer.parseInt(quantiteStr);
            if (quantite <= 0) throw new NumberFormatException();

            LigneCommande ligne = new LigneCommande();
            ligne.setProduit(produit);
            ligne.setQuantite(quantite);
            ligne.setPrixUnitaire(produit.getPrix());

            panier.add(ligne);
            actualiserPanier();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Quantité invalide",
                "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerDuPanier(ActionEvent e) {
        int selectedRow = tablePanier.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner une ligne",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        panier.remove(selectedRow);
        actualiserPanier();
    }

    private void viderPanier(ActionEvent e) {
        panier.clear();
        actualiserPanier();
    }

    private void validerCommande(ActionEvent e) {
        Client client = (Client) comboClients.getSelectedItem();
        if (client == null) {
            JOptionPane.showMessageDialog(this,
                "Veuillez sélectionner un client",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (panier.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Le panier est vide",
                "Avertissement", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "Confirmer la commande pour " + client + " ?\nTotal: " + calculerTotalPanier() + " €",
            "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                Commande commande = new Commande(client);
                commande.setDateCommande(LocalDateTime.now());
                
                for (LigneCommande ligne : panier) {
                    commande.ajouterLigne(ligne.getProduit(), ligne.getQuantite());
                }

                commandeDAO.create(commande);
                panier.clear();
                actualiserPanier();
                chargerCommandes();
                
                JOptionPane.showMessageDialog(this,
                    "Commande enregistrée avec succès",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this,
                    "Erreur lors de l'enregistrement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualiserPanier() {
        modelPanier.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;

        for (LigneCommande ligne : panier) {
            BigDecimal sousTotal = ligne.getPrixUnitaire().multiply(BigDecimal.valueOf(ligne.getQuantite()));
            modelPanier.addRow(new Object[]{
                ligne.getProduit().getNom(),
                ligne.getPrixUnitaire() + " €",
                ligne.getQuantite(),
                sousTotal + " €"
            });
            total = total.add(sousTotal);
        }

        lblTotal.setText("Total: " + total + " €");
    }

    private BigDecimal calculerTotalPanier() {
        return panier.stream()
            .map(l -> l.getPrixUnitaire().multiply(BigDecimal.valueOf(l.getQuantite())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}