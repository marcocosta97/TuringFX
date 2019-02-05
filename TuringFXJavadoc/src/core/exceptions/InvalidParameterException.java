/*
 * 
 * 
 * 
 */
package core.exceptions;

/**
 * Indica un un errore di creazione di un oggetto richiesta/risposta a causa
 * di uno o più parametri non validi.
 * 
 * @author mc - Marco Costa - 545144
 */
public class InvalidParameterException extends Exception {

    public InvalidParameterException() {
    }

    public InvalidParameterException(String message) {
        super(message);
    }
    
}
