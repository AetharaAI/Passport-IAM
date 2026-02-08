package org.passport.testframework.server;

public interface PassportServer {

    void start(PassportServerConfigBuilder passportServerConfigBuilder, boolean tlsEnabled);

    void stop();

    String getBaseUrl();

    String getManagementBaseUrl();

}
