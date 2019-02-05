/*
 * 
 * 
 * 
 */
package network;

import core.TuringParameters;

/**
 * Fornisce indirizzi IP Multicast univoci.
 * 
 * @author mc - Marco Costa - 545144
 */
public class ChatAddresser {
    
    private static final IncrementableIPAddress initAddress = new IncrementableIPAddress(TuringParameters.FIRST_MULTICAST_ADDRESS);
    
    public static String getChatAddress(int documentId) {
        return initAddress.increment(documentId);
    }
    
}
