/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
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

package org.passport.models.workflow;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

import org.passport.common.util.MultivaluedHashMap;
import org.passport.connections.jpa.JpaConnectionProvider;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.jpa.entities.UserEntity;
import org.passport.models.workflow.expression.BooleanConditionParser;
import org.passport.models.workflow.expression.EvaluatorUtils;
import org.passport.models.workflow.expression.PredicateEvaluator;
import org.passport.representations.workflows.WorkflowConstants;
import org.passport.utils.StringUtil;

import static org.passport.representations.workflows.WorkflowConstants.CONFIG_CONDITIONS;

public class UserResourceTypeWorkflowProvider implements ResourceTypeSelector {

    private final EntityManager em;
    private final PassportSession session;

    public UserResourceTypeWorkflowProvider(PassportSession session) {
        this.session = session;
        this.em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    @Override
    public List<String> getResourceIds(Workflow workflow) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<UserEntity> userRoot = query.from(UserEntity.class);
        List<Predicate> predicates = new ArrayList<>();

        // Subquery will find if a state record exists for the user and workflow
        // SELECT 1 FROM WorkflowActionStateEntity s WHERE s.resourceId = userRoot.id AND s.workflowId = :workflowId
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<WorkflowStateEntity> stateRoot = subquery.from(WorkflowStateEntity.class);
        subquery.select(cb.literal(1));
        subquery.where(
            cb.and(
                cb.equal(stateRoot.get("resourceId"), userRoot.get("id")),
                cb.equal(stateRoot.get("workflowId"), workflow.getId())
            )
        );
        RealmModel realm = session.getContext().getRealm();
        predicates.add(cb.equal(userRoot.get("realmId"), realm.getId()));
        Predicate notExistsPredicate = cb.not(cb.exists(subquery));
        predicates.add(notExistsPredicate);

        predicates.add(getConditionsPredicate(workflow, cb, query, userRoot));

        query.select(userRoot.get("id")).where(predicates);

        int batchSize = Integer.parseInt(workflow.getConfig().getFirstOrDefault(WorkflowConstants.CONFIG_SCHEDULE_BATCH_SIZE, "100"));

        return em.createQuery(query).setMaxResults(batchSize).getResultList();
    }

    @Override
    public Object resolveResource(String resourceId) {
        Objects.requireNonNull(resourceId, "resourceId");
        return ResourceType.USERS.resolveResource(session, resourceId);
    }

    private Predicate getConditionsPredicate(Workflow workflow, CriteriaBuilder cb, CriteriaQuery<String> query, Root<UserEntity> path) {
        MultivaluedHashMap<String, String> config = workflow.getConfig();
        String conditions = config.getFirst(CONFIG_CONDITIONS);

        if (StringUtil.isBlank(conditions)) {
            return cb.conjunction();
        }

        BooleanConditionParser.EvaluatorContext context = EvaluatorUtils.createEvaluatorContext(conditions);
        PredicateEvaluator evaluator = new PredicateEvaluator(session, cb, query, path);
        return evaluator.visit(context);
    }
}
