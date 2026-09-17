package Banque;

import Main.banque.CompteBancaire;
import Main.banque.exceptions.MontantInvalideException;
import Main.banque.exceptions.SoldeInsuffisantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompteBancaireTest {

    private CompteBancaire compte;

    @BeforeEach
    void setUp() {
        // Compte avec 100 de solde initial et 50 de découvert autorisé
        compte = new CompteBancaire("FR7612345000012345678900", "Alice", 100.0, 50.0);
    }

    // ---------- Cas nominaux ----------

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

    // ---------- Cas limites ----------

    @Nested
    @DisplayName("Cas limites")
    class CasLimites {

        @Test
        @DisplayName("Un retrait amenant exactement à -decouvertAutorise passe")
        void retraitJusquauDecouvertExactPasse() {
            // solde 100, découvert 50 -> on peut retirer jusqu'à 150
            compte.retirer(150.0);
            assertEquals(-50.0, compte.getSolde());
            assertTrue(compte.estEnDecouvert());
        }

        @Test
        @DisplayName("Un retrait d'un centime de plus que le découvert autorisé lève une exception")
        void retraitUnCentimeDeTropLeveException() {
            assertThrows(SoldeInsuffisantException.class, () -> compte.retirer(150.01));
            // le solde ne doit pas avoir bougé
            assertEquals(100.0, compte.getSolde());
        }

        @Test
        @DisplayName("Un dépôt de montant nul lève MontantInvalideException")
        void depotMontantNulLeveException() {
            assertThrows(MontantInvalideException.class, () -> compte.deposer(0.0));
        }

        @Test
        @DisplayName("Un retrait de montant nul lève MontantInvalideException")
        void retraitMontantNulLeveException() {
            assertThrows(MontantInvalideException.class, () -> compte.retirer(0.0));
        }
    }

    // ---------- Cas d'erreur / exceptions ----------

    @Nested
    @DisplayName("Cas d'erreur")
    class CasErreur {

        @Test
        @DisplayName("Un dépôt négatif lève MontantInvalideException")
        void depotNegatifLeveException() {
            assertThrows(MontantInvalideException.class, () -> compte.deposer(-10.0));
            assertEquals(100.0, compte.getSolde());
        }

        @Test
        @DisplayName("Un retrait négatif lève MontantInvalideException")
        void retraitNegatifLeveException() {
            assertThrows(MontantInvalideException.class, () -> compte.retirer(-10.0));
            assertEquals(100.0, compte.getSolde());
        }

        @Test
        @DisplayName("Un retrait dépassant le découvert lève SoldeInsuffisantException")
        void retraitDepassantDecouvertLeveException() {
            assertThrows(SoldeInsuffisantException.class, () -> compte.retirer(500.0));
        }

        @Test
        @DisplayName("Un taux d'intérêt négatif lève IllegalArgumentException")
        void tauxNegatifLeveException() {
            assertThrows(IllegalArgumentException.class, () -> compte.calculerInterets(-0.05));
        }

        @Test
        @DisplayName("calculerInterets renvoie 0 sur un solde négatif ou nul")
        void interetsNulsSurSoldeNegatifOuNul() {
            compte.retirer(100.0); // solde = 0
            assertEquals(0.0, compte.calculerInterets(0.1));

            compte.retirer(20.0); // solde = -20
            assertEquals(0.0, compte.calculerInterets(0.1));
        }

        @Test
        @DisplayName("estEnDecouvert reflète correctement l'état du solde")
        void estEnDecouvertRefleteLeSolde() {
            assertFalse(compte.estEnDecouvert());
            compte.retirer(120.0); // solde = -20
            assertTrue(compte.estEnDecouvert());
        }
    }
}
