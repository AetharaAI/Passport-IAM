package org.passport.common.crypto;

public enum FipsMode {
    NON_STRICT("org.passport.crypto.fips.FIPS1402Provider"),
    STRICT("org.passport.crypto.fips.Fips1402StrictCryptoProvider"),
    DISABLED("org.passport.crypto.def.DefaultCryptoProvider");

    private final String providerClassName;
    private final String optionName;

    FipsMode(String providerClassName) {
        this.providerClassName = providerClassName;
        this.optionName = name().toLowerCase().replace('_', '-');
    }

    public boolean isFipsEnabled() {
        return this.equals(NON_STRICT) || this.equals(STRICT);
    }

    public String getProviderClassName() {
        return providerClassName;
    }

    public static FipsMode valueOfOption(String name) {
        return valueOf(name.toUpperCase().replace('-', '_'));
    }

    @Override
    public String toString() {
        return optionName;
    }
}
