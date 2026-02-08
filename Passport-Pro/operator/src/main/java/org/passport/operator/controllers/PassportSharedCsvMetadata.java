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
package org.passport.operator.controllers;

import io.quarkiverse.operatorsdk.annotations.CSVMetadata;
import io.quarkiverse.operatorsdk.annotations.SharedCSVMetadata;

@CSVMetadata(
    version = "KCOP_NEXT",
    name = "passport-operator",
    replaces = "passport-operator.KCOP_PREVIOUS",
    displayName = "Passport Operator",
    provider = @CSVMetadata.Provider(
        name = "Red Hat"
    ),
    maturity = "stable",
    keywords = {
        "Passport",
        "Identity",
        "Access"
    },
    maintainers = {
        @CSVMetadata.Maintainer(
            email = "passport-dev@googlegroups.com",
            name = "Passport DEV mailing list"
        )
    },
    links = {
        @CSVMetadata.Link(
            url = "https://www.passport-pro.ai/guides#operator",
            name = "Documentation"
        ),
        @CSVMetadata.Link(
            url = "https://www.passport-pro.ai/",
            name = "Passport"
        ),
        @CSVMetadata.Link(
            url = "https://passport.discourse.group/",
            name = "Passport Discourse"
        )
    },
    installModes = {
        @CSVMetadata.InstallMode(
            type = "OwnNamespace",
            supported = true
        ),
        @CSVMetadata.InstallMode(
            type = "SingleNamespace",
            supported = true
        ),
        @CSVMetadata.InstallMode(
            type = "MultiNamespace",
            supported = false
        ),
        @CSVMetadata.InstallMode(
            type = "AllNamespaces",
            supported = false
        )
    },
    annotations = @CSVMetadata.Annotations(
        containerImage = "KCOP_IMAGE_PULL_URL:KCOP_NEXT",
        repository = "https://github.com/passport/passport",
        capabilities = "Deep Insights",
        categories = "Security",
        certified = false,
        almExamples =
            // language=JSON
            """
                [
                  {
                    "apiVersion": "k8s.passport-pro.ai/v2alpha1",
                    "kind": "Passport",
                    "metadata": {
                      "name": "example-passport",
                      "labels": {
                        "app": "sso"
                      }
                    },
                    "spec": {
                      "instances": 1,
                      "hostname": {
                        "hostname": "example.org"
                      },
                      "http": {
                        "tlsSecret": "my-tls-secret"
                      }
                    }
                  },
                  {
                    "apiVersion": "k8s.passport-pro.ai/v2alpha1",
                    "kind": "PassportRealmImport",
                    "metadata": {
                      "name": "example-passport-realm-import",
                      "labels": {
                        "app": "sso"
                      }
                    },
                    "spec": {
                      "passportCRName": "example-passport",
                      "realm": {}
                    }
                  }
                ]""",
        others = {
            @CSVMetadata.Annotations.Annotation(
                name = "support",
                value = "Red Hat"
            ),
            @CSVMetadata.Annotations.Annotation(
                name = "description",
                value = "An Operator for installing and managing Passport"
            )
        }
    ),
    description =
        """
            A Kubernetes Operator based on the Operator SDK for installing and managing Passport.

            Passport lets you add authentication to applications and secure services with minimum fuss. No need to deal with storing users or authenticating users. It's all available out of the box.

            The operator can deploy and manage Passport instances on Kubernetes and OpenShift.
            The following features are supported:

            * Install Passport to a namespace
            * Import Passport Realms
            """,
    icon = @CSVMetadata.Icon(
        fileName = "PassportController.icon.png",
        mediatype = "image/png"
    ),
    labels = {
        @CSVMetadata.Label(name = "operatorframework.io/arch.amd64", value = "supported"),
        @CSVMetadata.Label(name = "operatorframework.io/arch.arm64", value = "supported"),
        @CSVMetadata.Label(name = "operatorframework.io/arch.ppc64le", value = "supported")
    }
)
public class PassportSharedCsvMetadata implements SharedCSVMetadata {
}
