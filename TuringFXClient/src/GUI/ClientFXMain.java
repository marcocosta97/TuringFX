/*
 * 
 * 
 * 
 */
package GUI;

import GUI.dialogs.ErrorDialog;
import client.ChatService;
import client.ClientDriver;
import network.responses.DocumentEditResponse;
import network.responses.DocumentShowResponse;
import network.responses.TCPResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.controlsfx.control.PopOver;

/**
 * Classe per la gestione delle scene e delle finestre per l'interfaccia grafica. 
 * Nota: è una classe Singleton
 * 
 * @author mc - Marco Costa - 545144
 */
public class ClientFXMain extends Application {
    public static final String CLIENT_NAME = "TuringFX Client 0.1";
    
    private final InetAddress ADDRESS; /* settata su localhost nel costruttore */
    
    private Stage primaryStage;
    private Scene mainMenuScene;
    
    private static ClientFXMain instance;
    
    private Thread chatNotificationThread;
    private ChatService service;
    
    /**
     * Costruttore della classe.
     * Salva l'istanza della classe e imposta l'indirizzo di rete.
     * 
     * @throws UnknownHostException se l'host di rete è sconosciuto
     */
    public ClientFXMain() throws UnknownHostException {
        instance = this; /* salva l'istanza della classe */
        
        ADDRESS = InetAddress.getLocalHost();
        
    }
    
    /**
     * Restituisce l'istanza della classe. 
     * 
     * @return l'istanza del singleton
     */
    public static ClientFXMain getInstance() {
        return instance;
    }
    
    /** 
     * Routine di chiusura dei thread per la comunicazione col server e 
     * chiusura dell'interfaccia grafica.
     */
    public void exitRoutine() {
        closeChat();
        ClientDriver.closeSocket();
        
        Logger.getLogger(ClientFXMain.class.getName()).log(Level.WARNING, "GUI terminata correttamente");
        Platform.exit();
        System.exit(0);
    }
    
    /**
     * Metodo per la chiusura dell'eventuale Thread per la gestione della chat.
     */
    public void closeChat() {
        if(service != null)
        {
            service.closeSocket();
            try {
                chatNotificationThread.join();
                Logger.getLogger(ClientFXMain.class.getName()).log(Level.WARNING, "Chat Service chiuso correttamente");
            }
            catch (InterruptedException ex) {
                Logger.getLogger(ClientFXMain.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            /* inizializzazione */
            service = null;
            chatNotificationThread = null;
        }
    }
    
    /**
     * Procedura per il passaggio dall'interfaccia LOGIN all'interfaccia MENU.
     * Nota: la chiamata deve essere successiva ad un avvenuto login.
     */
    public void switchToMainMenu() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainMenuFXML.fxml"));
            Parent root = loader.load();
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
        catch (IOException ex) {
            Logger.getLogger(ClientFXMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Procedura per il passaggio da MENU a modalità EDIT_PAGE a seguito di una 
     *  avvenuta richiesta di Show o Edit. 
     * Se è stata effettuata una richiestadi editing il metodo si occupa di 
     * avviare il thread per la gestione della chat.
     * 
     * @param response la risposta ottenuta dal server, necessaria ad impostare
     *                 la corretta modalità di visualizzazione della finestra  
     */
    public void switchToEditPage(TCPResponse response) {
        /* salvo la scena principale per poter "tornare indietro", al termine 
           dell'editing */
        mainMenuScene = primaryStage.getScene(); 
        
        try {
            /* carico l'FXML della EDIT PAGE */
            FXMLLoader loader = new FXMLLoader(getClass().getResource("EditPageFXML.fxml"));
            Parent root = loader.load();
            EditPageFXMLController controller = loader.<EditPageFXMLController>getController();     
            
            /**
             * Controllo il tipo di istanza della risposta ottenuta dal Server
             */
            if(response instanceof DocumentEditResponse)
            {
                /* entro in modalità editing sul documento */
                String chatAddress = ((DocumentEditResponse) response).getChatAddress();
                String username = ClientDriver.getInstance().getUser().getUsername();
                
                NetworkInterface ni =  NetworkInterface.getByInetAddress(ADDRESS);
                
                if(ni == null)
                {
                    /* ah boh, su virtualbox pare non esistere l'interfaccia associata al localhost */
                    ni = NetworkInterface.getByInetAddress(InetAddress.getLoopbackAddress());
                    if(ni == null)
                        ni = NetworkInterface.getByIndex(0);
                }
                
                ObservableList<String> observableArrayList = FXCollections.observableArrayList();
                
                /**
                 * Costruisco e avvio in un nuovo thread il service per la chat, passando come argomento
                 * l'interfaccia di rete impostata, l'indirizzo IP della chat 
                 * (ottenuto dalla risposta del server), il nome utente e la 
                 * lista (vuota) dei messaggi mostrati a video
                 */
                service = new ChatService(ni, chatAddress, username, observableArrayList);
                /* passaggio dei dati al controller dell'interfaccia */
                controller.initData((DocumentEditResponse) response, service, observableArrayList);
                chatNotificationThread = new Thread(service);
                chatNotificationThread.start();
            }
            else if(response instanceof DocumentShowResponse)
            {
                /** 
                 *  imposto l'interfaccia in modalità "sola lettura", non deve
                 *  essere quindi avviato nessun servizio di chat
                 */    
                controller.initData((DocumentShowResponse) response);
                service = null;
            }
            
            
            primaryStage.setOnCloseRequest((value) -> {
                value.consume();
                new ErrorDialog("Uscire dalla finestra di editing prima di chiudere l'applicazione").showDialog();
            });
            
            /* l'interfaccia è stata inizializzata => posso mostrare la scena */
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);          
            primaryStage.show();
        }
        catch (IOException ex) {
            Logger.getLogger(ClientFXMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Procedura per ritornare al menù principale dopo la conclusione sulla 
     *  pagina di EDIT.
     */
    public void backToMainMenu() {
        closeChat(); /* chiudo la chat */
        
        /* carico la scena salvata in precedenza */
        primaryStage.setOnCloseRequest((value) -> {
            exitRoutine();
        });
        primaryStage.setScene(mainMenuScene);
        primaryStage.show();
    }
    
    /**
     * Procedurà per ritornare alla pagina di LOGIN a seguito di una avvenuta
     * disconnessione.
     */
    public void backToLoginMenu() {
        ClientDriver.closeSocket(); /* chiudo il thread per la omunicazione TCP */
        try {
            start(primaryStage); /* eseguo nuovamente l'inizializzazione */
        }
        catch (IOException ex) {
            Logger.getLogger(ClientFXMain.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /**
     * Metodo per la visualizzazione del popup di Info.
     */
    public void showAboutMe() {
        PopOver pop = new PopOver();
        
        pop.setCloseButtonEnabled(true);
        pop.setHeaderAlwaysVisible(true);
        pop.setTitle("About");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("AboutFXML.fxml"));
        try {
            pop.setContentNode((Parent) loader.load());
        }
        catch (IOException ex) {
            Logger.getLogger(ClientFXMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        pop.show(primaryStage);
    }
    
    /**
     * Metodo start per l'inizializzazione delle risorse della classe e 
     * l'esecuzione dell'interfaccia di LOGIN.
     * 
     * @param primaryStage stage di default
     * @throws IOException se non è possibile avviare l'interfaccia
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage; /* salvo lo stage */
        
        primaryStage.setTitle(CLIENT_NAME);
        /**
         * Icon made by prosymbols from www.flaticon.com 
         */
        primaryStage.getIcons().add(new Image(ClientFXMain.class.getResource("/GUI/icons/icon.png").toString()));
        primaryStage.setResizable(false);
        /* imposto la routine di pulizia successiva alla chiusura della finestra */
        primaryStage.setOnCloseRequest((value) -> {
            exitRoutine();
        });
        
        /* carico l'interfaccia di LOGIN */
        FXMLLoader root = new FXMLLoader(getClass().getResource("LoginFXML.fxml"));
        
        primaryStage.setScene(new Scene((Parent)root.load()));
        primaryStage.show(); /* mostro la finestra */
    }

    /**
     * Metodo main per l'esecuzione dell'interfaccia grafica.
     * 
     * @param args non prende argomenti
     */
    public static void main(String[] args) {
        Application.launch(); 
    }
    
}
