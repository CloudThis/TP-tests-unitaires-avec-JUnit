package Main.banque.exceptions;

/**
 * Levée lorsqu'on recherche un compte dont l'IBAN n'est pas géré.
 */
public class CompteInconnuException extends RuntimeException {

    public CompteInconnuException(String message) {
        super(message);
    }
}
