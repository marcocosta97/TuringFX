/*
 * 
 * 
 * 
 */
package network.responses;

import files.DocumentInfo;


/**
 * Conferma di accettata richiesta di Editing sul documento.
 * Contiene tutti i parametri necessari ad effettuare l'editing.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentEditResponse extends TCPResponse {
    private final String documentContent;
    private final DocumentInfo documentInfo;
    private final int documentSection; 
    private final String chatAddress;
    
    public DocumentEditResponse(String documentContent, DocumentInfo documentInfo, 
                                int documentSection, String chatAddress) {
        this.documentContent = documentContent;
        this.documentInfo = documentInfo;
        this.documentSection = documentSection;
        this.chatAddress = chatAddress;
    }

    public String getDocumentContent() {
        return documentContent;
    }

    public DocumentInfo getDocumentInfo() {
        return documentInfo;
    }

    public int getDocumentSection() {
        return documentSection;
    }

    public String getChatAddress() {
        return chatAddress;
    }
    
    
    
}
