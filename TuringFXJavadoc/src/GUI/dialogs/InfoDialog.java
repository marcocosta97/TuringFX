/*
 * 
 * 
 * 
 */
package GUI.dialogs;

import javafx.scene.control.Alert.AlertType;

/**
 * Dialogo informativo generico
 * 
 * @author mc - Marco Costa - 545144
 */
public class InfoDialog extends Dialog {

    public InfoDialog(String context) {
        super(AlertType.INFORMATION, "Informazioni", context);
    }

    @Override
    public void showDialog() {
        this.window.showAndWait();
    }
    
}
