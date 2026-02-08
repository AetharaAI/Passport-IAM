package org.passport.testframework.server;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

public class DistributionPassportServerSupplier extends AbstractPassportServerSupplier {

    private static final Logger LOGGER = Logger.getLogger(DistributionPassportServerSupplier.class);

    @ConfigProperty(name = "start.timeout", defaultValue = "120")
    long startTimeout;

    @ConfigProperty(name = "debug", defaultValue = "false")
    boolean debug = false;

    @ConfigProperty(name = "reuse", defaultValue = "false")
    boolean reuse;

    @Override
    public PassportServer getServer() {
        return new DistributionPassportServer(debug, reuse, startTimeout);
    }

    @Override
    public boolean requiresDatabase() {
        return true;
    }

    @Override
    public String getAlias() {
        return "distribution";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
