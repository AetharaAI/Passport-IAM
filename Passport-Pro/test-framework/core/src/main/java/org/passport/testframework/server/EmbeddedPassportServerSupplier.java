package org.passport.testframework.server;

import org.jboss.logging.Logger;

public class EmbeddedPassportServerSupplier extends AbstractPassportServerSupplier {

    private static final Logger LOGGER = Logger.getLogger(EmbeddedPassportServerSupplier.class);

    @Override
    public PassportServer getServer() {
        return new EmbeddedPassportServer();
    }

    @Override
    public boolean requiresDatabase() {
        return true;
    }

    @Override
    public String getAlias() {
        return "embedded";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
