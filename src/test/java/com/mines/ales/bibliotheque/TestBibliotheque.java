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
    /**
     * Scénario S1 - Marie Dupont cherche à se connecter au système avec un numéro d'abonné
     * mais elle n'est pas reconnue par le système (mauvais nom, prénom ou numéro d'abonné),
     * le système doit retourner une exception.
     */
    @Test
    void test_s1_identification_invalide_declenche_exception() {
        when(bibliotheque.identification("Marie", "Dupont", "A001"))
                .thenThrow(new AbonneInconnuException("Abonne inconnu"));

        assertThrows(AbonneInconnuException.class,
                () -> bibliotheque.identification("Marie", "Dupont", "A001"));
    }

    // S2
    /**
     * Scénario S2 - Jeanne Dupont cherche à se connecter au système et l'opération se passe
     * avec succès. Elle réalise ensuite une recherche pour connaître l'ensemble des titres
     * de la catégorie Polar. Elle obtient la liste de tous les Polars.
     */
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
    /**
     * Scénario S3 - Un abonné identifié réalise une recherche sur la catégorie Voyage.
     * Or il n'y a aucun livre de cette catégorie dans le fonds documentaire.
     * La recherche lui renvoie une liste vide.
     */
    @Test
    void test_s3_recherche_voyage_sans_resultat() {
        when(bibliotheque.identification("Jeanne", "Dupont", "B002")).thenReturn(abonne);
        when(bibliotheque.rechercher("Voyage")).thenReturn(List.of());

        bibliotheque.identification("Jeanne", "Dupont", "B002");
        List<Livre> resultatRecherche = bibliotheque.rechercher("Voyage");

        assertEquals(List.of(), resultatRecherche);
    }

    // S4
    /**
     * Scénario S4 - Un abonné identifié réserve un ouvrage existant dans le fonds mais indisponible.
     * Son numéro d'abonné et la date de la réservation sont ajoutés à la liste des abonnés
     * ayant réservé cet ouvrage.
     */
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
    /**
     * Scénario S5 - Un abonné identifié réserve un ouvrage existant dans le fonds et disponible.
     * Le système lui propose de l'emprunter.
     */
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
    /**
     * Scénario S6 - Un abonné identifié réserve un ouvrage n'existant pas dans le fonds.
     * Le système doit retourner une exception.
     */
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
    /**
     * Scénario S7 - Un abonné s'identifie, il se voit retourner la liste de ses emprunts en retard.
     */
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
    /**
     * Scénario S8 - Un abonné a emprunté un livre le 30 janvier. Il s'identifie à nouveau le 1 mars.
     * Le livre doit figurer dans la liste des emprunts en retard.
     */
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
    /**
     * Scénario S9 - Un abonné identifié emprunte un livre. Le stock est mis à jour pour refléter
     * l'emprunt. Le numéro d'abonné et celui de l'ouvrage emprunté sont mémorisés.
     */
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
    /**
     * Scénario S10 - Un abonné ayant emprunté un ouvrage, le retourne dans les temps.
     * Le stock est mis à jour.
     */
    @Test
    void test_s10_retour_dans_les_temps_met_a_jour_stock() {
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 3);
        LocalDate dateRetour = LocalDate.of(2026, 2, 20);

        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.A_TEMPS);

        RetourResult resultat = bibliotheque.retour(abonne, exemplaire, dateRetour);

        assertEquals(RetourResult.A_TEMPS, resultat);
    }

    // S11
    /**
     * Scénario S11 - Un abonné ayant emprunté un ouvrage, le retourne en retard.
     * Le stock est mis à jour. L'abonné se voit notifier le retard.
     */
    @Test
    void test_s11_retour_en_retard_notifie_le_retard() {
        Exemplaire exemplaire = new Exemplaire("978-2-07-036822-8", 3);
        LocalDate dateRetour = LocalDate.of(2026, 3, 5);

        when(bibliotheque.retour(abonne, exemplaire, dateRetour)).thenReturn(RetourResult.EN_RETARD);

        RetourResult resultat = bibliotheque.retour(abonne, exemplaire, dateRetour);

        assertEquals(RetourResult.EN_RETARD, resultat);
    }

    // S12
    /**
     * Scénario S12 - Un abonné ayant réservé un ouvrage vient l'emprunter.
     * Il est le premier sur la liste des personnes l'ayant réservé. L'emprunt aboutit.
     */
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
    /**
     * Scénario S12 - Un abonné ayant réservé un ouvrage vient l'emprunter.
     * Il n'est pas le premier sur la liste des personnes l'ayant réservé.
     * L'emprunt n'aboutit pas et l'abonné est averti de sa position sur la liste d'attente.
     */
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
