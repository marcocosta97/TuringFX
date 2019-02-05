/*
 * 
 * 
 * 
 */
package GUI.dialogs;

import javafx.scene.control.Alert;

/**
 * Dialogo di errore.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ErrorDialog extends Dialog {
    private static final String title = "Errore!";
    private static final String header = "Attenzione, si è verificato un errore:";
    
    public ErrorDialog(String context) {
        super(Alert.AlertType.ERROR, header, context);
    }

    @Override
    public void showDialog() {
        window.showAndWait();
    }
    
}
