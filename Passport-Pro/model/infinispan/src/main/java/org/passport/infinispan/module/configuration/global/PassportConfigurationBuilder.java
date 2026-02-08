package org.passport.infinispan.module.configuration.global;

import org.passport.models.PassportSessionFactory;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.configuration.Combine;
import org.infinispan.commons.configuration.attributes.AttributeSet;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;

public class PassportConfigurationBuilder implements Builder<PassportConfiguration> {

    private final AttributeSet attributes;

    public PassportConfigurationBuilder(GlobalConfigurationBuilder unused) {
        attributes = PassportConfiguration.attributeSet();
    }

    @Override
    public PassportConfiguration create() {
        return new PassportConfiguration(attributes.protect());
    }

    @Override
    public Builder<?> read(PassportConfiguration template, Combine combine) {
        attributes.read(template.attributes(), combine);
        return this;
    }

    @Override
    public AttributeSet attributes() {
        return attributes;
    }

    @Override
    public void validate() {

    }

    public PassportConfigurationBuilder setPassportSessionFactory(PassportSessionFactory passportSessionFactory) {
        attributes.attribute(PassportConfiguration.PASSPORT_SESSION_FACTORY).set(passportSessionFactory);
        return this;
    }

}
