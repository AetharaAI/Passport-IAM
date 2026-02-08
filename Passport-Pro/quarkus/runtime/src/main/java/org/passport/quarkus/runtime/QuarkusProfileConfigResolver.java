package org.passport.quarkus.runtime;

import org.passport.common.profile.CommaSeparatedListProfileConfigResolver;
import org.passport.quarkus.runtime.configuration.Configuration;

public class QuarkusProfileConfigResolver extends CommaSeparatedListProfileConfigResolver {

    public QuarkusProfileConfigResolver() {
        super(getConfig("kc.features"), getConfig("kc.features-disabled"));
    }

    static String getConfig(String key) {
        return Configuration.getRawPersistedProperty(key)
                .orElse(Configuration.getConfigValue(key).getValue());
    }

}
