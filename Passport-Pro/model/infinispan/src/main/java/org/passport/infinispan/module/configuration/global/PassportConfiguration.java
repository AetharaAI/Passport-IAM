package org.passport.infinispan.module.configuration.global;

import org.passport.models.PassportSessionFactory;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.attributes.AttributeDefinition;
import org.infinispan.commons.configuration.attributes.AttributeSet;

@BuiltBy(PassportConfigurationBuilder.class)
public class PassportConfiguration {

    static final AttributeDefinition<PassportSessionFactory> PASSPORT_SESSION_FACTORY = AttributeDefinition.builder("passport-session-factory", null, PassportSessionFactory.class)
            .global(true)
            .autoPersist(false)
            .immutable()
            .build();

    private final AttributeSet attributes;

    static AttributeSet attributeSet() {
        return new AttributeSet(PassportConfiguration.class, PASSPORT_SESSION_FACTORY);
    }

    PassportConfiguration(AttributeSet attributes) {
        this.attributes = attributes;
    }

    AttributeSet attributes() {
        return attributes;
    }

    public PassportSessionFactory passportSessionFactory() {
        return attributes.attribute(PASSPORT_SESSION_FACTORY).get();
    }

}
