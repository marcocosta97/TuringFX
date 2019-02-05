/*
 * 
 * 
 * 
 */
package files;

import core.User;
import core.exceptions.InvalidParameterException;
import files.exceptions.InvalidPermissionException;
import files.exceptions.InvalidSectionException;
import files.exceptions.SectionBusyException;
import files.exceptions.UserAlreadyAddedException;
import core.GenericUtils;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import core.TuringParameters;
import network.ChatAddresser;

/**
 * La seguente classe implementa tutte le strutture dati e i metodi necessari
 * per la creazione, gestione e modifica di un documento condiviso.
 * Ogni documento è identificato mediante un valore intero univoco.
 * Ad ogni documento, alla sua creazione, è anche associato un indirizzo IP 
 * multicast univoco su tutto il sistema.
 * Come da specifica, l'accesso ai File avviene mediante NIO.
 * 
 * @author mc - Marco Costa - 545144
 */
public class Document {    
    /* directory di salvataggio dei documenti */
    private static final File documentDirectoryFile;
    
    private static final User VOID_USER = null; /* rappresenta un utente vuoto */
    private static int MAX_BUFFER_SIZE = 5000000; // 5MB
    
    /**
     * Costruttore statico per la creazione e la inizializzazione della 
     * directory di lavoro e salvataggio dei documenti.
     */
    static {
        File tempDir = new File(TuringParameters.FILE_PARENT_DIRECTORY);
        
        /* creo la directory o la pulisco nel caso fosse già presente */
        documentDirectoryFile = new File(tempDir, TuringParameters.FILE_DIRECTORY_NAME);
        if(documentDirectoryFile.exists())
            documentDirectoryFile.delete();
        
        documentDirectoryFile.mkdir();
    }
    
    /* utilizzo come identificatore un valore generato da un contatore */
    private static final AtomicInteger NEXT_ID = new AtomicInteger(1);
    private final int documentId = NEXT_ID.getAndIncrement();
    
    /* valori di informazione del documento */
    private final DocumentInfo documentInfo;
    private final List<String> documentUsers; /* utenti con accesso al documento */
    private final int noSections;
    /* il documento è una lista di sezioni */
    private final List<FileInfoWrapper> documentContent;
    
    private final String chatIPAddress; /* indirizzo IP della chat associata */
    
    /* struttura che identifica i dati di una singola sezione */
    private static final class FileInfoWrapper{
        File document; /* file associato alla sezione */
        User workingUser; /* utente attualmentte al lavoro sulla sezione */

        public FileInfoWrapper(File document) {
            this.document = document;
            this.workingUser = VOID_USER;
        }
    }
    
    /**
     * Costruttore per la creazione di un nuovo oggetto Documento.
     * Ogni oggetto è rappresentato da "noSections" file presenti all'interno
     * della directory di salvataggio.
     * 
     * @param documentName nome del documento
     * @param documentCreator creatore del documento
     * @param noSections numero di sezioni
     * @throws InvalidSectionException se il numero di sezioni non è valido
     * @throws IOException in caso non fosse possibile creare i file associati
     *                     alle sezioni
     * @throws InvalidParameterException in caso i parametri di ingresso non 
     *                                   fossero validi
     */
    public Document(String documentName, String documentCreator, int noSections) throws InvalidSectionException, 
                                                                        IOException, InvalidParameterException {
        GenericUtils.checkEmptyString(documentName);
        GenericUtils.checkEmptyString(documentCreator);
        
        /**
         * Verifico che il numero di sezioni scelte sia valido
         */
        if((noSections <= 0) || (noSections > TuringParameters.DOCUMENT_MAX_SECTIONS))
            throw new InvalidSectionException("Numero di sezioni non valido, " + 
                    "il documento può avere da 1 fino a " + TuringParameters.DOCUMENT_MAX_SECTIONS + " sezioni");
        
        this.documentInfo = new DocumentInfo(documentName, documentCreator);
        this.noSections = noSections;
        
        /**
         * Un documento è rappresentato da una Lista (non modificabile) di 
         * File e utenti attualmente al lavoro sul File
         */
        documentContent = new ArrayList<>(noSections); 
        for(int i = 0; i < noSections; i++)
        {
            /* ogni file sezione salvato come id_sezione */
            File f = new File(documentDirectoryFile, documentId + "_" + i);
            if(f.exists())
                f.delete();
            f.createNewFile();
            documentContent.add(i, new FileInfoWrapper(f));
        }
        /* lista degli utenti con accesso al File */
        documentUsers = new ArrayList<>();   
        documentUsers.add(documentCreator);
        
        /* l'indirizzo IP è ottenuto a partire dall'id univoco del documento */
        chatIPAddress = ChatAddresser.getChatAddress(documentId);
    }
    
    /**
     * Aggiunta dei permessi di lettura e scrittura sul documento ad un utente.
     * 
     * @param s username
     * @throws UserAlreadyAddedException se l'utente ha già i permessi
     */
    public void addDocumentUser(String s) throws UserAlreadyAddedException {
        if(documentUsers.contains(s))
            throw new UserAlreadyAddedException();
        
        documentUsers.add(s);
    }

    /**
     * @deprecated use addDocumentUser instead
     * @return 
     */
    public List<String> getDocumentUsers() {
        return Collections.unmodifiableList(documentUsers);
    }
    
    /**
     * Verifica che la sezione alla quale si richiede l'accesso rispetti i 
     * limiti di creazione.
     * 
     * @param section il numero di sezione
     * @throws InvalidSectionException se la sezione non fosse valida
     */
    private void checkSectionRange(int section) throws InvalidSectionException {
        if((section < 0) || (section >= documentContent.size()))
            throw new InvalidSectionException("Numero di sezione non valido:\n"
                    + "il documento può essere modificato dalla sezione 1 alla sezione " 
                    + documentContent.size());
    }
    
    /**
     * Scrittura su un File mediante NIO.
     * L'intero contenuto del File è sostituito con la stringa "s".
     * 
     * @param s il nuovo contenuto del file
     * @param document il file
     * @throws FileNotFoundException in caso non fosse possibile trovare il File
     * @throws IOException in caso non fosse possibile scrivere sul File
     */
    private static void writeFile(String s, File document) throws FileNotFoundException, IOException {
        /**
         * per sicurezza elimino e ricreo il file con lo stesso nome
         */
        document.delete();
        document.createNewFile();
        
        /* try - with resources */
        try (FileChannel outChannel = new RandomAccessFile(document, "rw").getChannel()) { 
            /* alloco un buffer della stessa dimensione della stringa */
            ByteBuffer buffer = ByteBuffer.allocate(s.getBytes().length);
            /* inserimento stringa su buffer */
            buffer.put(s.getBytes(TuringParameters.DEFAULT_CHARSET));
            buffer.flip();
            /**
             * inserimento buffer su file
             */
            while(buffer.hasRemaining())
                outChannel.write(buffer);
            
        }
    }

    /**
     * Lettura dell'intero contenuto del File "document" mediante NIO.
     * 
     * @param document il file
     * @return la Stringa con il contenuto attuale del File
     * @throws FileNotFoundException in caso non fosse possibile trovare il File
     * @throws IOException in caso non fosse possibile leggere dal File
     */
    private static String readFile(File document) throws FileNotFoundException, IOException {
        String fileString;
        if(document.length() <= 0) /* file vuoto */
            return "";
        
        /** 
         * se la dimensione del file supera MAX_BUFFER_SIZE imposto un buffer di
         *   dimensione predefinita MAX_BUFFER_SIZE, altrimenti imposto la
         *   dimensione del File come dimensione del buffer 
         */
        int buffer_size = (int) document.length();
        if(buffer_size > MAX_BUFFER_SIZE)
                buffer_size = MAX_BUFFER_SIZE;
        
        /* try - with resources */
        try (FileChannel inChannel = new RandomAccessFile(document, "r").getChannel()) {           
            ByteBuffer buffer = ByteBuffer.allocate(buffer_size);
            
            fileString = "";
            
            /**
             * lettura da file e scrittura su "fileString"
             */
            while(inChannel.read(buffer) != -1)
            {
                buffer.flip();
                fileString += TuringParameters.DEFAULT_CHARSET.decode(buffer).toString();
                buffer.clear();
            }     
        }
        
        return fileString;   
    }
    
    /**
     * Richiede il permesso esclusivo in scrittura sul documento alla sezione
     * "section".
     * 
     * @param section il numero della sezione da modificare (da 1 a num_sezioni)
     * @param user l'utente che richiede l'accesso
     * @return il contenuto attuale della sezione in caso l'accesso venisse concesso
     * @throws SectionBusyException in caso un altro utente stesse già modificando
     *                              la sezione
     * @throws InvalidSectionException in caso il numero di sezione non fosse valido
     * @throws IOException in caso di problemi di IO con la lettura del File
     */
    public String startSectionEditing(int section, User user) throws SectionBusyException, InvalidSectionException, IOException {
        section--;     
        checkSectionRange(section);
          
        FileInfoWrapper wrap = documentContent.get(section);
        String fileContent = readFile(wrap.document); /* in caso di problemi di IO, lancio eccezioni prima di fare modifiche sui permessi */
        
        if((wrap.workingUser != VOID_USER) && (wrap.workingUser != user))
        {
            if(wrap.workingUser.isLogged()) /* l'utente che sta modificando è sempre loggato */
                throw new SectionBusyException("La sezione " + (section + 1) + " è attualmente "
                        + " occupata dall'utente " + wrap.workingUser);
            
            /* else fine editing forzata */
            wrap.workingUser = VOID_USER;
        }
        
        /* se sono qui nessuno sta lavorando sulla sezione, ho l'accesso esclusivo */
        wrap.workingUser = user;
        return fileContent; /* restituisco il contenuto del file */
    }
    
    /**
     * Termina l'editing della sezione alla quale è stato precedentemente concesso
     * l'accesso in scrittura.
     * 
     * @param newContent nuovo contenuto del documento
     * @param section la sezione sulla quale terminare l'editing
     * @param user l'utente che effettua la richiesta
     * @throws InvalidSectionException in caso il numero di sezione non fosse valido 
     * @throws IOException IOException in caso di problemi di IO con la scrittura del File
     * @throws InvalidPermissionException in caso non si possedessero i permessi in scrittura sul File
     */
    public void endSectionEditing(String newContent, int section, User user) throws InvalidSectionException, 
                                                                            IOException, InvalidPermissionException {
        section--;
        checkSectionRange(section);
        
        FileInfoWrapper wrap = documentContent.get(section);
        
        if(wrap.workingUser != user)
            throw new InvalidPermissionException("L'utente " + user + " non sta attualmente lavorando sulla sezione " + (section + 1));
        
        /** 
         * Se è previsto nei parametri che le sezioni siano divise da uno spazio
         * allora lo aggiungo, a meno che la sezione sia l'ultima del documento.
         * Nota: se "s" è stata terminata da uno spazio questo funge da doppia
         *       divisione
         */
        if((TuringParameters.DOCUMENT_SECTION_ENDS_WITH_NEWLINE) && 
                (section != (documentContent.size() - 1)))
            newContent += "\n";
        
        writeFile(newContent, wrap.document);
        
        /* imposto la sezione come libera */
        wrap.workingUser = VOID_USER;
    }
    
    /**
     * Richiesta di visualizzazione del contenuto di una sezione del documento.
     * 
     * Nota: la chiusura dei metodi di editing e l'implementazione sequenziale
     *       del Server garantiscono di poter accedere in lettura al File in ogni
     *       momento (anche in caso di editing contemporaneo) ottenendo sempre un contenuto
     *       del documento consistente.
     * 
     * @param section il numero di sezione
     * @return un oggetto wrapper formato da: 
     *  1. il contenuto della sezione nel caso non vengano lanciate eccezioni
     *  2. un eventuale messaggio contenente informazioni su chi sta attualmente
     *     modificando la sezione
     * @throws InvalidSectionException in caso il numero di sezione non fosse valido 
     * @throws IOException IOException in caso di problemi di IO con la lettura del File
     */
    public DocumentWrapper showDocumentSection(int section) throws IOException, InvalidSectionException {
        section--;
        checkSectionRange(section);
        
        FileInfoWrapper wrap = documentContent.get(section);
        String document = readFile(wrap.document);
        
        /**
         * Se un utente si è disconnesso forzatamente mentre era in editing potrebbe
         * risultare collegato.
         * Attenzione: questo controllo è messo solo perché la setOnCloseRequest su Linux
         *             pare non funzionare, infatti di norma la GUI è impostata per non chiudersi
         *             finché si è in fase di editing
         */
        if(wrap.workingUser != VOID_USER && !(wrap.workingUser.isLogged()))
            wrap.workingUser = VOID_USER;
        
        if(wrap.workingUser != VOID_USER)
            return new DocumentWrapper(document, "L'utente " + wrap.workingUser + " sta attualmente modificando la sezione");
        
        /* else */
        return new DocumentWrapper(document);
    }
    
    /**
     * Richiesta di visualizzazione dell'intero contenuto del documento.
     * 
     * Nota: la chiusura dei metodi di editing e l'implementazione sequenziale
     *       del Server garantiscono di poter accedere in lettura al File in ogni
     *       momento (anche in caso di editing contemporaneo) ottenendo sempre un contenuto
     *       del documento consistente.
     * 
     * @return un oggetto wrapper formato da: 
     *  1. il contenuto della sezione nel caso non vengano lanciate eccezioni
     *  2. un eventuale messaggio contenente informazioni su chi sta attualmente
     *     modificando le varie sezioni del documento 
     * @throws IOException IOException in caso di problemi di IO con la lettura dei File
     */
    public DocumentWrapper showEntireDocument() throws IOException {
        String file = "";
        String currentWorking = "";
        
        for(int i = 0; i < documentContent.size(); i++)
        {
            FileInfoWrapper wrap = documentContent.get(i);
            file += readFile(wrap.document);
            
            /**
            * Se un utente si è disconnesso forzatamente mentre era in editing potrebbe
            * risultare collegato.
            * Attenzione: questo controllo è messo solo perché la setOnCloseRequest su Linux
            *             pare non funzionare, infatti di norma la GUI è impostata per non chiudersi
            *             finché si è in fase di editing
            */
           if(wrap.workingUser != VOID_USER && !(wrap.workingUser.isLogged()))
               wrap.workingUser = VOID_USER;
        
            if(wrap.workingUser != VOID_USER)
            {
                if(currentWorking.isEmpty())
                    currentWorking += "I seguenti utenti stanno modificando il documento in questo "
                            + "momento:\n";
                
                currentWorking += "\t" + wrap.workingUser + " -> sezione " + (i + 1) + "\n";
            }
        }
        
        if(currentWorking.isEmpty())
            return new DocumentWrapper(file);
        
        /* else */
        return new DocumentWrapper(file, currentWorking);
    }

    
    public String getChatIPAddress() {
        return chatIPAddress;
    }
    
    @Override
    public String toString() {
        return documentInfo.getDocumentName();
    }
    
    
}
