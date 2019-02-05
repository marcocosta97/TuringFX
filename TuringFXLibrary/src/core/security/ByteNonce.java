/*
 * 
 * 
 * 
 */
package core.security;

/**
 * Classe che rappresenta un oggetto di tipo Nonce (o salt) mediante array di byte.
 * 
 * @author mc - Marco Costa - 545144
 */
/* p - private */ class ByteNonce implements Nonce {
    private static final int NONCE_SIZE = 16; //bytes
    
    private final byte[] nonce;
    
    /* p - private */ ByteNonce() {
        nonce = new byte[NONCE_SIZE];
    }
    
    /* p - private */ byte[] Bytes() {
        return nonce;
    }
}
