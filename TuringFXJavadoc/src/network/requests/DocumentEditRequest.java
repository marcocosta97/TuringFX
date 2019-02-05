/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;
import files.DocumentInfo;

/**
 * Richiesta di modifica di una sezione del documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentEditRequest extends TCPDocumentRequest {
    private final int section;
    
    public DocumentEditRequest(String requestUser, DocumentInfo d, int section) throws InvalidParameterException {
        super(requestUser, d);
        this.section = section;
    }

    public int getSection() {
        return section;
    }
    
    
}
