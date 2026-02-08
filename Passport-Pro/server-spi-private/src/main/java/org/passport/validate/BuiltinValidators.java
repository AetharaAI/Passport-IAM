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

import org.passport.validate.validators.DoubleValidator;
import org.passport.validate.validators.EmailValidator;
import org.passport.validate.validators.IntegerValidator;
import org.passport.validate.validators.IsoDateValidator;
import org.passport.validate.validators.LengthValidator;
import org.passport.validate.validators.LocalDateValidator;
import org.passport.validate.validators.NotBlankValidator;
import org.passport.validate.validators.NotEmptyValidator;
import org.passport.validate.validators.OptionsValidator;
import org.passport.validate.validators.PatternValidator;
import org.passport.validate.validators.UriValidator;
import org.passport.validate.validators.ValidatorConfigValidator;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class BuiltinValidators {

    public static NotBlankValidator notBlankValidator() {
        return NotBlankValidator.INSTANCE;
    }

    public static NotEmptyValidator notEmptyValidator() {
        return NotEmptyValidator.INSTANCE;
    }

    public static LengthValidator lengthValidator() {
        return LengthValidator.INSTANCE;
    }

    public static UriValidator uriValidator() {
        return UriValidator.INSTANCE;
    }

    public static EmailValidator emailValidator() {
        return EmailValidator.INSTANCE;
    }

    public static PatternValidator patternValidator() {
        return PatternValidator.INSTANCE;
    }

    public static DoubleValidator doubleValidator() {
        return DoubleValidator.INSTANCE;
    }

    public static IntegerValidator integerValidator() {
        return IntegerValidator.INSTANCE;
    }

    public static LocalDateValidator dateValidator() {
        return LocalDateValidator.INSTANCE;
    }

    public static IsoDateValidator isoDateValidator() {
        return IsoDateValidator.INSTANCE;
    }

    public static OptionsValidator optionsValidator() {
        return OptionsValidator.INSTANCE;
    }

    public static ValidatorConfigValidator validatorConfigValidator() {
        return ValidatorConfigValidator.INSTANCE;
    }
}
