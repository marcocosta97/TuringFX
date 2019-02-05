/*
 * 
 * 
 * 
 */
package core;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Parametri di configurazione di Turing.
 * 
 * @author mc - Marco Costa - 545144
 */
public class TuringParameters {
    /* valori del Server */
    public static final String SERVER_ADDRESS = "localhost";
    
    public static final int TCP_SERVER_PORT = 1111;   
    public static final int RMI_SERVER_PORT = 1112;
    public static final int CHAT_PORT = 1113;
    
    /* parametri di sicurezza */
    public static final String PASSWORD_HASH_ALGORITHM = "SHA-256";
    
    /* valori di riferimento per la registrazione degli utenti */
    public static final int MIN_USERNAME_LENGTH = 4;
    public static final int MAX_USERNAME_LENGTH = 15;
    public static final int MIN_PASSWORD_LENGTH = 4;
    public static final int MAX_PASSWORD_LENGTH = 15;
    
    /* parametri inerenti alla chat */
    
    /**
     * Da 230.0.0.0 a 232.0.0.0 sono liberi e si coprono circa 
     * 30 milioni di indirizzi, dovrebbero bastare
     */
    public static final String FIRST_MULTICAST_ADDRESS = "230.0.0.0";
    public static final int MAX_CHAT_MESSAGE_LENGTH = 100; /* bytes */
    public static final int CHAT_MAX_BUFFER_SIZE = MAX_CHAT_MESSAGE_LENGTH + MAX_USERNAME_LENGTH;
    
    /* charset di sistema */
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    
    /* parametri dei documenti */
    public static final int DOCUMENT_MAX_SECTIONS = 10;
    public static final boolean DOCUMENT_SECTION_ENDS_WITH_NEWLINE = false;
    /* uso la cartella temporanea definita dalla JVM */
    public static final String FILE_PARENT_DIRECTORY = System.getProperty("java.io.tmpdir");
    public static final String FILE_DIRECTORY_NAME = "Turing";
    
    
   
    
    
}
