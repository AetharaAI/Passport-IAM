/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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
package org.passport.subsystem.adapter.saml.extension;

import org.jboss.as.controller.ReloadRequiredRemoveStepHandler;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.common.GenericSubsystemDescribeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.RuntimePackageDependency;

import static org.passport.subsystem.adapter.saml.extension.PassportDependencyProcessor.PASSPORT_JBOSS_CORE_ADAPTER;

/**
 * Definition of subsystem=passport-saml.
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2013 Red Hat Inc.
 */
public class PassportSubsystemDefinition extends SimpleResourceDefinition {

    static final PassportSubsystemDefinition INSTANCE = new PassportSubsystemDefinition();

    private PassportSubsystemDefinition() {
        super(PassportSamlExtension.SUBSYSTEM_PATH,
                PassportSamlExtension.getResourceDescriptionResolver("subsystem"),
                PassportSubsystemAdd.INSTANCE,
                ReloadRequiredRemoveStepHandler.INSTANCE
        );
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        resourceRegistration.registerOperationHandler(GenericSubsystemDescribeHandler.DEFINITION, GenericSubsystemDescribeHandler.INSTANCE);
    }

    @Override
    public void registerAdditionalRuntimePackages(ManagementResourceRegistration resourceRegistration) {
        // This module is required by deployment but not referenced by JBoss modules
        resourceRegistration.registerAdditionalRuntimePackages(
                RuntimePackageDependency.required(PASSPORT_JBOSS_CORE_ADAPTER));
    }
}
