package org.passport.test.examples;

import org.passport.events.EventType;
import org.passport.representations.idm.EventRepresentation;
import org.passport.testframework.annotations.InjectEvents;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.events.Events;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.timeoffset.InjectTimeOffSet;
import org.passport.testframework.remote.timeoffset.TimeOffSet;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@PassportIntegrationTest
public class EventsTest {

    @InjectRealm
    private ManagedRealm realm;

    @InjectEvents
    private Events events;

    @InjectOAuthClient
    private OAuthClient oAuthClient;

    @InjectTimeOffSet
    TimeOffSet timeOffSet;

    @Test
    public void testFailedLogin() {
        oAuthClient.doPasswordGrantRequest("invalid", "invalid");

        EventRepresentation event = events.poll();
        Assertions.assertEquals(EventType.LOGIN_ERROR.name(), event.getType());
        Assertions.assertEquals("invalid", event.getDetails().get("username"));

        oAuthClient.doPasswordGrantRequest("invalid2", "invalid");

        event = events.poll();
        Assertions.assertEquals(EventType.LOGIN_ERROR.name(), event.getType());
        Assertions.assertEquals("invalid2", event.getDetails().get("username"));
    }

    @Test
    public void testTimeOffset() {
        timeOffSet.set(60);

        oAuthClient.doClientCredentialsGrantAccessTokenRequest();

        Assertions.assertEquals(EventType.CLIENT_LOGIN.name(), events.poll().getType());
    }

    @Test
    public void testClientLogin() {
        oAuthClient.doClientCredentialsGrantAccessTokenRequest();

        Assertions.assertEquals(EventType.CLIENT_LOGIN.name(), events.poll().getType());
    }

}
