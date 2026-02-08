package org.passport.tests.utils.runonserver;

import java.util.List;
import java.util.stream.Collectors;

import org.passport.credential.CredentialModel;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.ModelToRepresentation;
import org.passport.representations.idm.ComponentRepresentation;
import org.passport.representations.idm.CredentialRepresentation;
import org.passport.representations.idm.RealmRepresentation;
import org.passport.testframework.remote.providers.runonserver.FetchOnServer;
import org.passport.testframework.remote.providers.runonserver.FetchOnServerWrapper;

/**
 * Created by st on 26.01.17.
 */
public class RunHelpers {

    public static FetchOnServerWrapper<RealmRepresentation> internalRealm() {
        return new FetchOnServerWrapper() {

            @Override
            public FetchOnServer getRunOnServer() {
                return (FetchOnServer) session -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm(), true);
            }

            @Override
            public Class<RealmRepresentation> getResultClass() {
                return RealmRepresentation.class;
            }

        };
    }

    public static FetchOnServerWrapper<ComponentRepresentation> internalComponent(String componentId) {
        return new FetchOnServerWrapper() {

            @Override
            public FetchOnServer getRunOnServer() {
                return (FetchOnServer) session -> ModelToRepresentation.toRepresentation(session, session.getContext().getRealm().getComponent(componentId), true);
            }

            @Override
            public Class<ComponentRepresentation> getResultClass() {
                return ComponentRepresentation.class;
            }

        };
    }

    public static FetchOnServerWrapper<CredentialModel> fetchCredentials(String username) {
        return new FetchOnServerWrapper() {

            @Override
            public FetchOnServer getRunOnServer() {
                return (FetchOnServer) session -> {
                    RealmModel realm = session.getContext().getRealm();
                    UserModel user = session.users().getUserByUsername(realm, username);
                    List<CredentialModel> storedCredentialsByType = user.credentialManager().getStoredCredentialsByTypeStream(CredentialRepresentation.PASSWORD)
                            .collect(Collectors.toList());
                    return storedCredentialsByType.get(0);
                };
            }

            @Override
            public Class getResultClass() {
                return CredentialModel.class;
            }
        };
    }

}
