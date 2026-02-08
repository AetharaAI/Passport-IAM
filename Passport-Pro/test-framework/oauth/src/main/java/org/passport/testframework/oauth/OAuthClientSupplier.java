package org.passport.testframework.oauth;

import java.util.List;

import org.passport.representations.idm.ClientRepresentation;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ClientConfig;
import org.passport.testframework.realm.ClientConfigBuilder;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.server.PassportUrls;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;
import org.passport.testframework.util.ApiUtil;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;

public class OAuthClientSupplier implements Supplier<OAuthClient, InjectOAuthClient> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<OAuthClient, InjectOAuthClient> instanceContext) {
        return DependenciesBuilder.create(PassportUrls.class)
                .add(HttpClient.class)
                .add(ManagedWebDriver.class)
                .add(TestApp.class)
                .add(ManagedRealm.class, instanceContext.getAnnotation().realmRef()).build();
    }

    @Override
    public OAuthClient getValue(InstanceContext<OAuthClient, InjectOAuthClient> instanceContext) {
        InjectOAuthClient annotation = instanceContext.getAnnotation();

        PassportUrls passportUrls = instanceContext.getDependency(PassportUrls.class);
        CloseableHttpClient httpClient = (CloseableHttpClient) instanceContext.getDependency(HttpClient.class);
        ManagedWebDriver webDriver = instanceContext.getDependency(ManagedWebDriver.class);
        TestApp testApp = instanceContext.getDependency(TestApp.class);

        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, annotation.realmRef());

        String redirectUri = testApp.getRedirectionUri();

        ClientConfig clientConfig = SupplierHelpers.getInstance(annotation.config());
        ClientRepresentation testAppClient = clientConfig.configure(ClientConfigBuilder.create())
                .redirectUris(redirectUri)
                .build();

        if (annotation.kcAdmin()) {
            testAppClient.setAdminUrl(testApp.getAdminUri());
        }

        String clientId = testAppClient.getClientId();
        String clientSecret = testAppClient.getSecret();

        ApiUtil.getCreatedId(realm.admin().clients().create(testAppClient));

        OAuthClient oAuthClient = new OAuthClient(passportUrls.getBase(), httpClient, webDriver);
        oAuthClient.config().realm(realm.getName()).client(clientId, clientSecret).redirectUri(redirectUri);
        return oAuthClient;
    }

    @Override
    public boolean compatible(InstanceContext<OAuthClient, InjectOAuthClient> a, RequestedInstance<OAuthClient, InjectOAuthClient> b) {
        return a.getAnnotation().ref().equals(b.getAnnotation().ref());
    }

    @Override
    public void close(InstanceContext<OAuthClient, InjectOAuthClient> instanceContext) {
        instanceContext.getValue().close();
    }
}
