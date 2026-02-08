/*
 * Copyright 2024 AetherPro Technologies
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

package com.aetherpro.passport.agency.jpa;

import org.passport.Config.Scope;
import org.passport.connections.jpa.entityprovider.JpaEntityProvider;
import org.passport.connections.jpa.entityprovider.JpaEntityProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;

/**
 * Factory for Agency JPA Entity Provider.
 * Registers JPA entities and database migrations with Passport.
 */
public class AgencyJpaEntityProviderFactory implements JpaEntityProviderFactory {

    public static final String ID = "passport-agency-entity-provider";
    
    @Override
    public JpaEntityProvider create(PassportSession session) {
        return new AgencyJpaEntityProvider();
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public void init(Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
