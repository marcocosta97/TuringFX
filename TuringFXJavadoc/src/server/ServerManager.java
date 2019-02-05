/*
 * 
 * 
 * 
 */
package server;

import core.TuringParameters;
import core.ErrorCodes;
import core.SHA256UserManager;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.SocketException;
import core.UserManager;
import core.exceptions.InvalidParameterException;
import core.exceptions.UserAlreadySignedException;
import files.DocumentListHashImpl;
import core.security.SecurePasswordWrap;
import java.io.File;

/**
 * La classe ServerManager si occupa di gestire i flussi di esecuzione e la 
 *  gestione della memoria dei moduli fondamentali del Server.
 * 
 * @version 0.1
 * @author mc - Marco Costa - 545144
 */
public class ServerManager {
    
    /* moduli */
    private static TCPServer tcpServer;
    private static SignupRMIServer rmiServer;
    
    /* thread del server tcp */
    private static final Thread tcpServerThread;
    
    /* risorse principali condivise */
    private static UserManager userManager;    
    private static DocumentListHashImpl documentList;
    
    private static final Logger LOGGER = Logger.getLogger(ServerManager.class.getName());
    
    static {
        /* inizializzazione risorse condivise */
        userManager = new SHA256UserManager();
        documentList = new DocumentListHashImpl();
        
        /* inizializzazione moduli */
        try {
            tcpServer = new TCPServer(TuringParameters.SERVER_ADDRESS, TuringParameters.TCP_SERVER_PORT, userManager, documentList);
            rmiServer = new SignupRMIServer(TuringParameters.RMI_SERVER_PORT, userManager);
        }
        catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.exit(ErrorCodes.SERVER_RMI_STARTUP_ERROR.getErrorCode());
        }
        catch (SocketException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.exit(ErrorCodes.SERVER_TCP_UDP_STARTUP_ERROR.getErrorCode());
        }
        
        /* inizializzazione thread tcp */
        tcpServerThread = new Thread(tcpServer);
        tcpServerThread.setName("TCP Server");
    }
    
    /**
     * Avvio del Server.
     */
    private static void launch() {
        try {
            LOGGER.log(Level.WARNING, "Ciao. Avvio eseguito, inizializzo i Thread Server. "
                    + "\nLa cartella di salvataggio dei documenti si trova in: {0}"
                    + "\nDigitare un input sulla console in qualsiasi momento per eseguire la terminazione\n", 
                    new File(TuringParameters.FILE_PARENT_DIRECTORY, TuringParameters.FILE_DIRECTORY_NAME).getAbsolutePath());
            
            rmiServer.launch();
            tcpServerThread.start();
        }
        catch (RemoteException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.exit(ErrorCodes.SERVER_RMI_STARTUP_ERROR.getErrorCode());
        }
        try {
            /* attendo un input */
            System.in.read();
            
            tcpServer.close(); /* mando segnale di chiusura al server */
            rmiServer.close();
            tcpServerThread.join();
        }
        catch (IOException | InterruptedException | NotBoundException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            System.exit(ErrorCodes.SERVER_TCP_UDP_CLOSEUP_ERROR.getErrorCode());
        }
         
        
        LOGGER.log(Level.INFO, "\nChiusura eseguita correttamente. Buona giornata.");
    }
    
    /**
     * Main dell'applicazione.
     * 
     * @param args 
     */
    public static void main(String[] args) {
        launch();
    }
}
