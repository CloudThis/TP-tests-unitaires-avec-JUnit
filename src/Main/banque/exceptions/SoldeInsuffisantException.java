package Main.banque.exceptions;

/**
 * Levée lorsqu'un retrait dépasserait le découvert autorisé du compte.
 */
public class SoldeInsuffisantException extends RuntimeException {

    public SoldeInsuffisantException(String message) {
        super(message);
    }
}
