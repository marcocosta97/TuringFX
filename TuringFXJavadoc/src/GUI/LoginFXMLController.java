/*
 * 
 * 
 * 
 */
package GUI;

import GUI.dialogs.PasswordConfirmationDialog;
import GUI.dialogs.InfoDialog;
import GUI.dialogs.ErrorDialog;
import client.ClientDriver;
import core.exceptions.ErrorReceivedException;
import core.exceptions.InvalidParameterException;
import core.exceptions.ServerConnectionException;
import core.exceptions.UserAlreadySignedException;
import core.security.SecurePasswordWrap;
import core.GenericUtils;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

/**
 * FXML Controller per la finestra di Login (e Registrazione).
 *
 * @author mc - Marco Costa - 545144
 */
public class LoginFXMLController implements Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private BorderPane loginPane;
    @FXML
    private Button loginButton;
    @FXML
    private Button signinButton;

    
    public LoginFXMLController() {} 
    
    /**
     * Metodo per la verifica della consistenza dei campi username e password
     * 
     * @throws InvalidParameterException 
     */
    private void checkFields() throws InvalidParameterException {
        try {
            GenericUtils.checkEmptyString(usernameField.getText());
        }
        catch(InvalidParameterException ex) {
            throw new IllegalArgumentException("Il campo utente è vuoto!");
        }
        try {
            GenericUtils.checkEmptyString(passwordField.getText());
        }
        catch(InvalidParameterException ex) {
            throw new IllegalArgumentException("Il campo password è vuoto!");
        }
    }
    
    /**
     * Pulizia dei campi username e password.
     */
    private void clearFields() {
        passwordField.clear();
        
        Platform.runLater(() -> {
            loginPane.requestFocus();
        });
    }
    
    /**
     * Handler per l'operazione di Login.
     * 
     * @param event 
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        try {
            checkFields();
            
            String u = ClientDriver.getInstance().requestLogin(usernameField.getText(), passwordField.getText());
            
            new InfoDialog("Successo! Accesso eseguito come " + u).showDialog();
            
            ClientFXMain.getInstance().switchToMainMenu();
        }
        catch(IllegalArgumentException | IOException | ErrorReceivedException | InvalidParameterException ex) {
            new ErrorDialog(ex.getLocalizedMessage()).showDialog();
            clearFields();    
        }   
    }
    
    
    /**
     * Handler per l'operazione di registrazione.
     * 
     * @param event 
     */
    @FXML
    private void handleSignin(ActionEvent event) {
        try {
            checkFields();
            SecurePasswordWrap passWrap = new SecurePasswordWrap(passwordField.getText());
            PasswordConfirmationDialog confirmPass = new PasswordConfirmationDialog();
            Optional<String> result = confirmPass.showAndWait();
            
            if(result.isPresent())
            {
                /* verifica della password */
                // SecurePasswordWrap confirmPassWrap = new SecurePasswordWrap(result.get(), passWrap.getNonce());
                
                String confirmPassWrap = result.get();
                if(!passWrap.equals(confirmPassWrap))
                    throw new InvalidParameterException("Le due password non coincidono");
                
                try {
                    ClientDriver.getInstance().requestSignup(usernameField.getText(), passWrap);
                    new InfoDialog("Utente registrato con successo").showDialog();
                }
                catch(ServerConnectionException ex) {
                    new ErrorDialog(ex.getMessage()).showDialog();
                }
                catch (UserAlreadySignedException ex) {
                    new InfoDialog("Registrazione fallita! " + ex.getMessage()).showDialog();
                }
            }            
        }
        catch(InvalidParameterException ex) {
            new ErrorDialog(ex.getLocalizedMessage()).showDialog();
        }
        
        clearFields();
    }
    
    @FXML
    private void showInfo(ActionEvent event) {
        ClientFXMain.getInstance().showAboutMe();
    }
    
    /**
     * Inizializzazione predefinita della Scena di LOGIN.
     * 
     * @param url
     * @param rb 
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        /** 
         * disabilito i pulsanti di registrazione e login finché entrambi
         *  i campi username e password sono vuoti
         */
        BooleanBinding userObs = Bindings.isEmpty(usernameField.textProperty());
        BooleanBinding passObs = Bindings.isEmpty(passwordField.textProperty());
        
        loginButton.disableProperty().bind(userObs);
        loginButton.disableProperty().bind(passObs);
        signinButton.disableProperty().bind(userObs);
        signinButton.disableProperty().bind(passObs);
        
        Platform.runLater(() -> {
            loginPane.requestFocus();
        });
    }    
    
}
