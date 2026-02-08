package org.passport.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.passport.common.Profile;
import org.passport.common.Profile.Feature;
import org.passport.common.crypto.FipsMode;
import org.passport.config.SecurityOptions;

import io.smallrye.config.ConfigSourceInterceptorContext;

import static org.passport.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;

final class SecurityPropertyMappers implements PropertyMapperGrouping {


    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(SecurityOptions.FIPS_MODE).transformer(SecurityPropertyMappers::resolveFipsMode)
                        .paramLabel("mode")
                        .build()
        );
    }

    private static String resolveFipsMode(String value, ConfigSourceInterceptorContext context) {
        if (value == null) {
            if (Profile.isFeatureEnabled(Feature.FIPS)) {
                return FipsMode.NON_STRICT.toString();
            }

            return FipsMode.DISABLED.toString();
        }

        return value;
    }
}
