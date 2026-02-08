package org.passport.authorization.store.syncronization;

import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.fgap.AdminPermissionsSchema;
import org.passport.models.GroupModel.GroupRemovedEvent;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderFactory;

public class GroupSynchronizer implements Synchronizer<GroupRemovedEvent> {

    @Override
    public void synchronize(GroupRemovedEvent event, PassportSessionFactory factory) {
        ProviderFactory<AuthorizationProvider> providerFactory = factory.getProviderFactory(AuthorizationProvider.class);
        AuthorizationProvider authorizationProvider = providerFactory.create(event.getPassportSession());

        AdminPermissionsSchema.SCHEMA.removeResourceObject(authorizationProvider, event);
    }
}
