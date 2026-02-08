package org.passport.authentication.otp;

import org.passport.models.PassportSession;
import org.passport.models.OTPPolicy;

public class FreeOTPProvider implements OTPApplicationProviderFactory, OTPApplicationProvider {

    @Override
    public OTPApplicationProvider create(PassportSession session) {
        return this;
    }

    @Override
    public String getId() {
        return "freeotp";
    }

    @Override
    public String getName() {
        return "totpAppFreeOTPName";
    }

    @Override
    public boolean supports(OTPPolicy policy) {
        return true;
    }

    @Override
    public void close() {
    }

}
