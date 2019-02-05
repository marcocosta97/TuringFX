/*
 * 
 * 
 * 
 */
package files;

import files.exceptions.DocumentNameInvalidException;
import files.exceptions.DocumentNotPresentException;

/**
 * Interfaccia DocumentList. 
 * Un database di tutti i Documenti presenti nel sistema. Ogni documento è 
 * identificato univocamente dal proprio oggetto DocumentInfo (è la PK della collezione).
 * 
 * @author mc - Marco Costa - 545144
 */
public interface DocumentList {
    
    /**
     * Aggiunta di un documento alla collezione.
     * 
     * @param info identificatore univoco del documento (PK)
     * @param doc il documento
     * @throws DocumentNameInvalidException in caso la PK non fosse valida
     */
    public void add(DocumentInfo info, Document doc) throws DocumentNameInvalidException;
    
    /**
     * Restituzione di un documento data la PK che lo identifica.
     * 
     * @param info identificatore univoco del documento (PK)
     * @return il documento se presente
     * @throws DocumentNotPresentException in caso non fosse presente il documento richiesto
     */
    public Document get(DocumentInfo info) throws DocumentNotPresentException;
}
