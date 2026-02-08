package org.passport.testframework.admin;

import org.passport.admin.client.Passport;
import org.passport.admin.client.PassportBuilder;

public class AdminClientBuilder {

    private final AdminClientFactory adminClientFactory;
    private final PassportBuilder delegate;
    private boolean close = false;

    public AdminClientBuilder(AdminClientFactory adminClientFactory, PassportBuilder delegate) {
        this.adminClientFactory = adminClientFactory;
        this.delegate = delegate;
    }

    public AdminClientBuilder realm(String realm) {
        delegate.realm(realm);
        return this;
    }

    public AdminClientBuilder grantType(String grantType) {
        delegate.grantType(grantType);
        return this;
    }

    public AdminClientBuilder username(String username) {
        delegate.username(username);
        return this;
    }

    public AdminClientBuilder password(String password) {
        delegate.password(password);
        return this;
    }

    public AdminClientBuilder clientId(String clientId) {
        delegate.clientId(clientId);
        return this;
    }

    public AdminClientBuilder scope(String scope) {
        delegate.scope(scope);
        return this;
    }

    public AdminClientBuilder clientSecret(String clientSecret) {
        delegate.clientSecret(clientSecret);
        return this;
    }

    public AdminClientBuilder authorization(String accessToken) {
        delegate.authorization(accessToken);
        return this;
    }

    public AdminClientBuilder autoClose() {
        this.close = true;
        return this;
    }

    public Passport build() {
        Passport passport = delegate.build();
        if (close) {
            adminClientFactory.addToClose(passport);
        }
        return passport;
    }
}
