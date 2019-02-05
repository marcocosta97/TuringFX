/*
 * 
 * 
 * 
 */
package core;

import core.exceptions.InvalidParameterException;
import network.TCPMessage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import core.TuringParameters;

/**
 * Classe con metodi pubblici di utilità generica.
 * 
 * @author mc - Marco Costa - 545144
 */
public class GenericUtils {
    /**
     * Verifica che s non sia nulla o vuota.
     * 
     * @param s la stringa
     * @throws InvalidParameterException in caso fosse nulla o vuota
     */
    public static void checkEmptyString(String s) throws InvalidParameterException {
        if((s == null) || (s.isEmpty())) 
            throw new IllegalArgumentException("L'argomento è nullo");
    }
    
    /**
     * Verifica che le dimensioni di s rientri tra i valori min e max
     * 
     * @param s la stringa
     * @param min
     * @param max
     * @throws InvalidParameterException 
     */
    private static void checkStringBoundaries(String s, int min, int max) throws InvalidParameterException {
        checkEmptyString(s);
        if(s.length() < min || s.length() > max)
            throw new InvalidParameterException();    
    }
    
    /**
     * Verifica che la stringa risulti valida circa i vincoli per il nome utente
     * 
     * @param s il nome utente
     * @throws InvalidParameterException 
     */
    public static void checkUsernameString(String s) throws InvalidParameterException {
        try {
            checkStringBoundaries(s, TuringParameters.MIN_USERNAME_LENGTH, TuringParameters.MAX_USERNAME_LENGTH);
        }
        catch (InvalidParameterException ex) {
            throw new InvalidParameterException("Il nome utente deve avere minimo " + TuringParameters.MIN_USERNAME_LENGTH + 
                    " e massimo " + TuringParameters.MAX_USERNAME_LENGTH + " caratteri");
        }
    }
    
    /**
     * Verifica che la stringa risulti valida circa i vincoli per la password
     * 
     * @param s 
     * @throws core.exceptions.InvalidParameterException 
     */
    public static void checkPasswordString(String s) throws InvalidParameterException {
        try {
            checkStringBoundaries(s, TuringParameters.MIN_PASSWORD_LENGTH, TuringParameters.MAX_PASSWORD_LENGTH);
        }
        catch (InvalidParameterException ex) {
            throw new InvalidParameterException("La password deve avere minimo " + TuringParameters.MIN_PASSWORD_LENGTH + 
                    " e massimo " + TuringParameters.MAX_PASSWORD_LENGTH + " caratteri");
        }
    }
    
    /**
     * Serializza un oggetto generico o.
     * 
     * @param o l'oggetto SERIALIZZABILE
     * @return i byte serializzati
     * @throws IOException se non fosse possibile convertire l'oggetto
     */
    private static byte[] getByteFromObject(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Object in = o;
        
        try (ObjectOutput out = new ObjectOutputStream(bos);)
        {
            out.writeObject(in);
            out.flush();
        }
        catch (IOException ex) {
            throw new IOException("Impossibile convertire l'oggetto: " + ex);
        }
        
        return bos.toByteArray();
    }
    
    /**
     * De-serializza un oggetto dati i suoi byte precedentemente serializzati.
     * 
     * @param b i byte
     * @return l'oggetto
     * @throws IOException se non fosse possibile convertire l'oggetto
     */
    private static Object getObjectFromBytes(byte[] b) throws IOException {
        ByteArrayInputStream bis = new ByteArrayInputStream(b);
        
        try(ObjectInput in = new ObjectInputStream(bis);)
        {
            return in.readObject();
        }
        catch (ClassNotFoundException ex) {
            throw new IOException("Impossibile leggere l'oggetto");
        }
    }
    
    /**
     * Serializza un oggetto di tipo TCPMessage.
     * 
     * @param o il messaggio TCP
     * @return i byte serializzati 
     * @throws IOException se non fosse possibile convertire l'oggetto
     */
    public static byte[] getByteFromTCPMessage(TCPMessage o) throws IOException {
        return getByteFromObject(o);
    }
    
    /**
     * Deserializza un oggetto TCPMessage dati i byte.
     * 
     * @param b i byte
     * @return l'oggetto TCPMessage
     * @throws IOException se non fosse possibile convertire l'oggetto
     */
    public static TCPMessage getTCPMessageFromBytes(byte[] b) throws IOException {
        return (TCPMessage) getObjectFromBytes(b);
    }
    
}
