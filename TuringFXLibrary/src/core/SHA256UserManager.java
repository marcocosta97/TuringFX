/*
 * 
 * 
 * 
 */
package core;

import core.security.SecurePasswordWrap;
import core.exceptions.UserAlreadySignedException;
import core.exceptions.UserNotFoundException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * La seguente classe realizza la struttura dati contenente tutte i dati degli utenti
 * registrati al servizio.
 * L'implementazione prevede che le Password utente siano salvate in Hash 
 * mediante algoritmo SHA-1 con concatenazione password + nonce [hash = SHA-1(pw:nonce)]
 * in modo che per utenti che abbiano scelto la stessa password la struttura
 * memorizzi un hash diverso.
 * Inoltre la struttura di rappresentazione è concorrente, in quanto deve essere
 * acceduto sia dal Server RMI per registrare nuovi utenti che dal Server TCP
 * per tutte le operazioni di richiesta ricevute.
 * 
 * @author mc - Marco Costa - 545144
 */
public class SHA256UserManager implements UserManager {
    
    private final ConcurrentHashMap<String, User> manager;

    public SHA256UserManager() {
        manager = new ConcurrentHashMap<>();
    }

    /**
     * Aggiunta di un nuovo utente alla struttura e creazione dell'oggetto User
     * che lo rappresenta.
     * Nota: il nome utente è case-sensitive
     * 
     * @param username il nome utente
     * @param pass la password scelta
     * @return l'oggetto utente se non vi è alcuna eccezione
     * @throws UserAlreadySignedException se esiste già un utente registrato con quel nome
     */
    @Override
    public User addUser(String username, SecurePasswordWrap pass) throws UserAlreadySignedException {
        if(manager.containsKey(username))
            throw new UserAlreadySignedException(username + " is already signed");
        
        User user = new User(username, pass);
        manager.put(username, user);
        
        return user;
    }
    
    /**
     * Restituisce la struttura dati personale dell'utente "username".
     * 
     * @param username il nome utente
     * @return l'oggetto utente
     * @throws UserNotFoundException se non risulta nessu utente registrato con 
     *                               quel nome
     */
    @Override
    public User getUser(String username) throws UserNotFoundException {
        if(!manager.containsKey(username))
            throw new UserNotFoundException(username + " is not signed");
        
        return manager.get(username);
    }
}
