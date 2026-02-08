/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.passport.migration.migrators;

import org.passport.migration.ModelVersion;
import org.passport.models.AuthenticationFlowModel;
import org.passport.models.PassportSession;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.models.utils.DefaultAuthenticationFlows;
import org.passport.models.utils.DefaultKeyProviders;
import org.passport.representations.userprofile.config.UPConfig;
import org.passport.representations.userprofile.config.UPConfig.UnmanagedAttributePolicy;
import org.passport.storage.UserStorageProvider;
import org.passport.userprofile.UserProfileProvider;

import org.jboss.logging.Logger;

public class MigrateTo24_0_0 extends RealmMigration {

    private static final Logger LOG = Logger.getLogger(MigrateTo24_0_0.class);
    public static final ModelVersion VERSION = new ModelVersion("24.0.0");
    public static final String REALM_USER_PROFILE_ENABLED = "userProfileEnabled";


    @Override
    public ModelVersion getVersion() {
        return VERSION;
    }

    @Override
    public void migrateRealm(PassportSession session, RealmModel realm) {
        updateUserProfileSettings(session);
        updateLdapProviderConfig(session);
        createHS512ComponentModelKey(session);
        bindFirstBrokerLoginFlow(session);
    }

    private void updateUserProfileSettings(PassportSession session) {
        RealmModel realm = session.getContext().getRealm();
        boolean isUserProfileEnabled = Boolean.parseBoolean(realm.getAttribute(REALM_USER_PROFILE_ENABLED));

        // Remove attribute as user profile is always enabled from this version
        realm.removeAttribute(REALM_USER_PROFILE_ENABLED);

        if (isUserProfileEnabled) {
            // existing realms with user profile enabled does not need any addition migration step
            LOG.debugf("Skipping migration for realm %s. The declarative user profile is already enabled.", realm.getName());
            return;
        }

        // for backward compatibility in terms of behavior, we enable unmanaged attributes for existing realms
        // that don't have the declarative user profile enabled
        UserProfileProvider provider = session.getProvider(UserProfileProvider.class);
        UPConfig upConfig = provider.getConfiguration();
        upConfig.setUnmanagedAttributePolicy(UnmanagedAttributePolicy.ENABLED);
        provider.setConfiguration(upConfig);

        LOG.debugf("Enabled the declarative user profile to realm %s with support for unmanaged attributes", realm.getName());
    }

    private void updateLdapProviderConfig(final PassportSession session) {
        RealmModel realm = session.getContext().getRealm();
        // ensure `ldapsOnly` value for `useTruststoreSpi` in LDAP providers is migrated to `always`.
        realm.getComponentsStream(realm.getId(), UserStorageProvider.class.getName())
                .filter(c -> LDAPConstants.USE_TRUSTSTORE_LDAPS_ONLY.equals(c.getConfig().getFirst(LDAPConstants.USE_TRUSTSTORE_SPI)))
                .forEach(c -> {
                    c.getConfig().putSingle(LDAPConstants.USE_TRUSTSTORE_SPI, LDAPConstants.USE_TRUSTSTORE_ALWAYS);
                    realm.updateComponent(c);
                });
    }

    private void createHS512ComponentModelKey(PassportSession session) {
        RealmModel realm = session.getContext().getRealm();
        DefaultKeyProviders.createSecretProvider(realm);
    }

    private void bindFirstBrokerLoginFlow(PassportSession session) {
        RealmModel realm = session.getContext().getRealm();
        String flowAlias = DefaultAuthenticationFlows.FIRST_BROKER_LOGIN_FLOW;
        AuthenticationFlowModel flow = realm.getFlowByAlias(flowAlias);
        if (flow == null) {
           LOG.debugf("No flow found for alias '%s'. Skipping.", flowAlias);
           return;
        }
        realm.setFirstBrokerLoginFlow(flow);
        LOG.debugf("Flow '%s' has been bound to realm %s as 'First broker login' flow", flow.getId(), realm.getName());
    }
}
