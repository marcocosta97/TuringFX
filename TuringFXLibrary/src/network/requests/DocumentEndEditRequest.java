/*
 * 
 * 
 * 
 */
package network.requests;

import core.TuringParameters;
import core.exceptions.InvalidParameterException;
import network.responses.DocumentEditResponse;

/**
 * Richiesta di terminazione della modifica di un documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentEndEditRequest extends TCPDocumentRequest {
    private final int section;
    private final String newFileText;
    
    /* nota: richiede di reinviare la risposta di accesso ottenuto dal server */
    public DocumentEndEditRequest(String requestUser, DocumentEditResponse r, String newFileText) throws InvalidParameterException {
        super(requestUser, r.getDocumentInfo());
        
        /* controllo sulla dimensione massima, nel caso riduco la dimensione */
        if(newFileText.length() > TuringParameters.MAX_SECTION_LENGTH)
            newFileText = newFileText.substring(0, TuringParameters.MAX_SECTION_LENGTH);
        
        this.section = r.getDocumentSection();
        this.newFileText = newFileText;
    }

    public int getSection() {
        return section;
    }

    public String getNewFileText() {
        return newFileText;
    }
    
    
}
