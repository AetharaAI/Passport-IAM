package org.passport.tests.admin;

import java.io.IOException;

import org.passport.services.cors.Cors;
import org.passport.testframework.annotations.InjectHttpClient;
import org.passport.testframework.annotations.InjectPassportUrls;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.server.PassportUrls;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PassportIntegrationTest
public class AdminPreflightTest {

    @InjectHttpClient
    HttpClient client;

    @InjectPassportUrls
    PassportUrls passportUrls;

    @Test
    public void testPreflight() throws IOException {
        testPreflightForAdminPath("/realms/master/users");
    }

    @Test
    public void testPreflightServerInfo() throws IOException {
        testPreflightForAdminPath("/serverinfo");
    }

    private void testPreflightForAdminPath(String path) throws IOException {
        HttpOptions options = new HttpOptions(passportUrls.getAdminBuilder().path(path).build());
        options.setHeader("Origin", "http://test");

        HttpResponse response = client.execute(options);
        assertEquals(200, response.getStatusLine().getStatusCode());
        assertEquals("true", response.getFirstHeader(Cors.ACCESS_CONTROL_ALLOW_CREDENTIALS).getValue());
        assertEquals("DELETE, POST, GET, PUT", response.getFirstHeader(Cors.ACCESS_CONTROL_ALLOW_METHODS).getValue());
        assertEquals("http://test", response.getFirstHeader(Cors.ACCESS_CONTROL_ALLOW_ORIGIN).getValue());
        assertEquals("3600", response.getFirstHeader(Cors.ACCESS_CONTROL_MAX_AGE).getValue());
        assertTrue(response.getFirstHeader(Cors.ACCESS_CONTROL_ALLOW_HEADERS).getValue().contains("Authorization"));
        assertTrue(response.getFirstHeader(Cors.ACCESS_CONTROL_ALLOW_HEADERS).getValue().contains("Content-Type"));
    }

}
