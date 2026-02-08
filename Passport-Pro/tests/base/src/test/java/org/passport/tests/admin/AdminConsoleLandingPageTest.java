package org.passport.tests.admin;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.passport.testframework.annotations.InjectHttpClient;
import org.passport.testframework.annotations.InjectPassportUrls;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.server.PassportUrls;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class AdminConsoleLandingPageTest {

    @InjectPassportUrls
    PassportUrls passportUrls;

    @InjectHttpClient
    HttpClient httpClient;

    @Test
    public void landingPage() throws IOException {
        String body = EntityUtils.toString(httpClient.execute(new HttpGet(passportUrls.getBaseUrl().toString() + "/admin/master/console")).getEntity());

        Map<String, String> config = getConfig(body);
        String authUrl = config.get("authUrl");
        Assertions.assertEquals(passportUrls.getBaseUrl().toString()+ "", authUrl);

        String resourceUrl = config.get("resourceUrl");
        Assertions.assertTrue(resourceUrl.matches("/resources/[^/]*/admin/passport.v2"));

        String consoleBaseUrl = config.get("consoleBaseUrl");
        Assertions.assertEquals(consoleBaseUrl, "/admin/master/console/");

        Pattern p = Pattern.compile("link href=\"([^\"]*)\"");
        Matcher m = p.matcher(body);

        while(m.find()) {
            String url = m.group(1);
            Assertions.assertTrue(url.startsWith("/resources/"));
        }

        p = Pattern.compile("script src=\"([^\"]*)\"");
        m = p.matcher(body);

        while(m.find()) {
            String url = m.group(1);
            if (url.contains("passport.js")) {
                Assertions.assertTrue(url.startsWith("/js/"), url);
            } else {
                Assertions.assertTrue(url.startsWith("/resources/"), url);
            }
        }
    }

    private static Map<String, String> getConfig(String body) {
        Map<String, String> variables = new HashMap<>();
        String start = "<script id=\"environment\" type=\"application/json\">";
        String end = "</script>";

        String config = body.substring(body.indexOf(start) + start.length());
        config = config.substring(0, config.indexOf(end)).trim();

        Matcher matcher = Pattern.compile(".*\"(.*)\": \"(.*)\"").matcher(config);
        while (matcher.find()) {
            variables.put(matcher.group(1), matcher.group(2));
        }

        return variables;
    }
}
