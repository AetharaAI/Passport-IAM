/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates
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

package org.passport.testsuite.vault;

import java.util.List;
import java.util.Optional;

import org.passport.models.PassportSession;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.AbstractPassportTest;
import org.passport.testsuite.runonserver.RunOnServer;
import org.passport.testsuite.utils.io.IOUtil;
import org.passport.vault.VaultStringSecret;
import org.passport.vault.VaultTranscriber;

import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

/**
 * Tests the usage of the {@link VaultTranscriber} on the server side. The tests attempt to obtain the transcriber from
 * the session and then use it to obtain secrets from the configured provider.
 *
 * @author <a href="mailto:sguilhen@redhat.com">Stefan Guilhen</a>
 */

public abstract class AbstractPassportVaultTest extends AbstractPassportTest {

    @Override
    public void addTestRealms(List<RealmRepresentation> testRealms) {
        testRealms.add(IOUtil.loadRealm("/testrealm.json"));
    }

    static class PassportVaultServerTest implements RunOnServer {

        private String testKey;
        private String expectedSecret;

        public PassportVaultServerTest(final String key, final String expectedSecret) {
            this.testKey = key;
            this.expectedSecret = expectedSecret;
        }

        @Override
        public void run(PassportSession session) {
            VaultTranscriber transcriber = getVaultTranscriber(session);
            // obtain an existing secret from the vault.
            Optional<String> optional = getSecret(transcriber, testKey);
            Assert.assertTrue(optional.isPresent());
            Assert.assertEquals(expectedSecret, optional.get());

            // try obtaining a secret using a key that does not exist in the vault.
            optional = getSecret(transcriber, "${vault.invalid_entry}");
            Assert.assertFalse(optional.isPresent());

            // invoke the transcriber using a string that is not a vault expression.
            optional = getSecret(transcriber, "mysecret");
            Assert.assertTrue(optional.isPresent());
            Assert.assertEquals("mysecret", optional.get());
        }

        private Optional<String> getSecret(VaultTranscriber transcriber, String testKey) {
            VaultStringSecret secret = transcriber.getStringSecret(testKey);
            return secret.get();
        }
    }

    @NotNull
    private static VaultTranscriber getVaultTranscriber(PassportSession session) throws RuntimeException {
        return session.vault();
    }
}