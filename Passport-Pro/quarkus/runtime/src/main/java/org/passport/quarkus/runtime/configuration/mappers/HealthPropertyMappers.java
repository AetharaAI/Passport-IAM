package org.passport.quarkus.runtime.configuration.mappers;

import java.util.List;

import org.passport.config.HealthOptions;

import static org.passport.quarkus.runtime.configuration.mappers.PropertyMapper.fromOption;


final class HealthPropertyMappers implements PropertyMapperGrouping {


    @Override
    public List<PropertyMapper<?>> getPropertyMappers() {
        return List.of(
                fromOption(HealthOptions.HEALTH_ENABLED)
                        // no need to map to a quarkus option, this option exists to
                        // to control artifact / extension inclusion. Quarkus will default to enabled
                        .build()
        );
    }

}
