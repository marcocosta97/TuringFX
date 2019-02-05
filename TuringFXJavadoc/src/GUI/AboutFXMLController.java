/*
 * 
 * 
 * 
 */
package GUI;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;

/**
 * FXML Controller per il Popup di info.
 *
 * @author mc - Marco Costa - 545144
 */
public class AboutFXMLController implements Initializable {
    /* Componenti dell'interfaccia grafica */
    @FXML
    private Hyperlink gitUrl;

    @FXML
    private Hyperlink flickrUrl;

    /**
     * Inizializza il controller
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE))
        {
            /* apertura del browser se si clicca sui link */
            gitUrl.setOnAction((value) -> {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().browse(new URI("https://www." + gitUrl.getText()));
                    }
                    catch (IOException | URISyntaxException ex) {
                        Logger.getLogger(AboutFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }).start();
                
            });
            flickrUrl.setOnAction((value) -> {
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().browse(new URI("https://www." + flickrUrl.getText()));
                    }
                    catch (IOException | URISyntaxException ex) {
                        Logger.getLogger(AboutFXMLController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }).start();
            });
        }
        else
        {
            gitUrl.disarm();
            flickrUrl.disarm();
        }
        
    }    
    
}
