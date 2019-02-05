/*
 * 
 * 
 * 
 */
package core.exceptions;

/**
 * Indica che l'utente è già registrato al sistema.
 * 
 * @author mc - Marco Costa - 545144
 */
public class UserAlreadySignedException extends Exception {
    public UserAlreadySignedException() {
        super();
    }
    
    public UserAlreadySignedException(String message) {
        super(message);
    }
}
