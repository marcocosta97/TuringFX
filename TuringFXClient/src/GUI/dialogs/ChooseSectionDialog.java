/*
 * 
 * 
 * 
 */
package GUI.dialogs;

import java.util.Optional;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;

/**
 * Dialogo per la scelta della sezione alla richiestra di creazione di un 
 * documento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ChooseSectionDialog {
    private final TextInputDialog dialog;
    
    public ChooseSectionDialog() {
        dialog = new TextInputDialog();
        dialog.setHeaderText("Scegli la sezione sulla quale eseguire l'operazione");
        dialog.setContentText("Numero di sezione: ");

        TextField sectionsTextField = dialog.getEditor();
        /* disabilito i valori che non sono numeri nell'inserimento della sezione */
        sectionsTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (!newValue.matches("\\d*")) {
                sectionsTextField.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
    
    public Optional<String> showAndWait() {
        return dialog.showAndWait();
    }
}
