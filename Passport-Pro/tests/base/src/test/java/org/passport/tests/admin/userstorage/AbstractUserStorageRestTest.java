package org.passport.tests.admin.userstorage;

import jakarta.ws.rs.core.Response;

import org.passport.common.util.MultivaluedHashMap;
import org.passport.models.LDAPConstants;
import org.passport.representations.idm.ComponentRepresentation;
import org.passport.storage.UserStorageProvider;
import org.passport.testframework.annotations.InjectAdminEvents;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.events.AdminEvents;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.util.ApiUtil;

import org.junit.jupiter.api.Assertions;

public class AbstractUserStorageRestTest {

    @InjectRealm
    ManagedRealm managedRealm;

    @InjectAdminEvents
    AdminEvents adminEvents;

    protected String createComponent(ComponentRepresentation rep) {
        Response resp = managedRealm.admin().components().add(rep);
        Assertions.assertEquals(201, resp.getStatus());
        resp.close();
        String id = ApiUtil.getCreatedId(resp);

        adminEvents.clear();
        return id;
    }

    protected void removeComponent(String id) {
        managedRealm.admin().components().component(id).remove();
        adminEvents.clear();
    }

    protected ComponentRepresentation createBasicLDAPProviderRep() {
        ComponentRepresentation ldapRep = new ComponentRepresentation();
        ldapRep.setName("ldap2");
        ldapRep.setProviderId("ldap");
        ldapRep.setProviderType(UserStorageProvider.class.getName());
        ldapRep.setConfig(new MultivaluedHashMap<>());
        ldapRep.getConfig().putSingle("priority", Integer.toString(2));
        ldapRep.getConfig().putSingle(LDAPConstants.EDIT_MODE, UserStorageProvider.EditMode.WRITABLE.name());
        return ldapRep;
    }
}
