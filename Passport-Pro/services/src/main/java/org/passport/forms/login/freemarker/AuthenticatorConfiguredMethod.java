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

package org.passport.forms.login.freemarker;

import java.util.List;

import org.passport.authentication.Authenticator;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 */
public class AuthenticatorConfiguredMethod implements TemplateMethodModelEx {
    private final RealmModel realm;
    private final UserModel user;
    private final PassportSession session;

    public AuthenticatorConfiguredMethod(RealmModel realm, UserModel user, PassportSession session) {
        this.realm = realm;
        this.user = user;
        this.session = session;
    }

    @Override
    public Object exec(List list) throws TemplateModelException {
        String providerId = list.get(0).toString();
        Authenticator authenticator = session.getProvider(Authenticator.class, providerId);
        return authenticator.configuredFor(session, realm, user);
    }
}
