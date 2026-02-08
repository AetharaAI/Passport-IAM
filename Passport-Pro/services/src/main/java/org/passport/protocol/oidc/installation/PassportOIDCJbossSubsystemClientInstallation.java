/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
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

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PassportOIDCJbossSubsystemClientInstallation implements ClientInstallationProvider {
    @Override
    public Response generateInstallation(PassportSession session, RealmModel realm, ClientModel client, URI baseUri) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<secure-deployment name=\"WAR MODULE NAME.war\">\n");
        buffer.append("    <realm>").append(realm.getName()).append("</realm>\n");
        buffer.append("    <auth-server-url>").append(baseUri.toString()).append("</auth-server-url>\n");
        if (client.isBearerOnly()){
            buffer.append("    <bearer-only>true</bearer-only>\n");

        } else if (client.isPublicClient()) {
            buffer.append("    <public-client>true</public-client>\n");
        }
        buffer.append("    <ssl-required>").append(realm.getSslRequired().name()).append("</ssl-required>\n");
        buffer.append("    <resource>").append(client.getClientId()).append("</resource>\n");

        if (PassportOIDCClientInstallation.showVerifyTokenAudience(client)) {
            buffer.append("    <verify-token-audience>true</verify-token-audience>\n");
        }

        String cred = client.getSecret();
        if (PassportOIDCClientInstallation.showClientCredentialsAdapterConfig(client)) {
            Map<String, Object> adapterConfig = PassportOIDCClientInstallation.getClientCredentialsAdapterConfig(session, client);
            for (Map.Entry<String, Object> entry : adapterConfig.entrySet()) {
                buffer.append("    <credential name=\"" + entry.getKey() + "\">");

                Object value = entry.getValue();
                if (value instanceof Map) {
                    buffer.append("\n");
                    Map<String, Object> asMap = (Map<String, Object>) value;
                    for (Map.Entry<String, Object> credEntry : asMap.entrySet()) {
                        buffer.append("        <" + credEntry.getKey() + ">" + credEntry.getValue().toString() + "</" + credEntry.getKey() + ">\n");
                    }
                    buffer.append("    </credential>\n");
                } else {
                    buffer.append(value.toString()).append("</credential>\n");
                }
            }
        }
        if (client.getRolesStream().count() > 0) {
            buffer.append("    <use-resource-role-mappings>true</use-resource-role-mappings>\n");
        }
        buffer.append("</secure-deployment>\n");
        return Response.ok(buffer.toString(), MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public String getProtocol() {
        return OIDCLoginProtocol.LOGIN_PROTOCOL;
    }

    @Override
    public String getDisplayType() {
        return "Passport OIDC JBoss Subsystem XML";
    }

    @Override
    public String getHelpText() {
        return "XML snippet you must edit and add to the Passport OIDC subsystem on your client app server.  This type of configuration is useful when you can't or don't want to crack open your WAR file.";
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
        return "passport-oidc-jboss-subsystem";
    }

    @Override
    public boolean isDownloadOnly() {
        return false;
    }

    @Override
    public String getFilename() {
        return "passport-oidc-subsystem.xml";
    }

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_XML;
    }
}
