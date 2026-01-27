package com.mines.ales.bibliotheque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

class TestBibliotheque {
    // S1
    @Test
    void identification_invalide_declenche_exception() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);

        when(bibliotheque.identification("Marie", "Dupont", "A001"))
                .thenThrow(new AbonneInconnuException("Abonne inconnu"));

        assertThrows(AbonneInconnuException.class,
                () -> bibliotheque.identification("Marie", "Dupont", "A001"));
    }

    // S2
    @Test
    void identification_valide_et_recherche_polar() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        List<Livre> polars = List.of(
                new Livre("978-2-07-036822-8", "Le Silence", "Polar"),
                new Livre("978-2-07-011127-5", "Voyage au bout de la nuit", "Polar"));

        when(bibliotheque.identification("Jeanne", "Dupont", "B002")).thenReturn(abonne);
        when(bibliotheque.rechercher("Polar")).thenReturn(polars);

        Abonne resultat = bibliotheque.identification("Jeanne", "Dupont", "B002");
        List<Livre> resultatRecherche = bibliotheque.rechercher("Polar");
        assertSame(abonne, resultat);
        assertEquals(polars, resultatRecherche);
    }

    // S3
    @Test
    void recherche_voyage_sans_resultat() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);

        when(bibliotheque.identification("Jeanne", "Dupont", "B002")).thenReturn(abonne);
        when(bibliotheque.rechercher("Voyage")).thenReturn(List.of());

        bibliotheque.identification("Jeanne", "Dupont", "B002");
        List<Livre> resultatRecherche = bibliotheque.rechercher("Voyage");

        assertEquals(List.of(), resultatRecherche);
    }

    // S4
    @Test
    void reservation_ouvrage_indisponible_ajoute_attente() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Livre livre = new Livre("978-2-07-036822-8", "Le Silence", "Polar");
        LocalDate dateReservation = LocalDate.of(2026, 1, 15);

        when(bibliotheque.reservation(abonne, livre, dateReservation))
                .thenReturn(ReservationResult.AJOUTE_ATTENTE);

        ReservationResult resultat = bibliotheque.reservation(abonne, livre, dateReservation);

        assertEquals(ReservationResult.AJOUTE_ATTENTE, resultat);
    }

    // S5
    @Test
    void reservation_ouvrage_disponible_propose_emprunt() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Livre livre = new Livre("978-2-07-036822-8", "Le Silence", "Polar");
        LocalDate dateReservation = LocalDate.of(2026, 1, 16);

        when(bibliotheque.reservation(abonne, livre, dateReservation))
                .thenReturn(ReservationResult.DISPONIBLE_EMPRUNT);

        ReservationResult resultat = bibliotheque.reservation(abonne, livre, dateReservation);

        assertEquals(ReservationResult.DISPONIBLE_EMPRUNT, resultat);
    }

    // S6
    @Test
    void reservation_ouvrage_inexistant_declenche_exception() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Livre livre = new Livre("978-0-00-000000-0", "Inconnu", "Divers");
        LocalDate dateReservation = LocalDate.of(2026, 1, 16);

        when(bibliotheque.reservation(abonne, livre, dateReservation))
                .thenThrow(new OuvrageInconnuException("Ouvrage inconnu"));

        assertThrows(OuvrageInconnuException.class,
                () -> bibliotheque.reservation(abonne, livre, dateReservation));
    }

    // S7
    @Test
    void consultation_emprunts_en_retard() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 1);
        Emprunt emprunt = new Emprunt(abonne, exemplaire, LocalDate.of(2025, 12, 10),
                LocalDate.of(2026, 1, 10));
        List<Emprunt> emprunts = List.of(emprunt);
        LocalDate reference = LocalDate.of(2026, 1, 20);

        when(bibliotheque.empruntsEnRetard(abonne, reference)).thenReturn(emprunts);

        List<Emprunt> resultat = bibliotheque.empruntsEnRetard(abonne, reference);

        assertEquals(emprunts, resultat);
    }

    // S8
    @Test
    void emprunt_du_30_janvier_est_en_retard_le_1_mars() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 2);
        LocalDate dateEmprunt = LocalDate.of(2026, 1, 30);
        LocalDate dateRetourPrevue = dateEmprunt.plusMonths(1);
        Emprunt emprunt = new Emprunt(abonne, exemplaire, dateEmprunt, dateRetourPrevue);
        LocalDate reference = LocalDate.of(2026, 3, 1);

        when(bibliotheque.empruntsEnRetard(abonne, reference)).thenReturn(List.of(emprunt));

        List<Emprunt> resultat = bibliotheque.empruntsEnRetard(abonne, reference);

        assertEquals(List.of(emprunt), resultat);
    }

    // S9
    @Test
    void emprunt_ouvrage_met_a_jour_stock_et_memorise_abonne() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 3);
        LocalDate dateEmprunt = LocalDate.of(2026, 2, 2);
        LocalDate dateRetourPrevue = dateEmprunt.plusMonths(1);
        Emprunt emprunt = new Emprunt(abonne, exemplaire, dateEmprunt, dateRetourPrevue);

        when(bibliotheque.emprunt(abonne, exemplaire, dateEmprunt)).thenReturn(emprunt);

        Emprunt resultat = bibliotheque.emprunt(abonne, exemplaire, dateEmprunt);

        assertEquals(emprunt, resultat);
        verify(bibliotheque).emprunt(abonne, exemplaire, dateEmprunt);
    }

    // S10
    @Test
    void retour_dans_les_temps_met_a_jour_stock() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 3);
        LocalDate dateRetour = LocalDate.of(2026, 2, 20);

        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.A_TEMPS);

        RetourResult resultat = bibliotheque.retour(abonne, exemplaire, dateRetour);

        assertEquals(RetourResult.A_TEMPS, resultat);
    }

    // S11
    @Test
    void retour_en_retard_notifie_le_retard() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 3);
        LocalDate dateRetour = LocalDate.of(2026, 3, 5);

        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.EN_RETARD);

        RetourResult resultat = bibliotheque.retour(abonne, exemplaire, dateRetour);

        assertEquals(RetourResult.EN_RETARD, resultat);
    }

    // S12
    @Test
    void emprunt_reserve_premier_dans_la_file() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Livre livre = new Livre("978-2-07-036822-8", "Le Silence", "Polar");
        LocalDate dateEmprunt = LocalDate.of(2026, 2, 5);
        EmpruntDecision decision = new EmpruntDecision(EmpruntResult.ACCEPTE, 1,
                new Emprunt(abonne, new Exemplaire(livre.isbn(), 1), dateEmprunt, dateEmprunt.plusMonths(1)));

        when(bibliotheque.emprunt(abonne, livre, dateEmprunt)).thenReturn(decision);

        EmpruntDecision resultat = bibliotheque.emprunt(abonne, livre, dateEmprunt);

        assertEquals(decision, resultat);
    }

    // S12
    @Test
    void emprunt_reserve_pas_premier_dans_la_file() {
        Bibliotheque bibliotheque = mock(Bibliotheque.class);
        Abonne abonne = mock(Abonne.class);
        Livre livre = new Livre("978-2-07-036822-8", "Le Silence", "Polar");
        LocalDate dateEmprunt = LocalDate.of(2026, 2, 6);
        EmpruntDecision decision = new EmpruntDecision(EmpruntResult.REFUSE_POSITION, 2, null);

        when(bibliotheque.emprunt(abonne, livre, dateEmprunt)).thenReturn(decision);

        EmpruntDecision resultat = bibliotheque.emprunt(abonne, livre, dateEmprunt);

        assertEquals(decision, resultat);
    }
}
