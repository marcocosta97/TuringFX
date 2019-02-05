/*
 * 
 * 
 * 
 */
package network.responses;

/**
 * Risposta di errore ad una qualsiasi possibile richiesta.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ErrorResponse extends TCPResponse {
    private final String errorMessage;

    public ErrorResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return errorMessage;
    }
    
    
}
