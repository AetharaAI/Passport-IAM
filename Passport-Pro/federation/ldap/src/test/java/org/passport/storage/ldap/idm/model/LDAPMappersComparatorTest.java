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

package org.passport.storage.ldap.idm.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.passport.common.util.MultivaluedHashMap;
import org.passport.component.ComponentModel;
import org.passport.models.LDAPConstants;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;
import org.passport.storage.ldap.LDAPConfig;
import org.passport.storage.ldap.mappers.FullNameLDAPStorageMapper;
import org.passport.storage.ldap.mappers.FullNameLDAPStorageMapperFactory;
import org.passport.storage.ldap.mappers.LDAPMappersComparator;
import org.passport.storage.ldap.mappers.LDAPStorageMapper;
import org.passport.storage.ldap.mappers.UserAttributeLDAPStorageMapper;
import org.passport.storage.ldap.mappers.UserAttributeLDAPStorageMapperFactory;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class LDAPMappersComparatorTest {



    @Test
    public void testCompareWithCNUsername() {
        MultivaluedHashMap<String, String> cfg = new MultivaluedHashMap<>();
        cfg.add(LDAPConstants.USERNAME_LDAP_ATTRIBUTE, LDAPConstants.CN);
        LDAPMappersComparator ldapMappersComparator = new LDAPMappersComparator(new LDAPConfig(cfg));

        List<ComponentModel> mappers = getMappers();

        Collections.sort(mappers, ldapMappersComparator.sortAsc());
        assertOrder(mappers, "username-cn", "sAMAccountName", "first name", "full name");

        Collections.sort(mappers, ldapMappersComparator.sortDesc());
        assertOrder(mappers, "full name", "first name", "sAMAccountName", "username-cn");
    }

    @Test
    public void testCompareWithSAMAccountNameUsername() {
        MultivaluedHashMap<String, String> cfg = new MultivaluedHashMap<>();
        cfg.add(LDAPConstants.USERNAME_LDAP_ATTRIBUTE, LDAPConstants.SAM_ACCOUNT_NAME);
        LDAPMappersComparator ldapMappersComparator = new LDAPMappersComparator(new LDAPConfig(cfg));

        List<ComponentModel> mappers = getMappers();

        Collections.sort(mappers, ldapMappersComparator.sortAsc());
        assertOrder(mappers, "sAMAccountName", "username-cn", "first name", "full name");

        Collections.sort(mappers, ldapMappersComparator.sortDesc());
        assertOrder(mappers, "full name", "first name", "username-cn", "sAMAccountName");
    }

    private void assertOrder(List<ComponentModel> result, String... names) {
        Assert.assertEquals(result.size(), names.length);
        for (int i=0 ; i<names.length ; i++) {
            Assert.assertEquals(names[i], result.get(i).getName());
        }
    }

    private List<ComponentModel> getMappers() {
        List<ComponentModel> result = new LinkedList<>();

        ComponentModel mapperModel = PassportModelUtils.createComponentModel("first name",  "fed-provider", UserAttributeLDAPStorageMapperFactory.PROVIDER_ID, LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.FIRST_NAME,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.GIVENNAME,
                UserAttributeLDAPStorageMapper.READ_ONLY, "true",
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, "true",
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
        mapperModel.setId("idd1");
        result.add(mapperModel);

        mapperModel = PassportModelUtils.createComponentModel("username-cn", "fed-provider", UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.USERNAME,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.CN,
                UserAttributeLDAPStorageMapper.READ_ONLY, "true",
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, "false",
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
        mapperModel.setId("idd2");
        result.add(mapperModel);

        mapperModel = PassportModelUtils.createComponentModel("full name", "fed-provider", FullNameLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                FullNameLDAPStorageMapper.LDAP_FULL_NAME_ATTRIBUTE, LDAPConstants.CN,
                UserAttributeLDAPStorageMapper.READ_ONLY, "true");
        mapperModel.setId("idd3");
        result.add(mapperModel);

        mapperModel = PassportModelUtils.createComponentModel("sAMAccountName", "fed-provider", UserAttributeLDAPStorageMapperFactory.PROVIDER_ID,LDAPStorageMapper.class.getName(),
                UserAttributeLDAPStorageMapper.USER_MODEL_ATTRIBUTE, UserModel.USERNAME,
                UserAttributeLDAPStorageMapper.LDAP_ATTRIBUTE, LDAPConstants.SAM_ACCOUNT_NAME,
                UserAttributeLDAPStorageMapper.READ_ONLY, "false",
                UserAttributeLDAPStorageMapper.ALWAYS_READ_VALUE_FROM_LDAP, "false",
                UserAttributeLDAPStorageMapper.IS_MANDATORY_IN_LDAP, "true");
        mapperModel.setId("idd4");
        result.add(mapperModel);

        return result;
    }
}
