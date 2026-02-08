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

package org.passport.testsuite.arquillian;

import org.passport.testsuite.arquillian.decider.AdapterTestExecutionDecider;
import org.passport.testsuite.arquillian.decider.AuthServerExcludeExecutionDecider;
import org.passport.testsuite.arquillian.decider.BrowserDriverIgnoreDecider;
import org.passport.testsuite.arquillian.decider.MigrationTestExecutionDecider;
import org.passport.testsuite.arquillian.h2.H2TestEnricher;
import org.passport.testsuite.arquillian.jmx.JmxConnectorRegistryCreator;
import org.passport.testsuite.arquillian.provider.AdminClientProvider;
import org.passport.testsuite.arquillian.provider.LoadBalancerControllerProvider;
import org.passport.testsuite.arquillian.provider.OAuthClientProvider;
import org.passport.testsuite.arquillian.provider.SuiteContextProvider;
import org.passport.testsuite.arquillian.provider.TestContextProvider;
import org.passport.testsuite.arquillian.provider.URLProvider;
import org.passport.testsuite.drone.HtmlUnitScreenshots;
import org.passport.testsuite.drone.PassportDronePostSetup;
import org.passport.testsuite.drone.PassportWebDriverConfigurator;

import org.jboss.arquillian.container.test.impl.enricher.resource.URLResourceProvider;
import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.container.test.spi.client.deployment.DeploymentScenarioGenerator;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.graphene.location.ContainerCustomizableURLResourceProvider;
import org.jboss.arquillian.graphene.location.CustomizableURLResourceProvider;
import org.jboss.arquillian.test.spi.enricher.resource.ResourceProvider;
import org.jboss.arquillian.test.spi.execution.TestExecutionDecider;

/**
 *
 * @author tkyjovsk
 */
public class PassportArquillianExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {

        builder
                .service(ResourceProvider.class, SuiteContextProvider.class)
                .service(ResourceProvider.class, TestContextProvider.class)
                .service(ResourceProvider.class, AdminClientProvider.class)
                .service(ResourceProvider.class, OAuthClientProvider.class)
                .service(ResourceProvider.class, LoadBalancerControllerProvider.class);

        builder
                .service(DeploymentScenarioGenerator.class, DeploymentTargetModifier.class)
                .service(ApplicationArchiveProcessor.class, DeploymentArchiveProcessor.class)
                .observer(JmxConnectorRegistryCreator.class)
                .observer(AuthServerTestEnricher.class)
                .observer(AppServerTestEnricher.class)
                .observer(H2TestEnricher.class);
        builder
                .service(TestExecutionDecider.class, MigrationTestExecutionDecider.class)
                .service(TestExecutionDecider.class, AdapterTestExecutionDecider.class)
                .service(TestExecutionDecider.class, VaultTestExecutionDecider.class)
                .service(TestExecutionDecider.class, AuthServerExcludeExecutionDecider.class)
                .service(TestExecutionDecider.class, BrowserDriverIgnoreDecider.class);

        builder
                .override(ResourceProvider.class, URLResourceProvider.class, URLProvider.class)
                .override(ResourceProvider.class, CustomizableURLResourceProvider.class, URLProvider.class)
                .override(ResourceProvider.class, ContainerCustomizableURLResourceProvider.class, URLProvider.class);

        builder
                .observer(PassportWebDriverConfigurator.class)
                .observer(HtmlUnitScreenshots.class)
                .observer(PassportDronePostSetup.class);


    }

}
