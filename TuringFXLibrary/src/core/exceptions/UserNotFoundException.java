/*
 * 
 * 
 * 
 */
package core.exceptions;

/**
 * Indica che l'utente non è stato trovato nel sistema. 
 * 
 * @author mc - Marco Costa - 545144
 */
public class UserNotFoundException extends Exception {
    public UserNotFoundException(String message) {
        super(message);
    }
}
