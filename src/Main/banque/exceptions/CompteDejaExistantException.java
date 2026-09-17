package Main.banque.exceptions;

/**
 * Levée lorsqu'on tente d'ajouter un compte dont l'IBAN est déjà géré.
 */
public class CompteDejaExistantException extends RuntimeException {

    public CompteDejaExistantException(String message) {
        super(message);
    }
}
