package org.passport.testframework;

import java.util.List;
import java.util.Map;

import org.passport.testframework.admin.AdminClientFactorySupplier;
import org.passport.testframework.admin.AdminClientSupplier;
import org.passport.testframework.crypto.CryptoHelper;
import org.passport.testframework.crypto.CryptoHelperSupplier;
import org.passport.testframework.database.DevFileDatabaseSupplier;
import org.passport.testframework.database.DevMemDatabaseSupplier;
import org.passport.testframework.database.RemoteDatabaseSupplier;
import org.passport.testframework.database.TestDatabase;
import org.passport.testframework.events.AdminEventsSupplier;
import org.passport.testframework.events.EventsSupplier;
import org.passport.testframework.events.SysLogServerSupplier;
import org.passport.testframework.http.HttpClientSupplier;
import org.passport.testframework.http.HttpServerSupplier;
import org.passport.testframework.http.SimpleHttpSupplier;
import org.passport.testframework.https.CertificatesSupplier;
import org.passport.testframework.https.ManagedCertificates;
import org.passport.testframework.infinispan.InfinispanExternalServerSupplier;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.realm.ClientSupplier;
import org.passport.testframework.realm.RealmSupplier;
import org.passport.testframework.realm.UserSupplier;
import org.passport.testframework.server.DistributionPassportServerSupplier;
import org.passport.testframework.server.EmbeddedPassportServerSupplier;
import org.passport.testframework.server.PassportServer;
import org.passport.testframework.server.PassportUrlsSupplier;
import org.passport.testframework.server.RemotePassportServerSupplier;

public class CoreTestFrameworkExtension implements TestFrameworkExtension {

    @Override
    public List<Supplier<?, ?>> suppliers() {
        return List.of(
                new AdminClientSupplier(),
                new AdminClientFactorySupplier(),
                new ClientSupplier(),
                new RealmSupplier(),
                new UserSupplier(),
                new DistributionPassportServerSupplier(),
                new EmbeddedPassportServerSupplier(),
                new RemotePassportServerSupplier(),
                new PassportUrlsSupplier(),
                new DevMemDatabaseSupplier(),
                new DevFileDatabaseSupplier(),
                new RemoteDatabaseSupplier(),
                new SysLogServerSupplier(),
                new EventsSupplier(),
                new AdminEventsSupplier(),
                new HttpClientSupplier(),
                new HttpServerSupplier(),
                new InfinispanExternalServerSupplier(),
                new SimpleHttpSupplier(),
                new CertificatesSupplier(),
                new CryptoHelperSupplier()
        );
    }

    @Override
    public Map<Class<?>, String> valueTypeAliases() {
        return Map.of(
                PassportServer.class, "server",
                TestDatabase.class, "database",
                ManagedCertificates.class, "certificates",
                CryptoHelper.class, "crypto"
        );
    }

    @Override
    public List<Class<?>> alwaysEnabledValueTypes() {
        return List.of(CryptoHelper.class);
    }
}
