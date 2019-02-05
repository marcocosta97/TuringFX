/*
 * 
 * 
 * 
 */
package server;

import network.Signup;
import core.exceptions.UserAlreadySignedException;
import core.security.SecurePasswordWrap;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Level;
import java.util.logging.Logger;
import core.UserManager;

/**
 * Server RMI per l'operazione di registrazione al sistema.
 * 
 * @author mc - Marco Costa - 545144
 */
public class SignupRMIServer extends UnicastRemoteObject implements Signup {
    private final UserManager userManager;
    private Registry r;
    
    private final int port;
    
    private static final Logger LOGGER = Logger.getLogger(SignupRMIServer.class.getName());
    
    /**
     * Costruttore per l'oggetto.
     * 
     * @param port la porta del server
     * @param userManager il gestore degli utenti
     * @throws RemoteException in caso di errori di comunicazione con il server
     */
    public SignupRMIServer(int port, UserManager userManager) throws RemoteException {
        this.userManager = userManager;
        this.port = port;
    }
    
    /**
     * Esportazione dell'oggetto correnta sulla porta scelta.
     * 
     * @throws RemoteException in caso di errori di comunicazione con il server
     */
    public void launch() throws RemoteException {
        r = LocateRegistry.createRegistry(port);
        r.rebind(Signup.SERVICE_NAME, (Signup) this);
        LOGGER.log(Level.INFO, "Server RMI in esecuzione");
    }
    
    /**
     * Metodo per la registrazione al sistema.
     * 
     * @param username nome utente scelto
     * @param pass password wrapper
     * @throws UserAlreadySignedException in caso fosse già registrato un utente con
     *                                    quel nome
     * @throws RemoteException in caso di errori di comunicazione con il server
     */
    @Override
    public void signup(String username, SecurePasswordWrap pass) throws UserAlreadySignedException, RemoteException {
        try {
            userManager.addUser(username, pass);
            LOGGER.log(Level.INFO, "Registrato utente {0}", username);
        }
        catch(UserAlreadySignedException ex) {
            throw new UserAlreadySignedException("L'utente " + username + " è già registrato");
        }
    }
    
    /**
     * Chiusura del Server e un-esportazione dell'oggetto corrente.
     * 
     * @throws RemoteException in caso di errori di comunicazione con il server
     * @throws NotBoundException in caso il server non fosse stato avviato
     */
    public void close() throws RemoteException, NotBoundException {
        r.unbind(SERVICE_NAME);
        UnicastRemoteObject.unexportObject(this, true);
        
        LOGGER.log(Level.WARNING, "Server RMI chiuso correttamente");
    }
}
