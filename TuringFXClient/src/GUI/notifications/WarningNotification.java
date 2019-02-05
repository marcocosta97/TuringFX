/*
 * 
 * 
 * 
 */
package GUI.notifications;

/**
 * Notifica informativa di un determinato evento.
 * 
 * @author mc - Marco Costa - 545144
 */
public class WarningNotification extends Notification {

    public WarningNotification(String title, String info) {
        super(title, info);
    }

    @Override
    public void show() {
       this.n.showInformation();
    }
    
}
