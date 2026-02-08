package org.passport.broker.spiffe;

import org.passport.crypto.PublicKeysWrapper;
import org.passport.http.simple.SimpleHttp;
import org.passport.jose.jwk.JWK;
import org.passport.keys.PublicKeyLoader;
import org.passport.models.PassportSession;
import org.passport.util.JWKSUtils;

public class SpiffeBundleEndpointLoader implements PublicKeyLoader {

    private final PassportSession session;
    private final String bundleEndpoint;

    public SpiffeBundleEndpointLoader(PassportSession session, String bundleEndpoint) {
        this.session = session;
        this.bundleEndpoint = bundleEndpoint;
    }

    @Override
    public PublicKeysWrapper loadKeys() throws Exception {
        SpiffeJSONWebKeySet jwks = SimpleHttp.create(session).doGet(bundleEndpoint).asJson(SpiffeJSONWebKeySet.class);
        PublicKeysWrapper keysWrapper = JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.JWT_SVID, true);
        if (keysWrapper.getKeys().isEmpty()) {
            keysWrapper = JWKSUtils.getKeyWrappersForUse(jwks, JWK.Use.SIG, true);
        }
        return jwks.getSpiffeRefreshHint() == null ? keysWrapper : new PublicKeysWrapper(keysWrapper.getKeys(), jwks.getSpiffeRefreshHint());
    }

}
