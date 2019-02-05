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
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

/**
 * Dialogo per l'inserimento di informazioni sul nuovo documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class NewDocumentDialog {
    private javafx.scene.control.Dialog<Pair<String, Integer>> dialog = new javafx.scene.control.Dialog<>();
    
    public NewDocumentDialog() {
        dialog.setHeaderText("Inserisci dati documento: ");

        ButtonType loginButtonType = new ButtonType("Conferma", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        /* imposto la vista */
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField documentName = new TextField();
        TextField noSections = new TextField();

        grid.add(new Label("Nome documento:"), 0, 0);
        grid.add(documentName, 1, 0);
        grid.add(new Label("Numero sezioni:"), 0, 1);
        grid.add(noSections, 1, 1);

        /* imposto solo valori numerici per la sezione */
        noSections.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                noSections.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
        
        /* disabilito la conferma finché non vengono inserite tutte le informazioni */
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        BooleanBinding docObs = Bindings.isEmpty(documentName.textProperty());
        BooleanBinding secObs = Bindings.isEmpty(noSections.textProperty());
        
        loginButton.disableProperty().bind(docObs);
        loginButton.disableProperty().bind(secObs);
        
        dialog.getDialogPane().setContent(grid);

        Platform.runLater(() -> documentName.requestFocus());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return new Pair<>(documentName.getText(), Integer.parseInt(noSections.getText()));
            }
            return null;
        });

    }
    
    
    public Optional<Pair<String, Integer>> showAndWait() {
        return dialog.showAndWait();
    }
}
