package org.passport.tests.admin.identityprovider;

import java.util.Map;

import org.passport.events.admin.OperationType;
import org.passport.events.admin.ResourceType;
import org.passport.models.IdentityProviderModel;
import org.passport.models.utils.StripSecretsUtils;
import org.passport.representations.idm.IdentityProviderRepresentation;
import org.passport.testframework.annotations.InjectAdminEvents;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.events.AdminEventAssertion;
import org.passport.testframework.events.AdminEvents;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.testframework.util.ApiUtil;
import org.passport.tests.utils.admin.AdminEventPaths;

import org.junit.jupiter.api.Assertions;

public class AbstractIdentityProviderTest {

    @InjectRealm
    ManagedRealm managedRealm;

    @InjectAdminEvents
    AdminEvents adminEvents;

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    protected String create(IdentityProviderRepresentation idpRep) {
        String idpId = ApiUtil.getCreatedId(managedRealm.admin().identityProviders().create(idpRep));
        Assertions.assertNotNull(idpId);

        String secret = idpRep.getConfig() != null ? idpRep.getConfig().get("clientSecret") : null;
        idpRep = StripSecretsUtils.stripSecrets(null, idpRep);

        if ("true".equals(idpRep.getConfig().get(IdentityProviderModel.LEGACY_HIDE_ON_LOGIN_ATTR))) {
            idpRep.setHideOnLogin(true);
            idpRep.getConfig().remove(IdentityProviderModel.LEGACY_HIDE_ON_LOGIN_ATTR);
        }

        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.CREATE, AdminEventPaths.identityProviderPath(idpRep.getAlias()), idpRep, ResourceType.IDENTITY_PROVIDER);

        if (secret != null) {
            idpRep.getConfig().put("clientSecret", secret);
        }

        return idpId;
    }

    protected IdentityProviderRepresentation createRep(String alias, String providerId) {
        return createRep(alias, providerId,true, null);
    }

    protected IdentityProviderRepresentation createRep(String alias, String providerId,boolean enabled, Map<String, String> config) {
        return createRep(alias, alias, providerId, enabled, config);
    }

    protected IdentityProviderRepresentation createRep(String alias, String displayName, String providerId, boolean enabled, Map<String, String> config) {
        IdentityProviderRepresentation idp = new IdentityProviderRepresentation();

        idp.setAlias(alias);
        idp.setDisplayName(displayName);
        idp.setProviderId(providerId);
        idp.setEnabled(enabled);
        if (config != null) {
            idp.setConfig(config);
        }
        return idp;
    }

    protected void assertProviderInfo(Map<String, String> info, String id, String name) {
        System.out.println(info);
        Assertions.assertEquals(id, info.get("id"), "id");
        Assertions.assertEquals(name, info.get("name"), "name");
    }
}
