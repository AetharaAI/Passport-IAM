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
package org.passport.operator.crds.v2alpha1.realmimport;

import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static org.passport.operator.crds.v2alpha1.realmimport.PassportRealmImportStatusCondition.DONE;

public class PassportRealmImportStatus {
    private List<PassportRealmImportStatusCondition> conditions;

    public List<PassportRealmImportStatusCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<PassportRealmImportStatusCondition> conditions) {
        this.conditions = conditions;
    }

    @JsonIgnore
    public boolean isDone() {
        return conditions
                .stream()
                .anyMatch(c -> Boolean.TRUE.equals(c.getStatus()) && c.getType().equals(DONE));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PassportRealmImportStatus status = (PassportRealmImportStatus) o;
        return Objects.equals(getConditions(), status.getConditions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getConditions());
    }
}
