/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;

/**
 * Richiesta della lista dei documenti per un utente.
 * 
 * Nota: non è utilizzata dalla GUI
 * @author mc - Marco Costa - 545144
 */
public class DocumentListRequest extends TCPRequest {
    
    public DocumentListRequest(String requestUser) throws InvalidParameterException {
        super(requestUser);
    }
    
}
