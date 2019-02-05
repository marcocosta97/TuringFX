/*
 * 
 * 
 * 
 */
package files;

import files.exceptions.DocumentNameInvalidException;
import files.exceptions.DocumentNotPresentException;
import java.util.HashMap;

/**
 * Implementazione dell'interfaccia DocumentList mediante tabella Hash.
 * Garantisce accesso al documento in tempo O(k) conoscendo l'identificatore
 * univoco del documnto.
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentListHashImpl implements DocumentList {
    private static HashMap<DocumentInfo, Document> documentList;

    public DocumentListHashImpl() {
        documentList = new HashMap<>();
    }

    /**
     * Aggiunta di un documento alla collezione.
     * 
     * @param info identificatore univoco del documento (PK)
     * @param doc il documento
     * @throws DocumentNameInvalidException in caso la PK non fosse valida
     */
    @Override
    public void add(DocumentInfo info, Document doc) throws DocumentNameInvalidException {
        if(documentList.containsKey(info))
            throw new DocumentNameInvalidException("Esiste già un documento con questo nome");
        
        documentList.put(info, doc);
    }

    /**
     * Restituzione di un documento data la PK che lo identifica.
     * 
     * @param info identificatore univoco del documento (PK)
     * @return il documento se presente
     * @throws DocumentNotPresentException in caso non fosse presente il documento richiesto
     */
    @Override
    public Document get(DocumentInfo info) throws DocumentNotPresentException {
        Document doc = documentList.get(info);
        
        if(doc == null)
            throw new DocumentNotPresentException("Il documento richiesto non esiste");
        
        return doc;
    }
    
    
}
