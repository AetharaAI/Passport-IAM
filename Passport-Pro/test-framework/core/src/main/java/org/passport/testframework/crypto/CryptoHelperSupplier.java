package org.passport.testframework.crypto;

import org.passport.common.crypto.CryptoIntegration;
import org.passport.common.crypto.FipsMode;
import org.passport.crypto.def.DefaultCryptoProvider;
import org.passport.testframework.annotations.InjectCryptoHelper;
import org.passport.testframework.config.Config;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;

public class CryptoHelperSupplier implements Supplier<CryptoHelper, InjectCryptoHelper> {

    @Override
    public CryptoHelper getValue(InstanceContext<CryptoHelper, InjectCryptoHelper> instanceContext) {
        if (!CryptoIntegration.isInitialised()) {
            CryptoIntegration.setProvider(new DefaultCryptoProvider());
        }
        FipsMode fips = Config.getValueTypeConfig(CryptoHelper.class, "fips", FipsMode.DISABLED.name(), FipsMode.class);
        return new CryptoHelper(fips);
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<CryptoHelper, InjectCryptoHelper> a, RequestedInstance<CryptoHelper, InjectCryptoHelper> b) {
        return true;
    }

}
