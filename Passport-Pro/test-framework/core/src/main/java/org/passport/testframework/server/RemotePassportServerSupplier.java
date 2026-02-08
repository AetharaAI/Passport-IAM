package org.passport.testframework.server;

import org.jboss.logging.Logger;

public class RemotePassportServerSupplier extends AbstractPassportServerSupplier {

    private static final Logger LOGGER = Logger.getLogger(RemotePassportServerSupplier.class);

    @Override
    public PassportServer getServer() {
        return new RemotePassportServer();
    }

    @Override
    public boolean requiresDatabase() {
        return false;
    }

    @Override
    public String getAlias() {
        return "remote";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
