package org.passport.testframework.injection;

import org.passport.testframework.config.Config;
import org.passport.testframework.config.SuiteConfigSource;
import org.passport.testframework.server.PassportServerConfig;

public class SuiteSupport {

    private static SuiteConfig suiteConfig = new SuiteConfig();

    public static SuiteConfig startSuite() {
        if (suiteConfig == null) {
            suiteConfig = new SuiteConfig();
        }
        return suiteConfig;
    }

    public static void stopSuite() {
        SuiteConfigSource.clear();
        Config.initConfig();
        Extensions.reset();
        suiteConfig = null;
    }

    public static class SuiteConfig {

        public SuiteConfig registerServerConfig(Class<? extends PassportServerConfig> serverConfig) {
            registerSupplierConfig("server", serverConfig);
            return this;
        }

        public SuiteConfig registerSupplierConfig(String supplierValueType, Class<?> supplierConfig) {
            SuiteConfigSource.set("kc.test." + supplierValueType + ".config", supplierConfig.getName());
            return this;
        }

        public SuiteConfig registerSupplierConfig(String supplierValueType, String supplierConfigKey, String supplierConfigValue) {
            SuiteConfigSource.set("kc.test." + supplierValueType + "." + supplierConfigKey, supplierConfigValue);
            return this;
        }

        public SuiteConfig supplier(String name, String supplier) {
            SuiteConfigSource.set("kc.test." + name, supplier);
            return this;
        }

        public SuiteConfig includedSuppliers(String name, String... suppliers) {
            SuiteConfigSource.set("kc.test." + name + ".suppliers.included", String.join(",", suppliers));
            return this;
        }

        public SuiteConfig excludedSuppliers(String name, String... suppliers) {
            SuiteConfigSource.set("kc.test." + name + ".suppliers.excluded", String.join(",", suppliers));
            return this;
        }

    }

}
