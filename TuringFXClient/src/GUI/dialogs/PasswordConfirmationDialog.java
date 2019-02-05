/*
 * 
 * 
 * 
 */
package GUI.dialogs;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Finestra di dialogo per la conferma della password durante la registrazione.
 * 
 * @author mc - Marco Costa - 545144
 */
public class PasswordConfirmationDialog extends Dialog<String> {
  private PasswordField passwordField;

  public PasswordConfirmationDialog() {
    setTitle("Conferma password");
    setHeaderText("Per favore conferma la password");

    ButtonType passwordButtonType = new ButtonType("Conferma", ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(passwordButtonType, ButtonType.CANCEL);
    getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    
    passwordField = new PasswordField();
    passwordField.setPromptText("Password");

    HBox hBox = new HBox();
    hBox.getChildren().add(passwordField);
    hBox.setPadding(new Insets(20));

    HBox.setHgrow(passwordField, Priority.ALWAYS);

    getDialogPane().setContent(hBox);
    
    Platform.runLater(() -> passwordField.requestFocus());

    setResultConverter(dialogButton -> {
      if (dialogButton == passwordButtonType) {
        return passwordField.getText();
      }
      return null;
    });
  }

  /**
   * Restituzione del campo password
   * @return il campo password
   */
  public PasswordField getPasswordField() {
    return passwordField;
  }
}
