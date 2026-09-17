package banque;

import Main.banque.CompteBancaire;
import Main.banque.GestionnaireComptes;
import Main.banque.exceptions.CompteDejaExistantException;
import Main.banque.exceptions.CompteInconnuException;
import Main.banque.exceptions.SoldeInsuffisantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GestionnaireComptesTest {

    private GestionnaireComptes gestionnaire;
    private CompteBancaire compteA;
    private CompteBancaire compteB;

    @BeforeEach
    void setUp() {
        gestionnaire = new GestionnaireComptes();
        compteA = new CompteBancaire("FR76A", "Alice", 100.0, 0.0);
        compteB = new CompteBancaire("FR76B", "Bob", 20.0, 0.0);
        gestionnaire.ajouterCompte(compteA);
        gestionnaire.ajouterCompte(compteB);
    }

    // ---------- Cas nominaux ----------

    @Nested
    @DisplayName("Cas nominaux")
    class CasNominaux {

        @Test
        @DisplayName("rechercherCompte retourne le bon compte")
        void rechercherCompteRetourneLeBonCompte() {
            assertEquals(compteA, gestionnaire.rechercherCompte("FR76A"));
        }

        @Test
        @DisplayName("Un virement réussi met à jour les deux comptes")
        void virementReussiMetAJourLesSoldes() {
            gestionnaire.virement("FR76A", "FR76B", 30.0);
            assertEquals(70.0, compteA.getSolde());
            assertEquals(50.0, compteB.getSolde());
        }

        @Test
        @DisplayName("soldeTotal additionne correctement tous les comptes")
        void soldeTotalCorrect() {
            assertEquals(120.0, gestionnaire.soldeTotal());
        }
    }

    // ---------- Cas limites ----------

    @Nested
    @DisplayName("Cas limites")
    class CasLimites {

        @Test
        @DisplayName("listeComptesEnDecouvert retourne uniquement les comptes négatifs")
        void listeComptesEnDecouvertFiltreCorrectement() {
            CompteBancaire compteC = new CompteBancaire("FR76C", "Carla", 0.0, 50.0);
            compteC.retirer(20.0); // solde = -20
            gestionnaire.ajouterCompte(compteC);

            List<CompteBancaire> enDecouvert = gestionnaire.listeComptesEnDecouvert();

            assertEquals(1, enDecouvert.size());
            assertTrue(enDecouvert.contains(compteC));
        }

        @Test
        @DisplayName("Un virement du montant exact du solde vide le compte source")
        void virementDuSoldeExactViseCompteSource() {
            gestionnaire.virement("FR76A", "FR76B", 100.0);
            assertEquals(0.0, compteA.getSolde());
            assertEquals(120.0, compteB.getSolde());
        }
    }

    // ---------- Cas d'erreur / exceptions ----------

    @Nested
    @DisplayName("Cas d'erreur")
    class CasErreur {

        @Test
        @DisplayName("Rechercher un IBAN inconnu lève CompteInconnuException")
        void rechercherCompteInconnuLeveException() {
            assertThrows(CompteInconnuException.class, () -> gestionnaire.rechercherCompte("FR76INCONNU"));
        }

        @Test
        @DisplayName("Ajouter un IBAN déjà existant lève CompteDejaExistantException")
        void ajouterCompteExistantLeveException() {
            CompteBancaire doublon = new CompteBancaire("FR76A", "Alice2", 0.0, 0.0);
            assertThrows(CompteDejaExistantException.class, () -> gestionnaire.ajouterCompte(doublon));
        }

        @Test
        @DisplayName("Un virement qui échoue au milieu ne modifie aucun solde")
        void virementEchoueNeModifieAucunSolde() {
            double soldeADepart = compteA.getSolde();
            double soldeBDepart = compteB.getSolde();

            // montant supérieur au solde disponible de A (pas de découvert autorisé)
            assertThrows(SoldeInsuffisantException.class,
                    () -> gestionnaire.virement("FR76A", "FR76B", 500.0));

            assertEquals(soldeADepart, compteA.getSolde());
            assertEquals(soldeBDepart, compteB.getSolde());
        }

        @Test
        @DisplayName("Un virement vers un IBAN inconnu lève CompteInconnuException sans modifier le solde source")
        void virementVersCompteInconnuNeModifieRien() {
            double soldeADepart = compteA.getSolde();

            assertThrows(CompteInconnuException.class,
                    () -> gestionnaire.virement("FR76A", "FR76INCONNU", 10.0));

            assertEquals(soldeADepart, compteA.getSolde());
        }
    }
}
