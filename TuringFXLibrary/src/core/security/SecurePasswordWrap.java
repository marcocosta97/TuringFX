/*
 * 
 * 
 * 
 */
package core.security;

import core.exceptions.InvalidParameterException;
import core.GenericUtils;
import java.io.Serializable;
import core.TuringParameters;

/**
 * Classe per la generazione di un Wrapper Nonce - Hash.
 * Da una password in chiaro si ottiene un hash generato mediante Algoritmo scelto (default SHA-256) dovuto
 * alla concatenazione tra la password e un nonce generato mediante SRNG (Secure Random Number Generator).
 * Non è possibile ottenere i campi dell'oggetto, è solo possibile confrontare
 * il wrapper con una stringa o il wrapper con un altro wrapper
 * 
 * @see TuringParameters per l'algoritmo di Hash
 * @author mc - Marco Costa - 545144
 */
public class SecurePasswordWrap implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final ByteNonce nonce;
    private final String hash;
    
    /**
     * Costruttore per la creazione di un nuovo Wrapper a partire da una 
     * password in chiaro.
     * 
     * @param clearPassword password in chiaro
     * @throws InvalidParameterException se la password non è valida
     */
    public SecurePasswordWrap(String clearPassword) throws InvalidParameterException {
        /* generazione del nonce mediante SRNG */
        this(clearPassword, SecureRNG.generateNonce());
    }
    
    /**
     * Costruttore PRIVATO per la costruzione di un wrapper partendo da una 
     * stringa in chiaro e un nonce già generato.
     * 
     * @param clearPassword la password in chiaro
     * @param nonce il nonce
     * @throws InvalidParameterException se la password non è una stringa valida
     */
    private SecurePasswordWrap(String clearPassword, ByteNonce nonce) throws InvalidParameterException {
        GenericUtils.checkPasswordString(clearPassword);
            
        this.nonce = nonce;
        hash = SecureHash.generateHash(clearPassword, nonce);
    }

    
    /**
     * Metodo per la comparazione con un altro wrapper.
     * 
     * @param obj il wrapper da comparare
     * @return true se hanno lo stesso hash, false altrimenti
     */
    public boolean equals(SecurePasswordWrap obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
             
        return hash.equals(obj.hash);
    }
    
    /**
     * Metodo per la comparazione dell'hash con una stringa in chiaro
     * 
     * @param s la stringa
     * @return true se hanno lo stesso hash, false altrimenti
     * @throws InvalidParameterException se la stringa non è una password valida
     */
    public boolean equals(String s) throws InvalidParameterException {
        /* utilizzo il nonce dell'oggetto per generare un nuovo wrapper */
        return equals(new SecurePasswordWrap(s, this.nonce));
    }
    
}
