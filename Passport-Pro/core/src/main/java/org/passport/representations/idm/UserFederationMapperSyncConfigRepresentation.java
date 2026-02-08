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

package org.passport.representations.idm;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class UserFederationMapperSyncConfigRepresentation {

    private Boolean fedToPassportSyncSupported;
    private String fedToPassportSyncMessage; // applicable just if fedToPassportSyncSupported is true

    private Boolean passportToFedSyncSupported;
    private String passportToFedSyncMessage; // applicable just if passportToFedSyncSupported is true

    public UserFederationMapperSyncConfigRepresentation() {
    }

    public UserFederationMapperSyncConfigRepresentation(boolean fedToPassportSyncSupported, String fedToPassportSyncMessage,
                                                        boolean passportToFedSyncSupported, String passportToFedSyncMessage) {
        this.fedToPassportSyncSupported = fedToPassportSyncSupported;
        this.fedToPassportSyncMessage = fedToPassportSyncMessage;
        this.passportToFedSyncSupported = passportToFedSyncSupported;
        this.passportToFedSyncMessage = passportToFedSyncMessage;
    }

    public Boolean isFedToPassportSyncSupported() {
        return fedToPassportSyncSupported;
    }

    public void setFedToPassportSyncSupported(Boolean fedToPassportSyncSupported) {
        this.fedToPassportSyncSupported = fedToPassportSyncSupported;
    }

    public String getFedToPassportSyncMessage() {
        return fedToPassportSyncMessage;
    }

    public void setFedToPassportSyncMessage(String fedToPassportSyncMessage) {
        this.fedToPassportSyncMessage = fedToPassportSyncMessage;
    }

    public Boolean isPassportToFedSyncSupported() {
        return passportToFedSyncSupported;
    }

    public void setPassportToFedSyncSupported(Boolean passportToFedSyncSupported) {
        this.passportToFedSyncSupported = passportToFedSyncSupported;
    }

    public String getPassportToFedSyncMessage() {
        return passportToFedSyncMessage;
    }

    public void setPassportToFedSyncMessage(String passportToFedSyncMessage) {
        this.passportToFedSyncMessage = passportToFedSyncMessage;
    }
}
