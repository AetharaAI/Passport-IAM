package org.passport.services.resources.account;

import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.credential.PasswordCredentialModel;

public class PasswordUtil {

    private final UserModel user;

    @Deprecated
    public PasswordUtil(PassportSession session, UserModel user) {
        this.user = user;
    }

    public PasswordUtil(UserModel user) {
        this.user = user;
    }

    /**
     * @deprecated Instead, use {@link #isConfigured()}
     */
    @Deprecated
    public boolean isConfigured(PassportSession session, RealmModel realm, UserModel user) {
        return user.credentialManager().isConfiguredFor(PasswordCredentialModel.TYPE);
    }

    public boolean isConfigured() {
        return user.credentialManager().isConfiguredFor(PasswordCredentialModel.TYPE);
    }

    public void update() {

    }

}
