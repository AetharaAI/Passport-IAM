/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.authorization.common;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.passport.authorization.attribute.Attributes;
import org.passport.authorization.identity.Identity;
import org.passport.authorization.policy.evaluation.EvaluationContext;
import org.passport.models.PassportSession;
import org.passport.representations.AccessToken;

/**
 * @author <a href="mailto:psilva@redhat.com">Pedro Igor</a>
 */
public class DefaultEvaluationContext implements EvaluationContext {

    protected final PassportSession passportSession;
    protected final Identity identity;
    private final Map<String, List<String>> claims;
    private Attributes attributes;

    public DefaultEvaluationContext(Identity identity, PassportSession passportSession) {
        this(identity, null, passportSession);
    }

    public DefaultEvaluationContext(Identity identity, Map<String, List<String>> claims, PassportSession passportSession) {
        this.identity = identity;
        this.claims = claims;
        this.passportSession = passportSession;
    }

    @Override
    public Identity getIdentity() {
        return identity;
    }

    protected Map<String, Collection<String>> getBaseAttributes() {
        Map<String, Collection<String>> attributes = new HashMap<>();

        attributes.put("kc.time.date_time", Arrays.asList(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
        attributes.put("kc.client.network.ip_address", Arrays.asList(this.passportSession.getContext().getConnection().getRemoteAddr()));
        attributes.put("kc.client.network.host", Arrays.asList(this.passportSession.getContext().getConnection().getRemoteHost()));

        List<String> userAgents = this.passportSession.getContext().getRequestHeaders().getRequestHeader("User-Agent");

        if (userAgents != null) {
            attributes.put("kc.client.user_agent", userAgents);
        }

        attributes.put("kc.realm.name", Arrays.asList(this.passportSession.getContext().getRealm().getName()));

        if (claims != null) {
            for (Entry<String, List<String>> entry : claims.entrySet()) {
                attributes.put(entry.getKey(), entry.getValue());
            }
        }

        if (PassportIdentity.class.isInstance(identity)) {
            AccessToken accessToken = PassportIdentity.class.cast(this.identity).getAccessToken();

            if (accessToken != null) {
                attributes.put("kc.client.id", Arrays.asList(accessToken.getIssuedFor()));
            }
        }

        return attributes;
    }

    @Override
    public Attributes getAttributes() {
        if (attributes == null) {
            attributes = Attributes.from(getBaseAttributes());
        }
        return attributes;
    }
}
