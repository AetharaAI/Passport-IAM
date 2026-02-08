package org.passport.models.workflow.conditions;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.passport.models.GroupModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.jpa.entities.UserGroupMembershipEntity;
import org.passport.models.utils.PassportModelUtils;
import org.passport.models.workflow.ResourceType;
import org.passport.models.workflow.WorkflowConditionProvider;
import org.passport.models.workflow.WorkflowExecutionContext;
import org.passport.models.workflow.WorkflowInvalidStateException;
import org.passport.utils.StringUtil;

public class GroupMembershipWorkflowConditionProvider implements WorkflowConditionProvider {

    private final String expectedGroup;
    private final PassportSession session;

    public GroupMembershipWorkflowConditionProvider(PassportSession session,String expectedGroup) {
        this.session = session;
        this.expectedGroup = expectedGroup;
    }

    @Override
    public ResourceType getSupportedResourceType() {
        return ResourceType.USERS;
    }

    @Override
    public boolean evaluate(WorkflowExecutionContext context) {
        validate();

        RealmModel realm = session.getContext().getRealm();
        UserModel user = session.users().getUserById(realm, context.getResourceId());
        if (user == null) {
            return false;
        }

        GroupModel group = PassportModelUtils.findGroupByPath(session, realm, expectedGroup);
        return user.isMemberOf(group);
    }

    @Override
    public Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<String> query, Root<?> path) {
        validate();

        GroupModel group = PassportModelUtils.findGroupByPath(session, session.getContext().getRealm(), expectedGroup);
        if (group == null) {
            return cb.disjunction(); // always false
        }

        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<UserGroupMembershipEntity> from = subquery.from(UserGroupMembershipEntity.class);

        subquery.select(cb.literal(1));
        subquery.where(
                cb.and(
                        cb.equal(from.get("user").get("id"), path.get("id")),
                        cb.equal(from.get("groupId"), group.getId())
                )
        );

        return cb.exists(subquery);
    }

    @Override
    public void validate() {
        if (StringUtil.isBlank(this.expectedGroup)) {
            throw new WorkflowInvalidStateException("Expected group path not set.");
        }
        if (PassportModelUtils.findGroupByPath(session, session.getContext().getRealm(), expectedGroup) == null) {
            throw new WorkflowInvalidStateException(String.format("Group with name %s does not exist.", expectedGroup));
        }
    }

    @Override
    public void close() {

    }
}
