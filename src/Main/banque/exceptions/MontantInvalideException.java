package Main.banque.exceptions;

/**
 * Levée lorsqu'un montant fourni (dépôt ou retrait) est négatif ou nul.
 */
public class MontantInvalideException extends RuntimeException {

    public MontantInvalideException(String message) {
        super(message);
    }
}
