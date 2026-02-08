package org.passport.admin.ui.rest.model;

import org.passport.models.ClientModel;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;

public class RoleMapper {
    public static ClientRole convertToModel(RoleModel roleModel, RealmModel realm) {
        ClientModel clientModel = realm.getClientById(roleModel.getContainerId());
        if (clientModel==null) {
            throw new IllegalArgumentException("Could not find referenced client");
        }
        ClientRole clientRole = new ClientRole(roleModel.getId(), roleModel.getName(), roleModel.getDescription());
        clientRole.setClientId(clientModel.getId());
        clientRole.setClient(clientModel.getClientId());
        return clientRole;
    }
}
