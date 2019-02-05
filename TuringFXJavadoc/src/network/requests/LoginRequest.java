/*
 * 
 * 
 * 
 */
package network.requests;

import core.exceptions.InvalidParameterException;

/**
 * Richiesta di login al sistema.
 * 
 * @author mc - Marco Costa - 545144
 */
public class LoginRequest extends TCPRequest {
    private final String password;

    public LoginRequest(String username, String password) throws InvalidParameterException {
        super(username);
        this.password = password;
    }
    

    public String getPassword() {
        return password;
    }
    
    
}
