/*
 * 
 * 
 * 
 */
package GUI.dialogs;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Region;

/**
 * Classe per un generico dialogo di conferma.
 * @author mc - Marco Costa - 545144
 */
public class ConfirmationDialog {
    private final Alert alert;

    public ConfirmationDialog(String header) {
        alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText(header);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    }
    
    public Optional<ButtonType> show() {
        return alert.showAndWait();
    }
}
