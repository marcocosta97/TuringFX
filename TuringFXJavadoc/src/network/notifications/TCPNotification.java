/*
 * 
 * 
 * 
 */
package network.notifications;

import network.TCPMessage;

/**
 * Messaggio di notifica generico.
 * Ogni notifica inviabile su TCP deve estendere questa classe.
 * 
 * @author mc - Marco Costa - 545144
 */
public abstract class TCPNotification implements TCPMessage {
    /* messaggio di notifica */
    private final String message;
    
    public TCPNotification(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
    
    
}
