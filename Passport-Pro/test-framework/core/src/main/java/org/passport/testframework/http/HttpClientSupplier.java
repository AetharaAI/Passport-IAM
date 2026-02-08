package org.passport.testframework.http;

import java.io.IOException;
import java.util.List;
import javax.net.ssl.SSLContext;

import org.passport.testframework.annotations.InjectHttpClient;
import org.passport.testframework.https.ManagedCertificates;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientSupplier implements Supplier<HttpClient, InjectHttpClient> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<HttpClient, InjectHttpClient> instanceContext) {
        return DependenciesBuilder.create(ManagedCertificates.class).build();
    }

    @Override
    public HttpClient getValue(InstanceContext<HttpClient, InjectHttpClient> instanceContext) {
        HttpClientBuilder builder = HttpClientBuilder.create();

        ManagedCertificates managedCerts = instanceContext.getDependency(ManagedCertificates.class);

        if (managedCerts.isTlsEnabled()) {
            SSLContext sslContext = managedCerts.getClientSSLContext();
            SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier()
            );

            builder.setSSLSocketFactory(sslSocketFactory);
        }

        if (!instanceContext.getAnnotation().followRedirects()) {
            builder.disableRedirectHandling();
        }

        return builder.build();
    }

    @Override
    public void close(InstanceContext<HttpClient, InjectHttpClient> instanceContext) {
        try {
            ((CloseableHttpClient) instanceContext.getValue()).close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<HttpClient, InjectHttpClient> a, RequestedInstance<HttpClient, InjectHttpClient> b) {
        return a.getAnnotation().followRedirects() == b.getAnnotation().followRedirects();
    }

}
