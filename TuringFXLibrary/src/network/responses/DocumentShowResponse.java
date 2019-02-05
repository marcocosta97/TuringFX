/*
 * 
 * 
 * 
 */
package network.responses;

import files.DocumentWrapper;


/**
 * Risposta di conferma ad una richiesta di visualizzazione del Documento.
 * Contiene il contenuto del documento richiesto.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentShowResponse extends TCPResponse {
    private final DocumentWrapper documentWrapper; /* file + messaggio del server */
    
    public DocumentShowResponse(DocumentWrapper documentWrapper) {
        this.documentWrapper = documentWrapper;
    }

    public DocumentWrapper getDocumentWrapper() {
        return documentWrapper;
    }

    
    
    
}
