/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;
import files.DocumentInfo;
import core.GenericUtils;

/**
 * Richiesta per l'invito alla collaborazione su un documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class InvitationRequest extends TCPRequest {
    private final String invitedUser;
    private final DocumentInfo info;
    
    public InvitationRequest(String invitedUser, DocumentInfo d) throws InvalidParameterException {
        super(d.getCreator()); 
        GenericUtils.checkEmptyString(invitedUser);
        
        this.invitedUser = invitedUser;
        this.info = d;
    }

    public String getInvitedUser() {
        return invitedUser;
    }

    public DocumentInfo getDocumentInfo() {
        return info;
    }
    
    
}
