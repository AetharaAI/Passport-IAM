package org.passport.tests.admin;

import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

import org.passport.models.BrowserSecurityHeaders;
import org.passport.representations.idm.UserRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@PassportIntegrationTest
public class AdminHeadersTest {

    @InjectRealm
    private ManagedRealm realm;

    @Test
    public void testHeaders() {
        UserRepresentation userRep = new UserRepresentation();
        userRep.setUsername("headers-user");
        Response response = realm.admin().users().create(userRep);
        MultivaluedMap<String, Object> h = response.getHeaders();

        assertDefaultValue(BrowserSecurityHeaders.STRICT_TRANSPORT_SECURITY, h);
        assertDefaultValue(BrowserSecurityHeaders.X_FRAME_OPTIONS, h);
        assertDefaultValue(BrowserSecurityHeaders.X_CONTENT_TYPE_OPTIONS, h);
        assertDefaultValue(BrowserSecurityHeaders.REFERRER_POLICY, h);
        assertDefaultValue(BrowserSecurityHeaders.X_ROBOTS_TAG, h);

        response.close();
    }

    private void assertDefaultValue(BrowserSecurityHeaders header, MultivaluedMap<String, Object> h) {
        assertThat(h.getFirst(header.getHeaderName()), is(equalTo(header.getDefaultValue())));
    }
}
