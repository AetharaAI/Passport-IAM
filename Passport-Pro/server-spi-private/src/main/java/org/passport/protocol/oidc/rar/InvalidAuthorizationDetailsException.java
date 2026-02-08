package org.passport.protocol.oidc.rar;

public class InvalidAuthorizationDetailsException extends RuntimeException {

    public InvalidAuthorizationDetailsException() {
    }

    public InvalidAuthorizationDetailsException(String message) {
        super(message);
    }

    public InvalidAuthorizationDetailsException(String message, Throwable cause) {
        super(message, cause);
    }
}
