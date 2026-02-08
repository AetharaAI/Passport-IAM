package org.passport.broker.provider;

import org.passport.models.IdentityProviderMapperModel;
import org.passport.models.IdentityProviderMapperSyncMode;
import org.passport.models.IdentityProviderSyncMode;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;

import org.jboss.logging.Logger;

public final class IdentityProviderMapperSyncModeDelegate {

    protected static final Logger logger = Logger.getLogger(IdentityProviderMapperSyncModeDelegate.class);

    public static void delegateUpdateBrokeredUser(PassportSession session, RealmModel realm, UserModel user, IdentityProviderMapperModel mapperModel, BrokeredIdentityContext context, IdentityProviderMapper mapper) {
        IdentityProviderSyncMode idpSyncMode = context.getIdpConfig().getSyncMode();
        if (idpSyncMode == null) {
            idpSyncMode = IdentityProviderSyncMode.LEGACY;
        }
        IdentityProviderSyncMode effectiveSyncMode = combineIdpAndMapperSyncMode(idpSyncMode, mapperModel.getSyncMode());

        if (!mapper.supportsSyncMode(effectiveSyncMode)) {
            logger.warnf("The mapper %s does not explicitly support sync mode %s. Please ensure that the SPI supports the sync mode correctly and update it to reflect this.", mapper.getDisplayType(), effectiveSyncMode);
        }

        if (effectiveSyncMode == IdentityProviderSyncMode.LEGACY) {
            mapper.updateBrokeredUserLegacy(session, realm, user, mapperModel, context);
        } else if (effectiveSyncMode == IdentityProviderSyncMode.FORCE) {
            mapper.updateBrokeredUser(session, realm, user, mapperModel, context);
        }
    }

    public static IdentityProviderSyncMode combineIdpAndMapperSyncMode(IdentityProviderSyncMode syncMode, IdentityProviderMapperSyncMode mapperSyncMode) {
        return IdentityProviderMapperSyncMode.INHERIT.equals(mapperSyncMode) ? syncMode : IdentityProviderSyncMode.valueOf(mapperSyncMode.toString());
    }
}
