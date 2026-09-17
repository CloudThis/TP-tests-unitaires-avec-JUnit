package Main.banque;

import Main.banque.exceptions.CompteDejaExistantException;
import Main.banque.exceptions.CompteInconnuException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Gère un ensemble de comptes bancaires, indexés par IBAN.
 */
public class GestionnaireComptes {

    private final Map<String, CompteBancaire> comptes = new LinkedHashMap<>();

    /**
     * Ajoute un compte au gestionnaire.
     *
     * @throws CompteDejaExistantException si un compte avec le même IBAN est déjà géré.
     */
    public void ajouterCompte(CompteBancaire compte) {
        if (compte == null) {
            throw new IllegalArgumentException("Le compte ne peut pas être null.");
        }
        String iban = compte.getIban();
        if (comptes.containsKey(iban)) {
            throw new CompteDejaExistantException("Un compte avec l'IBAN " + iban + " existe déjà.");
        }
        comptes.put(iban, compte);
    }

    /**
     * Recherche un compte par son IBAN.
     *
     * @throws CompteInconnuException si aucun compte ne correspond à cet IBAN.
     */
    public CompteBancaire rechercherCompte(String iban) {
        CompteBancaire compte = comptes.get(iban);
        if (compte == null) {
            throw new CompteInconnuException("Aucun compte trouvé pour l'IBAN " + iban);
        }
        return compte;
    }

    /**
     * Effectue un virement entre deux comptes gérés.
     * L'opération est atomique : si le retrait échoue (montant invalide, solde
     * insuffisant, compte inconnu...), aucun dépôt n'a lieu et aucun solde n'est modifié.
     *
     * @throws CompteInconnuException                          si l'un des deux IBAN est inconnu.
     * @throws Main.banque.exceptions.MontantInvalideException  si le montant est invalide.
     * @throws Main.banque.exceptions.SoldeInsuffisantException si le compte source n'a pas
     *                                                              assez de solde/découvert.
     */
    public void virement(String ibanSource, String ibanDestination, double montant) {
        CompteBancaire source = rechercherCompte(ibanSource);
        CompteBancaire destination = rechercherCompte(ibanDestination);

        // Le retrait est effectué en premier : s'il lève une exception, il n'a
        // aucun effet de bord (solde inchangé) et le dépôt n'est jamais atteint.
        source.retirer(montant);
        try {
            destination.deposer(montant);
        } catch (RuntimeException e) {
            // Filet de sécurité : si le dépôt échoue pour une raison quelconque,
            // on annule le retrait déjà effectué pour garantir l'atomicité.
            source.deposer(montant);
            throw e;
        }
    }

    /**
     * @return la somme des soldes de tous les comptes gérés.
     */
    public double soldeTotal() {
        double total = 0.0;
        for (CompteBancaire compte : comptes.values()) {
            total += compte.getSolde();
        }
        return total;
    }

    /**
     * @return la liste des comptes dont le solde est négatif.
     */
    public List<CompteBancaire> listeComptesEnDecouvert() {
        List<CompteBancaire> resultat = new ArrayList<>();
        for (CompteBancaire compte : comptes.values()) {
            if (compte.estEnDecouvert()) {
                resultat.add(compte);
            }
        }
        return resultat;
    }

    /**
     * @return tous les comptes actuellement gérés (vue non modifiable).
     */
    public Collection<CompteBancaire> getComptes() {
        return List.copyOf(comptes.values());
    }
}