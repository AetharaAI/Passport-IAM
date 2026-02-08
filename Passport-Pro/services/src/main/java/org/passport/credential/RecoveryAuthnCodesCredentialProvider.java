package org.passport.credential;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import org.passport.authentication.requiredactions.RecoveryAuthnCodesAction;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RequiredActionProviderModel;
import org.passport.models.UserModel;
import org.passport.models.credential.RecoveryAuthnCodesCredentialModel;
import org.passport.models.credential.dto.RecoveryAuthnCodeRepresentation;
import org.passport.models.credential.dto.RecoveryAuthnCodesCredentialData;
import org.passport.models.utils.RecoveryAuthnCodesUtils;
import org.passport.util.JsonSerialization;
import org.passport.utils.RequiredActionHelper;

import org.jboss.logging.Logger;

import static org.passport.models.credential.RecoveryAuthnCodesCredentialModel.RECOVERY_CODES_GENERATE_NEW_CODES;
import static org.passport.models.credential.RecoveryAuthnCodesCredentialModel.RECOVERY_CODES_NUMBER_REMAINING;
import static org.passport.models.credential.RecoveryAuthnCodesCredentialModel.RECOVERY_CODES_NUMBER_USED;

public class RecoveryAuthnCodesCredentialProvider
        implements CredentialProvider<RecoveryAuthnCodesCredentialModel>, CredentialInputValidator {

    private static final Logger logger = Logger.getLogger(RecoveryAuthnCodesCredentialProvider.class);

    private final PassportSession session;

    public RecoveryAuthnCodesCredentialProvider(PassportSession session) {
        this.session = session;
    }

    @Override
    public String getType() {
        return RecoveryAuthnCodesCredentialModel.TYPE;
    }

    @Override
    public CredentialModel createCredential(RealmModel realm, UserModel user,
            RecoveryAuthnCodesCredentialModel credentialModel) {

        user.credentialManager().getStoredCredentialsByTypeStream(getType()).findFirst()
                .ifPresent(model -> deleteCredential(realm, user, model.getId()));

        return user.credentialManager().createStoredCredential(credentialModel);
    }

    @Override
    public boolean deleteCredential(RealmModel realm, UserModel user, String credentialId) {
        return user.credentialManager().removeStoredCredentialById(credentialId);
    }

    @Override
    public RecoveryAuthnCodesCredentialModel getCredentialFromModel(CredentialModel model) {
        return RecoveryAuthnCodesCredentialModel.createFromCredentialModel(model);
    }

    @Override
    public CredentialTypeMetadata getCredentialTypeMetadata(CredentialTypeMetadataContext metadataContext) {
        CredentialTypeMetadata.CredentialTypeMetadataBuilder builder = CredentialTypeMetadata.builder().type(getType())
                .category(CredentialTypeMetadata.Category.TWO_FACTOR).displayName("recovery-authn-codes-display-name")
                .helpText("recovery-authn-codes-help-text").iconCssClass("kcAuthenticatorRecoveryAuthnCodesClass")
                .removeable(true);
        UserModel user = metadataContext.getUser();
        builder.createAction(UserModel.RequiredAction.CONFIGURE_RECOVERY_AUTHN_CODES.name());
        return builder.build(session);
    }

    @Override
    public CredentialMetadata getCredentialMetadata(RecoveryAuthnCodesCredentialModel credentialModel, CredentialTypeMetadata credentialTypeMetadata) {

        CredentialMetadata credentialMetadata = new CredentialMetadata();
        try {
            RecoveryAuthnCodesCredentialData credentialData = JsonSerialization.readValue(credentialModel.getCredentialData(), RecoveryAuthnCodesCredentialData.class);
            if (credentialData.getRemainingCodes() < getWarningThreshold()) {
                credentialMetadata.setWarningMessageTitle(RECOVERY_CODES_NUMBER_REMAINING, String.valueOf(credentialData.getRemainingCodes()));
                credentialMetadata.setWarningMessageDescription(RECOVERY_CODES_GENERATE_NEW_CODES);
            }

            int codesUsed = credentialData.getTotalCodes() - credentialData.getRemainingCodes();
            String codesUsedMessage = codesUsed + "/" + credentialData.getTotalCodes();
            credentialMetadata.setInfoMessage(RECOVERY_CODES_NUMBER_USED, codesUsedMessage);
        } catch (IOException e) {
            logger.warn("unable to deserialize model information, skipping messages", e);
        }
        credentialMetadata.setCredentialModel(credentialModel);

        return credentialMetadata;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return getType().equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return user.credentialManager().getStoredCredentialsByTypeStream(credentialType).anyMatch(Objects::nonNull);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        String rawInputRecoveryAuthnCode = credentialInput.getChallengeResponse();
        Optional<CredentialModel> credential = user.credentialManager().getStoredCredentialsByTypeStream(getType()).findFirst();
        if (credential.isPresent()) {
            RecoveryAuthnCodesCredentialModel credentialModel = RecoveryAuthnCodesCredentialModel
                    .createFromCredentialModel(credential.get());
            if (!credentialModel.allCodesUsed()) {
                Optional<RecoveryAuthnCodeRepresentation> nextRecoveryAuthnCode = credentialModel.getNextRecoveryAuthnCode();
                if (nextRecoveryAuthnCode.isPresent()) {
                    String nextRecoveryCode = nextRecoveryAuthnCode.get().getEncodedHashedValue();
                    if (RecoveryAuthnCodesUtils.verifyRecoveryCodeInput(rawInputRecoveryAuthnCode, nextRecoveryCode)) {
                        credentialModel.removeRecoveryAuthnCode();
                        user.credentialManager().updateStoredCredential(credentialModel);
                        return true;
                    }

                }
            }
        }
        return false;
    }

    protected int getWarningThreshold() {
        RealmModel realm = session.getContext().getRealm();
        RequiredActionProviderModel requiredAction = RequiredActionHelper.getRequiredActionByProviderId(realm, RecoveryAuthnCodesAction.PROVIDER_ID);
        if (requiredAction != null && requiredAction.getConfig().containsKey(RecoveryAuthnCodesAction.WARNING_THRESHOLD)) {
            return Integer.parseInt(requiredAction.getConfig().get(RecoveryAuthnCodesAction.WARNING_THRESHOLD));
        } else {
            return session.getContext().getRealm().getPasswordPolicy().getRecoveryCodesWarningThreshold();
        }
    }
}
