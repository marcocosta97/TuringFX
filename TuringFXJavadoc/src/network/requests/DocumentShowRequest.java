/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;
import files.DocumentInfo;

/**
 * Richiesta di visualizzazione di un documento.
 * Nota: implementa sia la richiesta su singola sezione che su tutto il documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentShowRequest extends TCPDocumentRequest {
    public static final int ALL_SECTION = Integer.MAX_VALUE;
    
    private final int section;
    
    public DocumentShowRequest(String requestUser, DocumentInfo d, int section) throws InvalidParameterException {
        super(requestUser, d);
        this.section = section;
    }
    
    public DocumentShowRequest(String requestUser, DocumentInfo d) throws InvalidParameterException {
        this(requestUser, d, ALL_SECTION);
    }
    
    public int getSection() {
        return section;
    }
    
    
}
