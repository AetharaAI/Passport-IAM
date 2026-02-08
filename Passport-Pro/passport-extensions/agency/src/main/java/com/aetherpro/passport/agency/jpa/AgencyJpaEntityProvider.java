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

import java.util.Arrays;
import java.util.List;

import org.passport.connections.jpa.entityprovider.JpaEntityProvider;

/**
 * JPA Entity Provider for Agency/LBAC extension.
 * Registers JPA entities and Liquibase changelog with Passport.
 */
public class AgencyJpaEntityProvider implements JpaEntityProvider {

    @Override
    public List<Class<?>> getEntities() {
        return Arrays.asList(
            PrincipalEntity.class,
            DelegateEntity.class,
            MandateEntity.class,
            QualificationEntity.class,
            AgentPassportEntity.class,
            AgencyRealmConfigEntity.class
        );
    }

    @Override
    public String getChangelogLocation() {
        return "META-INF/passport-agency-changelog.xml";
    }
    
    @Override
    public void close() {
    }

    @Override
    public String getFactoryId() {
        return AgencyJpaEntityProviderFactory.ID;
    }
}
