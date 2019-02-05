/*
 * 
 * 
 * 
 */
package core.exceptions;

import java.io.IOException;

/**
 * Indica la ricezione di un messaggio non valido/inaspettato.
 * 
 * @author mc - Marco Costa - 545144
 */
public class InvalidMessageException extends IOException {
    public InvalidMessageException() {
        super();
    }

    public InvalidMessageException(String message) {
        super(message);
    }
    
}
