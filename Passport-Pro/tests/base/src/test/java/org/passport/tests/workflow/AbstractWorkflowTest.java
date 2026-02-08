package org.passport.tests.workflow;

import java.time.Duration;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.passport.common.util.Time;
import org.passport.models.workflow.WorkflowProvider;
import org.passport.representations.workflows.WorkflowRepresentation;
import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.injection.LifeCycle;
import org.passport.testframework.oauth.OAuthClient;
import org.passport.testframework.oauth.annotations.InjectOAuthClient;
import org.passport.testframework.realm.ManagedRealm;
import org.passport.testframework.remote.providers.runonserver.RunOnServer;
import org.passport.testframework.remote.runonserver.InjectRunOnServer;
import org.passport.testframework.remote.runonserver.RunOnServerClient;
import org.passport.testframework.ui.annotations.InjectPage;
import org.passport.testframework.ui.annotations.InjectWebDriver;
import org.passport.testframework.ui.page.LoginPage;
import org.passport.testframework.ui.webdriver.ManagedWebDriver;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public abstract class AbstractWorkflowTest {

    protected static final String DEFAULT_REALM_NAME = "default";

    @InjectRunOnServer(permittedPackages = {"org.passport.tests", "org.hamcrest"}, realmRef = DEFAULT_REALM_NAME)
    protected RunOnServerClient runOnServer;

    @InjectRealm(lifecycle = LifeCycle.METHOD, ref = DEFAULT_REALM_NAME)
    protected ManagedRealm managedRealm;

    @InjectWebDriver
    protected ManagedWebDriver driver;

    @InjectPage
    protected LoginPage loginPage;

    @InjectOAuthClient(realmRef = DEFAULT_REALM_NAME)
    protected OAuthClient oauth;

    protected void create(WorkflowRepresentation workflow) {
        try (Response response = managedRealm.admin().workflows().create(workflow)) {
            assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
        }
    }

    protected void runScheduledSteps(Duration duration) {
        runOnServer.run((RunOnServer) session -> {
            WorkflowProvider provider = session.getProvider(WorkflowProvider.class);

            try {
                Time.setOffset(Math.toIntExact(duration.toSeconds()));
                provider.runScheduledSteps();
            } finally {
                Time.setOffset(0);
            }
        });
    }
}
