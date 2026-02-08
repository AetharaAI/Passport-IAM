package org.passport.testframework.https;

import org.passport.testframework.config.Config;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.injection.SupplierOrder;

public class CertificatesSupplier implements Supplier<ManagedCertificates, InjectCertificates> {

    @Override
    public ManagedCertificates getValue(InstanceContext<ManagedCertificates, InjectCertificates> instanceContext) {
        CertificatesConfig certConfig = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
        CertificatesConfigBuilder certBuilder = new CertificatesConfigBuilder();
        certBuilder = certConfig.configure(certBuilder);

        String supplierConfig = Config.getSupplierConfig(ManagedCertificates.class);
        if (supplierConfig != null) {
            CertificatesConfig certConfigOverride = SupplierHelpers.getInstance(supplierConfig);
            certConfigOverride.configure(certBuilder);
        }
        return new ManagedCertificates(certBuilder);
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<ManagedCertificates, InjectCertificates> a, RequestedInstance<ManagedCertificates, InjectCertificates> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_PASSPORT_SERVER;
    }
}
