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
public class DocumentNotPresentException extends Exception {
    public DocumentNotPresentException() {
        super();
    }
    
    public DocumentNotPresentException(String message) {
        super(message);
    }
}
