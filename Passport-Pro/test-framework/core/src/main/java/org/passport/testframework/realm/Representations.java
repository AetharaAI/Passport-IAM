package org.passport.testframework.realm;

import org.passport.representations.idm.CredentialRepresentation;
import org.passport.representations.idm.GroupRepresentation;
import org.passport.representations.idm.RoleRepresentation;

class Representations {

    private Representations() {
    }

    static RoleRepresentation toRole(String roleName, boolean asClientRole) {
        RoleRepresentation role = new RoleRepresentation();
        role.setName(roleName);
        role.setClientRole(asClientRole);
        return role;
    }

    static GroupRepresentation toGroup(String groupName) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(groupName);
        return group;
    }

    static CredentialRepresentation toCredential(String type, String value) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(type);
        credential.setValue(value);
        return credential;
    }

}
