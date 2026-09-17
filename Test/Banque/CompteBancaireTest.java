package Banque;

import Main.banque.CompteBancaire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CompteBancaireTest {

    private CompteBancaire compte;

    @BeforeEach
    void setUp() {
        // Compte avec 100 de solde initial et 50 de découvert autorisé
        compte = new CompteBancaire("FR7612345000012345678900", "Alice", 100.0, 50.0);
    }

    @Nested
    @DisplayName("Cas nominaux")
    class CasNominaux {

        @Test
        @DisplayName("Un dépôt augmente correctement le solde")
        void deposerAugmenteLeSolde() {
            compte.deposer(50.0);
            assertEquals(150.0, compte.getSolde());
        }

        @Test
        @DisplayName("Un retrait diminue correctement le solde")
        void retirerDiminueLeSolde() {
            compte.retirer(30.0);
            assertEquals(70.0, compte.getSolde());
        }

        @Test
        @DisplayName("Le calcul des intérêts est correct sur un solde positif")
        void calculerInteretsSurSoldePositif() {
            double interets = compte.calculerInterets(0.1);
            assertEquals(10.0, interets);
            // le solde ne doit pas être modifié par le calcul
            assertEquals(100.0, compte.getSolde());
        }
    }
}