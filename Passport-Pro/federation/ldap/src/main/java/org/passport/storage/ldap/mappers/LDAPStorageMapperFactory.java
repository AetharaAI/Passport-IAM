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
package org.passport.storage.ldap.mappers;

import java.util.Collections;
import java.util.List;

import org.passport.Config;
import org.passport.component.ComponentModel;
import org.passport.component.ComponentValidationException;
import org.passport.component.SubComponentFactory;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.provider.ProviderConfigProperty;
import org.passport.storage.UserStorageProviderModel;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public interface LDAPStorageMapperFactory<T extends LDAPStorageMapper> extends SubComponentFactory<T, LDAPStorageMapper> {
    /**
     * called per Passport transaction.
     *
     * @param session
     * @param model
     * @return
     */
    T create(PassportSession session, ComponentModel model);

    /**
     * This is the name of the provider and will be showed in the admin console as an option.
     *
     * @return
     */
    @Override
    String getId();

    @Override
    default void init(Config.Scope config) {

    }

    @Override
    default void postInit(PassportSessionFactory factory) {

    }

    @Override
    default void close() {

    }

    @Override
    default String getHelpText() {
        return "";
    }

    @Override
    default List<ProviderConfigProperty> getConfigProperties() {
        return Collections.EMPTY_LIST;
    }

    @Override
    default void validateConfiguration(PassportSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {

    }

    default void onParentUpdate(RealmModel realm, UserStorageProviderModel oldParent, UserStorageProviderModel newParent, ComponentModel mapperModel) {

    }

    /**
     * Called when UserStorageProviderModel is created.  This allows you to do initialization of any additional configuration
     * you need to add.  For example, you may be introspecting a database or ldap schema to automatically create mappings.
     *
     * @param session
     * @param realm
     * @param model
     */
    @Override
    default void onCreate(PassportSession session, RealmModel realm, ComponentModel model) {

    }
}
