package Main.banque;

import Main.banque.exceptions.MontantInvalideException;
import Main.banque.exceptions.SoldeInsuffisantException;

import java.util.UUID;

/**
 * Représente un compte bancaire avec dépôt, retrait, découvert autorisé
 * et calcul d'intérêts.
 */
public class CompteBancaire {

    private final String iban;
    private final String titulaire;
    private double solde;
    private final double decouvertAutorise;

    /**
     * Construit un compte avec un IBAN généré automatiquement et un découvert de 0.
     */
    public CompteBancaire(String titulaire) {
        this(genererIban(), titulaire, 0.0, 0.0);
    }

    /**
     * Construit un compte avec un IBAN fourni et un découvert de 0.
     */
    public CompteBancaire(String iban, String titulaire) {
        this(iban, titulaire, 0.0, 0.0);
    }

    /**
     * Construit un compte avec un IBAN fourni, un solde initial et un découvert autorisé.
     */
    public CompteBancaire(String iban, String titulaire, double soldeInitial, double decouvertAutorise) {
        if (iban == null || iban.isBlank()) {
            throw new IllegalArgumentException("L'IBAN ne peut pas être vide.");
        }
        if (titulaire == null || titulaire.isBlank()) {
            throw new IllegalArgumentException("Le titulaire ne peut pas être vide.");
        }
        if (decouvertAutorise < 0) {
            throw new IllegalArgumentException("Le découvert autorisé ne peut pas être négatif.");
        }
        this.iban = iban;
        this.titulaire = titulaire;
        this.solde = soldeInitial;
        this.decouvertAutorise = decouvertAutorise;
    }

    private static String genererIban() {
        return "FR76" + UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    /**
     * Ajoute le montant au solde.
     *
     * @throws MontantInvalideException si le montant est négatif ou nul.
     */
    public void deposer(double montant) {
        if (montant <= 0) {
            throw new MontantInvalideException("Le montant du dépôt doit être strictement positif.");
        }
        solde += montant;
    }

    /**
     * Retire le montant du solde. Le solde peut descendre jusqu'à -decouvertAutorise,
     * mais pas en dessous.
     *
     * @throws MontantInvalideException  si le montant est négatif ou nul.
     * @throws SoldeInsuffisantException si le retrait dépasse le découvert autorisé.
     */
    public void retirer(double montant) {
        if (montant <= 0) {
            throw new MontantInvalideException("Le montant du retrait doit être strictement positif.");
        }
        double nouveauSolde = solde - montant;
        if (nouveauSolde < -decouvertAutorise) {
            throw new SoldeInsuffisantException(
                    "Retrait refusé : le découvert autorisé (" + decouvertAutorise + ") serait dépassé.");
        }
        solde = nouveauSolde;
    }

    /**
     * Calcule les intérêts (solde × taux) sans modifier le solde.
     * Retourne 0 si le solde n'est pas strictement positif.
     *
     * @throws IllegalArgumentException si le taux est négatif.
     */
    public double calculerInterets(double taux) {
        if (taux < 0) {
            throw new IllegalArgumentException("Le taux d'intérêt ne peut pas être négatif.");
        }
        if (solde <= 0) {
            return 0.0;
        }
        return solde * taux;
    }

    public boolean estEnDecouvert() {
        return solde < 0;
    }

    public double getSolde() {
        return solde;
    }

    public String getTitulaire() {
        return titulaire;
    }

    public String getIban() {
        return iban;
    }

    public double getDecouvertAutorise() {
        return decouvertAutorise;
    }
}
