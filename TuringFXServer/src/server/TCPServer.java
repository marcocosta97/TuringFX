/*
 * 
 * 
 * 
 */
package server;

import core.User;
import core.UserManager;
import files.exceptions.DocumentNameInvalidException;
import files.exceptions.DocumentNotPresentException;
import core.exceptions.InvalidParameterException;
import files.exceptions.InvalidSectionException;
import files.exceptions.SectionBusyException;
import files.exceptions.UserAlreadyAddedException;
import core.exceptions.UserNotFoundException;
import files.Document;
import files.DocumentInfo;
import files.DocumentList;
import files.DocumentWrapper;
import files.exceptions.InvalidPermissionException;
import network.NetworkProtocol;
import network.TCPMessage;
import network.notifications.InvitationNotification;
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
import java.net.SocketException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import network.requests.DocumentListRequest;
import network.responses.DocumentListResponse;

/**
 * Server TCP sequenziale non-bloccante.
 * 
 * @author mc - Marco Costa - 545144
 */
public class TCPServer implements Runnable {
    private ServerSocketChannel acceptSock;
    
    private final UserManager userManager;
    private final DocumentList documentManager;
    
    private boolean isRunning = false;
    private boolean isInterrupted = false;
    
    private Selector s;
    
    private static final Logger LOGGER = Logger.getLogger(TCPServer.class.getName());
    
    /**
     * Costruttore per un nuovo oggetto TCPServer.
     * 
     * @param address l'indirizzo del Server TCP
     * @param port la porta del server
     * @param userManager il gestore degli utenti
     * @param documentList il gestore dei documenti
     * @throws SocketException se non fosse possibile inizializzare i parametri di rete
     */
    public TCPServer(String address, int port, UserManager userManager, DocumentList documentList) throws SocketException {
        this.userManager = userManager;
        this.documentManager = documentList;
        
        try {
           acceptSock = ServerSocketChannel.open();
           acceptSock.socket().bind(new InetSocketAddress(address, port));
           acceptSock.configureBlocking(false);
       }
        catch (IOException ex) {
            throw new SocketException("Impossibile creare il server TCP: " + ex.getMessage());
        }
    }
    
    /**
     * Metodo per la gestione di ogni possibile richiesta TCP proveniente dai 
     * client (eccetto la disconnessione, gestita direttamente dalla routine).
     * 
     * @param m il messaggio arrivato
     * @param sock la socket di provenienza
     * @return il messaggio di risposta da inviare
     */
    private TCPResponse manageRequest(TCPMessage m, SocketChannel sock) {
        if(!(m instanceof TCPRequest))
            return new ErrorResponse("Il messaggio inviato non è una richiesta");
        
        /**
         * Verifico che l'utente sia registrato al sistema, infatti prima di 
         * poter inviare una richiesta TCP un utente deve registrarsi al sistema.
         */
        String username = ((TCPRequest) m).getRequestUser();
        User requestUser;
        try {
            requestUser = userManager.getUser(username);
        }
        catch (UserNotFoundException ex) {
            return new ErrorResponse("L'utente non risulta registrato");
        }
        
        /**
         * Richiesta di Login.
         */
        if(m instanceof LoginRequest)
        {
            LoginRequest request = (LoginRequest) m;
            SecurePasswordWrap savedPassword = requestUser.getPasswordWrap();
            try {
                if(!savedPassword.equals(request.getPassword()))
                    return new ErrorResponse("Errore! Password errata");
            }
            catch (InvalidParameterException ex) {
                return new ErrorResponse("Errore! Password errata");
            }
            
            /* imposto l'utente come attualmente collegato */
            if(requestUser.isLogged())
                return new ErrorResponse("L'utente risulta già collegato");
            
            requestUser.setLogged(true, sock);  
            
            LOGGER.log(Level.INFO, "{0} si è loggato", requestUser); 
            return new LoginResponse(requestUser);
        }
        
        /* a questo punto un utente deve avere eseguito almeno l'operazione di login
           e risultare collegato */
        if(!requestUser.isLogged())
            return new ErrorResponse("L'utente " + requestUser + " non è loggato");
        
        
        /**
         * Richiesta di creazione di un nuovo documento
         */
        if(m instanceof DocumentCreationRequest)
        {
            DocumentInfo documentInfo = ((DocumentCreationRequest) m).getDocumentInfo();
            String documentName = documentInfo.getDocumentName();
            
            int noSections = ((DocumentCreationRequest) m).getNoSections();
            try {
               Document doc = new Document(documentName, username, noSections);

               documentManager.add(documentInfo, doc);
               /* aggiungo il documento alla lista dei documenti dell'utente */
               requestUser.getDocuments().add(documentInfo);
               
               /* operazione a buon fine */
               LOGGER.log(Level.INFO, "{0} ha creato il documento {1}", new Object[]{username, documentName});
               return new DocumentCreationResponse(documentInfo); 
            }
            catch (InvalidParameterException | DocumentNameInvalidException | InvalidSectionException | IOException ex) {
                return new ErrorResponse(ex.getLocalizedMessage());
            }
        }
        /**
         * Nuova richiesta di invito di lavoro su un documento
         * 
         * Necessario:
         *  - Verificare che il documento esista
         *  - Verificare che il documento sia creato dall'utente
         *  - Verificare che l'utente invitato esista
         *  - Aggiungere alla lista dei documenti accessibili dall'utente
         *  - Aggiungere al documento il nuovo utente
         *  - Notificare il creatore dell'avvenuta richiesta
         *  - Notificare l'utente invitato
         */
        else if(m instanceof InvitationRequest)
        {
            DocumentInfo documentInfo = ((InvitationRequest) m).getDocumentInfo();
            String invitedUsername = ((InvitationRequest) m).getInvitedUser();
            
            if(invitedUsername.equals(username))
                return new ErrorResponse("Vuoi invitarti da solo?");
            
            User invitedUser = null;
            try {
                Document document = documentManager.get(documentInfo);
                
                invitedUser = userManager.getUser(invitedUsername);
                /* 
                   prima di aggiungere dati verifico che l'utente non sia già
                   stato aggiunto al documento e, in caso contrario lo aggiungo
                   (lancia una checked exception)
                */
                document.addDocumentUser(invitedUsername);
                
                invitedUser.addDocument(documentInfo);
                InvitationNotification invitationNotification = 
                        new InvitationNotification("Sei stato invitato da " + username + 
                                " a collaborare al documento " + documentInfo.getDocumentName()
                                , documentInfo);
                
                /**
                 * notifico immediatamente l'utente invitato se collegato,
                 * altrimenti aggiungo la notifica al suo elenco di pending
                 * notifications
                 */
                if(invitedUser.isLogged())
                    NetworkProtocol.sendTCPData(invitedUser.getSocket(), invitationNotification);
                else
                    invitedUser.addNotification(invitationNotification);
                
                LOGGER.log(Level.INFO, "{0} ha invitato {1} a collaborare sul documento {2} ", 
                        new Object[]{username, invitedUsername, document});
                return new InvitationResponse();
            }
            catch (UserNotFoundException ex) {
                return new ErrorResponse("L''utente " + invitedUsername + " non risulta registrato");
            }
            catch (DocumentNotPresentException ex) {
                return new ErrorResponse("Il documento scelto non esiste");
            }
            catch (IOException ex) {}
            catch (UserAlreadyAddedException ex) {
                return new ErrorResponse("L'utente è già stato invitato");
            }   
        }
        /**
         * Richiesta di editing di un documento
         */
        else if(m instanceof DocumentEditRequest)
        {
            DocumentInfo documentInfo = ((DocumentEditRequest) m).getDocumentInfo();
            int section = ((DocumentEditRequest) m).getSection();
            
            try {
                Document d = documentManager.get(documentInfo);
                
                String file = d.startSectionEditing(section, requestUser);
                
                LOGGER.log(Level.INFO, "{0} ha ottenuto l''accesso in modifica alla sezione {1} del documento {2}",
                        new Object[]{requestUser, section, d});
                return new DocumentEditResponse(file, documentInfo, section, d.getChatIPAddress());
            }
            catch (DocumentNotPresentException ex) {
                /* documento non presente -> errore */
                return new ErrorResponse("Il documento scelto non è presente nel database");
            }
            catch (SectionBusyException | InvalidSectionException ex) {
                return new ErrorResponse(ex.getLocalizedMessage());
            }
            catch (IOException ex) {
                return new ErrorResponse("Impossibile ottenere il documento dal server");
            }
        }
        /**
         * Richiesta di fine editing
         */
        else if(m instanceof DocumentEndEditRequest)
        {
            DocumentInfo documentInfo = ((DocumentEndEditRequest) m).getDocumentInfo();
            int section = ((DocumentEndEditRequest) m).getSection();
            String newFileText = ((DocumentEndEditRequest) m).getNewFileText();
            
            try {
                Document d = documentManager.get(documentInfo);
                d.endSectionEditing(newFileText, section, requestUser);
                
                LOGGER.log(Level.INFO, "{0} ha terminato la modifica della sezione {1} "
                        + "del documento {2}", new Object[]{username, section, documentInfo.getDocumentName()});
                return new DocumentEndEditResponse();
            }
            catch (DocumentNotPresentException ex) {
                return new ErrorResponse("Il documento scelto non è presente nel database");
            }
            catch (InvalidSectionException | IOException ex) {
                return new ErrorResponse("Errore imprevisto dal server");
            }
            catch (InvalidPermissionException ex) {
                return new ErrorResponse("Errore! Non si possiedono i permessi per eseguire la richiesta");
            } 
        }
        /**
         * Richiesta di visualizzazione di un documento
         */
        else if(m instanceof DocumentShowRequest)
        {
            DocumentInfo documentInfo = ((DocumentShowRequest) m).getDocumentInfo();
            int section = ((DocumentShowRequest) m).getSection();
            
            try {
                Document d = documentManager.get(documentInfo);
                DocumentWrapper documentWrapper;
                
                if(section == DocumentShowRequest.ALL_SECTION)
                {
                    documentWrapper = d.showEntireDocument();
                    LOGGER.log(Level.INFO, "{0} ha richiesto la visualizzazione dell''intero documento {1}", 
                            new Object[]{username, documentInfo.getDocumentName()});
                }
                else
                {
                    documentWrapper = d.showDocumentSection(section);  
                    LOGGER.log(Level.INFO, "{0} ha richiesto la visualizzazione della sezione {1} "
                        + "del documento {2}", new Object[]{username, section, documentInfo.getDocumentName()});
                }
                   
                return new DocumentShowResponse(documentWrapper);
            }
            catch (DocumentNotPresentException ex) {
                return new ErrorResponse("Il documento scelto non è presente nel database");
            }
            catch (IOException ex) {
                return new ErrorResponse("Errore imprevisto dal server");
            }
            catch (InvalidSectionException ex) {
                return new ErrorResponse("La sezione richiesta non è valida");
            }
        }
        else if(m instanceof DocumentListRequest)
            return new DocumentListResponse(requestUser.getDocuments());
        
        
        return new ErrorResponse("Richiesta sconosciuta");
    }

    /**
     * Pulizia del Server e chiusura delle connessioni attualmente attive e 
     * della socket di accettazione.
     */
    private void free() {
        Set<SelectionKey> keys = s.keys();
        Iterator<SelectionKey> it = keys.iterator();
        
        /* chiudo le connessioni con i client */
        while(it.hasNext())
        {
            SelectionKey k = it.next();
            if(k.isValid())
            {
                k.cancel();
                try {
                    k.channel().close();
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }   
        }
        /* chiudo la accept socket */
        try 
        {
            acceptSock.socket().close();
            acceptSock.close();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        
        LOGGER.log(Level.WARNING, "Server TCP chiuso correttamente");
    }
    
    /**
     * Metodo per la richiesta di chiusura del Server (se attivo).
     */
    public void close() {
        if(isRunning)
        {
            isInterrupted = true;
            s.wakeup();
        }
    }
    
    /**
     * Routine del Server.
     */
    @Override
    public void run() {
        if(isRunning) throw new IllegalStateException("Il server è già in esecuzione");
        
        try {
            s = Selector.open();
            
            acceptSock.register(s, acceptSock.validOps());
            isRunning = true;
            
            LOGGER.log(Level.INFO, "Server TCP in esecuzione");
            
            while(true)
            {
                s.select();
                if(isInterrupted) 
                    return; /* goto finally */
                
                Set<SelectionKey> keys = s.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                
                while(it.hasNext())
                {
                    SelectionKey k = it.next();
                    it.remove();
                    try {
                        if(k.isAcceptable())
                        {
                            SocketChannel client = acceptSock.accept();

                            client.configureBlocking(false);
                            /* lo registro in lettura per attendere una sua richiesta */
                            client.register(s, SelectionKey.OP_READ);

                            LOGGER.log(Level.INFO, "Connessione accettata: {0}",
                                        client.getRemoteAddress());
                        }
                        /* nuova richiesta */
                        else if(k.isReadable())
                        {
                            SocketChannel clientSock = (SocketChannel) k.channel();
                            TCPMessage req = NetworkProtocol.receiveTCPData(clientSock);
                            
                            /** 
                             *  la disconnessione è gestita dalla routine del Server
                             *  nello stesso modo sia in caso di richiesta che in caso di chiusura
                             *  forzata del client
                             */
                            if(req instanceof DisconnectRequest)
                                throw new IOException();
                            
                            /* else */
                            k.attach(req);
                            /* passo in scrittura sulla socket del client */
                            k.interestOps(SelectionKey.OP_WRITE);
                        }
                        /* rispondo alla richiesta */
                        else if(k.isWritable())
                        {
                            SocketChannel clientSock = (SocketChannel) k.channel();
                            TCPRequest request = (TCPRequest) k.attachment();
                            
                            /* gestisco la richiesta e invio la risposta */
                            NetworkProtocol.sendTCPData(clientSock, manageRequest(request, clientSock));
                            /* ritorno in lettura */
                            k.interestOps(SelectionKey.OP_READ);
                        }
                    }
                    /**
                     * Disconnessione di un utente.
                     * Chiudo il canale e la socket associata e imposto l'utente
                     * come offline.
                     */
                    catch(IOException ex) {
                        k.cancel();
                        try {
                            k.channel().close();
                        }
                        catch(IOException e) {
                            LOGGER.log(Level.SEVERE, null, e);
                        }
                        
                        /**
                         * Eseguo la disconnessione dell'utente tramite il suo
                         * ultimo messaggio di richiesta
                         */  
                        Object attach = k.attachment();
                        if(attach instanceof TCPRequest)
                        {
                            try {
                                User u = userManager.getUser(((TCPRequest) attach).getRequestUser());
                                u.setLogged(false, null);
                                
                                LOGGER.log(Level.INFO, "Utente {0} disconnesso", u);
                            }
                            catch (UserNotFoundException ex1) {
                                /* do nothing */
                            }
                        }
                    }
                }
            }
        }
        catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
        finally { /* pulizia e chiusura */
            free();
            isRunning = false;
        }
    }
    
}
