package org.passport.testframework.realm;

import java.util.List;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.passport.admin.client.resource.ClientResource;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.testframework.annotations.InjectClient;
import org.passport.testframework.injection.DependenciesBuilder;
import org.passport.testframework.injection.Dependency;
import org.passport.testframework.injection.InstanceContext;
import org.passport.testframework.injection.RequestedInstance;
import org.passport.testframework.injection.Supplier;
import org.passport.testframework.injection.SupplierHelpers;
import org.passport.testframework.util.ApiUtil;

public class ClientSupplier implements Supplier<ManagedClient, InjectClient> {

    @Override
    public List<Dependency> getDependencies(RequestedInstance<ManagedClient, InjectClient> instanceContext) {
        return DependenciesBuilder.create(ManagedRealm.class, instanceContext.getAnnotation().realmRef()).build();
    }

    @Override
    public ManagedClient getValue(InstanceContext<ManagedClient, InjectClient> instanceContext) {
        ManagedRealm realm = instanceContext.getDependency(ManagedRealm.class, instanceContext.getAnnotation().realmRef());

        String attachTo = instanceContext.getAnnotation().attachTo();
        boolean managed = attachTo.isEmpty();

        ClientRepresentation clientRepresentation;

        if (managed) {
            ClientConfig config = SupplierHelpers.getInstance(instanceContext.getAnnotation().config());
            clientRepresentation = config.configure(ClientConfigBuilder.create()).build();

            if (clientRepresentation.getClientId() == null) {
                clientRepresentation.setClientId(SupplierHelpers.createName(instanceContext));
            }

            Response response = realm.admin().clients().create(clientRepresentation);
            if (Status.CONFLICT.equals(Status.fromStatusCode(response.getStatus()))) {
                throw new IllegalStateException("Client already exist with client id: " + clientRepresentation.getClientId());
            }
            clientRepresentation.setId(ApiUtil.getCreatedId(response));
        } else {
            List<ClientRepresentation> clients = realm.admin().clients().findByClientId(attachTo);
            if (clients.isEmpty()) {
                throw new IllegalStateException("No client found with client id: " + attachTo);
            }
            clientRepresentation = clients.get(0);
        }

        instanceContext.addNote("managed", managed);

        ClientResource clientResource = realm.admin().clients().get(clientRepresentation.getId());
        return new ManagedClient(clientRepresentation, clientResource);
    }

    @Override
    public boolean compatible(InstanceContext<ManagedClient, InjectClient> a, RequestedInstance<ManagedClient, InjectClient> b) {
        return a.getAnnotation().config().equals(b.getAnnotation().config());
    }

    @Override
    public void close(InstanceContext<ManagedClient, InjectClient> instanceContext) {
        if (instanceContext.getNote("managed", Boolean.class)) {
            try {
                instanceContext.getValue().admin().remove();
            } catch (NotFoundException ex) {}
        }
    }

}
