/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;
import files.DocumentInfo;

/**
 * Classe astratta per la richiesta di operazione generica su un documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public abstract class TCPDocumentRequest extends TCPRequest {
    private final DocumentInfo documentInfo;
    
    public TCPDocumentRequest(String requestUser, DocumentInfo documentInfo) throws InvalidParameterException {
        super(requestUser);
        this.documentInfo = documentInfo;
    }
    
    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }
    
}
