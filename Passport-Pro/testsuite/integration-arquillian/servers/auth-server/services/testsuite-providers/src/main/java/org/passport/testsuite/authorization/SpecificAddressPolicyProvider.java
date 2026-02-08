package org.passport.testsuite.authorization;

import org.passport.Config;
import org.passport.authorization.AuthorizationProvider;
import org.passport.authorization.attribute.Attributes;
import org.passport.authorization.model.Policy;
import org.passport.authorization.model.ResourceServer;
import org.passport.authorization.policy.evaluation.Evaluation;
import org.passport.authorization.policy.evaluation.EvaluationContext;
import org.passport.authorization.policy.provider.PolicyProvider;
import org.passport.authorization.policy.provider.PolicyProviderAdminService;
import org.passport.authorization.policy.provider.PolicyProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.representations.idm.authorization.PolicyRepresentation;

public class SpecificAddressPolicyProvider implements PolicyProviderFactory<PolicyRepresentation>, PolicyProvider {

    @Override
    public String getName() {
        return "Allow from specific address";
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
        return "only-from-specific-address-policy";
    }

    @Override
    public void evaluate(Evaluation evaluation) {
        EvaluationContext context = evaluation.getContext();
        Attributes attributes = context.getAttributes();

        if (attributes.containsValue("kc.client.network.ip_address", "127.0.0.1") || attributes.containsValue("kc.client.network.ip_address", "0:0:0:0:0:0:0:1")) {
            evaluation.grant();
        }
    }
}
