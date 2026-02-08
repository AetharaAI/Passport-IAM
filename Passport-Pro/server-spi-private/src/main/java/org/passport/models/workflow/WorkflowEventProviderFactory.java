package org.passport.models.workflow;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderFactory;

public interface WorkflowEventProviderFactory <P extends WorkflowEventProvider> extends ProviderFactory<P>, EnvironmentDependentProviderFactory {

    P create(PassportSession session, String configParameter);

    @Override
    default P create(PassportSession session) {
        return create(session, null);
    }

    @Override
    default boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.WORKFLOWS);
    }

    @Override
    default void init(Config.Scope config) {
        // no-op default
    }

    @Override
    default void postInit(PassportSessionFactory factory) {
        // no-op default
    }

    @Override
    default void close() {
        // no-op default
    }
}
