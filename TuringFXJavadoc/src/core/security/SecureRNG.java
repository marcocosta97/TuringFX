/*
 * 
 * 
 * 
 */
package core.security;

import java.security.SecureRandom;

/**
 * Implementa un generatore sicuro di numeri casuali (SRNG).
 * 
 * @author mc - Marco Costa - 545144
 */
/* p - private */ class SecureRNG {
    /**
     * Constructs a secure random number generator (RNG) implementing the default random number algorithm.
     * 
     * Note that if a seed is not provided, it will generate a seed from a true random number generator (TRNG).
     */
    private static SecureRandom rand = new SecureRandom();
    
    /**
     * Genera un nonce casuale.
     * 
     * @return l'oggetto ByteNonce
     */
    /* package-private */ static ByteNonce generateNonce() {
        ByteNonce n = new ByteNonce();
        
        rand.nextBytes(n.Bytes());
        
        return n;
    }
}
