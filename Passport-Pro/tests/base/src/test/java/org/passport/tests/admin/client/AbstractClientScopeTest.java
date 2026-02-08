package org.passport.tests.admin.client;

import java.io.IOException;
import java.util.Map;

import jakarta.ws.rs.core.Response;

import org.passport.admin.client.resource.ClientScopesResource;
import org.passport.events.admin.OperationType;
import org.passport.events.admin.ResourceType;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.ClientScopeRepresentation;
import org.passport.testframework.annotations.InjectAdminEvents;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.events.AdminEventAssertion;
import org.passport.testframework.events.AdminEvents;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.util.ApiUtil;
import org.passport.tests.utils.admin.AdminEventPaths;
import org.passport.util.JsonSerialization;

import org.junit.jupiter.api.Assertions;

@PassportIntegrationTest
public class AbstractClientScopeTest {

    @InjectRealm
    ManagedRealm managedRealm;

    @InjectAdminEvents
    AdminEvents adminEvents;

    void handleExpectedCreateFailure(ClientScopeRepresentation scopeRep, int expectedErrorCode, String expectedErrorMessage) {
        try (Response resp = clientScopes().create(scopeRep)) {
            Assertions.assertEquals(expectedErrorCode, resp.getStatus());
            String respBody = resp.readEntity(String.class);
            Map<String, String> responseJson;
            try {
                responseJson = JsonSerialization.readValue(respBody, Map.class);
                Assertions.assertEquals(expectedErrorMessage, responseJson.get("errorMessage"));
            } catch (IOException e) {
                Assertions.fail("Failed to extract the errorMessage from a CreateScope Response");
            }
        }
    }

    ClientScopesResource clientScopes() {
        return managedRealm.admin().clientScopes();
    }

    String createClientScope(ClientScopeRepresentation clientScopeRep) {
        Response resp = clientScopes().create(clientScopeRep);
        final String clientScopeId = ApiUtil.getCreatedId(resp);

        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.CREATE, AdminEventPaths.clientScopeResourcePath(clientScopeId), clientScopeRep, ResourceType.CLIENT_SCOPE);

        return clientScopeId;
    }

    String createClientScopeWithCleanup(ClientScopeRepresentation clientScopeRep) {
        String clientScopeId = createClientScope(clientScopeRep);
        managedRealm.cleanup().add(r -> r.clientScopes().get(clientScopeId).remove());
        return clientScopeId;
    }

    String createClientWithCleanup(ClientRepresentation clientRep) {
        Response resp = managedRealm.admin().clients().create(clientRep);
        final String clientUuid = ApiUtil.getCreatedId(resp);
        managedRealm.cleanup().add(r -> r.clients().get(clientUuid).remove());

        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.CREATE, AdminEventPaths.clientResourcePath(clientUuid), clientRep, ResourceType.CLIENT);
        return clientUuid;
    }

}
