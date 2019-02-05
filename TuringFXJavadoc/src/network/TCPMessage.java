/*
 * 
 * 
 * 
 */
package network;

import java.io.Serializable;

/**
 * Interfaccia radice TCPMessage. OGNI oggetto TCP inviato deve implementare questa classe.
 * Nota: implementa Serializable
 * 
 * @author mc - Marco Costa - 545144
 */
public interface TCPMessage extends Serializable {
    static final long SerialVersionUID = 1L;
}
