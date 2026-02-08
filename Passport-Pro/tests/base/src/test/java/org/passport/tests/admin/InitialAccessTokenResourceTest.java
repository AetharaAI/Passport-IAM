/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

package org.passport.tests.admin;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.ws.rs.BadRequestException;

import org.passport.admin.client.resource.ClientInitialAccessResource;
import org.passport.common.util.Time;
import org.passport.events.admin.OperationType;
import org.passport.events.admin.ResourceType;
import org.passport.models.RealmModel;
import org.passport.models.session.UserSessionPersisterProvider;
import org.passport.representations.idm.ClientInitialAccessCreatePresentation;
import org.passport.representations.idm.ClientInitialAccessPresentation;
import org.passport.representations.idm.OAuth2ErrorRepresentation;
import org.passport.testframework.annotations.InjectAdminEvents;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.events.AdminEventAssertion;
import org.passport.testframework.events.AdminEvents;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.testframework.remote.timeoffset.InjectTimeOffSet;
import org.passport.testframework.remote.timeoffset.TimeOffSet;
import org.passport.tests.utils.Assert;
import org.passport.tests.utils.admin.AdminEventPaths;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
@PassportIntegrationTest
public class InitialAccessTokenResourceTest {

    @InjectRealm
    ManagedRealm managedRealm;

    @InjectAdminEvents
    AdminEvents adminEvents;

    @InjectTimeOffSet
    TimeOffSet timeOffSet;

    @InjectRunOnServer
    RunOnServerClient runOnServer;

    private ClientInitialAccessResource resource;

    @BeforeEach
    public void before() {
        resource = managedRealm.admin().clientInitialAccess();
    }

    @Test
    public void testInitialAccessTokens() {
        ClientInitialAccessCreatePresentation rep = new ClientInitialAccessCreatePresentation();
        rep.setCount(2);
        rep.setExpiration(100);

        int time = Time.currentTime();

        ClientInitialAccessPresentation response = resource.create(rep);
        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.CREATE, AdminEventPaths.clientInitialAccessPath(response.getId()), rep, ResourceType.CLIENT_INITIAL_ACCESS_MODEL);

        assertNotNull(response.getId());
        assertEquals(Integer.valueOf(2), response.getCount());
        assertEquals(Integer.valueOf(2), response.getRemainingCount());
        assertEquals(Integer.valueOf(100), response.getExpiration());
        assertThat(response.getTimestamp(), allOf(greaterThanOrEqualTo(time), lessThanOrEqualTo(Time.currentTime())));
        assertNotNull(response.getToken());

        rep.setCount(3);
        response = resource.create(rep);
        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.CREATE, AdminEventPaths.clientInitialAccessPath(response.getId()), rep, ResourceType.CLIENT_INITIAL_ACCESS_MODEL);

        rep.setCount(4);
        response = resource.create(rep);
        String lastId = response.getId();
        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.CREATE, AdminEventPaths.clientInitialAccessPath(lastId), rep, ResourceType.CLIENT_INITIAL_ACCESS_MODEL);

        List<ClientInitialAccessPresentation> list = resource.list();
        assertEquals(3, list.size());

        assertEquals(9, list.get(0).getCount() + list.get(1).getCount() + list.get(2).getCount());
        assertNull(list.get(0).getToken());

        // Delete last and assert it was deleted
        resource.delete(lastId);
        AdminEventAssertion.assertEvent(adminEvents.poll(), OperationType.DELETE, AdminEventPaths.clientInitialAccessPath(lastId), ResourceType.CLIENT_INITIAL_ACCESS_MODEL);

        list = resource.list();
        assertEquals(2, list.size());
        assertEquals(5, list.get(0).getCount() + list.get(1).getCount());
    }

    @Test
    public void testInvalidParametersWhileCreatingInitialAccessTokens() {
        // Set Count as -1
        ClientInitialAccessCreatePresentation rep = new ClientInitialAccessCreatePresentation();
        rep.setCount(-1);
        rep.setExpiration(100);
        try {
            resource.create(rep);
            Assertions.fail("Invalid value for count");
        } catch (BadRequestException e) {
            OAuth2ErrorRepresentation error = e.getResponse().readEntity(OAuth2ErrorRepresentation.class);
            Assertions.assertEquals("Invalid value for count", error.getError());
            Assertions.assertEquals("The count cannot be less than 0", error.getErrorDescription());
        }

        // Set Expiration as -10
        rep = new ClientInitialAccessCreatePresentation();
        rep.setCount(100);
        rep.setExpiration(-10);
        try {
            resource.create(rep);
            Assertions.fail("Invalid value for expiration");
        } catch (BadRequestException e) {
            OAuth2ErrorRepresentation error = e.getResponse().readEntity(OAuth2ErrorRepresentation.class);
            Assertions.assertEquals("Invalid value for expiration", error.getError());
            Assertions.assertEquals("The expiration time interval cannot be less than 0", error.getErrorDescription());
        }
    }

    private void removeExpired(String realmUuid) {
        runOnServer.run(session -> {
            RealmModel realm = session.realms().getRealm(realmUuid);

            session.getProvider(UserSessionPersisterProvider.class).removeExpired(realm);
            session.realms().removeExpiredClientInitialAccess();
        });
    }

    @Test
    public void testPeriodicExpiration() {
        ClientInitialAccessPresentation response1 = resource.create(new ClientInitialAccessCreatePresentation(1, 1));
        ClientInitialAccessPresentation response2 = resource.create(new ClientInitialAccessCreatePresentation(1000, 1));
        ClientInitialAccessPresentation response3 = resource.create(new ClientInitialAccessCreatePresentation(1000, 0));
        ClientInitialAccessPresentation response4 = resource.create(new ClientInitialAccessCreatePresentation(0, 1));

        List<ClientInitialAccessPresentation> list = resource.list();
        assertEquals(4, list.size());

        timeOffSet.set(10);

        final String realmUuid = managedRealm.getId();
        removeExpired(realmUuid);

        list = resource.list();
        assertEquals(2, list.size());

        List<String> remainingIds = list.stream()
                .map(ClientInitialAccessPresentation::getId)
                .collect(Collectors.toList());

        Assert.assertNames(remainingIds, response2.getId(), response4.getId());

        timeOffSet.set(2000);

        removeExpired(realmUuid);

        list = resource.list();
        assertEquals(1, list.size());
        Assertions.assertEquals(list.get(0).getId(), response4.getId());

        // Cleanup
        managedRealm.admin().clientInitialAccess().delete(response4.getId());
    }

}
