package org.passport.representations;

import org.passport.TokenCategory;

public class AuthorizationResponseToken extends JsonWebToken{

    @Override
    public TokenCategory getCategory() {
        return TokenCategory.AUTHORIZATION_RESPONSE;
    }
}
