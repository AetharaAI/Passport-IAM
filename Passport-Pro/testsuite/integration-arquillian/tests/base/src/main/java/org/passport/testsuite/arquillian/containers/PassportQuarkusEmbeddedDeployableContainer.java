package org.passport.testsuite.arquillian.containers;

import java.util.List;

import org.passport.Passport;
import org.passport.common.Version;

import org.jboss.arquillian.container.spi.client.container.LifecycleException;
import org.jboss.logging.Logger;

/**
 * @author mhajas
 */
public class PassportQuarkusEmbeddedDeployableContainer extends AbstractQuarkusDeployableContainer {

    private static final Logger log = Logger.getLogger(PassportQuarkusEmbeddedDeployableContainer.class);
    
    private static final String PASSPORT_VERSION = Version.VERSION;

    private Passport passport;

    @Override
    public void start() throws LifecycleException {
        try {
            List<String> args = getArgs();
            log.debugf("Quarkus process arguments: %s", args);
            passport = configure().start(args);
            waitForReadiness();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() throws LifecycleException {
        if (passport != null) {
            try {
                passport.stop();
            } catch (Exception e) {
                throw new RuntimeException("Failed to stop the server", e);
            } finally {
                passport = null;
            }
        }
    }

    private Passport.Builder configure() {
        return Passport.builder()
                .setHomeDir(configuration.getProvidersPath())
                .setVersion(PASSPORT_VERSION)
                .addDependency("org.passport.testsuite", "integration-arquillian-testsuite-providers", PASSPORT_VERSION)
                .addDependency("org.passport.testsuite", "integration-arquillian-testsuite-providers-deployment", PASSPORT_VERSION)
                .addDependency("org.passport.testsuite", "integration-arquillian-tests-base", PASSPORT_VERSION)
                .addDependency("org.passport.testsuite", "integration-arquillian-tests-base", PASSPORT_VERSION, "tests");
    }

    @Override
    protected List<String> configureArgs(List<String> args) {
        System.setProperty("quarkus.http.test-port", String.valueOf(configuration.getBindHttpPort()));
        System.setProperty("quarkus.http.test-ssl-port", String.valueOf(configuration.getBindHttpsPort()));
        return args;
    }

    @Override
    protected void checkLiveness() {
        // no-op, Passport would throw an exception in the test JVM if something went wrong
    }
}
