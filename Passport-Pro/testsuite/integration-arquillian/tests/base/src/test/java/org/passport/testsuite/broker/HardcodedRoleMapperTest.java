package org.passport.testsuite.broker;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.passport.broker.provider.ConfigConstants;
import org.passport.broker.provider.HardcodedRoleMapper;
import org.passport.models.IdentityProviderMapperModel;
import org.passport.models.IdentityProviderMapperSyncMode;
import org.passport.representations.idm.IdentityProviderMapperRepresentation;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;

import static org.passport.models.IdentityProviderMapperSyncMode.FORCE;
import static org.passport.models.IdentityProviderMapperSyncMode.IMPORT;
import static org.passport.models.IdentityProviderMapperSyncMode.LEGACY;

/**
 * @author <a href="mailto:external.martin.idel@bosch.io">Martin Idel</a>,
 * <a href="mailto:daniel.fesenmeyer@bosch.io">Daniel Fesenmeyer</a>
 */
public class HardcodedRoleMapperTest extends AbstractRoleMapperTest {

    @Override
    protected BrokerConfiguration getBrokerConfiguration() {
        return new KcOidcBrokerConfiguration();
    }

    @Before
    public void setupRealm() {
        super.addClients();
    }

    @Test
    public void mapperGrantsRoleOnFirstLogin() {
        createMapperThenLoginAsUserTwiceWithHardcodedRoleMapper(IMPORT);

        assertThatRoleHasBeenAssignedInConsumerRealm();
    }

    @Test
    public void mapperDoesNotGrantRoleInModeImportIfMapperIsAddedLater() {
        loginAsUserThenCreateMapperAndLoginAgainWithHardcodedRoleMapper(IMPORT);

        assertThatRoleHasNotBeenAssignedInConsumerRealm();
    }

    @Test
    public void updateBrokeredUserDoesNotGrantRoleInLegacyMode() {
        loginAsUserThenCreateMapperAndLoginAgainWithHardcodedRoleMapper(LEGACY);

        assertThatRoleHasNotBeenAssignedInConsumerRealm();
    }

    @Test
    public void updateBrokeredUserGrantsRoleInForceMode() {
        loginAsUserThenCreateMapperAndLoginAgainWithHardcodedRoleMapper(FORCE);

        assertThatRoleHasBeenAssignedInConsumerRealm();
    }

    @Test
    public void updateBrokeredUserMatchDoesntDeleteRole() {
        createMapperThenLoginAsUserTwiceWithHardcodedRoleMapper(FORCE);

        assertThatRoleHasBeenAssignedInConsumerRealm();
    }

    private void createMapperThenLoginAsUserTwiceWithHardcodedRoleMapper(IdentityProviderMapperSyncMode syncMode) {
        loginAsUserTwiceWithMapper(syncMode, false, Collections.emptyMap());
    }

    private void loginAsUserThenCreateMapperAndLoginAgainWithHardcodedRoleMapper(IdentityProviderMapperSyncMode syncMode) {
        loginAsUserTwiceWithMapper(syncMode, true, Collections.emptyMap());
    }

    @Override
    protected void createMapperInIdp(IdentityProviderMapperSyncMode syncMode, String roleValue) {
        IdentityProviderMapperRepresentation advancedClaimToRoleMapper = new IdentityProviderMapperRepresentation();
        advancedClaimToRoleMapper.setName("oidc-hardcoded-role-mapper");
        advancedClaimToRoleMapper.setIdentityProviderMapper(HardcodedRoleMapper.PROVIDER_ID);
        advancedClaimToRoleMapper.setConfig(ImmutableMap.<String, String> builder()
                .put(IdentityProviderMapperModel.SYNC_MODE, syncMode.toString())
                .put(ConfigConstants.ROLE, roleValue)
                .build());

        persistMapper(advancedClaimToRoleMapper);
    }

    @Override
    protected Map<String, List<String>> createUserConfigForRole(String roleValue) {
        return Collections.emptyMap();
    }
}
