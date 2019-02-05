/*
 * 
 * 
 * 
 */
package files;

import core.exceptions.InvalidParameterException;
import core.GenericUtils;
import java.io.Serializable;
import java.util.Objects;

/**
 * Classe wrapper per la coppia di informazioni "nome documento - creatore".
 * 
 * @author mc - Marco Costa - 545144
 */
public class DocumentInfo implements Serializable {
    private final String documentName;
    private final String creator;

    public DocumentInfo(String documentName, String creator) throws InvalidParameterException {
        GenericUtils.checkEmptyString(documentName);
        GenericUtils.checkEmptyString(creator);
        
        this.documentName = documentName;
        this.creator = creator;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getCreator() {
        return creator;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentInfo other = (DocumentInfo) obj;
        
        return documentName.equals(other.documentName) && creator.equals(other.creator);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Objects.hashCode(this.documentName);
        hash = 37 * hash + Objects.hashCode(this.creator);
        return hash;
    }
    
    
    
}
