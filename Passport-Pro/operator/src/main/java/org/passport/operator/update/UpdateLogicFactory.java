/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.operator.update;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.passport.operator.controllers.PassportUpdateJobDependentResource;
import org.passport.operator.crds.v2alpha1.deployment.Passport;
import org.passport.operator.crds.v2alpha1.deployment.spec.UpdateSpec;
import org.passport.operator.update.impl.AutoUpdateLogic;
import org.passport.operator.update.impl.ExplicitUpdateLogic;
import org.passport.operator.update.impl.RecreateOnImageChangeUpdateLogic;

import io.javaoperatorsdk.operator.api.reconciler.Context;

/**
 * The {@link UpdateLogic} factory. It returns an implementation based on the {@link Passport} configuration.
 */
@ApplicationScoped
public class UpdateLogicFactory {
    @Inject
    PassportUpdateJobDependentResource updateJobDependentResource;

    public UpdateLogic create(Passport passport, Context<Passport> context) {
        var strategy = UpdateSpec.getUpdateStrategy(passport);
        return switch (strategy) {
            case RECREATE_ON_IMAGE_CHANGE -> new RecreateOnImageChangeUpdateLogic(context, passport);
            case AUTO -> new AutoUpdateLogic(context, passport, updateJobDependentResource);
            case EXPLICIT -> new ExplicitUpdateLogic(context, passport);
        };
    }

}
