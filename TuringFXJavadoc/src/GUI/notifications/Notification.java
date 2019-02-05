/*
 * 
 * 
 * 
 */
package GUI.notifications;

import javafx.util.Duration;
import org.controlsfx.control.Notifications;

/**
 * Classe generica per la creazione di una notifica.
 * 
 * @author mc - Marco Costa - 545144
 */
public abstract class Notification {
    private static final Duration HIDE_AFTER = Duration.seconds(30);
    
    protected final Notifications n;
    
    public Notification(String title, String info) {
        n = Notifications.create();
        n.title(title);
        n.text(info);
        n.hideAfter(HIDE_AFTER);
    }
    
    public abstract void show();
}
