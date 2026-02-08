package org.passport.testsuite.authz;

import org.passport.admin.client.resource.AuthorizationResource;
import org.passport.jose.jws.JWSInput;
import org.passport.jose.jws.JWSInputException;
import org.passport.models.utils.PassportModelUtils;
import org.passport.representations.AccessToken;
import org.passport.representations.idm.authorization.PolicyRepresentation;
import org.passport.testsuite.AbstractPassportTest;
import org.passport.testsuite.ProfileAssume;

import org.junit.BeforeClass;

import static org.passport.common.Profile.Feature.AUTHORIZATION;

/**
 * @author mhajas
 */
public abstract class AbstractAuthzTest extends AbstractPassportTest {

    @BeforeClass
    public static void enabled() {
        ProfileAssume.assumeFeatureEnabled(AUTHORIZATION);
    }

    protected AccessToken toAccessToken(String rpt) {
        AccessToken accessToken;

        try {
            accessToken = new JWSInput(rpt).readJsonContent(AccessToken.class);
        } catch (JWSInputException cause) {
            throw new RuntimeException("Failed to deserialize RPT", cause);
        }
        return accessToken;
    }

    protected PolicyRepresentation createAlwaysGrantPolicy(AuthorizationResource authorization) {
        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName(PassportModelUtils.generateId());
        policy.setType("always-grant");
        authorization.policies().create(policy).close();
        return policy;
    }

    protected PolicyRepresentation createAlwaysDenyPolicy(AuthorizationResource authorization) {
        PolicyRepresentation policy = new PolicyRepresentation();
        policy.setName(PassportModelUtils.generateId());
        policy.setType("always-deny");
        authorization.policies().create(policy).close();
        return policy;
    }

    protected PolicyRepresentation createOnlyOwnerPolicy(AuthorizationResource authorization) {
        PolicyRepresentation onlyOwnerPolicy = new PolicyRepresentation();

        onlyOwnerPolicy.setName(PassportModelUtils.generateId());
        onlyOwnerPolicy.setType("allow-resource-owner");

        authorization.policies().create(onlyOwnerPolicy).close();

        return onlyOwnerPolicy;
    }
}
