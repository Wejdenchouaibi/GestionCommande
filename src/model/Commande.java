/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author wijde
 */


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Commande {
    private int id;
    private String numeroCommande;
    private Client client;
    private LocalDateTime dateCommande;
    private String statut;
    private final List<LigneCommande> lignes = new ArrayList<>();
    
    // Constructeurs
    public Commande() {
        this.dateCommande = LocalDateTime.now();
        this.statut = "EN_ATTENTE";
    }
    
    public Commande(Client client) {
        this();
        this.client = client;
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getNumeroCommande() { return numeroCommande; }
    public void setNumeroCommande(String numeroCommande) { this.numeroCommande = numeroCommande; }
    
    public Client getClient() { return client; }
    public void setClient(Client client) { this.client = client; }
    
    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }
    
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    
    public List<LigneCommande> getLignes() { return new ArrayList<>(lignes); }
    
    // Méthodes métiers
    public void ajouterLigne(Produit produit, int quantite) {
        LigneCommande ligne = new LigneCommande(this, produit, quantite);
        lignes.add(ligne);
    }
    
    public BigDecimal getTotal() {
        return lignes.stream()
            .map(LigneCommande::getSousTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    public void valider() {
        this.statut = "VALIDE";
        this.lignes.forEach(l -> l.getProduit().decrementerStock(l.getQuantite()));
    }
}