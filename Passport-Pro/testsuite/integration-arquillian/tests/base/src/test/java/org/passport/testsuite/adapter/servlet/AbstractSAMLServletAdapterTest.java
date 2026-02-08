package org.passport.testsuite.adapter.servlet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;

import org.passport.admin.client.resource.ProtocolMappersResource;
import org.passport.representations.idm.ProtocolMapperRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.adapter.AbstractServletsAdapterTest;
import org.passport.testsuite.adapter.filter.AdapterActionsFilter;
import org.passport.testsuite.auth.page.login.Login;
import org.passport.testsuite.page.AbstractPage;
import org.passport.testsuite.util.SamlClient;
import org.passport.testsuite.util.SamlClientBuilder;
import org.passport.testsuite.utils.io.IOUtil;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import static org.passport.testsuite.admin.ApiUtil.getCreatedId;
import static org.passport.testsuite.auth.page.AuthRealm.SAMLSERVLETDEMO;
import static org.passport.testsuite.util.URLAssert.assertCurrentUrlStartsWith;
import static org.passport.testsuite.util.WaitUtils.waitForPageToLoad;

public abstract class AbstractSAMLServletAdapterTest extends AbstractServletsAdapterTest {

    public static final String WEB_XML_WITH_ACTION_FILTER = "web-with-action-filter.xml";

    @Override
    public void setDefaultPageUriParameters() {
        super.setDefaultPageUriParameters();
        testRealmPage.setAuthRealm(SAMLSERVLETDEMO);
        testRealmSAMLRedirectLoginPage.setAuthRealm(SAMLSERVLETDEMO);
        testRealmSAMLPostLoginPage.setAuthRealm(SAMLSERVLETDEMO);
    }

    @Override
    public void addAdapterTestRealms(List<RealmRepresentation> testRealms) {
        testRealms.add(IOUtil.loadRealm("/adapter-test/passport-saml/testsaml.json"));
        testRealms.add(IOUtil.loadRealm("/adapter-test/passport-saml/tenant1-realm.json"));
        testRealms.add(IOUtil.loadRealm("/adapter-test/passport-saml/tenant2-realm.json"));
    }

    protected void setAdapterAndServerTimeOffset(int timeOffset, String... servletUris) {
        setTimeOffset(timeOffset);

        Arrays.stream(servletUris)
                .map(url -> url += "unsecured")
                .forEach(servletUri -> {
                    String url = UriBuilder.fromUri(servletUri)
                            .queryParam(AdapterActionsFilter.TIME_OFFSET_PARAM, timeOffset)
                            .build().toString();
                    try (CloseableHttpClient client = HttpClientBuilder.create().build()) {
                        HttpUriRequest request = new HttpGet(url);
                        CloseableHttpResponse httpResponse = client.execute(request);

                        System.out.println(EntityUtils.toString(httpResponse.getEntity()));
                        httpResponse.close();
                    } catch (IOException e) {
                        throw new RuntimeException("Cannot change time on url " + url, e);
                    }
                });
    }

    protected SamlClientBuilder beginAuthenticationAndLogin(AbstractPage page, SamlClient.Binding binding) {
        return new SamlClientBuilder()
                .navigateTo(page.buildUri())
                .processSamlResponse(binding) // Process AuthnResponse
                .build()

                .login().user(bburkeUser)
                .build();
    }

    protected AutoCloseable createProtocolMapper(ProtocolMappersResource resource, String name, String protocol, String protocolMapper, Map<String, String> config) {
        ProtocolMapperRepresentation representation = new ProtocolMapperRepresentation();
        representation.setName(name);
        representation.setProtocol(protocol);
        representation.setProtocolMapper(protocolMapper);
        representation.setConfig(config);
        try (Response response = resource.createMapper(representation)) {
            String createdId = getCreatedId(response);
            return () -> resource.delete(createdId);
        }
    }

    protected void checkLoggedOut(AbstractPage page, Login loginPage) {
        page.navigateTo();
        waitForPageToLoad();
        assertCurrentUrlStartsWith(loginPage);
    }
}
