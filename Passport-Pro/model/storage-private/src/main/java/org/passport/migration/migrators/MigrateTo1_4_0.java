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

package org.passport.migration.migrators;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.passport.component.ComponentModel;
import org.passport.migration.ModelVersion;
import org.passport.models.ImpersonationConstants;
import org.passport.models.PassportSession;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.models.StorageProviderRealmModel;
import org.passport.models.UserModel;
import org.passport.models.cache.UserCache;
import org.passport.models.utils.DefaultAuthenticationFlows;
import org.passport.models.utils.DefaultRequiredActions;
import org.passport.models.utils.PassportModelUtils;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.storage.UserStoragePrivateUtil;
import org.passport.storage.UserStorageUtil;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class MigrateTo1_4_0 implements Migration {
    public static final ModelVersion VERSION = new ModelVersion("1.4.0");
    public ModelVersion getVersion() {
        return VERSION;
    }

    public void migrate(PassportSession session) {
        session.realms().getRealmsStream().forEach(realm -> migrateRealm(session, realm));
    }

    protected void migrateRealm(PassportSession session, RealmModel realm) {
        if (realm.getAuthenticationFlowsStream().count() == 0) {
            DefaultAuthenticationFlows.migrateFlows(realm);
            DefaultRequiredActions.addActions(realm);
        }
        ImpersonationConstants.setupImpersonationService(session, realm);

        migrateLDAPMappers(session, realm);
        migrateUsers(session, realm);
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(session, realm);

    }

    private void migrateLDAPMappers(PassportSession session, RealmModel realm) {
        List<String> mandatoryInLdap = Arrays.asList("username", "username-cn", "first name", "last name");
        ((StorageProviderRealmModel) realm).getUserStorageProvidersStream()
                .filter(providerModel -> Objects.equals(providerModel.getProviderId(), LDAPConstants.LDAP_PROVIDER))
                .forEachOrdered(providerModel -> realm.getComponentsStream(providerModel.getId())
                        .filter(mapper -> mandatoryInLdap.contains(mapper.getName()))
                        .forEach(mapper -> {
                            mapper = new ComponentModel(mapper);  // don't want to modify cache
                            mapper.getConfig().putSingle("is.mandatory.in.ldap", "true");
                            realm.updateComponent(mapper);
                        }));
    }

    private void migrateUsers(PassportSession session, RealmModel realm) {
        Map<String, String> searchAttributes = new HashMap<>(1);
        searchAttributes.put(UserModel.INCLUDE_SERVICE_ACCOUNT, Boolean.FALSE.toString());

        UserStoragePrivateUtil.userLocalStorage(session).searchForUserStream(realm, searchAttributes)
                .forEach(user -> {
                    String email = PassportModelUtils.toLowerCaseSafe(user.getEmail());
                    if (email != null && !email.equals(user.getEmail())) {
                        user.setEmail(email);
                        UserCache userCache = UserStorageUtil.userCache(session);
                        if (userCache != null) {
                            userCache.evict(realm, user);
                        }
                    }
                });
    }
}
