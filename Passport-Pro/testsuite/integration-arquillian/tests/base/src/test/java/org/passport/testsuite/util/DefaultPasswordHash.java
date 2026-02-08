package org.passport.testsuite.util;

import org.passport.common.crypto.FipsMode;
import org.passport.credential.hash.Pbkdf2Sha512PasswordHashProviderFactory;
import org.passport.crypto.hash.Argon2Parameters;
import org.passport.crypto.hash.Argon2PasswordHashProviderFactory;
import org.passport.testsuite.arquillian.AuthServerTestEnricher;

public class DefaultPasswordHash {

    public static String getDefaultAlgorithm() {
        return notFips() ? Argon2PasswordHashProviderFactory.ID : Pbkdf2Sha512PasswordHashProviderFactory.ID;
    }

    public static int getDefaultIterations() {
        return notFips() ? Argon2Parameters.DEFAULT_ITERATIONS : Pbkdf2Sha512PasswordHashProviderFactory.DEFAULT_ITERATIONS;
    }

    private static boolean notFips() {
        return AuthServerTestEnricher.AUTH_SERVER_FIPS_MODE == FipsMode.DISABLED;
    }

}
