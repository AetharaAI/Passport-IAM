package org.passport.models.workflow;

import java.util.List;
import java.util.stream.Stream;

import org.passport.component.ComponentModel;
import org.passport.models.GroupModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.utils.PassportModelUtils;

import org.jboss.logging.Logger;

public abstract class GroupBasedStepProvider implements WorkflowStepProvider {

    private final Logger log = Logger.getLogger(GroupBasedStepProvider.class);
    public static final String CONFIG_GROUP = "group";

    private final PassportSession session;
    private final ComponentModel model;

    public GroupBasedStepProvider(PassportSession session, ComponentModel model) {
        this.session = session;
        this.model = model;
    }

    @Override
    public void run(WorkflowExecutionContext context) {
        UserModel user = session.users().getUserById(getRealm(), context.getResourceId());

        if (user != null) {
            try {
                getGroups().forEach(group -> run(user, group));
            } catch (Exception e) {
                log.errorf(e, "Failed to manage group membership for user %s", user.getId());
            }
        }
    }

    protected abstract void run(UserModel user, GroupModel group);

    @Override
    public void close() {
    }

    private Stream<GroupModel> getGroups() {
        return model.getConfig().getOrDefault(CONFIG_GROUP, List.of()).stream()
                .map(name -> PassportModelUtils.findGroupByPath(session, getRealm(), name));
    }

    private RealmModel getRealm() {
        return session.getContext().getRealm();
    }
}
