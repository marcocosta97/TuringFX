/*
 * 
 * 
 * 
 */
package core;

import files.DocumentInfo;
import network.notifications.TCPNotification;
import core.security.SecurePasswordWrap;
import java.io.Serializable;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe che contiene tutti i dati relativi alle informazioni sull'utente.
 * Nome utente, hash della passwordWrap, se è online (eventuale socket), lista
 dei documenti ai quali ha accesso, ecc.
 * 
 * @author mc - Marco Costa - 545144
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String username;
    
    private final ArrayList<DocumentInfo> documents;
    /* lista di notifiche non ancora consumate */
    private final List<TCPNotification> notifications;
    
    private final transient SecurePasswordWrap passwordWrap;
    
    private transient SocketChannel socket = null;
    private transient boolean logged = false;
    
    /**
     * Costruttore per un nuovo utente.
     * 
     * @param username nome utente
     * @param password passwordWrap wrapper
     */
    public User(String username, SecurePasswordWrap password) {
        this.username = username;
        this.passwordWrap = password;
        
        documents = new ArrayList<>();
        notifications = new ArrayList<>();
    }
    
    /**
     * Costruttore di copia.
     * Generalmente utilizzato per costruire un nuovo oggetto "speculare" da
     * inviare al client utente.
     * 
     * @param u utente da cui copiare
     */
    public User(User u) {
        /* immutables */
        this.username = u.username;
        this.passwordWrap = null; /* la passwordWrap non si copia */
        
        /* mutables */
        this.documents = new ArrayList<>(u.documents);
        this.notifications = new ArrayList<>(u.notifications);
    }
    
    /**
     * Nuovo documento a cui ha accesso l'utente.
     * 
     * @param d il documento
     */
    public void addDocument(DocumentInfo d) {
        documents.add(d);
    }
    
    /**
     * Aggiunta di una notifica da inviare all'utente appena possibile.
     * 
     * @param n la notifica
     */
    public void addNotification(TCPNotification n) {
        notifications.add(n);
    }
    
    /**
     * Pulizia delle pending notification dell'utente.
     */
    public void clearNotification() {
        notifications.clear();
    }
    
    public String getUsername() {
        return username;
    }
    
    public SecurePasswordWrap getPasswordWrap() {
        return passwordWrap;
    }
    
    
    public ArrayList<DocumentInfo> getDocuments() {
        return documents;
    }

    public List<TCPNotification> getNotifications() {
        return notifications;
    }
    
    /**
     * Imposta l'utente online o meno, se online deve essere associata la 
     * socket sulla quale è attualmente connesso.
     * 
     * @param logged true or false
     * @param sock la socket se logged == true
     */
    public void setLogged(boolean logged, SocketChannel sock) {
        this.logged = logged;
        
        if(logged)
            this.socket = sock;
        else
            this.socket = null;
    }

    public boolean isLogged() {
        return logged;
    }

    public SocketChannel getSocket() {
        return socket;
    }
    
    @Override
    public String toString() {
        return username;
    }
    

}
