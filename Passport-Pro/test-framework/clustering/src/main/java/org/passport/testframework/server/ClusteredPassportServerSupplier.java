package org.passport.testframework.server;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

public class ClusteredPassportServerSupplier extends AbstractPassportServerSupplier {

    private static final Logger LOGGER = Logger.getLogger(ClusteredPassportServerSupplier.class);

    @ConfigProperty(name = "numContainer", defaultValue = "2")
    int numContainers = 2;

    @ConfigProperty(name = "images", defaultValue = ClusteredPassportServer.SNAPSHOT_IMAGE)
    String images = ClusteredPassportServer.SNAPSHOT_IMAGE;

    @Override
    public PassportServer getServer() {
        return new ClusteredPassportServer(numContainers, images);
    }

    @Override
    public boolean requiresDatabase() {
        return true;
    }

    @Override
    public String getAlias() {
        return "cluster";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
