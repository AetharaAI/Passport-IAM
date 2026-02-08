package org.passport.testframework.oauth;

import java.util.List;

import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.oauth.annotations.InjectOAuthIdentityProvider;

import com.sun.net.httpserver.HttpServer;

public class OAuthIdentityProviderSupplier implements Supplier<OAuthIdentityProvider, InjectOAuthIdentityProvider> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<OAuthIdentityProvider, InjectOAuthIdentityProvider> instanceContext) {
        return DependenciesBuilder.create(HttpServer.class).build();
    }

    @Override
    public OAuthIdentityProvider getValue(InstanceContext<OAuthIdentityProvider, InjectOAuthIdentityProvider> instanceContext) {
        HttpServer httpServer = instanceContext.getDependency(HttpServer.class);
        OAuthIdentityProviderConfig config = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
        OAuthIdentityProviderConfigBuilder configBuilder = new OAuthIdentityProviderConfigBuilder();
        OAuthIdentityProviderConfigBuilder.OAuthIdentityProviderConfiguration configuration = config.configure(configBuilder).build();

        return new OAuthIdentityProvider(httpServer, configuration);
    }

    @Override
    public void close(InstanceContext<OAuthIdentityProvider, InjectOAuthIdentityProvider> instanceContext) {
        instanceContext.getValue().close();
    }

    @Override
    public boolean compatible(InstanceContext<OAuthIdentityProvider, InjectOAuthIdentityProvider> a, RequestedInstance<OAuthIdentityProvider, InjectOAuthIdentityProvider> b) {
        return a.getAnnotation().equals(b.getAnnotation());
    }

}
