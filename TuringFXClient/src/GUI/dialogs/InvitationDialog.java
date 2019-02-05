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
import javafx.util.Pair;

/**
 * Dialogo per la richiesta di invito di un utente a collaborare.
 * 
 * @author mc - Marco Costa - 545144
 */
public class InvitationDialog {
    private javafx.scene.control.Dialog<Pair<String, String>> dialog = new javafx.scene.control.Dialog<>();
    
    public InvitationDialog(ObservableList<String> values) {
        dialog.setHeaderText("Inserisci dati documento: ");

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

        
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        BooleanBinding docObs = Bindings.isEmpty(invitedUser.textProperty());
        
        loginButton.disableProperty().bind(docObs);
        loginButton.disableProperty().bind(docObs);
        
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> invitedUser.requestFocus());

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
