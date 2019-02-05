/*
 * 
 * 
 * 
 */
package GUI;

import GUI.dialogs.ErrorDialog;
import GUI.dialogs.InfoDialog;
import client.ChatService;
import client.ClientDriver;
import core.exceptions.ErrorReceivedException;
import core.exceptions.InvalidParameterException;
import network.responses.DocumentEditResponse;
import network.responses.DocumentShowResponse;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import core.TuringParameters;

/**
 * FXML Controller per l'interfaccia di Editing e visualizzazione.
 * Assume due possibili stati a seconda che venga richiesta VISUALIZZAZIONE o 
 * MODIFICA del documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class EditPageFXMLController implements Initializable {
    
    /* Componenti dell'interfaccia grafica */
    @FXML
    private TextArea documentArea;

    @FXML
    private Button saveDocumentButton;

    @FXML
    private Button exitDocumentButton;
    
    @FXML
    private ListView<String> chatView;

    @FXML
    private Button sendMessage;

    @FXML
    private TextArea chatBox;
    
    /* variabili di controllo */
    private DocumentEditResponse documentEditResponse;
    private ChatService service = null;
    
    /**
     * Handler per l'azione di invio di un nuovo messaggio.
     * Acquisisce il testo dalla chatBox e, se questo non è vuoto, aggiunge il
     *  messaggio alla lista di messaggi da inviare al servizio di chat.
     * 
     * Nota: il pulsante di invio è abilitato solo in modalità MODIFICA.
     * 
     * @param event evento
     */
    @FXML
    void handleSendMessage(ActionEvent event) {
        try {
            String text = chatBox.getText();
            
            if(!text.isEmpty())
                service.sendMessage(text);
            chatBox.clear();
        }
        catch (InterruptedException ex) {
            new ErrorDialog(ex.getLocalizedMessage()).showDialog();
        }
    }

    /**
     * Handler per il salvataggio del documento e uscita dalla schermata.
     * 
     * Acquisisce il testo dall'area del documento e effettua una richiesta
     * di End_Edit al server.
     * In caso di successo nel salvataggio mostra una schermata informativa o, in caso contrario,
     * una schermata di errore.
     * Restituisce il controllo al MENU PRINCIPALE.
     * 
     * @param event 
     */
    @FXML
    void handleSaveDocumentAndExit(ActionEvent event) {
        String newDocumentText = documentArea.getText();
        
        try {
            ClientDriver.getInstance().requestDocumentEndEdit(documentEditResponse, newDocumentText);
            new InfoDialog("Documento salvato con successo!").showDialog();        
        }
        catch (IOException | ErrorReceivedException | InvalidParameterException ex) {
            new ErrorDialog(ex.getLocalizedMessage()).showDialog();
        }
        
        ClientFXMain.getInstance().backToMainMenu();
    }
    
    @FXML
    void showInfo(ActionEvent event) {
        ClientFXMain.getInstance().showAboutMe();
    }
    
    /** 
     * Inizializzazione della scena in modalità EDITING
     * 
     * @param documentResponse la response del server
     * @param service il servizio di notifiche
     * @param list la lista che rappresenta la vista della chat
     */
    public void initData(DocumentEditResponse documentResponse, ChatService service, ObservableList<String> list) {
        this.documentEditResponse = documentResponse;
        this.service = service;
        
        String currentDocument = documentResponse.getDocumentContent();
        
        /* inserisco la stringa passata dal server nella text box */
        documentArea.setText(currentDocument);
        
        /**
         * Se si esce senza salvare invio al Server come End_Edit la stringa
         *  inviata inizialmente (documento prima della modifica)
         */
        exitDocumentButton.setOnAction((event) -> {
            try {
                ClientDriver.getInstance().requestDocumentEndEdit(documentResponse, currentDocument);
                ClientFXMain.getInstance().backToMainMenu();
            }
            catch (IOException | ErrorReceivedException | InvalidParameterException ex) {
                new ErrorDialog("Errore imprevisto " + ex.getLocalizedMessage()).showDialog();
            }
        });
        
        /* imposto come lista che rappresenta la visualizzazione dei messaggi
            la nuova lista controllata dal servizio di notifiche */
        chatView.setItems(list);
        
    }
    
    /**
     * Inizializzazione della scena in modalità VISUALIZZAZIONE.
     * 
     * @param documentResponse la risposta del server alla richiesta di Show
     *                         contiene 
     */
    public void initData(DocumentShowResponse documentResponse) {    
        /* disabilito le componenti non necessarie */
        saveDocumentButton.setDisable(true);  
        sendMessage.setDisable(true);
        documentArea.setEditable(false);
        chatBox.setDisable(true);
        
        /* imposto il testo del documento */
        documentArea.setText(documentResponse.getDocumentWrapper().getDocumentContent());
        
        /* imposto l'handler del pulsante esci per tornare al menù */
        exitDocumentButton.setText("Esci");
        exitDocumentButton.setOnAction((event) -> {
            ClientFXMain.getInstance().backToMainMenu();
        });      
    }
    
    /**
     * Inizializzazione predefinita della Scena.
     * 
     * @param location
     * @param resources 
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        documentArea.setWrapText(true);
        chatBox.setWrapText(true);
        
        /* imposta la dimensione massima del messaggio inseribile nella chat box */
        chatBox.lengthProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.intValue() > oldValue.intValue()) {
                if (chatBox.getText().getBytes().length >= TuringParameters.MAX_CHAT_MESSAGE_LENGTH) {
                    chatBox.setText(chatBox.getText().substring(0, TuringParameters.MAX_CHAT_MESSAGE_LENGTH));
                }
            }
        });

        /* imposta i messaggi della chat visualizzabili su più righe */
        chatView.setCellFactory(tc -> {
            ListCell<String> cell = new ListCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            cell.setPrefWidth(chatView.getWidth());
            text.setWrappingWidth(chatView.getWidth());
            text.textProperty().bind(cell.itemProperty());
            
            return cell;
        }); 
      
    }
    
}
