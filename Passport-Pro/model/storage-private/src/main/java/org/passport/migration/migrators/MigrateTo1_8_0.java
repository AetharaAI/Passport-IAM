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

import java.util.Objects;

import org.passport.component.ComponentModel;
import org.passport.migration.ModelVersion;
import org.passport.models.PassportSession;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.models.StorageProviderRealmModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.storage.UserStorageProviderModel;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class MigrateTo1_8_0 implements Migration {

    public static final ModelVersion VERSION = new ModelVersion("1.8.0");

    public ModelVersion getVersion() {
        return VERSION;
    }


    public void migrate(PassportSession session) {
        session.realms().getRealmsStream().forEach(this::migrateRealm);
    }

    @Override
    public void migrateImport(PassportSession session, RealmModel realm, RealmRepresentation rep, boolean skipUserDependent) {
        migrateRealm(realm);
    }

    protected void migrateRealm(RealmModel realm) {
        ((StorageProviderRealmModel) realm).getUserStorageProvidersStream()
                .filter(fedProvider -> Objects.equals(fedProvider.getProviderId(), LDAPConstants.LDAP_PROVIDER))
                .filter(this::isActiveDirectory)
                .filter(fedProvider -> Objects.isNull(getMapperByName(realm, fedProvider, "MSAD account controls")))
                // Create mapper for MSAD account controls
                .map(fedProvider -> PassportModelUtils.createComponentModel("MSAD account controls",
                        fedProvider.getId(), LDAPConstants.MSAD_USER_ACCOUNT_CONTROL_MAPPER,
                        "org.passport.storage.ldap.mappers.LDAPStorageMapper"))
                .forEachOrdered(realm::addComponentModel);
    }

    public static ComponentModel getMapperByName(RealmModel realm, ComponentModel providerModel, String name) {
        return realm.getComponentsStream(providerModel.getId(), "org.passport.storage.ldap.mappers.LDAPStorageMapper")
                .filter(component -> Objects.equals(component.getName(), name))
                .findFirst()
                .orElse(null);
    }


    private boolean isActiveDirectory(UserStorageProviderModel provider) {
        String vendor = provider.getConfig().getFirst(LDAPConstants.VENDOR);
        return vendor != null && vendor.equals(LDAPConstants.VENDOR_ACTIVE_DIRECTORY);
    }
}
