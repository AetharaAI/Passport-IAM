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
package org.passport.services.resources.admin.fgap;

import org.passport.common.Profile;
import org.passport.models.ClientModel;
import org.passport.models.GroupModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RoleContainerModel;
import org.passport.models.RoleModel;
import org.passport.models.UserModel;
import org.passport.provider.ProviderEventManager;
import org.passport.services.resources.admin.AdminAuth;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class AdminPermissions {


    public static AdminPermissionEvaluator evaluator(PassportSession session, RealmModel realm, AdminAuth auth) {
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ_V2)) {
            return new MgmtPermissionsV2(session, realm, auth);
        }
        return new MgmtPermissions(session, realm, auth);
    }
    public static AdminPermissionEvaluator evaluator(PassportSession session, RealmModel realm, RealmModel adminsRealm, UserModel admin) {
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ_V2)) {
            return new MgmtPermissionsV2(session, adminsRealm, admin);
        }
        return new MgmtPermissions(session, realm, adminsRealm, admin);
    }

    public static RealmsPermissionEvaluator realms(PassportSession session, AdminAuth auth) {
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ_V2)) {
            return new MgmtPermissionsV2(session, auth);
        }
        return new MgmtPermissions(session, auth);
    }

    public static AdminPermissionManagement management(PassportSession session, RealmModel realm) {
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ_V2)) {
             return new MgmtPermissionsV2(session, realm);
        }
        return new MgmtPermissions(session, realm);
    }

    public static void registerListener(ProviderEventManager manager) {
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_FINE_GRAINED_AUTHZ)) {
            manager.register(event -> {
                if (event instanceof RoleContainerModel.RoleRemovedEvent) {
                    RoleContainerModel.RoleRemovedEvent cast = (RoleContainerModel.RoleRemovedEvent) event;
                    RoleModel role = cast.getRole();
                    RealmModel realm;
                    if (role.getContainer() instanceof ClientModel) {
                        realm = ((ClientModel) role.getContainer()).getRealm();

                    } else {
                        realm = (RealmModel) role.getContainer();
                    }
                    management(cast.getPassportSession(), realm).roles().setPermissionsEnabled(role, false);
                } else if (event instanceof ClientModel.ClientRemovedEvent) {
                    ClientModel.ClientRemovedEvent cast = (ClientModel.ClientRemovedEvent) event;
                    management(cast.getPassportSession(), cast.getClient().getRealm()).clients().setPermissionsEnabled(cast.getClient(), false);
                } else if (event instanceof GroupModel.GroupRemovedEvent) {
                    GroupModel.GroupRemovedEvent cast = (GroupModel.GroupRemovedEvent) event;
                    management(cast.getPassportSession(), cast.getRealm()).groups().setPermissionsEnabled(cast.getGroup(), false);
                }
            });
        }
    }


}
