/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.services.securityprofile;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.passport.common.Profile;
import org.passport.common.crypto.CryptoIntegration;
import org.passport.common.crypto.CryptoProvider;
import org.passport.models.PassportSession;
import org.passport.securityprofile.SecurityProfileProvider;
import org.passport.securityprofile.SecurityProfileProviderFactory;
import org.passport.services.resteasy.ResteasyPassportSession;
import org.passport.services.resteasy.ResteasyPassportSessionFactory;
import org.passport.utils.ScopeUtil;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author rmartinc
 */
@RunWith(Parameterized.class)
public class DefaultSecurityProfileProverFactoryTest {

    private static PassportSession session;
    private final String name;

    @Parameters
    public static Collection<Object[]> data() {
        // return of json profile files packed with passport
        return Arrays.asList(new Object[][]{
            {"none-security-profile"},
            {"lax-security-profile"},
            {"strict-security-profile"},
        });
    }

    public DefaultSecurityProfileProverFactoryTest(String name) {
        this.name = name;
    }

    @BeforeClass
    public static void beforeClass() {
        Profile.defaults();
        CryptoIntegration.init(CryptoProvider.class.getClassLoader());
        ResteasyPassportSessionFactory sessionFactory = new ResteasyPassportSessionFactory();
        sessionFactory.init();
        session = new ResteasyPassportSession(sessionFactory);
    }

    @Test
    public void testConfigurationFile() {
        SecurityProfileProviderFactory fact = new DefaultSecurityProfileProviderFactory();
        fact.init(ScopeUtil.createScope(Collections.singletonMap("name", name)));
        SecurityProfileProvider prov = fact.create(session);
        Assert.assertNotNull(prov.getName());
        Assert.assertNotNull(prov.getDefaultClientProfiles());
        Assert.assertNotNull(prov.getDefaultClientPolicies());
    }
}
