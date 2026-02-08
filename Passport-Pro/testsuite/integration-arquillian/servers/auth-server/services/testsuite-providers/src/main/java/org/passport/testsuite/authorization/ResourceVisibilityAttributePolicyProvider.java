package org.passport.testsuite.authorization;

import java.util.List;

import org.passport.Config;
import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.model.Policy;
import org.passport.authorization.model.Resource;
import org.passport.authorization.model.ResourceServer;
import org.passport.authorization.permission.ResourcePermission;
import org.passport.authorization.policy.evaluation.Evaluation;
import org.passport.authorization.policy.provider.PolicyProvider;
import org.passport.authorization.policy.provider.PolicyProviderAdminService;
import org.passport.authorization.policy.provider.PolicyProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.representations.idm.authorization.PolicyRepresentation;

public class ResourceVisibilityAttributePolicyProvider implements PolicyProviderFactory<PolicyRepresentation>, PolicyProvider {

    @Override
    public String getName() {
        return "Check resource visibility";
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
        return "resource-visibility-attribute-policy";
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        ResourcePermission permission = evaluation.getPermission();
        Resource resource = permission.getResource();

        if (isPublic(resource)) {
            evaluation.grant();
        }
    }

    private static boolean isPublic(Resource resource) {
        List<String> values = resource.getAttributes().get("visibility");
        return values == null || !values.contains("private");
    }
}
