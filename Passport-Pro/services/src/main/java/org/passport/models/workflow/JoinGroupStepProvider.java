package org.passport.models.workflow;

import org.passport.component.ComponentModel;
import org.passport.models.GroupModel;
import org.passport.models.PassportSession;
import org.passport.models.UserModel;

import org.jboss.logging.Logger;

import static org.passport.models.utils.ModelToRepresentation.buildGroupPath;

public class JoinGroupStepProvider extends GroupBasedStepProvider {

    private final Logger log = Logger.getLogger(JoinGroupStepProvider.class);

    protected JoinGroupStepProvider(PassportSession session, ComponentModel model) {
        super(session, model);
    }

    @Override
    protected void run(UserModel user, GroupModel group) {
        log.debugv("Adding user %s to group %s)", user.getId(), buildGroupPath(group));
        user.joinGroup(group);
    }
}
