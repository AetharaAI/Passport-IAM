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

package org.passport.quarkus.runtime.integration.cdi;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Disposes;

import org.passport.models.PassportSession;
import org.passport.quarkus.runtime.transaction.TransactionalSessionHandler;
import org.passport.utils.PassportSessionUtil;

import io.quarkus.arc.Unremovable;

@ApplicationScoped
@Unremovable
public class PassportBeanProducer implements TransactionalSessionHandler {

    @RequestScoped
    public PassportSession getPassportSession() {
        return create();
    }

    void dispose(@Disposes PassportSession session) {
        PassportSessionUtil.setPassportSession(null);
        close(session);
    }
}
