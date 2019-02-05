/*
 * 
 * 
 * 
 */
package network.responses;

import files.DocumentInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Lista dei documenti attualmente accessibili dall'utente.
 * 
 * Nota: non utilizzata dalla GUI
 * @author mc - Marco Costa - 545144
 */
public class DocumentListResponse extends TCPResponse {
    private final List<DocumentInfo> list;

    public DocumentListResponse(ArrayList<DocumentInfo> list) {
        this.list = Collections.unmodifiableList(new ArrayList(list));
    }
}
