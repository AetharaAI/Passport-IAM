package org.passport.models.workflow.conditions;

import java.util.stream.Stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.passport.models.FederatedIdentityModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserModel;
import org.passport.models.jpa.entities.FederatedIdentityEntity;
import org.passport.models.workflow.ResourceType;
import org.passport.models.workflow.WorkflowConditionProvider;
import org.passport.models.workflow.WorkflowExecutionContext;
import org.passport.models.workflow.WorkflowInvalidStateException;
import org.passport.utils.StringUtil;

public class IdentityProviderWorkflowConditionProvider implements WorkflowConditionProvider {

    private final String expectedAlias;
    private final PassportSession session;

    public IdentityProviderWorkflowConditionProvider(PassportSession session, String expectedAlias) {
        this.session = session;
        this.expectedAlias = expectedAlias;
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

        Stream<FederatedIdentityModel> federatedIdentities = session.users().getFederatedIdentitiesStream(realm, user);
        return federatedIdentities
                .map(FederatedIdentityModel::getIdentityProvider)
                .anyMatch(expectedAlias::equals);
    }

    @Override
    public Predicate toPredicate(CriteriaBuilder cb, CriteriaQuery<String> query, Root<?> path) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<FederatedIdentityEntity> from = subquery.from(FederatedIdentityEntity.class);

        subquery.select(cb.literal(1));
        subquery.where(
                cb.and(
                        cb.equal(from.get("user").get("id"), path.get("id")),
                        cb.equal(from.get("identityProvider"), expectedAlias)
                )
        );

        return cb.exists(subquery);
    }

    @Override
    public void validate() {
        if (StringUtil.isBlank(expectedAlias)) {
            throw new WorkflowInvalidStateException("Expected identity provider alias is not set.");
        }
        if (session.identityProviders().getByAlias(expectedAlias) == null) {
            throw new WorkflowInvalidStateException(String.format("Identity provider %s does not exist.", expectedAlias));
        }
    }

    @Override
    public void close() {

    }
}
