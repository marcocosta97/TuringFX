/*
 * 
 * 
 * 
 */
package network;

import core.exceptions.UserAlreadySignedException;
import core.security.SecurePasswordWrap;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaccia remota per la registrazione al Sistema.
 * 
 * @author mc - Marco Costa - 545144
 */
public interface Signup extends Remote {
    public static final String SERVICE_NAME = "SignupService";
    
    /**
     * Registrazione al sistema.
     * 
     * @param username nome utente che richiede la registrazione
     * @param pass password wrapper generato
     * @throws UserAlreadySignedException se un utente con quel nome è già registrato
     * @throws RemoteException in caso non fosse possibile contattare il server remoto
     */
    public void signup(String username, SecurePasswordWrap pass) throws UserAlreadySignedException,
                                                                     RemoteException;
}
