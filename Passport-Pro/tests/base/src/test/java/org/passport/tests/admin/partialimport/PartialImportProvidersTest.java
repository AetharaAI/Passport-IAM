package org.passport.tests.admin.partialimport;

import java.util.Arrays;
import java.util.Map;

import org.passport.admin.client.resource.IdentityProviderResource;
import org.passport.partialimport.PartialImportResult;
import org.passport.partialimport.PartialImportResults;
import org.passport.partialimport.ResourceType;
import org.passport.representations.idm.IdentityProviderMapperRepresentation;
import org.passport.representations.idm.IdentityProviderRepresentation;
import org.passport.testframework.annotations.PassportIntegrationTest;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PassportIntegrationTest(config = AbstractPartialImportTest.PartialImportServerConfig.class)
public class PartialImportProvidersTest extends AbstractPartialImportTest {

    @Test
    public void testAddProviders() {
        setFail();
        addProviders();

        PartialImportResults results = doImport();
        assertEquals(IDP_ALIASES.length, results.getAdded());

        for (PartialImportResult result : results.getResults()) {
            String id = result.getId();
            IdentityProviderResource idpRsc = managedRealm.admin().identityProviders().get(id);
            IdentityProviderRepresentation idp = idpRsc.toRepresentation();
            Map<String, String> config = idp.getConfig();
            assertTrue(Arrays.asList(IDP_ALIASES).contains(config.get("clientId")));
        }
    }

    @Test
    public void testAddProviderMappers() {
        setFail();
        addProviderMappers();

        PartialImportResults results = doImport();
        assertEquals(IDP_ALIASES.length*2, results.getAdded());

        for (PartialImportResult result : results.getResults()) {
            if (ResourceType.IDP.equals(result.getResourceType())) {
                String id = result.getId();
                IdentityProviderResource idpRsc = managedRealm.admin().identityProviders().get(id);
                IdentityProviderMapperRepresentation idpMap = idpRsc.getMappers().get(0);
                String alias = idpMap.getIdentityProviderAlias();
                assertTrue(Arrays.asList(IDP_ALIASES).contains(alias));
                assertEquals(alias + "_mapper", idpMap.getName());
                assertEquals("passport-oidc-role-to-role-idp-mapper", idpMap.getIdentityProviderMapper());
                assertEquals("IDP.TEST_ROLE", idpMap.getConfig().get("external.role"));
                assertEquals("FORCE", idpMap.getConfig().get("syncMode"));
                assertEquals("TEST_ROLE", idpMap.getConfig().get("role"));
            }
        }
    }

    @Test
    public void testAddProvidersFail() {
        addProviders();
        testFail();
    }

    @Test
    public void testAddProviderMappersFail() {
        addProviderMappers();
        testFail();
    }

    @Test
    public void testAddProvidersSkip() {
        addProviders();
        testSkip();
    }

    @Test
    public void testAddProviderMappersSkip() {
        addProviderMappers();
        testSkip(NUM_ENTITIES*2);
    }

    @Test
    public void testAddProvidersOverwrite() {
        addProviders();
        testOverwrite();
    }

    @Test
    public void testAddProviderMappersOverwrite() {
        addProviderMappers();
        testOverwrite(NUM_ENTITIES*2);
    }
}
