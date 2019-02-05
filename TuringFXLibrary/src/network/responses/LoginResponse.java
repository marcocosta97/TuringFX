/*
 * 
 * 
 * 
 */
package network.responses;

import core.User;

/**
 * Risposta di conferma ad una richiesta di Login.
 * Restituzione dell'oggetto utente.
 * 
 * @author mc - Marco Costa - 545144
 */
public class LoginResponse extends TCPResponse {
    private final User user;
    
    public LoginResponse(User user) {
        this.user = new User(user); /* copia dell'oggetto utente */
        user.clearNotification(); /* pulizia delle pending notification */
    }

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return user.toString();
    }
    
    
}
