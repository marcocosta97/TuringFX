/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;

/**
 * Richiesta TCP di disconnessione.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DisconnectRequest extends TCPRequest {   
    public DisconnectRequest(String requestUser) throws InvalidParameterException {
        super(requestUser);
    }
    
}
