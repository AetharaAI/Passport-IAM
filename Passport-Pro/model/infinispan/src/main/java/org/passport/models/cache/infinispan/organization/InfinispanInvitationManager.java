package org.passport.models.cache.infinispan.organization;

import java.util.Map;
import java.util.stream.Stream;

import org.passport.models.OrganizationInvitationModel;
import org.passport.models.OrganizationInvitationModel.Filter;
import org.passport.models.OrganizationModel;
import org.passport.organization.InvitationManager;

record InfinispanInvitationManager(InvitationManager delegate) implements InvitationManager {

    @Override
    public OrganizationInvitationModel create(OrganizationModel organization, String email, String firstName, String lastName) {
        return delegate().create(organization, email, firstName, lastName);
    }

    @Override
    public OrganizationInvitationModel getById(String id) {
        return delegate().getById(id);
    }

    @Override
    public Stream<OrganizationInvitationModel> getAllStream(OrganizationModel organization, Map<Filter, String> attributes, Integer first, Integer max) {
        return delegate().getAllStream(organization, attributes, first, max);
    }

    @Override
    public boolean remove(String id) {
        return delegate().remove(id);
    }
}
