package org.passport.testframework.mail;

import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.mail.annotations.InjectMailServer;
import org.passport.testframework.realm.RealmConfigBuilder;
import org.passport.testframework.realm.RealmConfigInterceptor;

public class GreenMailSupplier implements Supplier<MailServer, InjectMailServer>, RealmConfigInterceptor<MailServer, InjectMailServer> {

    private final String HOSTNAME = "localhost";
    private final int PORT = 3025;
    private final String FROM = "auto@passport-pro.ai";

    @Override
    public MailServer getValue(InstanceContext<MailServer, InjectMailServer> instanceContext) {
        return new MailServer(HOSTNAME, PORT);
    }

    @Override
    public void close(InstanceContext<MailServer, InjectMailServer> instanceContext) {
        instanceContext.getValue().stop();
    }

    @Override
    public boolean compatible(InstanceContext<MailServer, InjectMailServer> a, RequestedInstance<MailServer, InjectMailServer> b) {
        return true;
    }

    @Override
    public RealmConfigBuilder intercept(RealmConfigBuilder realm, InstanceContext<MailServer, InjectMailServer> instanceContext) {
        return realm.smtp(HOSTNAME, PORT, FROM);
    }

    @Override
    public int order() {
        return SupplierOrder.BEFORE_REALM;
    }
}
