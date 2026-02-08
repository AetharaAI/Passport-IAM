/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
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
package org.passport.testsuite.model.parameters;

import java.util.Set;

import org.passport.authorization.jpa.store.JPAAuthorizationStoreFactory;
import org.passport.broker.provider.IdentityProviderFactory;
import org.passport.broker.provider.IdentityProviderSpi;
import org.passport.connections.jpa.DefaultJpaConnectionProviderFactory;
import org.passport.connections.jpa.JpaConnectionSpi;
import org.passport.connections.jpa.updater.JpaUpdaterProviderFactory;
import org.passport.connections.jpa.updater.JpaUpdaterSpi;
import org.passport.connections.jpa.updater.liquibase.conn.LiquibaseConnectionProviderFactory;
import org.passport.connections.jpa.updater.liquibase.conn.LiquibaseConnectionSpi;
import org.passport.connections.jpa.updater.liquibase.lock.LiquibaseDBLockProviderFactory;
import org.passport.events.jpa.JpaEventStoreProviderFactory;
import org.passport.migration.MigrationProviderFactory;
import org.passport.migration.MigrationSpi;
import org.passport.models.IdentityProviderStorageSpi;
import org.passport.models.dblock.DBLockSpi;
import org.passport.models.jpa.JpaClientProviderFactory;
import org.passport.models.jpa.JpaClientScopeProviderFactory;
import org.passport.models.jpa.JpaGroupProviderFactory;
import org.passport.models.jpa.JpaIdentityProviderStorageProviderFactory;
import org.passport.models.jpa.JpaRealmProviderFactory;
import org.passport.models.jpa.JpaRoleProviderFactory;
import org.passport.models.jpa.JpaUserProviderFactory;
import org.passport.models.jpa.session.JpaRevokedTokensPersisterProviderFactory;
import org.passport.models.jpa.session.JpaUserSessionPersisterProviderFactory;
import org.passport.models.session.RevokedTokenPersisterSpi;
import org.passport.models.session.UserSessionPersisterSpi;
import org.passport.organization.OrganizationSpi;
import org.passport.organization.jpa.JpaOrganizationProviderFactory;
import org.passport.protocol.LoginProtocolFactory;
import org.passport.protocol.LoginProtocolSpi;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;
import org.passport.storage.DatastoreSpi;
import org.passport.storage.datastore.DefaultDatastoreProviderFactory;
import org.passport.testsuite.model.Config;
import org.passport.testsuite.model.PassportModelParameters;

import com.google.common.collect.ImmutableSet;

/**
 *
 * @author hmlnarik
 */
public class Jpa extends PassportModelParameters {

    static final Set<Class<? extends Spi>> ALLOWED_SPIS = ImmutableSet.<Class<? extends Spi>>builder()
      // jpa-specific
      .add(JpaConnectionSpi.class)
      .add(JpaUpdaterSpi.class)
      .add(LiquibaseConnectionSpi.class)
      .add(UserSessionPersisterSpi.class)
      .add(RevokedTokenPersisterSpi.class)

      .add(DatastoreSpi.class)

      //required for migrateModel
      .add(MigrationSpi.class)
      .add(LoginProtocolSpi.class)

      .add(DBLockSpi.class)

      //required for FederatedIdentityModel
      .add(IdentityProviderStorageSpi.class)
      .add(IdentityProviderSpi.class)

      .add(OrganizationSpi.class)

      .build();

    static final Set<Class<? extends ProviderFactory>> ALLOWED_FACTORIES = ImmutableSet.<Class<? extends ProviderFactory>>builder()
      // jpa-specific
      .add(DefaultDatastoreProviderFactory.class)

      .add(DefaultJpaConnectionProviderFactory.class)
      .add(JPAAuthorizationStoreFactory.class)
      .add(JpaClientProviderFactory.class)
      .add(JpaClientScopeProviderFactory.class)
      .add(JpaEventStoreProviderFactory.class)
      .add(JpaGroupProviderFactory.class)
      .add(JpaIdentityProviderStorageProviderFactory.class)
      .add(JpaRealmProviderFactory.class)
      .add(JpaRoleProviderFactory.class)
      .add(JpaUpdaterProviderFactory.class)
      .add(JpaUserProviderFactory.class)
      .add(LiquibaseConnectionProviderFactory.class)
      .add(LiquibaseDBLockProviderFactory.class)
      .add(JpaUserSessionPersisterProviderFactory.class)
      .add(JpaRevokedTokensPersisterProviderFactory.class)

      //required for migrateModel
      .add(MigrationProviderFactory.class)
      .add(LoginProtocolFactory.class)

      //required for FederatedIdentityModel
      .add(IdentityProviderFactory.class)

      .add(JpaOrganizationProviderFactory.class)

      .build();

    public Jpa() {
        super(ALLOWED_SPIS, ALLOWED_FACTORIES);
    }


    @Override
    public void updateConfig(Config cf) {
        updateConfigForJpa(cf);
    }

    public static void updateConfigForJpa(Config cf) {
        cf.spi("client").defaultProvider("jpa")
          .spi("clientScope").defaultProvider("jpa")
          .spi("group").defaultProvider("jpa")
          .spi("idp").defaultProvider("jpa")
          .spi("role").defaultProvider("jpa")
          .spi("user").defaultProvider("jpa")
          .spi("realm").defaultProvider("jpa")
          .spi("deploymentState").defaultProvider("jpa")
          .spi("dblock").defaultProvider("jpa")
        ;
// Use this for running model tests with Postgres database
//        cf.spi("connectionsJpa")
//                .provider("default")
//                .config("url", "jdbc:postgresql://localhost:5432/passportDB")
//                .config("user", "passport")
//                .config("password", "pass")
//                .config("driver", "org.postgresql.Driver");
//
    }
}
