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

public class Produit {
    private int id;
    private String reference;
    private String nom;
    private String description;
    private BigDecimal prix;
    private int stock;
    
    // Constructeurs
    public Produit() {}
    
    public Produit(String reference, String nom, BigDecimal prix, int stock) {
        this.reference = reference;
        this.nom = nom;
        this.prix = prix;
        this.stock = stock;
    }
    
    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }
    
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    
    // Méthode métier
    public void decrementerStock(int quantite) {
        if (quantite > this.stock) {
            throw new IllegalArgumentException("Stock insuffisant");
        }
        this.stock -= quantite;
    }
    
    @Override
    public String toString() {
        return nom + " (" + reference + ") - " + prix + "€ [Stock: " + stock + "]";
    }
}
