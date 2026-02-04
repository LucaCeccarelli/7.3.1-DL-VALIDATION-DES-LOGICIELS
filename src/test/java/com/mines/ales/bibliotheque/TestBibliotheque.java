package com.mines.ales.bibliotheque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TestBibliotheque {
    private Bibliotheque bibliotheque;
    private Abonne abonne;

    @BeforeEach
    public void setUp() {
        bibliotheque = mock(Bibliotheque.class);
        abonne = mock(Abonne.class);
    }
    // S1
    @Test
    void test_s1_identification_invalide_declenche_exception() {
        when(bibliotheque.identification("Marie", "Dupont", "A001"))
                .thenThrow(new AbonneInconnuException("Abonne inconnu"));

        assertThrows(AbonneInconnuException.class,
                () -> bibliotheque.identification("Marie", "Dupont", "A001"));
    }

    // S2
    @Test
    void test_s2_identification_valide_et_recherche_polar() {
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
    void test_s3_recherche_voyage_sans_resultat() {
        when(bibliotheque.identification("Jeanne", "Dupont", "B002")).thenReturn(abonne);
        when(bibliotheque.rechercher("Voyage")).thenReturn(List.of());

        bibliotheque.identification("Jeanne", "Dupont", "B002");
        List<Livre> resultatRecherche = bibliotheque.rechercher("Voyage");

        assertEquals(List.of(), resultatRecherche);
    }

    // S4
    @Test
    void test_s4_reservation_ouvrage_indisponible_ajoute_attente() {
        Livre livre = new Livre("978-2-07-036822-8", "Le Silence", "Polar");
        LocalDate dateReservation = LocalDate.of(2026, 1, 15);

        when(bibliotheque.reservation(abonne, livre, dateReservation))
                .thenReturn(ReservationResult.AJOUTE_ATTENTE);

        ReservationResult resultat = bibliotheque.reservation(abonne, livre, dateReservation);

        assertEquals(ReservationResult.AJOUTE_ATTENTE, resultat);
    }

    // S5
    @Test
    void test_s5_reservation_ouvrage_disponible_propose_emprunt() {
        Livre livre = new Livre("978-2-07-036822-8", "Le Silence", "Polar");
        LocalDate dateReservation = LocalDate.of(2026, 1, 16);

        when(bibliotheque.reservation(abonne, livre, dateReservation))
                .thenReturn(ReservationResult.DISPONIBLE_EMPRUNT);

        ReservationResult resultat = bibliotheque.reservation(abonne, livre, dateReservation);

        assertEquals(ReservationResult.DISPONIBLE_EMPRUNT, resultat);
    }

    // S6
    @Test
    void test_s6_reservation_ouvrage_inexistant_declenche_exception() {
        Livre livre = new Livre("978-0-00-000000-0", "Inconnu", "Divers");
        LocalDate dateReservation = LocalDate.of(2026, 1, 16);

        when(bibliotheque.reservation(abonne, livre, dateReservation))
                .thenThrow(new OuvrageInconnuException("Ouvrage inconnu"));

        assertThrows(OuvrageInconnuException.class,
                () -> bibliotheque.reservation(abonne, livre, dateReservation));
    }

    // S7
    @Test
    void test_s7_consultation_emprunts_en_retard() {
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 1);
        LocalDate dateRetour = LocalDate.of(2026, 1, 20);

        when(bibliotheque.identification("Jeanne", "Dupont", "B002")).thenReturn(abonne);
        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.EN_RETARD);

        Abonne resultatIdentification = bibliotheque.identification("Jeanne", "Dupont", "B002");
        RetourResult resultat = bibliotheque.retour(resultatIdentification, exemplaire, dateRetour);

        assertSame(abonne, resultatIdentification);
        assertEquals(RetourResult.EN_RETARD, resultat);
    }

    // S8
    @Test
    void test_s8_emprunt_du_30_janvier_est_en_retard_le_1_mars() {
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 2);
        LocalDate dateEmprunt = LocalDate.of(2026, 1, 30);
        LocalDate dateRetour = LocalDate.of(2026, 3, 1);
        Emprunt emprunt = new Emprunt(abonne, exemplaire, dateEmprunt, dateEmprunt.plusMonths(1));

        when(bibliotheque.emprunt(abonne, exemplaire, dateEmprunt)).thenReturn(emprunt);
        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.EN_RETARD);

        Emprunt resultatEmprunt = bibliotheque.emprunt(abonne, exemplaire, dateEmprunt);
        RetourResult resultatRetour = bibliotheque.retour(abonne, exemplaire, dateRetour);

        assertEquals(emprunt, resultatEmprunt);
        assertEquals(RetourResult.EN_RETARD, resultatRetour);
    }

    // S9
    @Test
    void test_s9_emprunt_ouvrage_met_a_jour_stock_et_memorise_abonne() {
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
    void test_s10_retour_dans_les_temps_met_a_jour_stock() {
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 3);
        LocalDate dateRetour = LocalDate.of(2026, 2, 20);

        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.A_TEMPS);

        RetourResult resultat = bibliotheque.retour(abonne, exemplaire, dateRetour);

        assertEquals(RetourResult.A_TEMPS, resultat);
    }

    // S11
    @Test
    void test_s11_retour_en_retard_notifie_le_retard() {
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 3);
        LocalDate dateRetour = LocalDate.of(2026, 3, 5);

        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.EN_RETARD);

        RetourResult resultat = bibliotheque.retour(abonne, exemplaire, dateRetour);

        assertEquals(RetourResult.EN_RETARD, resultat);
    }

    // S12
    @Test
    void test_s12_emprunt_reserve_premier_dans_la_file() {
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
    void test_s12_emprunt_reserve_pas_premier_dans_la_file() {
        Livre livre = new Livre("978-2-07-036822-8", "Le Silence", "Polar");
        LocalDate dateEmprunt = LocalDate.of(2026, 2, 6);
        EmpruntDecision decision = new EmpruntDecision(EmpruntResult.REFUSE_POSITION, 2, null);

        when(bibliotheque.emprunt(abonne, livre, dateEmprunt)).thenReturn(decision);

        EmpruntDecision resultat = bibliotheque.emprunt(abonne, livre, dateEmprunt);

        assertEquals(decision, resultat);
    }
}
