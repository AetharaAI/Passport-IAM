/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.passport.testsuite.saml;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.core.Response;

import org.passport.dom.saml.v2.assertion.AudienceRestrictionType;
import org.passport.dom.saml.v2.protocol.ResponseType;
import org.passport.protocol.saml.mappers.SAMLAudienceProtocolMapper;
import org.passport.protocol.saml.mappers.SAMLAudienceResolveProtocolMapper;
import org.passport.representations.idm.ClientScopeRepresentation;
import org.passport.representations.idm.RoleRepresentation;
import org.passport.saml.common.constants.JBossSAMLURIConstants;
import org.passport.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.passport.testsuite.admin.ApiUtil;
import org.passport.testsuite.updaters.ClientAttributeUpdater;
import org.passport.testsuite.updaters.ProtocolMappersUpdater;
import org.passport.testsuite.updaters.RoleScopeUpdater;
import org.passport.testsuite.updaters.UserAttributeUpdater;
import org.passport.testsuite.util.Matchers;
import org.passport.testsuite.util.SamlClient;
import org.passport.testsuite.util.SamlClientBuilder;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.passport.testsuite.saml.RoleMapperTest.createSamlProtocolMapper;
import static org.passport.testsuite.util.ServerURLs.AUTH_SERVER_PORT;
import static org.passport.testsuite.util.ServerURLs.AUTH_SERVER_SCHEME;
import static org.passport.testsuite.util.ServerURLs.AUTH_SERVER_SSL_REQUIRED;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.greaterThan;

/**
 *
 * @author rmartinc
 */
public class AudienceProtocolMappersTest extends AbstractSamlTest {

    public static final String SAML_ASSERTION_CONSUMER_URL_EMPLOYEE_2 = AUTH_SERVER_SCHEME + "://localhost:" + (AUTH_SERVER_SSL_REQUIRED ? AUTH_SERVER_PORT : 8080) + "/employee2/";

    private ProtocolMappersUpdater pmu;

    @Before
    public void cleanMappersAndScopes() {
        this.pmu = ClientAttributeUpdater.forClient(adminClient, REALM_NAME, SAML_CLIENT_ID_EMPLOYEE_2).protocolMappers()
                .clear()
                .update();
    }

    @After
    public void revertCleanMappersAndScopes() throws IOException {
        this.pmu.close();
    }

    public void testExpectedAudiences(String... audiences) {
        SAMLDocumentHolder document = new SamlClientBuilder()
          .authnRequest(getAuthServerSamlEndpoint(REALM_NAME), SAML_CLIENT_ID_EMPLOYEE_2, SAML_ASSERTION_CONSUMER_URL_EMPLOYEE_2, SamlClient.Binding.POST).build()
          .login().user(bburkeUser).build()
          .getSamlResponse(SamlClient.Binding.POST);

        Assert.assertNotNull(document.getSamlObject());
        assertThat(document.getSamlObject(), Matchers.isSamlResponse(JBossSAMLURIConstants.STATUS_SUCCESS));
        Assert.assertNotNull(((ResponseType) document.getSamlObject()).getAssertions());
        assertThat(((ResponseType) document.getSamlObject()).getAssertions().size(), greaterThan(0));
        Assert.assertNotNull(((ResponseType) document.getSamlObject()).getAssertions().get(0));
        Assert.assertNotNull(((ResponseType) document.getSamlObject()).getAssertions().get(0).getAssertion());
        AudienceRestrictionType audience = ((ResponseType) document.getSamlObject())
                .getAssertions().get(0).getAssertion().getConditions().getConditions()
                .stream()
                .filter(AudienceRestrictionType.class::isInstance)
                .map(AudienceRestrictionType.class::cast)
                .findFirst().orElse(null);
        Assert.assertNotNull(audience);
        Assert.assertNotNull(audience.getAudience());
        List<String> values = audience.getAudience().stream().map(uri -> uri.toString()).collect(Collectors.toList());
        assertThat(values, containsInAnyOrder(audiences));
    }

    @Test
    public void testDefaultAudience() throws Exception {
        this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2);
    }

    @Test
    public void testCustomAudience() throws Exception {
        pmu.add(
          createSamlProtocolMapper(SAMLAudienceProtocolMapper.PROVIDER_ID,
            SAMLAudienceProtocolMapper.INCLUDED_CUSTOM_AUDIENCE, "https://test.com/test"
          )
        ).update();
        this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2, "https://test.com/test");
    }

    @Test
    public void testClientAudience() throws Exception {
        pmu.add(
          createSamlProtocolMapper(SAMLAudienceProtocolMapper.PROVIDER_ID,
            SAMLAudienceProtocolMapper.INCLUDED_CLIENT_AUDIENCE, SAML_CLIENT_ID_SALES_POST
          )
        ).update();
        this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2, SAML_CLIENT_ID_SALES_POST);
    }

    @Test
    public void testClientAndCustomAudience() throws Exception {
        pmu.add(
          createSamlProtocolMapper(SAMLAudienceProtocolMapper.PROVIDER_ID,
            SAMLAudienceProtocolMapper.INCLUDED_CLIENT_AUDIENCE, SAML_CLIENT_ID_SALES_POST,
            SAMLAudienceProtocolMapper.INCLUDED_CUSTOM_AUDIENCE, "https://test.com/test"
          )
        ).update();
        // only client is expected because it works as the OIDC one (same labels used)
        this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2, SAML_CLIENT_ID_SALES_POST);
    }

    @Test
    public void testAudienceResolveFullScope() throws Exception {
        pmu.add(createSamlProtocolMapper(SAMLAudienceResolveProtocolMapper.PROVIDER_ID)).update();
        // bburke in the saml realm belongs to three different SAML clients groups
        // "http://localhost:8280/employee/": [ "employee" ],
        // "http://localhost:8280/employee2/": [ "empl.oyee", "employee" ],
        // "http://localhost:8280/employee-role-mapping/": ["employee"]
        // this way it should contain the three apps by default
        this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2, "http://localhost:8280/employee/", "http://localhost:8280/employee-role-mapping/");
        // remove one of the groups (employee) and check the employee audience is removed
        String employeeId = adminClient.realm(REALM_NAME).clients().findByClientId("http://localhost:8280/employee/").get(0).getId();
        Assert.assertNotNull(employeeId);
        try (RoleScopeUpdater rsc = UserAttributeUpdater.forUserByUsername(adminClient, REALM_NAME, bburkeUser.getUsername())
                .clientRoleScope(employeeId)
                .removeByName("employee")
                .update()) {
            this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2, "http://localhost:8280/employee-role-mapping/");
        }
    }

    @Test
    public void testAudienceResolveNoFullScope() throws Exception {
        pmu.add(createSamlProtocolMapper(SAMLAudienceResolveProtocolMapper.PROVIDER_ID)).update();
        // remove full scope
        try (ClientAttributeUpdater cau = ClientAttributeUpdater.forClient(adminClient, REALM_NAME, SAML_CLIENT_ID_EMPLOYEE_2)
                .setFullScopeAllowed(false)
                .update()) {
            // now only the same client should be in the audience
            this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2);

            // add another client in the scope
            String employee2Id = adminClient.realm(REALM_NAME).clients().findByClientId("http://localhost:8280/employee2/").get(0).getId();
            Assert.assertNotNull(employee2Id);
            String employeeId = adminClient.realm(REALM_NAME).clients().findByClientId("http://localhost:8280/employee/").get(0).getId();
            Assert.assertNotNull(employeeId);
            List<RoleRepresentation> availables = adminClient.realm(REALM_NAME).clients().get(employee2Id).getScopeMappings().clientLevel(employeeId).listAvailable();
            assertThat(availables.size(), greaterThan(0));
            // assign scope to only employee2 (employee-role-mapping should not be there)
            try (RoleScopeUpdater ru = cau.clientRoleScope(employeeId)
                    .add(availables.get(0))
                    .update()) {
                this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2, "http://localhost:8280/employee/");
            }
        }
    }

    @Test
    public void testAudienceResolveNoFullScopeClientScopes() throws Exception {
        // create the mapper using a client scope
        ClientScopeRepresentation clientScope = new ClientScopeRepresentation();
        clientScope.setName("audience-mapper-test-client-scope");
        clientScope.setProtocol("saml");
        clientScope.setProtocolMappers(Collections.singletonList(createSamlProtocolMapper(SAMLAudienceResolveProtocolMapper.PROVIDER_ID)));
        Response res = adminClient.realm(REALM_NAME).clientScopes().create(clientScope);
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), res.getStatus());
        String clientScopeId = ApiUtil.getCreatedId(res);

        try {
            // add a mapping to the client scope to employee2.employee role (this way employee should be in the audience)
            String employee2Id = adminClient.realm(REALM_NAME).clients().findByClientId("http://localhost:8280/employee2/").get(0).getId();
            Assert.assertNotNull(employee2Id);
            String employeeId = adminClient.realm(REALM_NAME).clients().findByClientId("http://localhost:8280/employee/").get(0).getId();
            Assert.assertNotNull(employeeId);
            List<RoleRepresentation> availables = adminClient.realm(REALM_NAME).clientScopes().get(clientScopeId).getScopeMappings().clientLevel(employeeId).listAvailable();
            assertThat(availables.size(), greaterThan(0));
            adminClient.realm(REALM_NAME).clientScopes().get(clientScopeId).getScopeMappings().clientLevel(employeeId).add(availables);

            // remove full scope and add the client scope
            try (ClientAttributeUpdater cau = ClientAttributeUpdater.forClient(adminClient, REALM_NAME, SAML_CLIENT_ID_EMPLOYEE_2)
                    .setFullScopeAllowed(false)
                    .addDefaultClientScope("audience-mapper-test-client-scope")
                    .update()) {
                this.testExpectedAudiences(SAML_CLIENT_ID_EMPLOYEE_2, "http://localhost:8280/employee/");
            }
        } finally {
            adminClient.realm(REALM_NAME).clientScopes().get(clientScopeId).remove();
        }
    }
}
