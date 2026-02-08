package org.passport.models.workflow.events;

import org.passport.models.GroupModel.GroupMemberLeaveEvent;
import org.passport.models.PassportSession;
import org.passport.models.utils.PassportModelUtils;
import org.passport.models.workflow.AbstractWorkflowEventProvider;
import org.passport.models.workflow.ResourceType;
import org.passport.models.workflow.WorkflowExecutionContext;
import org.passport.provider.ProviderEvent;

import static org.passport.models.utils.PassportModelUtils.GROUP_PATH_SEPARATOR;

public class UserGroupMembershipRemovedWorkflowEventProvider extends AbstractWorkflowEventProvider {

    public UserGroupMembershipRemovedWorkflowEventProvider(final PassportSession session, final String configParameter, final String providerId) {
        super(session, configParameter, providerId);
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean supports(ProviderEvent providerEvent) {
        return providerEvent instanceof GroupMemberLeaveEvent;
    }

    @Override
    protected String resolveResourceId(ProviderEvent providerEvent) {
        if (providerEvent instanceof GroupMemberLeaveEvent gme) {
            return gme.getUser().getId();
        }
        return null;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        if (!super.evaluate(context)) {
            return false;
        }
        if (super.configParameter != null) {
            String groupName = configParameter;
            // this is the case when the group name is passed as a parameter to the event provider - like user-group-membership-removed(mygroup)
            if (!groupName.startsWith(GROUP_PATH_SEPARATOR))
                groupName = GROUP_PATH_SEPARATOR + groupName;
            ProviderEvent groupEvent = (ProviderEvent) context.getEvent().getEvent();
            if (groupEvent instanceof GroupMemberLeaveEvent leaveEvent) {
                return groupName.equals(PassportModelUtils.buildGroupPath(leaveEvent.getGroup()));
            } else {
                return false;
            }
        } else {
            // nothing else to check
            return true;
        }
    }
}
