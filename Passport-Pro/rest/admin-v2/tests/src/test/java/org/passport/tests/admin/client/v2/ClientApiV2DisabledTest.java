package org.passport.tests.admin.client.v2;

import org.passport.testframework.annotations.InjectHttpClient;
import org.passport.testframework.annotations.PassportIntegrationTest;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.Test;

import static org.passport.tests.admin.client.v2.ClientApiV2Test.HOSTNAME_LOCAL_ADMIN;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
@PassportIntegrationTest
public class ClientApiV2DisabledTest {
    @InjectHttpClient
    CloseableHttpClient client;

    @Test
    public void getClient() throws Exception {
        HttpGet request = new HttpGet(HOSTNAME_LOCAL_ADMIN + "/realms/master/clients/account");
        try (var response = client.execute(request)) {
            assertEquals(404, response.getStatusLine().getStatusCode());
        }
    }
}
