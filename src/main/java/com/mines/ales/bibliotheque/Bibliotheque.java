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

    public Bibliotheque() {}

    public Abonne identification(String nom, String prenom, String numeroAbonne) {
        throw new UnsupportedOperationException("Non implemente");
    }

    public List<Livre> rechercher(String categorie) {
        throw new UnsupportedOperationException("Non implemente");
    }

    public ReservationResult reservation(Abonne abonne, Livre livre, LocalDate dateReservation) {
        throw new UnsupportedOperationException("Non implemente");
    }

    public Emprunt emprunt(Abonne abonne, Exemplaire exemplaire, LocalDate dateEmprunt) {
        throw new UnsupportedOperationException("Non implemente");
    }

    public EmpruntDecision emprunt(Abonne abonne, Livre livre, LocalDate dateEmprunt) {
        throw new UnsupportedOperationException("Non implemente");
    }

    public RetourResult retour(Abonne abonne, Exemplaire exemplaire, LocalDate dateRetour) {
        throw new UnsupportedOperationException("Non implemente");
    }

    private record Reservation(Abonne abonne, LocalDate dateReservation) {
    }
}
