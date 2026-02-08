package org.passport.cache;

import java.util.Map;

import org.passport.models.ClientModel;
import org.passport.models.IdentityProviderModel;
import org.passport.models.PassportSession;
import org.passport.provider.Provider;

public interface AlternativeLookupProvider extends Provider {

    IdentityProviderModel lookupIdentityProviderFromIssuer(PassportSession session, String issuerUrl);

    ClientModel lookupClientFromClientAttributes(PassportSession session, Map<String, String> attributes);

}
