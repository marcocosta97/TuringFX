/*
 * 
 * 
 * 
 */
package GUI.dialogs;

import GUI.ClientFXMain;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Region;

/**
 * Classe astratta per un generico dialogo.
 * 
 * @author mc - Marco Costa - 545144
 */
public abstract class Dialog {
    /* la finestra di dialogo */
    protected final Alert window;
    
    /**
     * Costruttore per la finestra di dialogo.
     * 
     * @param alertType tipo di finestra (errore, conferma, ecc)
     * @param header testo dell'header
     * @param context testo del contesto
     */
    public Dialog(AlertType alertType, String header, String context) {
        window = new Alert(alertType);
        window.setHeaderText(header);
        window.setTitle(ClientFXMain.CLIENT_NAME);
        window.setContentText(context);
        window.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
    }
    
    /**
     * Mostra il dialogo.
     */
    public abstract void showDialog();
}
