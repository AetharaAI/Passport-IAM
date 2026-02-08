package org.passport.testframework.admin;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;

import org.passport.admin.client.Passport;
import org.passport.admin.client.PassportBuilder;

public class AdminClientFactory {

    private final Supplier<PassportBuilder> delegateSupplier;

    private final List<Passport> instanceToClose = new LinkedList<>();

    AdminClientFactory(String serverUrl) {
        delegateSupplier = () -> PassportBuilder.builder().serverUrl(serverUrl);
    }

    AdminClientFactory(String serverUrl, SSLContext sslContext) {
            delegateSupplier = () ->
                    PassportBuilder.builder()
                            .serverUrl(serverUrl)
                            .resteasyClient(Passport.getClientProvider().newRestEasyClient(null, sslContext, false));
    }

    public AdminClientBuilder create() {
        return new AdminClientBuilder(this, delegateSupplier.get());
    }

    void addToClose(Passport passport) {
        instanceToClose.add(passport);
    }

    public void close() {
        instanceToClose.forEach(Passport::close);
    }

}
