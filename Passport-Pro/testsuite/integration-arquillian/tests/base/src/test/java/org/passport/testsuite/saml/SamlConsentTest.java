package org.passport.testsuite.saml;

import java.util.List;

import org.passport.protocol.saml.SamlConfigAttributes;
import org.passport.protocol.saml.SamlProtocol;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.saml.common.exceptions.ConfigurationException;
import org.passport.saml.common.exceptions.ParsingException;
import org.passport.saml.common.exceptions.ProcessingException;
import org.passport.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.passport.testsuite.util.ClientBuilder;
import org.passport.testsuite.util.SamlClient.Binding;
import org.passport.testsuite.util.SamlClientBuilder;
import org.passport.testsuite.utils.io.IOUtil;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author mhajas
 */
public class SamlConsentTest extends AbstractSamlTest {

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        testRealms.add(IOUtil.loadRealm("/adapter-test/passport-saml/testsaml.json"));
    }

    @Test
    public void rejectedConsentResponseTest() throws ParsingException, ConfigurationException, ProcessingException {
        ClientRepresentation client = adminClient.realm(REALM_NAME)
                .clients()
                .findByClientId(SAML_CLIENT_ID_SALES_POST)
                .get(0);

        adminClient.realm(REALM_NAME)
                .clients()
                .get(client.getId())
                .update(ClientBuilder.edit(client)
                        .consentRequired(true)
                        .attribute(SamlProtocol.SAML_IDP_INITIATED_SSO_URL_NAME, "sales-post")
                        .attribute(SamlProtocol.SAML_ASSERTION_CONSUMER_URL_POST_ATTRIBUTE, SAML_ASSERTION_CONSUMER_URL_SALES_POST + "saml")
                        .attribute(SamlConfigAttributes.SAML_SERVER_SIGNATURE, "true")
                        .build());

        log.debug("Log in using idp initiated login");
        SAMLDocumentHolder documentHolder = new SamlClientBuilder()
          .authnRequest(getAuthServerSamlEndpoint(REALM_NAME), SAML_CLIENT_ID_SALES_POST, SAML_ASSERTION_CONSUMER_URL_SALES_POST, Binding.POST).build()
          .login().user(bburkeUser).build()
          .consentRequired().approveConsent(false).build()
          .getSamlResponse(Binding.POST);

        final String samlDocumentString = IOUtil.documentToString(documentHolder.getSamlDocument());
        assertThat(samlDocumentString, containsString("<dsig:Signature")); // PASSPORT-4262
        assertThat(samlDocumentString, not(containsString("<samlp:LogoutResponse"))); // PASSPORT-4261
        assertThat(samlDocumentString, containsString("<samlp:Response")); // PASSPORT-4261
        assertThat(samlDocumentString, containsString("<samlp:Status")); // PASSPORT-4181
        assertThat(samlDocumentString, containsString("<samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:RequestDenied\"")); // PASSPORT-4181
    }
}
