/*
 * 
 * 
 * 
 */
package core.security;

import core.ErrorCodes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import core.TuringParameters;

/**
 * Classe statica privata per la generazione di un hash sicuro mediante algoritmo 
 * specificato nei parametri dell'applicazione.
 * 
 * @see TuringParameters
 * @author mc - Marco Costa - 545144
 */
/* p - private */ class SecureHash {    
    private static MessageDigest md;
    
    static {
        try {
            md = MessageDigest.getInstance(TuringParameters.PASSWORD_HASH_ALGORITHM);
        }
        catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SecureHash.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(ErrorCodes.SERVER_DATA_INIT_ERROR.getErrorCode());
        }
    }
    
    /**
     * Genera una stringa hash formato dalla concatenazione di un nonce (o salt) e 
     * la password in chiaro.
     * 
     * @param password
     * @param nonce
     * @return la stringa che rappresenta l'hash
     */
    /* p - private */ static String generateHash(String password, ByteNonce nonce) {
        md.update(nonce.Bytes());
        byte[] bytes = md.digest(password.getBytes());
        
        StringBuilder sb = new StringBuilder();
        /* conversione byte in stringa */
        for(int i=0; i < bytes.length ;i++)
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        
        
        return sb.toString();
    }
}
