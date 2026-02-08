package org.passport.testframework.database;

import org.passport.testframework.util.ContainerImages;

import org.jboss.logging.Logger;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.utility.DockerImageName;

public class EnterpriseDbTestDatabase extends AbstractContainerTestDatabase {
    private static final Logger LOGGER = Logger.getLogger(EnterpriseDbTestDatabase.class);

    public static final String NAME = "edb";

    @Override
    public JdbcDatabaseContainer<?> createContainer() {
        return new PassportEnterpriseDbContainer(DockerImageName.parse(ContainerImages.getContainerImageName(NAME)));
    }

    @Override
    public String getDatabaseVendor() {
        return "postgres";
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }
}
