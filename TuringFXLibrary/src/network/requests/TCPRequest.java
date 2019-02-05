/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;
import network.TCPMessage;
import core.GenericUtils;

/**
 * Messaggio di richiesta di operazione TCP.
 * 
 * @author mc - Marco Costa - 545144
 */
public abstract class TCPRequest implements TCPMessage {
    /* utente richiedente */
    protected final String requestUser;

    protected TCPRequest(String requestUser) throws InvalidParameterException {
        GenericUtils.checkEmptyString(requestUser);
        
        this.requestUser = requestUser;
    }

    public String getRequestUser() {
        return requestUser;
    }
    
    
}
