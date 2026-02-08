/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.passport.protocol.oidc.installation;

import java.net.URI;
import java.util.Map;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.Config;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.protocol.ClientInstallationProvider;
import org.passport.protocol.oidc.OIDCLoginProtocol;

import static org.passport.protocol.util.ClientCliInstallationUtil.quote;

public class PassportOIDCJbossSubsystemClientCliInstallation implements ClientInstallationProvider {

    @Override
    public Response generateInstallation(PassportSession session, RealmModel realm, ClientModel client, URI baseUri) {
        String deploymentName = "WAR MODULE NAME.war";
        StringBuilder builder = new StringBuilder();
        
        builder
                .append("/subsystem=passport/secure-deployment=").append(quote(deploymentName)).append("/:add( \\\n")
                .append("    realm=").append(quote(realm.getName())).append(", \\\n")
                .append("    resource=").append(quote(client.getClientId())).append(", \\\n")
                .append("    auth-server-url=").append(baseUri).append(", \\\n");

        if (client.isBearerOnly()){
            builder.append("    bearer-only=true, \\\n");
        } else if (client.isPublicClient()) {
            builder.append("    public-client=true, \\\n");
        }

        if (PassportOIDCClientInstallation.showVerifyTokenAudience(client)) {
            builder.append("    verify-token-audience=true, \\\n");
        }
        if (client.getRolesStream().count() > 0) {
            builder.append("    use-resource-role-mappings=true, \\\n");
        }
        builder.append("    ssl-required=").append(realm.getSslRequired().name()).append(")\n\n");


        if (PassportOIDCClientInstallation.showClientCredentialsAdapterConfig(client)) {
            Map<String, Object> adapterConfig = PassportOIDCClientInstallation.getClientCredentialsAdapterConfig(session, client);
            for (Map.Entry<String, Object> entry : adapterConfig.entrySet()) {
                builder.append("/subsystem=passport/secure-deployment=").append(quote(deploymentName)).append("/")
                       .append("credential=").append(entry.getKey()).append(":add(value=").append(entry.getValue())
                       .append(")\n");
            }
        }
        return Response.ok(builder.toString(), MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public String getProtocol() {
        return OIDCLoginProtocol.LOGIN_PROTOCOL;
    }

    @Override
    public String getDisplayType() {
        return "Passport OIDC JBoss Subsystem CLI";
    }

    @Override
    public String getHelpText() {
        return "CLI script you must edit and apply to your client app server. This type of configuration is useful when you can't or don't want to crack open your WAR file.";
    }

    @Override
    public void close() {

    }

    @Override
    public ClientInstallationProvider create(PassportSession session) {
        return this;
    }

    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public String getId() {
        return "passport-oidc-jboss-subsystem-cli";
    }

    @Override
    public boolean isDownloadOnly() {
        return false;
    }

    @Override
    public String getFilename() {
        return "passport-oidc-subsystem.cli";
    }

    @Override
    public String getMediaType() {
        return MediaType.TEXT_PLAIN;
    }
}
