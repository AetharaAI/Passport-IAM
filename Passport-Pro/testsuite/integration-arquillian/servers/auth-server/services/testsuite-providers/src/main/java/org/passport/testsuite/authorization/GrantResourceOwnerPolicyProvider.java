package org.passport.testsuite.authorization;

import org.passport.Config;
import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.identity.Identity;
import org.passport.authorization.model.Policy;
import org.passport.authorization.model.Resource;
import org.passport.authorization.model.ResourceServer;
import org.passport.authorization.policy.evaluation.Evaluation;
import org.passport.authorization.policy.provider.PolicyProvider;
import org.passport.authorization.policy.provider.PolicyProviderAdminService;
import org.passport.authorization.policy.provider.PolicyProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.representations.idm.authorization.PolicyRepresentation;

public class GrantResourceOwnerPolicyProvider implements PolicyProviderFactory<PolicyRepresentation>, PolicyProvider {

    @Override
    public String getName() {
        return "Allow Resource Owner";
    }

    @Override
    public String getGroup() {
        return "Test Suite";
    }

    @Override
    public PolicyProvider create(AuthorizationProvider authorization) {
        return this;
    }

    @Override
    public PolicyRepresentation toRepresentation(Policy policy, AuthorizationProvider authorization) {
        return new PolicyRepresentation();
    }

    @Override
    public Class getRepresentationType() {
        return PolicyRepresentation.class;
    }

    @Override
    public PolicyProviderAdminService getAdminResource(ResourceServer resourceServer, AuthorizationProvider authorization) {
        return null;
    }

    @Override
    public PolicyProvider create(PassportSession session) {
        return null;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "allow-resource-owner";
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        Resource resource = evaluation.getPermission().getResource();
        Identity identity = evaluation.getContext().getIdentity();

        if (identity.getId().equals(resource.getOwner())) {
            evaluation.grant();
        }
    }
}
