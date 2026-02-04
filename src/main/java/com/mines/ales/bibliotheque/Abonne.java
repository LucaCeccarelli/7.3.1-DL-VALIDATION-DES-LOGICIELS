package com.mines.ales.bibliotheque;

public class Abonne {
    private final String nom;
    private final String prenom;
    private final String numeroAbonne;

    public Abonne(String nom, String prenom, String numeroAbonne) {
        this.nom = nom;
        this.prenom = prenom;
        this.numeroAbonne = numeroAbonne;
    }

    public String getNom() {
        throw new UnsupportedOperationException("Non implemente");
    }

    public String getPrenom() {
        throw new UnsupportedOperationException("Non implemente");
    }

    public String getNumeroAbonne() {
        throw new UnsupportedOperationException("Non implemente");
    }
}
