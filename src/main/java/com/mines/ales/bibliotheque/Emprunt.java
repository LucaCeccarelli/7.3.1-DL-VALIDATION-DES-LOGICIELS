package com.mines.ales.bibliotheque;

import java.time.LocalDate;

public record Emprunt(Abonne abonne, Exemplaire exemplaire, LocalDate dateEmprunt, LocalDate dateRetourPrevue) {
}
