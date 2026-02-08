/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.utils;

import org.passport.common.util.Resteasy;
import org.passport.models.PassportContext;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;

public class PassportSessionUtil {

    private static final String NO_REALM = "no_realm_found_in_session";

    private PassportSessionUtil() {

    }

    /**
     * Get the {@link PassportSession} currently associated with the thread.
     *
     * @return the current session
     */
    public static PassportSession getPassportSession() {
        return Resteasy.getContextData(PassportSession.class);
    }

    /**
     * Associate the {@link PassportSession} with the current thread.
     * <br>Warning: should not be called directly. Passport will manage this.
     *
     * @param session
     * @return the existing {@link PassportSession} or null
     */
    public static PassportSession setPassportSession(PassportSession session) {
        return Resteasy.pushContext(PassportSession.class, session);
    }

    public static String getRealmNameFromContext(PassportSession session) {
        if(session == null) {
            return NO_REALM;
        }

        PassportContext context = session.getContext();
        if(context == null) {
            return NO_REALM;
        }

        RealmModel realm = context.getRealm();
        if (realm == null) {
            return NO_REALM;
        }

        if(realm.getName() != null) {
            return realm.getName();
        } else {
            return NO_REALM;
        }
    }

}
