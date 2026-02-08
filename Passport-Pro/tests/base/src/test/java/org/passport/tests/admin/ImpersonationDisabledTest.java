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

import jakarta.ws.rs.ServerErrorException;
import jakarta.ws.rs.core.Response;

import org.passport.common.Profile;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.annotations.InjectUser;
import org.passport.testframework.annotations.PassportIntegrationTest;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.realm.ManagedUser;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:vramik@redhat.com">Vlastislav Ramik</a>
 */
@PassportIntegrationTest(config = ImpersonationDisabledTest.ServerConfig.class)
public class ImpersonationDisabledTest {

    @InjectRealm
    private ManagedRealm realm;

    @InjectUser
    private ManagedUser user;

    @Test
    public void testImpersonationDisabled() {
        
        try {
            user.admin().impersonate();
            Assertions.fail("Feature impersonation should be disabled.");
        } catch (ServerErrorException e) {
            Assertions.assertEquals(Response.Status.NOT_IMPLEMENTED.getStatusCode(), e.getResponse().getStatus());
        }
    }

    public static class ServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.featuresDisabled(Profile.Feature.IMPERSONATION);
        }

    }

}
