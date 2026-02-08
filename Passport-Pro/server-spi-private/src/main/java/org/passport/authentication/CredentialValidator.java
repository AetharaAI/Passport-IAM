package org.passport.authentication;

import java.util.List;
import java.util.stream.Collectors;

import org.passport.credential.CredentialModel;
import org.passport.credential.CredentialProvider;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;

public interface CredentialValidator<T extends CredentialProvider> {
    T getCredentialProvider(PassportSession session);
    default List<CredentialModel> getCredentials(PassportSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().getStoredCredentialsByTypeStream(getCredentialProvider(session).getType())
                .collect(Collectors.toList());
    }
    default String getType(PassportSession session) {
        return getCredentialProvider(session).getType();
    }
}
