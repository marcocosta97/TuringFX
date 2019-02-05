/*
 * 
 * 
 * 
 */
package core;

/**
 * Vari codici di errore per l'applicazione.
 * 
 * @author mc - Marco Costa - 545144
 */
public enum ErrorCodes {
    SERVER_RMI_STARTUP_ERROR(1),
    SERVER_TCP_UDP_STARTUP_ERROR(2),
    SERVER_RMI_CLOSEUP_ERROR(3),
    SERVER_TCP_UDP_CLOSEUP_ERROR(4),
    SERVER_DATA_INIT_ERROR(5);
    
    private final int errorCode;
    
    ErrorCodes(int errorCode) {
        this.errorCode = errorCode;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
}
