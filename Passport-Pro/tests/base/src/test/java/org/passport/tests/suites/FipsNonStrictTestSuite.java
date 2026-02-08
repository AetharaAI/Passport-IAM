package org.passport.tests.suites;

import org.passport.common.Profile;
import org.passport.common.crypto.FipsMode;
import org.passport.common.util.KeystoreUtil;
import org.passport.testframework.https.CertificatesConfig;
import org.passport.testframework.https.CertificatesConfigBuilder;
import org.passport.testframework.injection.SuiteSupport;
import org.passport.testframework.server.PassportServerConfig;
import org.passport.testframework.server.PassportServerConfigBuilder;
import org.passport.tests.admin.ServerInfoTest;
import org.passport.tests.admin.client.CredentialsTest;
import org.passport.tests.keys.JavaKeystoreKeyProviderTest;

import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CredentialsTest.class,
        JavaKeystoreKeyProviderTest.class,
        ServerInfoTest.class
})
public class FipsNonStrictTestSuite {

    @BeforeSuite
    public static void beforeSuite() {
        SuiteSupport.startSuite()
                .registerServerConfig(FipsNonStrictServerConfig.class)
                .registerSupplierConfig("certificates", FipsNonStrictCertificatesConfig.class)
                .registerSupplierConfig("crypto", "fips", FipsMode.NON_STRICT.name());
    }

    @AfterSuite
    public static void afterSuite() {
        SuiteSupport.stopSuite();
    }

    public static class FipsNonStrictServerConfig implements PassportServerConfig {

        @Override
        public PassportServerConfigBuilder configure(PassportServerConfigBuilder config) {
            return config.features(Profile.Feature.FIPS)
                .option("fips-mode", "non-strict")
                .dependency("org.bouncycastle", "bc-fips")
                .dependency("org.bouncycastle", "bctls-fips")
                .dependency("org.bouncycastle", "bcpkix-fips")
                .dependency("org.bouncycastle", "bcutil-fips");
        }
    }

    public static class FipsNonStrictCertificatesConfig implements CertificatesConfig {

        @Override
        public CertificatesConfigBuilder configure(CertificatesConfigBuilder config) {
            return config.tlsEnabled(true).keystoreFormat(KeystoreUtil.KeystoreFormat.PKCS12);
        }
    }
}
