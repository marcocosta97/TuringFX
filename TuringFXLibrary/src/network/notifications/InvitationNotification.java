/*
 * 
 * 
 * 
 */
package network.notifications;

import files.DocumentInfo;

/**
 * Notifica di nuovo invito a collaborare su un documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class InvitationNotification extends TCPNotification {
    /* info sul documento */
    private final DocumentInfo info;

    public InvitationNotification(String message, DocumentInfo info) {
        super(message);
        this.info = info;
    }

    public DocumentInfo getDocumentInfo() {
        return info;
    }
    
}
