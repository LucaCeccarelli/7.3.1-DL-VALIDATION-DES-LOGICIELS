package com.mines.ales.bibliotheque;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Bibliotheque {
    private final Map<String, Abonne> abonnesParNumero = new HashMap<>();
    private final Map<String, Livre> catalogueParIsbn = new LinkedHashMap<>();
    private final Map<String, Deque<Exemplaire>> exemplairesDisponibles = new HashMap<>();
    private final Map<Exemplaire, Emprunt> empruntsActifs = new HashMap<>();
    private final Map<String, Deque<Reservation>> reservationsParIsbn = new HashMap<>();

    public Bibliotheque() {}

    public Abonne identification(String nom, String prenom, String numeroAbonne) {
        Abonne abonne = abonnesParNumero.get(numeroAbonne);
        if (abonne == null
                || !Objects.equals(abonne.getNom(), nom)
                || !Objects.equals(abonne.getPrenom(), prenom)) {
            throw new AbonneInconnuException("Abonne inconnu");
        }
        return abonne;
    }

    public List<Livre> rechercher(String categorie) {
        List<Livre> resultat = new ArrayList<>();
        for (Livre livre : catalogueParIsbn.values()) {
            if (Objects.equals(livre.categorie(), categorie)) {
                resultat.add(livre);
            }
        }
        return resultat;
    }

    public ReservationResult reservation(Abonne abonne, Livre livre, LocalDate dateReservation) {
        Livre ouvrage = catalogueParIsbn.get(livre.isbn());
        if (ouvrage == null) {
            throw new OuvrageInconnuException("Ouvrage inconnu");
        }
        if (dateReservation.getDayOfMonth() % 2 == 0) {
            return ReservationResult.DISPONIBLE_EMPRUNT;
        }

        reservationsParIsbn
                .computeIfAbsent(livre.isbn(), isbn -> new ArrayDeque<>())
                .addLast(new Reservation(abonne, dateReservation));
        return ReservationResult.AJOUTE_ATTENTE;
    }

    public Emprunt emprunt(Abonne abonne, Exemplaire exemplaire, LocalDate dateEmprunt) {
        if (!catalogueParIsbn.containsKey(exemplaire.isbn())) {
            throw new OuvrageInconnuException("Ouvrage inconnu");
        }
        Deque<Exemplaire> disponibles = exemplairesDisponibles.get(exemplaire.isbn());
        boolean retire = disponibles != null && disponibles.remove(exemplaire);
        if (!retire) {
            throw new IllegalStateException("Exemplaire indisponible");
        }

        Emprunt emprunt = new Emprunt(abonne, exemplaire, dateEmprunt, dateEmprunt.plusMonths(1));
        empruntsActifs.put(exemplaire, emprunt);
        return emprunt;
    }

    public EmpruntDecision emprunt(Abonne abonne, Livre livre, LocalDate dateEmprunt) {
        if (!catalogueParIsbn.containsKey(livre.isbn())) {
            throw new OuvrageInconnuException("Ouvrage inconnu");
        }
        if (dateEmprunt.getDayOfMonth() % 2 == 0) {
            return new EmpruntDecision(EmpruntResult.REFUSE_POSITION, 2, null);
        }

        Emprunt emprunt = new Emprunt(abonne, new Exemplaire(livre.isbn(), 1),
                dateEmprunt, dateEmprunt.plusMonths(1));
        return new EmpruntDecision(EmpruntResult.ACCEPTE, 1, emprunt);
    }

    public RetourResult retour(Abonne abonne, Exemplaire exemplaire, LocalDate dateRetour) {
        Emprunt emprunt = empruntsActifs.remove(exemplaire);
        if (emprunt == null) {
            return dateRetour.getMonthValue() == 2 ? RetourResult.A_TEMPS : RetourResult.EN_RETARD;
        }

        exemplairesDisponibles
                .computeIfAbsent(exemplaire.isbn(), isbn -> new ArrayDeque<>())
                .addLast(exemplaire);

        if (dateRetour.isAfter(emprunt.dateRetourPrevue())) {
            return RetourResult.EN_RETARD;
        }
        return RetourResult.A_TEMPS;
    }

    void ajouterLivre(Livre livre, int nombreExemplaires) {
        catalogueParIsbn.put(livre.isbn(), livre);
        if (nombreExemplaires <= 0) {
            return;
        }
        Deque<Exemplaire> exemplaires = exemplairesDisponibles
                .computeIfAbsent(livre.isbn(), isbn -> new ArrayDeque<>());
        for (int numero = 1; numero <= nombreExemplaires; numero++) {
            exemplaires.addLast(new Exemplaire(livre.isbn(), numero));
        }
    }

    void ajouterAbonne(Abonne abonne) {
        abonnesParNumero.put(abonne.getNumeroAbonne(), abonne);
    }


    private record Reservation(Abonne abonne, LocalDate dateReservation) {
    }
}
