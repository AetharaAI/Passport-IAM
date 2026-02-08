package org.passport.services.clientpolicy.context;

import org.passport.models.ClientModel;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.services.clientpolicy.ClientPolicyEvent;
import org.passport.utils.StringUtil;

public class ClientSecretRotationContext extends AdminClientUpdateContext {

    private final String currentSecret;

    public ClientSecretRotationContext(ClientRepresentation proposedClientRepresentation,
                                       ClientModel targetClient, String currentSecret) {
        super(proposedClientRepresentation, targetClient, null);
        this.currentSecret = currentSecret;
    }

    @Override
    public ClientPolicyEvent getEvent() {
        return ClientPolicyEvent.UPDATED;
    }

    public String getCurrentSecret() {
        return currentSecret;
    }

    public boolean isForceRotation() {
        return StringUtil.isNotBlank(currentSecret);
    }
}
