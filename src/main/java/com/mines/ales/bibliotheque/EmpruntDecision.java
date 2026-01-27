package com.mines.ales.bibliotheque;

public record EmpruntDecision(EmpruntResult result, int positionAttente, Emprunt emprunt) {
}
