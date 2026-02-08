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

package org.passport.protocol.saml.installation;

import java.net.URI;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.passport.Config;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.RealmModel;
import org.passport.protocol.ClientInstallationProvider;
import org.passport.protocol.saml.SamlClient;
import org.passport.protocol.saml.SamlProtocol;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class PassportSamlSubsystemInstallation implements ClientInstallationProvider {

    @Override
    public Response generateInstallation(PassportSession session, RealmModel realm, ClientModel client, URI baseUri) {
        SamlClient samlClient = new SamlClient(client);
        StringBuilder buffer = new StringBuilder();
        buffer.append("<secure-deployment name=\"YOUR-WAR.war\">\n");
        PassportSamlClientInstallation.baseXml(session, realm, client, baseUri, samlClient, buffer);
        buffer.append("</secure-deployment>\n");
        return Response.ok(buffer.toString(), MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public String getProtocol() {
        return SamlProtocol.LOGIN_PROTOCOL;
    }

    @Override
    public String getDisplayType() {
        return "Passport SAML JBoss Subsystem XML";
    }

    @Override
    public String getHelpText() {
        return "Passport SAML adapter JBoss subsystem xml you must edit. Put this into <subsystem xmlns=\"urn:jboss:domain:passport-saml:1.2\"> element of your standalone.xml file.";
    }

    @Override
    public String getFilename() {
        return "passport-saml-subsystem.xml";
    }

    @Override
    public String getMediaType() {
        return MediaType.APPLICATION_XML;
    }

    @Override
    public boolean isDownloadOnly() {
        return false;
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
        return "passport-saml-subsystem";
    }
}
