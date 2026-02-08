package org.passport.testframework.admin;

import java.util.List;

import org.passport.OAuth2Constants;
import org.passport.admin.client.Passport;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testframework.TestFrameworkException;
import org.passport.testframework.annotations.InjectAdminClient;
import org.passport.testframework.config.Config;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;

public class AdminClientSupplier implements Supplier<Passport, InjectAdminClient> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<Passport, InjectAdminClient> instanceContext) {
        DependenciesBuilder builder = DependenciesBuilder.create(AdminClientFactory.class);
        if (instanceContext.getAnnotation().mode().equals(InjectAdminClient.Mode.MANAGED_REALM)) {
            builder.add(ManagedRealm.class);
        }
        return builder.build();
    }

    @Override
    public Passport getValue(InstanceContext<Passport, InjectAdminClient> instanceContext) {
        InjectAdminClient annotation = instanceContext.getAnnotation();

        InjectAdminClient.Mode mode = annotation.mode();

        AdminClientBuilder adminBuilder = instanceContext.getDependency(AdminClientFactory.class).create()
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS);

        if (mode.equals(InjectAdminClient.Mode.BOOTSTRAP)) {
            adminBuilder.realm("master").clientId(Config.getAdminClientId()).clientSecret(Config.getAdminClientSecret());
        } else if (mode.equals(InjectAdminClient.Mode.MANAGED_REALM)) {
            ManagedRealm managedRealm = instanceContext.getDependency(ManagedRealm.class);
            adminBuilder.realm(managedRealm.getName());

            String clientId = !annotation.client().isEmpty() ? annotation.client() : null;
            String userId = !annotation.user().isEmpty() ? annotation.user() : null;

            if (clientId == null) {
                throw new TestFrameworkException("Client is required when using managed realm mode");
            }

            RealmRepresentation realmRep = managedRealm.getCreatedRepresentation();
            ClientRepresentation clientRep = realmRep.getClients().stream()
                    .filter(c -> c.getClientId().equals(annotation.client()))
                    .findFirst().orElseThrow(() -> new TestFrameworkException("Client " + annotation.client() + " not found in managed realm"));

            adminBuilder.clientId(clientId).clientSecret(clientRep.getSecret());

            if (userId != null) {
                UserRepresentation userRep = realmRep.getUsers().stream()
                        .filter(u -> u.getUsername().equals(annotation.user()))
                        .findFirst().orElseThrow(() -> new TestFrameworkException("User " + annotation.user() + " not found in managed realm"));
                String password = ManagedUser.getPassword(userRep);
                adminBuilder.username(userRep.getUsername()).password(password);
                adminBuilder.grantType(OAuth2Constants.PASSWORD);
            }
        }

        return adminBuilder.build();
    }

    @Override
    public LifeCycle getDefaultLifecycle() {
        return LifeCycle.GLOBAL;
    }

    @Override
    public boolean compatible(InstanceContext<Passport, InjectAdminClient> a, RequestedInstance<Passport, InjectAdminClient> b) {
        return true;
    }

    @Override
    public void close(InstanceContext<Passport, InjectAdminClient> instanceContext) {
        instanceContext.getValue().close();
    }

}
