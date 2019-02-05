/*
 * 
 * 
 * 
 */
package files;

import java.io.Serializable;

/**
 * Wrapper per la coppia di informazioni "contenuto documento - messaggio per l'utente".
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentWrapper implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String documentContent;
    private final String userMessage;

    public DocumentWrapper(String documentContent, String userMessage) {
        this.documentContent = documentContent;
        this.userMessage = userMessage;
    }

    public DocumentWrapper(String documentContent) {
        this.documentContent = documentContent;
        this.userMessage = "";
    }
    
    public String getDocumentContent() {
        return documentContent;
    }

    public String getUserMessage() {
        return userMessage;
    }
    
    
}
