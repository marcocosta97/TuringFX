/*
 * 
 * 
 * 
 */
package GUI.dialogs;

import java.util.Optional;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.util.Pair;

/**
 * Dialogo per la scelta dell'operazione da eseguire su un documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentOperationDialog {
    private javafx.scene.control.Dialog<Pair<String, String>> dialog = new javafx.scene.control.Dialog<>();
    
    public DocumentOperationDialog(ObservableList<String> values) {
        dialog.setHeaderText("Inserisci dati documento: ");

        // Set the button types.
        ButtonType loginButtonType = new ButtonType("Conferma", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField invitedUser = new TextField();
        ChoiceBox<String> documentList = new ChoiceBox<>(values);

        grid.add(new Label("Nome utente:"), 0, 0);
        grid.add(invitedUser, 1, 0);
        grid.add(new Label("Documento:"), 0, 1);
        grid.add(documentList, 1, 1);

        
        // Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        BooleanBinding docObs = Bindings.isEmpty(invitedUser.textProperty());
        
        loginButton.disableProperty().bind(docObs);
        loginButton.disableProperty().bind(docObs);
        
        dialog.getDialogPane().setContent(grid);

        // Request focus on the username field by default.
        Platform.runLater(() -> invitedUser.requestFocus());

        // Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(invitedUser.getText(), documentList.getValue());
            }
            return null;
        });

    }
    
    public Optional<Pair<String, String>> showAndWait() {
        return dialog.showAndWait();
    }
}
