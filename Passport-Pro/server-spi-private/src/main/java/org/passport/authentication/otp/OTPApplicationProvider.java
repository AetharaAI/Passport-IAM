package org.passport.authentication.otp;

import org.passport.models.OTPPolicy;
import org.passport.provider.Provider;

public interface OTPApplicationProvider extends Provider {

    String getName();

    boolean supports(OTPPolicy policy);

}
