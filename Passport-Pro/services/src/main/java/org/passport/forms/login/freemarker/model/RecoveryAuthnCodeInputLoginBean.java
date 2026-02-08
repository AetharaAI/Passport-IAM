package org.passport.forms.login.freemarker.model;

import java.util.Optional;

import org.passport.credential.CredentialModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.credential.RecoveryAuthnCodesCredentialModel;
import org.passport.models.utils.RecoveryAuthnCodesUtils;

public class RecoveryAuthnCodeInputLoginBean {

    private final int codeNumber;

    public RecoveryAuthnCodeInputLoginBean(PassportSession session, RealmModel realm, UserModel user) {
        Optional<CredentialModel> credentialModelOpt = RecoveryAuthnCodesUtils.getCredential(user);

        RecoveryAuthnCodesCredentialModel recoveryCodeCredentialModel = RecoveryAuthnCodesCredentialModel.createFromCredentialModel(credentialModelOpt.get());

        this.codeNumber = recoveryCodeCredentialModel.getNextRecoveryAuthnCode().get().getNumber();
    }

    public int getCodeNumber() {
        return this.codeNumber;
    }

}
