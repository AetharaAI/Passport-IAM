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

package org.passport.services.clientregistration.policy;

import java.util.Objects;

import org.passport.component.ComponentModel;
import org.passport.events.Details;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.services.ServicesLogger;
import org.passport.services.clientregistration.ClientRegistrationContext;
import org.passport.services.clientregistration.ClientRegistrationProvider;

import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class ClientRegistrationPolicyManager {

    private static final Logger logger = Logger.getLogger(ClientRegistrationPolicyManager.class);

    public static void triggerBeforeRegister(ClientRegistrationContext context, RegistrationAuth authType) throws ClientRegistrationPolicyException  {
        triggerPolicies(context.getSession(), context.getProvider(), authType, "before register client", (ClientRegistrationPolicy policy) -> {

            policy.beforeRegister(context);

        });
    }

    public static void triggerAfterRegister(ClientRegistrationContext context, RegistrationAuth authType, ClientModel client) {
        try {
            triggerPolicies(context.getSession(), context.getProvider(), authType, "after register client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

                policy.afterRegister(context, client);

            });
        } catch (ClientRegistrationPolicyException crpe) {
            throw new IllegalStateException(crpe);
        }
    }


    public static void triggerBeforeUpdate(ClientRegistrationContext context, RegistrationAuth authType, ClientModel client) throws ClientRegistrationPolicyException  {
        triggerPolicies(context.getSession(), context.getProvider(), authType, "before update client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

            policy.beforeUpdate(context, client);

        });
    }

    public static void triggerAfterUpdate(ClientRegistrationContext context, RegistrationAuth authType, ClientModel client) {
        try {
            triggerPolicies(context.getSession(), context.getProvider(), authType, "after update client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

                policy.afterUpdate(context, client);

            });
        } catch (ClientRegistrationPolicyException crpe) {
            throw new IllegalStateException(crpe);
        }
    }

    public static void triggerBeforeView(PassportSession session, ClientRegistrationProvider provider, RegistrationAuth authType, ClientModel client) throws ClientRegistrationPolicyException {
        triggerPolicies(session, provider, authType, "before view client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

            policy.beforeView(provider, client);

        });
    }

    public static void triggerBeforeRemove(PassportSession session, ClientRegistrationProvider provider, RegistrationAuth authType, ClientModel client) throws ClientRegistrationPolicyException {
        triggerPolicies(session, provider, authType, "before delete client " + client.getClientId(), (ClientRegistrationPolicy policy) -> {

            policy.beforeDelete(provider, client);

        });
    }



    private static void triggerPolicies(PassportSession session, ClientRegistrationProvider provider, RegistrationAuth authType,
                                        String opDescription, ClientRegOperation op) throws ClientRegistrationPolicyException {
        RealmModel realm = session.getContext().getRealm();

        String policyTypeKey = getComponentTypeKey(authType);
        realm.getComponentsStream(realm.getId(), ClientRegistrationPolicy.class.getName())
                .filter(componentModel -> Objects.equals(componentModel.getSubType(), policyTypeKey))
                .forEach(policyModel -> runPolicy(policyModel, session, provider, opDescription, op));
    }

    private static void runPolicy(ComponentModel policyModel, PassportSession session, ClientRegistrationProvider provider,
                           String opDescription, ClientRegOperation op) throws ClientRegistrationPolicyException {
        ClientRegistrationPolicy policy = session.getProvider(ClientRegistrationPolicy.class, policyModel);
        if (policy == null) {
            throw new ClientRegistrationPolicyException("Policy of type '" + policyModel.getProviderId() + "' not found");
        }

        if (logger.isTraceEnabled()) {
            logger.tracef("Running policy '%s' %s", policyModel.getName(), opDescription);
        }

        try {
            op.run(policy);
        } catch (ClientRegistrationPolicyException crpe) {
            provider.getEvent().detail(Details.CLIENT_REGISTRATION_POLICY, policyModel.getName());
            crpe.setPolicyModel(policyModel);
            ServicesLogger.LOGGER.clientRegistrationRequestRejected(opDescription, crpe.getMessage());
            throw crpe;
        }
    }

    private interface ClientRegOperation {

        void run(ClientRegistrationPolicy policy) throws ClientRegistrationPolicyException;

    }

    public static String getComponentTypeKey(RegistrationAuth authType) {
        return authType.toString().toLowerCase();
    }
}
