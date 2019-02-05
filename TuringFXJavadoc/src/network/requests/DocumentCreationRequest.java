/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;
import files.DocumentInfo;

/**
 * Richiesta di creazione di un nuovo documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentCreationRequest extends TCPDocumentRequest {
    /* numero di sezioni */
    private final int noSections;
    
    public DocumentCreationRequest(String username, String documentName, int noSections) throws InvalidParameterException {
        super(username, new DocumentInfo(documentName, username));
        
        this.noSections = noSections;
    }

    public int getNoSections() {
        return noSections;
    }
    
    
}
