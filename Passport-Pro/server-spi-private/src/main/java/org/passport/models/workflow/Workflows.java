package org.passport.models.workflow;

import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.provider.Provider;

public final class Workflows {

    public static WorkflowConditionProvider getConditionProvider(PassportSession session, String name, String configParameter) {
        return getConditionProviderFactory(session, name).create(session, configParameter);
    }

    public static WorkflowConditionProviderFactory<WorkflowConditionProvider> getConditionProviderFactory(PassportSession session, String providerId) {
        return getProviderFactory(session, WorkflowConditionProvider.class, providerId);
    }

    public static WorkflowEventProvider getEventProvider(PassportSession session, String name, String configParameter) {
        return getEventProviderFactory(session, name).create(session, configParameter);
    }

    public static WorkflowEventProviderFactory<WorkflowEventProvider> getEventProviderFactory(PassportSession session, String providerId) {
        return getProviderFactory(session, WorkflowEventProvider.class, providerId);
    }

    public static WorkflowStepProvider getStepProvider(PassportSession session, WorkflowStep step) {
        RealmModel realm = session.getContext().getRealm();
        return getStepProviderFactory(session, step).create(session, realm.getComponent(step.getId()));
    }

    public static WorkflowStepProviderFactory<WorkflowStepProvider> getStepProviderFactory(PassportSession session, WorkflowStep step) {
        return getProviderFactory(session, WorkflowStepProvider.class, step.getProviderId());
    }

    private static <P extends Provider, F> F getProviderFactory(PassportSession session, Class<P> providerClass, String providerId) {
        PassportSessionFactory sessionFactory = session.getPassportSessionFactory();
        @SuppressWarnings("unchecked")
        F providerFactory = (F) sessionFactory.getProviderFactory(providerClass, providerId);

        if (providerFactory == null) {
            throw new WorkflowInvalidStateException("Could not find provider factory with id: " + providerId);
        }
        return providerFactory;
    }
}
