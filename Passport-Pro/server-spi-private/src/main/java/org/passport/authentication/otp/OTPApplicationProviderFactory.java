package org.passport.authentication.otp;

import org.passport.Config;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.ProviderFactory;

public interface OTPApplicationProviderFactory extends ProviderFactory<OTPApplicationProvider> {

    @Override
    default void init(Config.Scope config) {
    }

    @Override
    default void postInit(PassportSessionFactory factory) {
    }

}
