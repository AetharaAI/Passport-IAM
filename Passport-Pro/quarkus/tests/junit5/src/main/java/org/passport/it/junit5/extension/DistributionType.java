/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates
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

package org.passport.it.junit5.extension;

import java.util.Optional;
import java.util.function.Function;

import org.passport.it.utils.DockerPassportDistribution;
import org.passport.it.utils.PassportDistribution;
import org.passport.it.utils.RawPassportDistribution;
import org.passport.utils.StringUtil;

public enum DistributionType {

    RAW(DistributionType::createRawDistribution),
    DOCKER(DistributionType::createDockerDistribution);

    private static PassportDistribution createDockerDistribution(DistributionTest config) {
        return new DockerPassportDistribution(
                config.debug(),
                config.keepAlive(),
                config.requestPort(),
                config.containerExposedPorts());
    }

    private static PassportDistribution createRawDistribution(DistributionTest config) {
        return new RawPassportDistribution(
                config.debug(),
                config.keepAlive(),
                config.enableTls(),
                !DistributionTest.ReInstall.NEVER.equals(config.reInstall()),
                config.removeBuildOptionsAfterBuild(),
                config.requestPort());
    }

    private final Function<DistributionTest, PassportDistribution> factory;

    DistributionType(Function<DistributionTest, PassportDistribution> factory) {
        this.factory = factory;
    }

    public static Optional<DistributionType> getCurrent() {
        String distributionType = System.getProperty("kc.quarkus.tests.dist");

        if (StringUtil.isBlank(distributionType)) {
            return Optional.empty();
        }

        try {
            return Optional.of(valueOf(distributionType.toUpperCase()));
        } catch (IllegalStateException cause) {
            throw new RuntimeException("Invalid distribution type: " + distributionType);
        }
    }

    public static boolean isContainerDist() {
        return DistributionType.getCurrent().map(f -> f.equals(DistributionType.DOCKER)).orElse(false);
    }

    public static boolean isRawDist() {
        return DistributionType.getCurrent().map(f -> f.equals(DistributionType.RAW)).orElse(false);
    }

    public PassportDistribution newInstance(DistributionTest config) {
        return factory.apply(config);
    }
}
