package org.passport.authorization.store.syncronization;

import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.fgap.AdminPermissionsSchema;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RoleContainerModel.RoleRemovedEvent;
import org.passport.provider.ProviderFactory;

public class RoleSynchronizer implements Synchronizer<RoleRemovedEvent> {

    @Override
    public void synchronize(RoleRemovedEvent event, PassportSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getPassportSession());

        AdminPermissionsSchema.SCHEMA.removeResourceObject(authorizationProvider, event);
    }
}
