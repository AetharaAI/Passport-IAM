package org.passport.models.workflow;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

public class WorkflowEventSpi implements Spi {

    public static final String NAME = "workflow-event";

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return NAME;
    }


    @Override
    public Class<? extends Provider> getProviderClass() {
        return WorkflowEventProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return WorkflowEventProviderFactory.class;
    }
}
