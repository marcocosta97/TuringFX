/*
 * 
 * 
 * 
 */
package network.responses;

import files.DocumentInfo;

/**
 * Conferma di avvenuta richiesta di creazione di un nuovo documento.
 * Contiene le informazioni del documento per far aggiornare la vista al Client.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentCreationResponse extends TCPResponse {
    private final DocumentInfo d;
    
    public DocumentCreationResponse(DocumentInfo d) {
        this.d = d;
    }

    public DocumentInfo getDocumentInfo() {
        return d;
    }

    
}
