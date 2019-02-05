/*
 * 
 * 
 * 
 */
package client;

import GUI.ClientFXMain;
import GUI.MainMenuFXMLController;
import GUI.dialogs.ErrorDialog;
import GUI.notifications.WarningNotification;
import core.User;
import core.exceptions.ErrorReceivedException;
import core.exceptions.InvalidMessageException;
import core.exceptions.InvalidParameterException;
import core.exceptions.ServerConnectionException;
import core.exceptions.UserAlreadySignedException;
import files.DocumentInfo;
import network.NetworkProtocol;
import network.TCPMessage;
import network.notifications.InvitationNotification;
import network.notifications.TCPNotification;
import network.requests.DisconnectRequest;
import network.requests.DocumentCreationRequest;
import network.requests.DocumentEditRequest;
import network.requests.DocumentEndEditRequest;
import network.requests.DocumentShowRequest;
import network.requests.InvitationRequest;
import network.requests.LoginRequest;
import network.requests.TCPRequest;
import network.responses.DocumentCreationResponse;
import network.responses.DocumentEditResponse;
import network.responses.DocumentEndEditResponse;
import network.responses.DocumentShowResponse;
import network.responses.ErrorResponse;
import network.responses.InvitationResponse;
import network.responses.LoginResponse;
import network.responses.TCPResponse;
import core.security.SecurePasswordWrap;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import network.Signup;
import core.TuringParameters;

/**
 * Classe che implementa il Driver per la comunicazione tra la GUI e il Server.
 * Nota: è una classe Singleton
 * 
 * @author mc - Marco Costa - 545144
 */
public class ClientDriver implements Runnable {
    /* istanza del Driver */
    private static final ClientDriver instance = new ClientDriver();
    /* attributi relativi all'utente collegato */
    private User user;
    private String username;
    /* variabili relative al Thread di comunicazione col Server TCP */
    private static boolean isInterrupted;
    private static boolean isRunning;
    private static SocketChannel serverSock = null;  
    private static Selector s;
    /* variabili per lo scambio threadsafe di richieste tra Thread grafico e Driver */
    private static TCPRequest currentRequest;
    private static TCPResponse currentResponse;
    private static AtomicBoolean newInterThreadRequest;
    
    private ClientDriver() {}
    
    /**
     * Restituizione dell'istanza della classe
     * @return l'istanza della classe
     */
    public static ClientDriver getInstance() {
        return instance;
    }
    
    private void setUser(User u) {
        this.user = u;
    }

    /**
     * Restituisce le informazioni sull'utente attualmente collegato.
     * 
     * @return l'utente
     */
    public User getUser() {
        return user;
    }
    
    /**
     * Inizializzazione degli attributi del Driver e collegamento al Server TCP.
     * Da effettuare prima di una richiesta di Login.
     * 
     * @throws IOException se non è possibile contattare il server
     */
    private void initializeDriver() throws IOException {
        serverSock = null;
        try {
            /* apertura della socket e connessione */
            serverSock = SocketChannel.open(new InetSocketAddress(TuringParameters.SERVER_ADDRESS, TuringParameters.TCP_SERVER_PORT));
            serverSock.finishConnect();
            if(!serverSock.isConnected())
                throw new IOException();
            
            /* inizializzazione variabili condivise */
            isInterrupted = false;
            isRunning = false;
            currentRequest = null;
            currentResponse = null;
            newInterThreadRequest = new AtomicBoolean(false);
        }
        catch (IOException ex) {
            throw new IOException("Errore! Impossibile connettersi al server");
        }
    }
    
    /**
     * Effettua una richiesta sincrona di invio al Thread di comunicazione col Server TCP.
     * 
     * @throws InterruptedException in caso di interruzione forzata dell'attesa
     * @throws IOException in caso non fosse possibile ottenere risposta
     * 
     * @param r la richiesta
     * @return la risposta ottenuta dal Thread
     */
    private static TCPResponse synchronizedRequest(TCPRequest r) throws InterruptedException, IOException {   
        synchronized(r) {
            currentRequest = r;
            newInterThreadRequest.set(true);
            s.wakeup();
            r.wait();
        }
        if(currentResponse != null)
        {
            TCPResponse t = currentResponse;
            currentResponse = null;
            currentRequest = null;
            
            return t;
        }
        
        /* reimposto a nulle le variabili condivise */
        currentRequest = null;
        currentResponse = null;
        
        throw new IOException(); 
    }
    
    /**
     * Effettua una richiesta di tipo "richiesta-risposta" al Server.
     * 
     * @param r la richiesta
     * @return la risposta ottenuta dal server
     * 
     * @throws InvalidMessageException se il messaggio ricevuto non è valido
     * @throws IOException se non è possibile contattare il server
     * @throws ErrorReceivedException se è stato ricevuto dal server un messaggio
     *                                di errore relativo alla richiesta
     */
    private TCPResponse requestResponseMessage(TCPRequest r) throws InvalidMessageException, 
                                                                    IOException,
                                                                    ErrorReceivedException {
        TCPResponse m;
        try {
            m = synchronizedRequest(r);
            
            if(!(m instanceof TCPResponse))
                throw new InvalidMessageException();
        
            if(m instanceof ErrorResponse)
                throw new ErrorReceivedException(((ErrorResponse)m).toString());

            return (TCPResponse) m;
        }
        catch (InterruptedException ex) {}
        
        throw new IOException("Impossibile comunicare con il server");
    }
    
    /**
     * Interfaccia per la richiesta di Registrazione al servizio mediante 
     * Server RMI.
     * Per la connessione si utilizzano i parametri specificati nella classe
     * TuringParameters
     * 
     * @see TuringParameters
     * @param username il nome utente desiderato
     * @param pass il wrapper contenente l'hash della password scelta e il nonce
     *             generato casualmente mediante il RNG
     * @throws ServerConnectionException in caso non fosse possibile contattare il server
     * @throws UserAlreadySignedException in caso il nome utente scelto non 
     *                                    fosse disponibile
     */
    public void requestSignup(String username, SecurePasswordWrap pass) throws ServerConnectionException, UserAlreadySignedException {
        try {
            Registry r = LocateRegistry.getRegistry(TuringParameters.SERVER_ADDRESS, TuringParameters.RMI_SERVER_PORT);
            Signup s = (Signup) r.lookup(Signup.SERVICE_NAME);
            
            s.signup(username, pass);
        }
        catch (RemoteException | NotBoundException ex) {
            throw new ServerConnectionException("Impossibile contattare il server");
        }
        catch (UserAlreadySignedException ex) {
            throw new UserAlreadySignedException("L'username scelto non è disponibile");
        }
    }
    
    
    /**
     * Interfaccia per la richiestta TCP di Login al server.
     * Poiché questa è, in ordine, la prima richiesta TCP effettuata al server
     *  è necessario inizializzare il driver mediante il metodo initializeDriver().
     * Inoltre, dopo l'avvenuto Login il metodo si occupa di avviare il Thread
     *  per la comunicazione asincrona con il Server.
     * Questa è l'UNICA richiesta sincrona effettuata dal Driver verso il Server.
     * 
     * @param username il nome utente
     * @param password la password con la quale è richiesto il login
     * @return il nome utente se l'accesso è avvenuto, altrimenti viene sollevata
     *         una eccezione
     * @throws IOException se non è possibile comunicare con il server
     * @throws ErrorReceivedException se è stato ricvuto un errore dal server 
     *                                come risposta
     * @throws InvalidParameterException se non è stato possibile costruire il
     *                                   messaggio di richiesta
     */
    public String requestLogin(String username, String password) throws IOException, ErrorReceivedException, InvalidParameterException {
        initializeDriver();
        
        LoginRequest request = new LoginRequest(username, password);
        
        /* invio la richiesta e attendo risposta */
        NetworkProtocol.sendTCPData(serverSock, request);
        TCPMessage r = NetworkProtocol.receiveTCPData(serverSock);
        
        /* sono loggato */
        if((r instanceof LoginResponse))
        {   /* verifico che il nome utente col quale ho effettuato la richiesta
               sia lo stesso per il quale ho ottenuto conferma di login */
            if(((LoginResponse) r).getUser().getUsername().equals(username))
            {
                /* salvo la risposta del server */
                this.setUser(((LoginResponse) r).getUser());
                this.username = user.getUsername();
                
                /* avvio il Thread per la comunicazione asincrona */
                new Thread(this).start();
                
                return username;
            }
        }
        /* non è stato possibile autenticarsi */
        else if(r instanceof ErrorResponse)
            throw new ErrorReceivedException(((ErrorResponse)r).toString());
        
        /* se arrivo qui c'è stato un errore di comunicazione */
        throw new IOException("Errore di comunicazione col server");
    }
    
    /**
     * Interfaccia per la richiesta TCP di creazione di un nuovo documento.
     * Create(D, numSezioni)
     * Nota: il creatore del documento è inserito automaticamente dal Driver
     *       come l'utente attualmente collegato
     * 
     * @param documentName il nome del documento
     * @param noSections il numero di sezioni del documento
     * @return il wrapper contenente le informazioni sul documento se la creazione
     *         è avvenuta con successo, altrimenti viene sollevata una eccezione
     * @throws IOException se non è possibile comunicare con il server
     * @throws ErrorReceivedException se è stato ricvuto un errore dal server 
     *                                come risposta
     * @throws InvalidParameterException se non è stato possibile costruire il
     *                                   messaggio di richiesta 
     */
    public DocumentInfo requestDocumentCreation(String documentName, int noSections) throws IOException,
                                                                                            ErrorReceivedException,
                                                                                            InvalidParameterException {
        DocumentCreationRequest request = new DocumentCreationRequest(user.getUsername(), documentName, noSections);
        
        /* effettuo una richiesta "richiesta-risposta" al thread asincrono */
        TCPResponse r = requestResponseMessage(request);
        
        /* verifico la risposta sia l'istanza corretta che mi aspetto */
        if(r instanceof DocumentCreationResponse)
        {
            DocumentInfo documentInfo = ((DocumentCreationResponse) r).getDocumentInfo();
            user.getDocuments().add(documentInfo);
            return documentInfo;
        }
           
        /* se arrivo qui c'è stato un errore di comunicazione */
        throw new IOException("Errore di comunicazione col server");
    }
    
    /**
     * Interfaccia per la richiesta TCP di invito di un utente alla collaborazione
     * sul documento.
     * 
     * @param d il documento sul quale si richiede di invitare l'utente
     * @param invitedUser l'utente invitato
     * @return un oggetto di tipo InvitationResponse se l'invito è avvenuto con
     *         successo, altrimenti viene sollevata una eccezione
     * @throws IOException se non è possibile comunicare con il server
     * @throws ErrorReceivedException se è stato ricvuto un errore dal server 
     *                                come risposta
     * @throws InvalidParameterException se non è stato possibile costruire il
     *                                   messaggio di richiesta 
     */
    public InvitationResponse requestInvitation(DocumentInfo d, String invitedUser) throws IOException, 
                                                                                                ErrorReceivedException,
                                                                                                InvalidParameterException {
        /* verifico che l'utente collegato sia il creatore del documento */
        if(!(d.getCreator().equals(username)))
            throw new InvalidParameterException("Non sei il creatore del documento!");
        
        InvitationRequest r = new InvitationRequest(invitedUser, d);
        
        TCPResponse ans = requestResponseMessage(r);
        
        /* verifico la risposta sia l'istanza corretta che mi aspetto */
        if(ans instanceof InvitationResponse)
            return (InvitationResponse) ans;
        
        /* se arrivo qui c'è stato un errore di comunicazione */
        throw new IOException("Errore di comunicazione col server");
    }
    
    /**
     * Interfaccia per la richiesta TCP di Edit(S, D) su un documento D.
     * 
     * @param requestedDocument il documento sul quale è richiesta la modifica
     * @param section la sezione di modifica
     * @return un oggetto DocumentEditResponse se la richiesta è stata accolta, altrimenti viene sollevata una eccezione
     * @throws IOException se non è possibile comunicare con il server
     * @throws ErrorReceivedException se è stato ricvuto un errore dal server 
     *                                come risposta
     * @throws InvalidParameterException se non è stato possibile costruire il
     *                                   messaggio di richiesta 
     */
    public DocumentEditResponse requestDocumentEdit(DocumentInfo requestedDocument, int section) throws IOException, ErrorReceivedException, InvalidParameterException {
        
        DocumentEditRequest r = new DocumentEditRequest(user.getUsername(), requestedDocument, section);
        
        TCPResponse ans = requestResponseMessage(r);
        
        if(ans instanceof DocumentEditResponse)
            return (DocumentEditResponse) ans;
        
        /* se arrivo qui c'è stato un errore di comunicazione */
        throw new IOException("Errore di comunicazione col server");
        
    }
    
    /**
     * Interfaccia per la richiesta TCP di End_Edit(S, D) su un documento D, sezione S.
     * 
     * @param document la risposta ottenuta dal Server in seguito alla richiesta
     *                 di Edit(S, D) la quale "attesta" il permesso ottenuto dal Server
     * @param newDocumentText la nuova versione del documento
     * @return un oggetto DocumentEndEditResponse se la richiesta è stata accolta,
     *          altrimenti viene sollevata una eccezione
     * @throws IOException se non è possibile comunicare con il server
     * @throws ErrorReceivedException se è stato ricvuto un errore dal server 
     *                                come risposta
     * @throws InvalidParameterException se non è stato possibile costruire il
     *                                   messaggio di richiesta 
     */
    public DocumentEndEditResponse requestDocumentEndEdit(DocumentEditResponse document, String newDocumentText) throws IOException, 
                                                                                                                        ErrorReceivedException,
                                                                                                                        InvalidParameterException {
        
        DocumentEndEditRequest r = new DocumentEndEditRequest(user.getUsername(), document, newDocumentText);
        
        TCPResponse ans = requestResponseMessage(r);
        
        if(ans instanceof DocumentEndEditResponse)
            return (DocumentEndEditResponse) ans;
        
        /* se arrivo qui c'è stato un errore di comunicazione */
        throw new IOException("Errore di comunicazione col server");
        
    }
    
    /**
     * Interfaccia per la richiesta TCP di Show(S, D) su un documento D, sezione S.
     * 
     * @param requestedDocument il documento richiesto da visualizzare
     * @param section la sezione richiesta 
     * @return un oggetto DocumentShowResponse se la richiesta è stata accolta,
     *          altrimenti viene sollevata una eccezione
     * @throws IOException se non è possibile comunicare con il server
     * @throws ErrorReceivedException se è stato ricvuto un errore dal server 
     *                                come risposta
     * @throws InvalidParameterException se non è stato possibile costruire il
     *                                   messaggio di richiesta 
     */
    public DocumentShowResponse requestDocumentShow(DocumentInfo requestedDocument, int section) throws IOException, 
                                                                                                        ErrorReceivedException,
                                                                                                        InvalidParameterException {
        DocumentShowRequest r = new DocumentShowRequest(user.getUsername(), requestedDocument, section);
        
        TCPResponse ans = requestResponseMessage(r);
        
        if(ans instanceof DocumentShowResponse)
            return (DocumentShowResponse) ans;
        
        /* se arrivo qui c'è stato un errore di comunicazione */
        throw new IOException("Errore di comunicazione col server");
    }
    
    /**
     * Interfaccia per la richiesta TCP di Show(D) su un documento D, intero documento.
     * 
     * @param requestedDocument il documento richiesto da visualizzare
     * @return un oggetto DocumentShowResponse se la richiesta è stata accolta,
     *          altrimenti viene sollevata una eccezione
     * @throws IOException se non è possibile comunicare con il server
     * @throws ErrorReceivedException se è stato ricvuto un errore dal server 
     *                                come risposta
     * @throws InvalidParameterException se non è stato possibile costruire il
     *                                   messaggio di richiesta 
     */
    public DocumentShowResponse requestDocumentShow(DocumentInfo requestedDocument) throws  IOException, 
                                                                                            ErrorReceivedException,
                                                                                            InvalidParameterException {
        return requestDocumentShow(requestedDocument, DocumentShowRequest.ALL_SECTION);
    }
       
    /**
     * Metodo per la chiusura controllata del Thread per la comunicazione asincrona
     * col Server TCP.
     */
    public static void closeSocket() {
        if(isRunning)
        {
            isInterrupted = true;
            s.wakeup();
        }
    }
    
    /**
     * Routine del Thread per la comunicazione asincrona con il Server.
     * L'implementazione permette di ricevere notifiche dal Server che 
     *  potrebbero arrivare in ogni momento, senza necessità di richiesta e 
     *  di risposte successive a richieste effettuate dal Thread principale.
     */
    @Override
    public void run() {
        if(isRunning) throw new IllegalStateException("Il Thread è già in esecuzione");
        
        try {
            serverSock.configureBlocking(false);
            s = Selector.open();
            /**
             * Registro il selettore SOLO in lettura per la ricezione di
             *  notifiche e risposte.
             */
            serverSock.register(s, SelectionKey.OP_READ);         
            isRunning = true;
            /* routine */
            while(true) 
            {
                s.select();
                if(isInterrupted) /* devo chiudere, goto finally */
                    return;
                /* nuova richiesta da inviare al server */
                else if(newInterThreadRequest.get())
                {
                    NetworkProtocol.sendTCPData(serverSock, currentRequest);
                    newInterThreadRequest.set(false);
                }
                
                Set<SelectionKey> keys = s.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                
                while(it.hasNext())
                {
                    SelectionKey k = it.next();
                    it.remove();
                    if(k.isReadable())
                    {
                        /* ricevo la comunicazione */
                        TCPMessage message = NetworkProtocol.receiveTCPData(serverSock);
                        /* è arrivata una notifica, chiedo che venga gestita */       
                        if(message instanceof TCPNotification)
                            manageNotification((TCPNotification) message);
                        /* è arrivata una risposta ad una precedente richiesta */
                        else if((message instanceof TCPResponse) && (currentRequest != null))
                        {
                            /* imposto la risposta nella variabile condivisa */
                            currentResponse = (TCPResponse) message;
                            /* sveglio il Thread grafico dall'attesa */
                            synchronized(currentRequest) {
                                currentRequest.notify();
                            }
                        }
                    }
                }
            }
        }
        catch (IOException ex) {
            /* errore di comunicazione con il server, notifico di
                           chiudere l'applicazione */
            Platform.runLater(() -> {
                new ErrorDialog("Impossibile contattare il server, chiusura").showDialog();
                ClientFXMain.getInstance().exitRoutine();
            });
        }
        finally {
            try {
                /* send disconnect */
                if(isInterrupted)
                {
                    NetworkProtocol.sendTCPData(serverSock, new DisconnectRequest(username));
                     Logger.getLogger(ClientDriver.class.getName()).log(Level.WARNING, "Driver chiuso correttamente");
                }
            }
            catch (IOException | InvalidParameterException ex) {
                Logger.getLogger(ClientDriver.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Metodo per la richiesta al Thread grafico della gestione di una nuova
     * notifica.
     * 
     * @param notification la notifica ricevuta
     */
    private void manageNotification(TCPNotification notification) {
        /* nuovo invito a collaborare su un documento */
        if(notification instanceof InvitationNotification)
        {
            DocumentInfo d = ((InvitationNotification) notification).getDocumentInfo();
            
            /* inserisco nella coda del Thread grafico la notifica grafica e 
               l'aggiunta del documento alla lista dei documenti accessibili
               dall'utente */
            Platform.runLater(() -> {
                user.getDocuments().add(d);
                MainMenuFXMLController.getInstance().addDocumentInfo(d);
                
                new WarningNotification("Nuovo invito", notification.getMessage()).show();
            });   
        }
        
        /* al momento non sono previste altre notifiche */
    }

    
}
