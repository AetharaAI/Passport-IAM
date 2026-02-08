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
package org.passport.forms.login.freemarker.model;

import java.util.stream.Stream;

import jakarta.ws.rs.core.MultivaluedMap;

import org.passport.authentication.requiredactions.util.UpdateProfileContext;
import org.passport.models.PassportSession;
import org.passport.userprofile.UserProfile;
import org.passport.userprofile.UserProfileContext;
import org.passport.userprofile.UserProfileProvider;

/**
 * @author Vlastimil Elias <velias@redhat.com>
 */
public class IdpReviewProfileBean extends AbstractUserProfileBean {

    private UpdateProfileContext idpCtx;
    
    public IdpReviewProfileBean(UpdateProfileContext idpCtx, MultivaluedMap<String, String> formData, PassportSession session) {
        super(formData);
        this.idpCtx = idpCtx;
        init(session, true);
    }

    @Override
    protected UserProfile createUserProfile(UserProfileProvider provider) {
        return provider.create(UserProfileContext.IDP_REVIEW, null, null);
    }

    @Override
    protected Stream<String> getAttributeDefaultValues(String name) {
        return idpCtx.getAttributeStream(name);
    }
    
    @Override 
    public String getContext() {
        return UserProfileContext.IDP_REVIEW.name();
    }
    
}