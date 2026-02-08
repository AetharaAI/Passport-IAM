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

package org.passport.representations.adapters.action;

import java.util.List;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class LogoutAction extends AdminAction {
    public static final String LOGOUT = "LOGOUT";
    protected List<String> adapterSessionIds;
    protected int notBefore;
    protected List<String> passportSessionIds;

    public LogoutAction() {
    }

    public LogoutAction(String id, int expiration, String resource, List<String> adapterSessionIds, int notBefore, List<String> passportSessionIds) {
        super(id, expiration, resource, LOGOUT);
        this.adapterSessionIds = adapterSessionIds;
        this.notBefore = notBefore;
        this.passportSessionIds = passportSessionIds;
    }


    public int getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(int notBefore) {
        this.notBefore = notBefore;
    }

    public List<String> getAdapterSessionIds() {
        return adapterSessionIds;
    }

    public List<String> getPassportSessionIds() {
        return passportSessionIds;
    }

    public void setPassportSessionIds(List<String> passportSessionIds) {
        this.passportSessionIds = passportSessionIds;
    }

    @Override
    public boolean validate() {
        return LOGOUT.equals(action);
    }
}
