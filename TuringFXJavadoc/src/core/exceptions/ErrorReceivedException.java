/*
 * 
 * 
 * 
 */
package core.exceptions;

/**
 * Indica un generico errore di ricezione di un oggetto.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ErrorReceivedException extends Exception {
    public ErrorReceivedException() {
        super();
    }

    public ErrorReceivedException(String message) {
        super(message);
    }
    
}
