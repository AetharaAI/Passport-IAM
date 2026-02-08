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

package org.passport.services.resteasy;

import org.passport.http.HttpRequest;
import org.passport.http.HttpResponse;
import org.passport.models.PassportSession;
import org.passport.services.DefaultPassportContext;

import org.jboss.resteasy.core.ResteasyContext;

public class ResteasyPassportContext extends DefaultPassportContext {

    public ResteasyPassportContext(PassportSession session) {
        super(session);
    }

    @Override
    protected HttpRequest createHttpRequest() {
        return new HttpRequestImpl(ResteasyContext.getContextData(org.jboss.resteasy.spi.HttpRequest.class));
    }

    @Override
    protected HttpResponse createHttpResponse() {
        return new HttpResponseImpl(ResteasyContext.getContextData(org.jboss.resteasy.spi.HttpResponse.class));
    }

}
