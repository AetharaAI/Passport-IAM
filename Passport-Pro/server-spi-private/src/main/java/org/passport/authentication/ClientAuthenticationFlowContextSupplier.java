package org.passport.authentication;

public interface ClientAuthenticationFlowContextSupplier<T> {

    T get(ClientAuthenticationFlowContext context) throws Exception;

}
