/*
 * 
 * 
 * 
 */
package GUI;

import GUI.dialogs.ChooseSectionDialog;
import GUI.dialogs.NewDocumentDialog;
import GUI.dialogs.ErrorDialog;
import GUI.dialogs.InfoDialog;
import GUI.dialogs.DocumentOperationDialog;
import GUI.notifications.WarningNotification;
import client.ClientDriver;
import core.User;
import core.exceptions.ErrorReceivedException;
import core.exceptions.InvalidParameterException;
import files.DocumentInfo;
import network.notifications.TCPNotification;
import network.responses.DocumentEditResponse;
import network.responses.DocumentShowResponse;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.util.Pair;

/**
 * Controller per la schermata MENU.
 * Nota: è una classe Singleton
 * 
 * @author mc - Marco Costa - 545144
 */
public class MainMenuFXMLController implements Initializable {
    /* Componenti dell'interfaccia grafica */
    @FXML
    private MenuItem signoutMenu;

    @FXML
    private Button createDocumentButton;

    @FXML
    private Button invitationButton;

    @FXML
    private Button signoutButton;

    @FXML
    private MenuButton documentList;
    
    @FXML
    private Text helloText;
    
    /* variabili locali */
    private User user;
    private ObservableList<MenuItem> items;
    private final HashMap<MenuItem, DocumentInfo> mapping = new HashMap<>(); 
    
    private static MainMenuFXMLController instance; /* istanza della classe */
    
    
    public MainMenuFXMLController() {
        instance = this;
    }
    
    public static MainMenuFXMLController getInstance() {
        return instance;
    }
    
    /**
     * Aggiornamento della vista a seguito dell'aggiunta di un nuovo documento
     * (sia creato dall'utente che a seguito di un invito )
     * 
     * @param d le informazioni sul documento da aggiungere
     */
    public void addDocumentInfo(DocumentInfo d) {
        /**
         * Se questo è il primo documento abilito il pulsante di invito 
         * e il menù dei documenti
         */
        if(items.isEmpty())
            documentList.setDisable(false);
        if(invitationButton.isDisabled() && d.getCreator().equals(user.getUsername()))
            invitationButton.setDisable(false);
            
        
        MenuItem m = new MenuItem(d.getDocumentName() + " (creator: " + d.getCreator() + ")");
        m.setOnAction((event) -> {
            handleDocumentOperation(m);
        });
        
        mapping.put(m, d);
        items.add(m);
    }
    
    /**
     * Handler per la creazione di un nuovo documento.
     * Apre una nuova finestra di dialogo per l'immissione dei dati e 
     * contatta il Driver per la gestione della richiesta.
     * 
     * @param event 
     */
    @FXML
    void handleCreateDocument(ActionEvent event) {
        try {
            NewDocumentDialog dialog = new NewDocumentDialog();

            Optional<Pair<String, Integer>> result = dialog.showAndWait();
            if (result.isPresent()){
                String documentName = result.get().getKey();
                DocumentInfo d = ClientDriver.getInstance().requestDocumentCreation(documentName, result.get().getValue());
                
                /* se non è stata generata alcuna eccezione posso aggiungere il 
                   documento alla lista dei documenti utente */
                addDocumentInfo(d);
                
                new InfoDialog("Documento " + documentName + " creato con successo!").showDialog();
            }   
        }
        catch (IOException | ErrorReceivedException | InvalidParameterException ex) {
            new ErrorDialog(ex.getLocalizedMessage()).showDialog();
        }
    }

    /**
     * Handler per l'operazione di invito di un utente a collaborare.
     * Apre una finestra di dialogo per l'immissione del nome utente del 
     * collaboratore e del documento sul quale si vuole collaborare.
     * 
     * @param event 
     */
    @FXML
    void handleInvitationButton(ActionEvent event) {
        try {
            ObservableList<String> validDocuments = FXCollections.observableArrayList();
            
            /* seleziono tutti i documenti CREATI dall'utente loggato */
            for(DocumentInfo info : user.getDocuments())
            {
                if(info.getCreator().equals(user.getUsername()))
                    validDocuments.add(info.getDocumentName());
            }
           
            /* apertura della finestra di dialogo */
            DocumentOperationDialog dialog = new DocumentOperationDialog(validDocuments);
            Optional<Pair<String, String>> result = dialog.showAndWait();
            
            if (result.isPresent())
            {
                String invitedUser = result.get().getKey();
                String documentName = result.get().getValue();
                
                /* invio della richiesta al Driver */
                ClientDriver.getInstance().requestInvitation(new DocumentInfo(documentName, user.getUsername()),
                        invitedUser);
                
                new InfoDialog("Invito eseguito correttamente!").showDialog();
            }
            
        }
        catch (IOException | ErrorReceivedException | InvalidParameterException ex) {
            new ErrorDialog("Errore! " + ex.getLocalizedMessage()).showDialog();
        }
    }

    /**
     * Handler per le diverse operazioni possibili su un documento già creato.
     * Mostra a video una finestra di dialogo con le possibili scelte (Edit, 
     * Show(S, D), Show(D)) ed utilizza l'interfaccia del Driver appropriata
     * per la gestione della richiesta.
     * 
     * @param m 
     */
    private void handleDocumentOperation(MenuItem m) {
        /* creazione della finestra di dialogo */
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setHeaderText("Che operazione vuoi effettuare su " + m.getText() + "?");

        ButtonType showPart = new ButtonType("Mostra sezione");
        ButtonType showEntire = new ButtonType("Mostra tutto");
        ButtonType editDocument = new ButtonType("Modifica");
        ButtonType buttonTypeCancel = new ButtonType("Annulla", ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(showPart, showEntire, editDocument, buttonTypeCancel);
        alert.getDialogPane().setMinWidth(Region.USE_PREF_SIZE);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        /* mostro il dialogo */
        Optional<ButtonType> result = alert.showAndWait();
        
        ButtonType resultButton = result.get();
        
        /* switch sui possibili risultati */ 
        /**
         *  è stato scelto di modificare il documento o di visualizzare
         *  una sezione specifica, quindi deve essere mostrata a video una 
         *  finestra di dialogo per la scelta della sezione
         */
        if ((resultButton == showPart) || (resultButton == editDocument))
        {   
            ChooseSectionDialog dialog = new ChooseSectionDialog();
            
            /* mostro il dialogo */
            Optional<String> r = dialog.showAndWait();
            if (r.isPresent())
            {
                int section = Integer.parseInt(r.get());
                /* Show(S, D) */
                if(resultButton == showPart)
                {
                    try {
                        DocumentShowResponse requestDocumentShow = ClientDriver.getInstance().requestDocumentShow(mapping.get(m), section);               
                        String userMessage = requestDocumentShow.getDocumentWrapper().getUserMessage();
                        /* mostro l'eventuale messaggio inviato dal server */
                        if(!userMessage.isEmpty())
                            new InfoDialog(userMessage).showDialog();
                        
                        ClientFXMain.getInstance().switchToEditPage(requestDocumentShow);
                    }
                    catch (IOException | ErrorReceivedException | InvalidParameterException ex) {
                        new ErrorDialog(ex.getLocalizedMessage()).showDialog();
                    }
                }
                else /* Edit(S, D) */
                {
                    try {
                        DocumentEditResponse requestDocumentEdit = ClientDriver.getInstance().requestDocumentEdit(mapping.get(m), section);
                        ClientFXMain.getInstance().switchToEditPage(requestDocumentEdit);
                    }
                    catch (IOException | ErrorReceivedException | InvalidParameterException ex) {
                        new ErrorDialog(ex.getLocalizedMessage()).showDialog();
                        
                    }
                }   
            }
            
        }
        /**
         * Visualizzazione dell'intero documento, non deve essere mostrata 
         * alcuna finestra di dialogo
         */
        else if (resultButton == showEntire) 
        {
            try {
                DocumentShowResponse requestDocumentShow = ClientDriver.getInstance().requestDocumentShow(mapping.get(m));

                String userMessage = requestDocumentShow.getDocumentWrapper().getUserMessage();
                /* mostro l'eventuale messaggio inviato dal server */
                if(!userMessage.isEmpty())
                    new InfoDialog(userMessage).showDialog();
                ClientFXMain.getInstance().switchToEditPage(requestDocumentShow);
            }
            catch (IOException | ErrorReceivedException | InvalidParameterException ex) {
                new ErrorDialog(ex.getLocalizedMessage()).showDialog();
            }
        }
    }
    
    /**
     * Handler per l'operazione di Disconnessione.
     * 
     * @param event 
     */
    @FXML
    void handleSignout(ActionEvent event) {
        ClientFXMain.getInstance().backToLoginMenu();
    }
    
    @FXML
    void showInfo(ActionEvent event) {
        ClientFXMain.getInstance().showAboutMe();
    }
    
    /**
     * Inizializzazione delle informazioni dell'utente sulla vista, vengono 
     * inoltre mostrate le eventuali notifiche ricevute dall'utente mentre non
     * era online.
     */
    private void initUserInfo() {
        for(DocumentInfo d : user.getDocuments())
            addDocumentInfo(d);
        
        for(TCPNotification notify : user.getNotifications())
            new WarningNotification("Turing", notify.getMessage()).show();
        
    }
    
    @Override
    @FXML public void initialize(URL location, ResourceBundle resources) {
        user = ClientDriver.getInstance().getUser();
        items = documentList.getItems();
        
        Platform.runLater(() -> {
            helloText.setText("Ciao " + user.getUsername() + "!");
            
            documentList.setDisable(true);
            invitationButton.setDisable(true);
            initUserInfo();
        });      
    }

    


    
}

