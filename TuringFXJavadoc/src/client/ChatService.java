/*
 * 
 * 
 * 
 */
package client;

import network.NetworkProtocol;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import core.TuringParameters;

/**
 * Servizio di chat multicast UDP.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ChatService implements Runnable {
    private final DatagramChannel chatChannel; /* -> multicast UDP non bloccante */ 
    private final Selector s;
    private final InetAddress group;
    private final NetworkInterface ni;
    private final InetSocketAddress sockaddr;
    
    private boolean isRunning = false;
    private boolean isInterrupted = false;
    
    private final String username;
    private final ObservableList<String> chatList;
    
    /**
     * Lista FIFO concorrente per l'inserimento della richiesta di invio di un messaggio da
     * parte del Thread grafico
     */
    private LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<>();
    
    /**
     * Costruttore per il Servizio di Chat Multicast UDP.
     * 
     * @param ni l'interfaccia di rete
     * @param groupAddress l'indirizzo IP del gruppo
     * @param username il nome utente
     * @param chatList lista dei messaggi della chat (aggiornano l'interfaccia grafica)
     * @throws IOException 
     */
    public ChatService(NetworkInterface ni, String groupAddress, String username, ObservableList<String> chatList) throws IOException {
        this.chatList = chatList;
        this.ni = ni;    
        this.username = username;
        
        /**
         * Imposto il DatagramChannel in modalità Multicast.
         * 
         * @see https://docs.oracle.com/javase/7/docs/api/java/nio/channels/MulticastChannel.html
         */
        try {
            chatChannel = DatagramChannel.open(StandardProtocolFamily.INET)
                    .setOption(StandardSocketOptions.SO_REUSEADDR, true)
                    .bind(new InetSocketAddress(TuringParameters.CHAT_PORT))
                    .setOption(StandardSocketOptions.IP_MULTICAST_IF, ni);
            
            
            group = InetAddress.getByName(groupAddress);
            /* imposto il canale non bloccante */
            chatChannel.configureBlocking(false);
            
            s = Selector.open();
            
            sockaddr = new InetSocketAddress(group, TuringParameters.CHAT_PORT);
            /* join al gruppo */
            chatChannel.join(group, ni);
        }
        catch (IOException ex) {
            throw new IOException("Impossibile avviare il servizio di chat");
        }
        
    }
    
    /**
     * Richiesta di invio di un nuovo messaggio nella chat.
     * 
     * @param message il messaggio
     * @throws InterruptedException se il Thread è stato interrotto durante
     *                              l'inserimento del messaggio
     */
    public void sendMessage(String message) throws InterruptedException {
        requests.put(message);
        s.wakeup(); /* sveglio il Thread in attesa sul selettore */
    }
    
    /**
     * Chiusura del Thread service per la chat.
     */
    public void closeSocket() {
        if(isRunning)
        {
            isInterrupted = true;
            s.wakeup();
        }
    }
    
    /**
     * Routine del Servizio di chat.
     * 
     */
    @Override
    public void run() {
        if(isRunning) throw new IllegalStateException("Il server di notifica è già in esecuzione");
        
        try {
            /* registro il selettore in SOLA LETTURA di messaggi */
            chatChannel.register(s, SelectionKey.OP_READ);
            isRunning = true;
            
            while(true)
            {
                s.select();
                if(isInterrupted)
                    return; /* goto finally */
                
                /* verifico che non ci sia un messaggio nella lista delle richieste 
                    NOTA: la poll() restituisce immediatamente il controllo */
                String sentMessage;
                while((sentMessage = requests.poll()) != null) /* c'è messaggio da inviare */
                    NetworkProtocol.sendChatMessage(username, sentMessage, chatChannel, sockaddr);
                
                             
                Set<SelectionKey> keys = s.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                
                while(it.hasNext())
                {
                    SelectionKey k = it.next();
                    it.remove();
                    try {
                        if(!k.isValid()) continue;
                        if(k.isReadable()) /* nuovo messaggio */
                        {
                            String message = NetworkProtocol.receiveChatMessage(chatChannel);
                            /* lo passo al thread grafico */
                            showMessage(message); 
                        }
                    }
                    catch(IOException ex) {
                        Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }  
            }
            
        }
        catch (ClosedChannelException ex) {
            Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex) {
            Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                /* mi tolgo dal gruppo */
                chatChannel.close();
            }
            catch (IOException ex) {
                Logger.getLogger(ChatService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Richiesta al Thread grafico di mostrare il messaggio sulla vista grafica.
     * 
     * @param message il messaggio
     */
    private void showMessage(String message) {
        Platform.runLater(() -> {
            chatList.add(message);
        });
        
    }
    
}
