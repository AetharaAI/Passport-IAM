package org.passport.testframework.realm;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.passport.admin.client.Passport;
import org.passport.admin.client.resource.RealmResource;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.injection.AbstractInterceptorHelper;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.Registry;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.injection.SupplierOrder;
import org.passport.testframework.server.PassportServer;
import org.passport.util.JsonSerialization;
import org.passport.util.Strings;

public class RealmSupplier implements Supplier<ManagedRealm, InjectRealm> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<ManagedRealm, InjectRealm> instanceContext) {
        return DependenciesBuilder.create(PassportServer.class)
                .add(Passport.class, "bootstrap-client").build();
    }

    @Override
    public ManagedRealm getValue(InstanceContext<ManagedRealm, InjectRealm> instanceContext) {
        PassportServer server = instanceContext.getDependency(PassportServer.class);
        Passport adminClient = instanceContext.getDependency(Passport.class, "bootstrap-client");

        String attachTo = instanceContext.getAnnotation().attachTo();
        boolean managed = attachTo.isEmpty();

        RealmRepresentation realmRepresentation;

        if (managed) {
            RealmConfigBuilder realmConfigBuilder;
            if (!Strings.isEmpty(instanceContext.getAnnotation().fromJson())) {
                try {
                    InputStream jsonStream = instanceContext.getRegistry().getCurrentContext().getRequiredTestClass().getResourceAsStream(instanceContext.getAnnotation().fromJson());
                    if (jsonStream == null) {
                        throw new RuntimeException("Realm JSON representation not found in classpath");
                    }
                    realmConfigBuilder = RealmConfigBuilder.update(JsonSerialization.readValue(jsonStream, RealmRepresentation.class));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                realmConfigBuilder = RealmConfigBuilder.create();
            }

            RealmConfig config = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
            realmConfigBuilder = config.configure(realmConfigBuilder);

            RealmConfigInterceptorHelper interceptor = new RealmConfigInterceptorHelper(instanceContext.getRegistry());
            realmConfigBuilder = interceptor.intercept(realmConfigBuilder, instanceContext);

            realmRepresentation = realmConfigBuilder.build();

            if (realmRepresentation.getRealm() == null) {
                realmRepresentation.setRealm(SupplierHelpers.createName(instanceContext));
            }

            if (realmRepresentation.getId() == null) {
                realmRepresentation.setId(realmRepresentation.getRealm());
            }

            adminClient.realms().create(realmRepresentation);

            // TODO Token needs to be invalidated after creating realm to have roles for new realm in the token. Maybe lightweight access tokens could help.
            adminClient.tokenManager().invalidate(adminClient.tokenManager().getAccessTokenString());
        } else {
            realmRepresentation = adminClient.realm(attachTo).toRepresentation();
        }

        instanceContext.addNote("managed", managed);

        RealmResource realmResource = adminClient.realm(realmRepresentation.getRealm());
        return new ManagedRealm(server.getBaseUrl() + "/realms/" + realmRepresentation.getRealm(), realmRepresentation, realmResource);
    }

    @Override
    public boolean compatible(InstanceContext<ManagedRealm, InjectRealm> a, RequestedInstance<ManagedRealm, InjectRealm> b) {
        InjectRealm aa = a.getAnnotation();
        InjectRealm ba = b.getAnnotation();
        return aa.config().equals(ba.config()) && aa.fromJson().equals(ba.fromJson());
    }

    @Override
    public void close(InstanceContext<ManagedRealm, InjectRealm> instanceContext) {
        if (instanceContext.getNote("managed", Boolean.class)) {
            instanceContext.getValue().admin().remove();
        }
    }

    @Override
    public int order() {
        return SupplierOrder.REALM;
    }

    private static class RealmConfigInterceptorHelper extends AbstractInterceptorHelper<RealmConfigInterceptor, RealmConfigBuilder> {

        private RealmConfigInterceptorHelper(Registry registry) {
            super(registry, RealmConfigInterceptor.class);
        }

        @Override
        public RealmConfigBuilder intercept(RealmConfigBuilder value, Supplier<?, ?> supplier, InstanceContext<?, ?> existingInstance) {
            if (supplier instanceof RealmConfigInterceptor interceptor) {
                value = interceptor.intercept(value, existingInstance);
            }
            return value;
        }

    }

}
