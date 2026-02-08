package org.passport.broker.kubernetes;

import java.util.Objects;

import org.passport.broker.oidc.OIDCIdentityProviderConfig;
import org.passport.cache.AlternativeLookupProvider;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.util.Strings;
import org.passport.utils.PassportSessionUtil;

import static org.passport.common.util.UriUtils.checkUrl;

public class KubernetesIdentityProviderConfig extends IdentityProviderModel {

    public static final String ISSUER = OIDCIdentityProviderConfig.ISSUER;

    public KubernetesIdentityProviderConfig() {
    }

    public KubernetesIdentityProviderConfig(IdentityProviderModel model) {
        super(model);
    }

    public String getIssuer() {
        return getConfig().get(ISSUER);
    }

    public int getAllowedClockSkew() {
        String allowedClockSkew = getConfig().get(ALLOWED_CLOCK_SKEW);
        if (allowedClockSkew == null || allowedClockSkew.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(getConfig().get(ALLOWED_CLOCK_SKEW));
        } catch (NumberFormatException e) {
            // ignore it and use default
            return 0;
        }
    }

    @Override
    public Boolean isHideOnLogin() {
        return true;
    }

    @Override
    public void validate(RealmModel realm) {
        super.validate(realm);

        String issuer = getIssuer();
        if (Strings.isEmpty(issuer)) {
            throw new IllegalArgumentException(ISSUER + " is required");
        }
        checkUrl(realm.getSslRequired(), issuer, ISSUER);

        PassportSession session = PassportSessionUtil.getPassportSession();
        AlternativeLookupProvider lookupProvider = session.getProvider(AlternativeLookupProvider.class);
        IdentityProviderModel existingIdp = lookupProvider.lookupIdentityProviderFromIssuer(session, getIssuer());
        if (existingIdp != null && (getInternalId() == null || !Objects.equals(existingIdp.getInternalId(), getInternalId()))) {
            throw new IllegalArgumentException("Issuer URL already used for IDP '" + existingIdp.getAlias() + "'");
        }

    }
}
