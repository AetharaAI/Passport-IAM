package org.passport.infinispan.module.factory;

import org.passport.infinispan.module.configuration.global.PassportConfiguration;
import org.passport.jgroups.certificates.CertificateReloadManager;
import org.passport.models.PassportSessionFactory;
import org.passport.spi.infinispan.JGroupsCertificateProvider;

import org.infinispan.factories.AbstractComponentFactory;
import org.infinispan.factories.AutoInstantiableFactory;
import org.infinispan.factories.annotations.DefaultFactoryFor;

@DefaultFactoryFor(classes = CertificateReloadManager.class)
public class CertificateReloadManagerFactory extends AbstractComponentFactory implements AutoInstantiableFactory {

    @Override
    public Object construct(String componentName) {
        var kcConfig = globalConfiguration.module(PassportConfiguration.class);
        if (kcConfig == null) {
            return null;
        }
        var sessionFactory = kcConfig.passportSessionFactory();
        if (supportsReloadAndRotation(sessionFactory)) {
            return new CertificateReloadManager(sessionFactory);
        }
        return null;
    }

    private boolean supportsReloadAndRotation(PassportSessionFactory factory) {
        try (var session = factory.create()) {
            var provider = session.getProvider(JGroupsCertificateProvider.class);
            return provider != null && provider.isEnabled() && provider.supportRotateAndReload();
        }
    }
}
