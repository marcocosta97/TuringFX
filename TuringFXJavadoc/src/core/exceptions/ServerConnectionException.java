/*
 * 
 * 
 * 
 */
package core.exceptions;

/**
 * Indica un errore di connessione con il Server.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ServerConnectionException extends Exception {
    public ServerConnectionException() {
        super();
    }
    
    public ServerConnectionException(String message) {
        super(message);
    }
}
