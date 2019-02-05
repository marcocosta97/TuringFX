/*
 * 
 * 
 * 
 */
package network;

import core.GenericUtils;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import core.TuringParameters;
import java.util.Arrays;

/**
 * Protocollo di comunicazione Client/Server (TCP) e Client/Clients (UDP).
 * 
 * @author mc - Marco Costa - 545144
 */
public class NetworkProtocol {
    /* dimensione predefinita del Buffer per l'invio e ricezione della dimensione del 
        TCPMessage */
    private static final int TCP_LENGTH_BUFFER_SIZE = Integer.BYTES;
    
    /**
     * Interfaccia per l'invio di un messaggio TCP su SocketChannel.
     * 
     * @param destSock il canale di destinazione
     * @param o il messaggio da inviare
     * @throws IOException in caso non fosse possibile eseguire l'invio
     */
    public static void sendTCPData(SocketChannel destSock, TCPMessage o) throws IOException {
        /**
         * Il protocollo prevede:
         *  - Serializzazione del messaggio
         *  - Invio della dimensione del messaggio serializzato
         *  - Invio del messaggio serializzato
         */  
        byte[] b = GenericUtils.getByteFromTCPMessage(o);
        int length = b.length;
        
        /* allocazione di un buffer contenente la dimensione del messaggio */
        ByteBuffer objectSize = ByteBuffer.allocate(TCP_LENGTH_BUFFER_SIZE);
        objectSize.putInt(length);
        objectSize.flip();
        
        /* invio dimensione del messaggio */
        while(objectSize.hasRemaining())
            destSock.write(objectSize);
        
        /* invio messaggio */
        ByteBuffer objectBuffer = ByteBuffer.wrap(b);
        while(objectBuffer.hasRemaining())
            destSock.write(objectBuffer);
    }
    
    /**
     * Interfaccia per la ricezione di un messaggio TCP su SocketChannel.
     * 
     * @param mittSock il canale di provenienza
     * @return il messaggio ricevuto
     * @throws IOException in caso non fosse possibile eseguire la ricezione
     */
    public static TCPMessage receiveTCPData(SocketChannel mittSock) throws IOException {
        /**
         * Protocollo:
         *  - Ricezione della dimensione del messaggio in arrivo
         *  - Ricezione del messaggio serializzato
         *  - Deserializzazione del messaggio
         */   
        ByteBuffer objectSize = ByteBuffer.allocate(TCP_LENGTH_BUFFER_SIZE);
        
        while(objectSize.remaining() != 0)
        {
            int read = mittSock.read(objectSize);
            if(read == -1) /* end of stream -> errore */
                throw new IOException();
        }
        
        objectSize.flip();
        ByteBuffer objectBuff = ByteBuffer.allocate(objectSize.getInt());
        
        while(objectBuff.remaining() != 0)
        {
            int read = mittSock.read(objectBuff);
            if(read == -1) /* end of stream -> errore */
                throw new IOException();
        }
        
        return GenericUtils.getTCPMessageFromBytes(objectBuff.array());
    }
    
    /**
     * Interfaccia per la ricezione di un messaggio di Chat multicast su 
     *  DatagramChannel.
     * 
     * @param mittSock il canale di provenienza
     * @return il messaggio ricevuto
     * @throws IOException in caso non fosse possibile eseguire la ricezione
     */
    public static String receiveChatMessage(DatagramChannel mittSock) throws IOException {
        ByteBuffer objectBuff = ByteBuffer.allocate(TuringParameters.CHAT_MAX_BUFFER_SIZE);
        
        /* non è necessario incapsulare in un oggetto datagramma, è fatto dal canale */
        mittSock.receive(objectBuff);
           
        return new String(objectBuff.array(), TuringParameters.DEFAULT_CHARSET).trim();
    }
    
    /**
     * Interfaccia per l'invio di un messaggio di Chat multicast su DatagramChannel.
     * Nota: esegue la formattazione del messaggio
     * 
     * @param username nome dell'utente che esegue l'invio
     * @param content contenuto del messaggio
     * @param destSock canale di destinazione
     * @param target indirizzo dal quale deve essere inviato il datagramma
     * @throws IOException in caso non fosse possibile eseguire l'invio
     */
    public static void sendChatMessage(String username, String content, DatagramChannel destSock, SocketAddress target) throws IOException {
        String message = username + ": " + content.trim();
        byte[] messageBytes = message.getBytes(TuringParameters.DEFAULT_CHARSET);
        
        if(messageBytes.length > TuringParameters.CHAT_MAX_BUFFER_SIZE)
            messageBytes = Arrays.copyOf(messageBytes, TuringParameters.CHAT_MAX_BUFFER_SIZE);
        
        ByteBuffer objectBuffer = ByteBuffer.wrap(messageBytes);
        
        /* non è necessario incapsulare in un oggetto datagramma, è fatto dal canale */
        while(objectBuffer.hasRemaining())
            destSock.send(objectBuffer, target);
       
    }
}
