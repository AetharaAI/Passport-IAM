package org.passport.testsuite.util;

import java.io.InputStream;

import org.passport.adapters.saml.SamlDeployment;
import org.passport.adapters.saml.config.parsers.DeploymentBuilder;
import org.passport.adapters.saml.config.parsers.ResourceLoader;
import org.passport.admin.client.resource.ClientsResource;
import org.passport.dom.saml.v2.metadata.EntityDescriptorType;
import org.passport.dom.saml.v2.metadata.SPSSODescriptorType;
import org.passport.protocol.saml.installation.SamlSPDescriptorClientInstallation;
import org.passport.representations.idm.ClientRepresentation;
import org.passport.saml.common.exceptions.ParsingException;
import org.passport.saml.processing.core.parsers.saml.SAMLParser;
import org.passport.testsuite.utils.arquillian.DeploymentArchiveProcessorUtils;
import org.passport.testsuite.utils.io.IOUtil;

import org.apache.tools.ant.filters.StringInputStream;
import org.w3c.dom.Document;

public class SamlUtils {
    public static SamlDeployment getSamlDeploymentForClient(String client) throws ParsingException {
        InputStream is = SamlUtils.class.getResourceAsStream("/adapter-test/passport-saml/" + client + "/WEB-INF/passport-saml.xml");

        // InputStream -> Document
        Document doc = IOUtil.loadXML(is);

        // Modify saml deployment the same way as before deploying to real app server
        DeploymentArchiveProcessorUtils.modifySAMLDocument(doc);

        // Document -> InputStream
        InputStream isProcessed = IOUtil.documentToInputStream(doc);

        // InputStream -> SamlDeployment
        ResourceLoader loader = new ResourceLoader() {
            @Override
            public InputStream getResourceAsStream(String resource) {
                return getClass().getResourceAsStream("/adapter-test/passport-saml/" + client + resource);
            }
        };
        return new DeploymentBuilder().build(isProcessed, loader);
    }

    public static SPSSODescriptorType getSPInstallationDescriptor(ClientsResource res, String clientId) throws ParsingException {
        String spDescriptorString = res.findByClientId(clientId).stream().findFirst()
                .map(ClientRepresentation::getId)
                .map(res::get)
                .map(clientResource -> clientResource.getInstallationProvider(SamlSPDescriptorClientInstallation.SAML_CLIENT_INSTALATION_SP_DESCRIPTOR))
                .orElseThrow(() -> new RuntimeException("Missing descriptor"));

        SAMLParser parser = SAMLParser.getInstance();
        EntityDescriptorType o = (EntityDescriptorType) parser.parse(new StringInputStream(spDescriptorString));
        return o.getChoiceType().get(0).getDescriptors().get(0).getSpDescriptor();
    }
}
