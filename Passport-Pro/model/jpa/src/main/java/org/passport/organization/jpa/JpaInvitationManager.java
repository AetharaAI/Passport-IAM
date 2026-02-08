package org.passport.organization.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.passport.common.util.Time;
import org.passport.models.PassportSession;
import org.passport.models.OrganizationInvitationModel;
import org.passport.models.OrganizationInvitationModel.Filter;
import org.passport.models.OrganizationModel;
import org.passport.models.jpa.entities.OrganizationInvitationEntity;
import org.passport.models.utils.PassportModelUtils;
import org.passport.organization.InvitationManager;
import org.passport.representations.idm.OrganizationInvitationRepresentation.Status;

import static org.passport.models.jpa.PaginationUtils.paginateQuery;
import static org.passport.utils.StreamsUtil.closing;

record JpaInvitationManager(PassportSession session, EntityManager em) implements InvitationManager {

    @Override
    public OrganizationInvitationModel create(OrganizationModel organization, String email,
                                              String firstName, String lastName) {
        String id = PassportModelUtils.generateId();
        OrganizationInvitationEntity entity = new OrganizationInvitationEntity(
                id, organization.getId(), email.trim(),
                firstName != null ? firstName.trim() : null,
                lastName != null ? lastName.trim() : null
        );

        entity.setExpiresAt(getExpiration());

        em.persist(entity);

        return entity;
    }

    @Override
    public OrganizationInvitationModel getById(String id) {
        OrganizationInvitationEntity entity = em.find(OrganizationInvitationEntity.class, id);

        if (entity == null) {
            return null;
        }

        return entity;
    }

    @Override
    public Stream<OrganizationInvitationModel> getAllStream(OrganizationModel organization, Map<Filter, String> attributes, Integer first, Integer max) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<OrganizationInvitationEntity> query = builder.createQuery(OrganizationInvitationEntity.class);
        Root<OrganizationInvitationEntity> root = query.from(OrganizationInvitationEntity.class);
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(builder.equal(root.get("organizationId"), organization.getId()));

        for (Entry<Filter, String> filter : attributes.entrySet()) {
            switch (filter.getKey()) {
                case EMAIL -> predicates.add(builder.like(root.get("email"), builder.literal("%" + filter.getValue().toLowerCase() + "%")));
                case STATUS -> {
                    Status value = Status.valueOf(filter.getValue());

                    if (Status.EXPIRED.equals(value)) {
                        predicates.add(builder.lessThan(root.get("expiresAt"), Time.currentTime()));
                    } else {
                        predicates.add(builder.greaterThanOrEqualTo(root.get("expiresAt"), Time.currentTime()));
                    }
                }
                case FIRST_NAME -> predicates.add(builder.equal(root.get("firstName"), builder.literal("%" + filter.getValue().toLowerCase() + "%")));
                case LAST_NAME -> predicates.add(builder.equal(root.get("lastName"), builder.literal("%" + filter.getValue().toLowerCase() + "%")));
                case SEARCH -> {
                    String searchValue = "%" + filter.getValue().toLowerCase() + "%";

                    Predicate emailPredicate = builder.like(builder.lower(root.get("email")), searchValue);
                    Predicate firstNamePredicate = builder.like(builder.lower(root.get("firstName")), searchValue);
                    Predicate lastNamePredicate = builder.like(builder.lower(root.get("lastName")), searchValue);

                    predicates.add(builder.or(emailPredicate, firstNamePredicate, lastNamePredicate));
                }
            }
        }

        query.where(predicates.toArray(new Predicate[0]));

        TypedQuery<OrganizationInvitationEntity> typedQuery = em.createQuery(query);

        return closing(paginateQuery(typedQuery, first, max).getResultStream().map(OrganizationInvitationModel.class::cast));
    }

    @Override
    public boolean remove(String id) {
        OrganizationInvitationEntity invitation = (OrganizationInvitationEntity) getById(id);

        if (invitation == null) {
            return false;
        }

        em.remove(invitation);

        return true;
    }

    private int getExpiration() {
        return Time.currentTime() + session.getContext().getRealm().getActionTokenGeneratedByAdminLifespan();
    }
}
