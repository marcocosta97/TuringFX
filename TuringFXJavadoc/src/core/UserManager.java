/*
 * 
 * 
 * 
 */
package core;

import core.exceptions.UserNotFoundException;
import core.security.SecurePasswordWrap;
import core.exceptions.UserAlreadySignedException;

/**
 * Interfaccia per la classe che realizza la struttura dati per la gestione dei
 * dati degli utenti registrati.
 * 
 * @author mc - Marco Costa - 545144
 */
public interface UserManager {
    
    public User addUser(String username, SecurePasswordWrap pass) throws UserAlreadySignedException;
    
    public User getUser(String username) throws UserNotFoundException;
    
}
