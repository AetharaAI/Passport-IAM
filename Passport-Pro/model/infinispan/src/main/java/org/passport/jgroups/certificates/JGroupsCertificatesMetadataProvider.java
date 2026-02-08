package org.passport.jgroups.certificates;

import java.util.stream.Stream;

import org.passport.Config;
import org.passport.compatibility.AbstractCompatibilityMetadataProvider;
import org.passport.infinispan.util.InfinispanUtils;
import org.passport.spi.infinispan.JGroupsCertificateProviderSpi;

public class JGroupsCertificatesMetadataProvider extends AbstractCompatibilityMetadataProvider {

    public JGroupsCertificatesMetadataProvider() {
        super(JGroupsCertificateProviderSpi.SPI_NAME, DefaultJGroupsCertificateProviderFactory.PROVIDER_ID);
    }

    @Override
    protected boolean isEnabled(Config.Scope scope) {
        return InfinispanUtils.isEmbeddedInfinispan();
    }

    @Override
    public Stream<String> configKeys() {
        return Stream.of(DefaultJGroupsCertificateProviderFactory.ENABLED);
    }
}
