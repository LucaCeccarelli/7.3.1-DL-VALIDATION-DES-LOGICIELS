package com.mines.ales.bibliotheque;

import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Bibliotheque {
    private final Map<String, Abonne> abonnesParNumero = new HashMap<>();
    private final Map<String, Livre> catalogueParIsbn = new HashMap<>();
    private final Map<String, Deque<Exemplaire>> exemplairesDisponibles = new HashMap<>();
    private final Map<Exemplaire, Emprunt> empruntsActifs = new HashMap<>();
    private final Map<String, Deque<Reservation>> reservationsParIsbn = new HashMap<>();

    public Bibliotheque() {
    }

    public void ajouterAbonne(Abonne abonne) {
        Objects.requireNonNull(abonne, "abonne");
        abonnesParNumero.put(abonne.getNumeroAbonne(), abonne);
    }

    public void ajouterLivre(Livre livre) {
        Objects.requireNonNull(livre, "livre");
        catalogueParIsbn.put(livre.isbn(), livre);
    }

    public void ajouterExemplaire(Exemplaire exemplaire) {
        Objects.requireNonNull(exemplaire, "exemplaire");
        exemplairesDisponibles
                .computeIfAbsent(exemplaire.isbn(), key -> new ArrayDeque<>())
                .addLast(exemplaire);
    }

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
        List<Livre> resultats = new ArrayList<>();
        for (Livre livre : catalogueParIsbn.values()) {
            if (Objects.equals(livre.categorie(), categorie)) {
                resultats.add(livre);
            }
        }
        return resultats;
    }

    public ReservationResult reservation(Abonne abonne, Livre livre, LocalDate dateReservation) {
        verifierOuvrage(livre);
        Deque<Exemplaire> disponibles = exemplairesDisponibles.get(livre.isbn());
        if (disponibles != null && !disponibles.isEmpty()) {
            return ReservationResult.DISPONIBLE_EMPRUNT;
        }
        Deque<Reservation> reservations = reservationsParIsbn.computeIfAbsent(livre.isbn(), key -> new ArrayDeque<>());
        reservations.addLast(new Reservation(abonne, dateReservation));
        return ReservationResult.AJOUTE_ATTENTE;
    }

    public Emprunt emprunt(Abonne abonne, Exemplaire exemplaire, LocalDate dateEmprunt) {
        Deque<Exemplaire> disponibles = exemplairesDisponibles.get(exemplaire.isbn());
        if (disponibles == null || !disponibles.remove(exemplaire)) {
            throw new IllegalStateException("Exemplaire indisponible");
        }
        Emprunt emprunt = new Emprunt(abonne, exemplaire, dateEmprunt, dateEmprunt.plusMonths(1));
        empruntsActifs.put(exemplaire, emprunt);
        return emprunt;
    }

    public EmpruntDecision emprunt(Abonne abonne, Livre livre, LocalDate dateEmprunt) {
        verifierOuvrage(livre);
        Deque<Reservation> reservations = reservationsParIsbn.get(livre.isbn());
        int position = positionReservation(reservations, abonne);
        if (position > 1) {
            return new EmpruntDecision(EmpruntResult.REFUSE_POSITION, position, null);
        }
        Deque<Exemplaire> disponibles = exemplairesDisponibles.get(livre.isbn());
        if (disponibles == null || disponibles.isEmpty()) {
            return new EmpruntDecision(EmpruntResult.INDISPONIBLE, position, null);
        }
        Exemplaire exemplaire = disponibles.removeFirst();
        Emprunt emprunt = new Emprunt(abonne, exemplaire, dateEmprunt, dateEmprunt.plusMonths(1));
        empruntsActifs.put(exemplaire, emprunt);
        if (position == 1 && reservations != null) {
            reservations.removeFirst();
        }
        return new EmpruntDecision(EmpruntResult.ACCEPTE, 1, emprunt);
    }

    public List<Emprunt> empruntsEnRetard(Abonne abonne, LocalDate dateReference) {
        List<Emprunt> retards = new ArrayList<>();
        for (Emprunt emprunt : empruntsActifs.values()) {
            if (Objects.equals(emprunt.abonne(), abonne)
                    && dateReference.isAfter(emprunt.dateRetourPrevue())) {
                retards.add(emprunt);
            }
        }
        return retards;
    }

    public RetourResult retour(Abonne abonne, Exemplaire exemplaire, LocalDate dateRetour) {
        Emprunt emprunt = empruntsActifs.remove(exemplaire);
        if (emprunt == null) {
            throw new IllegalStateException("Emprunt introuvable");
        }
        exemplairesDisponibles
                .computeIfAbsent(exemplaire.isbn(), key -> new ArrayDeque<>())
                .addLast(exemplaire);
        if (dateRetour.isAfter(emprunt.dateRetourPrevue())) {
            return RetourResult.EN_RETARD;
        }
        return RetourResult.A_TEMPS;
    }

    private void verifierOuvrage(Livre livre) {
        if (livre == null || !catalogueParIsbn.containsKey(livre.isbn())) {
            throw new OuvrageInconnuException("Ouvrage inconnu");
        }
    }

    private int positionReservation(Deque<Reservation> reservations, Abonne abonne) {
        if (reservations == null || reservations.isEmpty()) {
            return 0;
        }
        int position = 1;
        for (Reservation reservation : reservations) {
            if (Objects.equals(reservation.abonne(), abonne)) {
                return position;
            }
            position += 1;
        }
        return reservations.size() + 1;
    }

    private record Reservation(Abonne abonne, LocalDate dateReservation) {
    }
}
