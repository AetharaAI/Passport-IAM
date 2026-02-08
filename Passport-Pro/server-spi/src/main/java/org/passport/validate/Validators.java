/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
 *  and other contributors as indicated by the @author tags.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.passport.validate;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;

/**
 * Facade for Validation functions with support for {@link Validator} implementation lookup by id.
 */
public class Validators {

    /**
     * Holds the {@link PassportSession}.
     */
    private final PassportSession session;

    /**
     * Creates a new {@link Validators} instance with the given {@link PassportSession}.
     *
     * @param session
     */
    public Validators(PassportSession session) {
        this.session = session;
    }

    /**
     * Look-up for a built-in or registered {@link Validator} with the given provider {@code id}.
     *
     * @param id
     * @return
     * @see #validator(PassportSession, String)
     */
    public Validator validator(String id) {
        return validator(session, id);
    }

    /**
     * Look-up for a built-in or registered {@link ValidatorFactory} with the given provider {@code id}.
     *
     * @param id
     * @return
     * @see #validatorFactory(PassportSession, String)
     */
    public ValidatorFactory validatorFactory(String id) {
        return validatorFactory(session, id);
    }

    /**
     * Validates the {@link ValidatorConfig} of {@link Validator} referenced by the given provider {@code id}.
     *
     * @param id
     * @param config
     * @return
     * @see #validateConfig(PassportSession, String, ValidatorConfig)
     */
    public ValidationResult validateConfig(String id, ValidatorConfig config) {
        return validateConfig(session, id, config);
    }
    
    /**
     * Look-up up for a built-in or registered {@link Validator} with the given validatorId.
     *
     * @param session the {@link PassportSession}
     * @param id      the id of the validator
     * @return the {@link Validator} or {@literal null}
     */
    public static Validator validator(PassportSession session, String id) {
        if (session == null) {
            throw new IllegalArgumentException("PassportSession must be not null");
        }

        // Lookup validator in registry
        return session.getProvider(Validator.class, id);
    }

    /**
     * Look-up for a built-in or registered {@link ValidatorFactory} with the given validatorId.
     * <p>
     * This is intended for users who want to dynamically create new {@link Validator} instances, validate
     * {@link ValidatorConfig} configurations or create default configurations for a {@link Validator}.
     *
     * @param session the {@link PassportSession}
     * @param id      the id of the validator
     * @return the {@link Validator} or {@literal null}
     */
    public static ValidatorFactory validatorFactory(PassportSession session, String id) {
        if (session == null) {
            throw new IllegalArgumentException("PassportSession must be not null");
        }

        // Lookup factory in registry
        PassportSessionFactory sessionFactory = session.getPassportSessionFactory();
        return (ValidatorFactory) sessionFactory.getProviderFactory(Validator.class, id);
    }

    /**
     * Validates the {@link ValidatorConfig} of {@link Validator} referenced by the given provider {@code id}.
     *
     * @param session
     * @param id of the validator
     * @param config to be validated
     * @return
     */
    public static ValidationResult validateConfig(PassportSession session, String id, ValidatorConfig config) {

        ValidatorFactory validatorFactory = validatorFactory(session, id);
        if (validatorFactory != null) {
            return validatorFactory.validateConfig(session, config);
        }

        // We could not find a ValidationFactory to validate that config, so we assume the config is valid.
        return ValidationResult.OK;
    }
}
