package org.passport.quarkus.runtime;

import java.util.HashMap;
import java.util.Map;

import org.passport.common.profile.SingleProfileConfigResolver;
import org.passport.config.FeatureOptions;
import org.passport.config.WildcardOptionsUtil;
import org.passport.quarkus.runtime.cli.PropertyException;
import org.passport.quarkus.runtime.configuration.Configuration;

import static org.passport.quarkus.runtime.configuration.MicroProfileConfigProvider.NS_PASSPORT_PREFIX;

public class QuarkusSingleProfileConfigResolver extends SingleProfileConfigResolver {

    public QuarkusSingleProfileConfigResolver() {
        super(getQuarkusFeatureState());
    }

    protected static Map<String, Boolean> getQuarkusFeatureState() {
        var map = new HashMap<String, Boolean>();
        var featureEnabledOptionPrefix = NS_PASSPORT_PREFIX + WildcardOptionsUtil.getWildcardPrefix(FeatureOptions.FEATURE.getKey());

        Configuration.getPropertyNames().forEach(property -> {
            if (property.startsWith(NS_PASSPORT_PREFIX) && property.startsWith(featureEnabledOptionPrefix)) {
                var feature = WildcardOptionsUtil.getWildcardValue(FeatureOptions.FEATURE, property);
                var value = Configuration.getOptionalValue(property).orElseThrow(
                        () -> new PropertyException("Missing value for feature '%s'".formatted(feature)));

                if (value.startsWith("v")) {
                    map.put(feature + ":" + value, true);
                } else {
                    map.put(feature, switch (value) {
                        case "enabled" -> Boolean.TRUE;
                        case "disabled" -> Boolean.FALSE;
                        default -> null;
                    });
                }
            }
        });

        return map;
    }
}
