package org.passport.testframework.server;

import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.passport.Passport;
import org.passport.common.Version;
import org.passport.it.utils.Maven;

import io.quarkus.maven.dependency.Dependency;
import org.eclipse.aether.artifact.Artifact;

public class EmbeddedPassportServer implements PassportServer {

    private Passport passport;
    private boolean tlsEnabled = false;

    @Override
    public void start(PassportServerConfigBuilder passportServerConfigBuilder, boolean tlsEnabled) {
        Passport.Builder builder = Passport.builder().setVersion(Version.VERSION);
        this.tlsEnabled = tlsEnabled;

        for(Dependency dependency : passportServerConfigBuilder.toDependencies()) {
            var version = Optional.ofNullable(Maven.getArtifact(dependency.getGroupId(), dependency.getArtifactId()))
                    .map(Artifact::getVersion)
                    .orElse("");
            builder.addDependency(dependency.getGroupId(), dependency.getArtifactId(), version);
        }

        passport = builder.start(passportServerConfigBuilder.toArgs());
    }

    @Override
    public void stop() {
        try {
            passport.stop();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:8443";
        } else {
            return "http://localhost:8080";
        }
    }

    @Override
    public String getManagementBaseUrl() {
        if (tlsEnabled) {
            return "https://localhost:9001";
        } else {
            return "http://localhost:9001";
        }
    }
}
