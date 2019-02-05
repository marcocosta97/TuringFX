/*
 * 
 * 
 * 
 */
package files.exceptions;

/**
 *
 * @author mc - Marco Costa - 545144
 */
public class UserAlreadyAddedException extends Exception {

    public UserAlreadyAddedException() {
        super();
    }
    
    public UserAlreadyAddedException(String message) {
        super(message);
    }
}
