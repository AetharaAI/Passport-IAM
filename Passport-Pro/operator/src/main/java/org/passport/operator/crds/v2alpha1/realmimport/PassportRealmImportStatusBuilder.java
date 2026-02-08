/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates
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

package org.passport.operator.crds.v2alpha1.realmimport;

import java.util.ArrayList;
import java.util.List;

public class PassportRealmImportStatusBuilder {
    private final PassportRealmImportStatusCondition readyCondition;
    private final PassportRealmImportStatusCondition startedCondition;
    private final PassportRealmImportStatusCondition hasErrorsCondition;

    private final List<String> notReadyMessages = new ArrayList<>();
    private final List<String> startedMessages = new ArrayList<>();
    private final List<String> errorMessages = new ArrayList<>();

    public PassportRealmImportStatusBuilder() {
        readyCondition = new PassportRealmImportStatusCondition();
        readyCondition.setType(PassportRealmImportStatusCondition.DONE);
        readyCondition.setStatus(false);

        startedCondition = new PassportRealmImportStatusCondition();
        startedCondition.setType(PassportRealmImportStatusCondition.STARTED);
        startedCondition.setStatus(false);

        hasErrorsCondition = new PassportRealmImportStatusCondition();
        hasErrorsCondition.setType(PassportRealmImportStatusCondition.HAS_ERRORS);
        hasErrorsCondition.setStatus(false);
    }

    public PassportRealmImportStatusBuilder addStartedMessage(String message) {
        startedCondition.setStatus(true);
        readyCondition.setStatus(false);
        hasErrorsCondition.setStatus(false);
        startedMessages.add(message);
        return this;
    }

    public PassportRealmImportStatusBuilder addDone() {
        startedCondition.setStatus(false);
        readyCondition.setStatus(true);
        hasErrorsCondition.setStatus(false);
        return this;
    }

    public PassportRealmImportStatusBuilder addNotReadyMessage(String message) {
        startedCondition.setStatus(false);
        readyCondition.setStatus(false);
        hasErrorsCondition.setStatus(false);
        notReadyMessages.add(message);
        return this;
    }

    public PassportRealmImportStatusBuilder addErrorMessage(String message) {
        startedCondition.setStatus(false);
        readyCondition.setStatus(false);
        hasErrorsCondition.setStatus(true);
        errorMessages.add(message);
        return this;
    }

    public PassportRealmImportStatus build() {
        readyCondition.setMessage(String.join("\n", notReadyMessages));
        startedCondition.setMessage(String.join("\n", startedMessages));
        hasErrorsCondition.setMessage(String.join("\n", errorMessages));

        PassportRealmImportStatus status = new PassportRealmImportStatus();
        status.setConditions(List.of(readyCondition, startedCondition, hasErrorsCondition));
        return status;
    }
}
