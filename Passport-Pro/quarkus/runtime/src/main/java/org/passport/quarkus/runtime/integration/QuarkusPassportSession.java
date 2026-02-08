/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.passport.quarkus.runtime.integration;

import org.passport.models.PassportSession;
import org.passport.quarkus.runtime.integration.resteasy.QuarkusPassportContext;
import org.passport.services.DefaultPassportContext;
import org.passport.services.DefaultPassportSession;
import org.passport.services.DefaultPassportSessionFactory;

public final class QuarkusPassportSession extends DefaultPassportSession {

    public QuarkusPassportSession(DefaultPassportSessionFactory factory) {
        super(factory);
    }

    @Override
    protected DefaultPassportContext createPassportContext(PassportSession session) {
        return new QuarkusPassportContext(session);
    }
}
