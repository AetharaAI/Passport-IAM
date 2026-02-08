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
package org.passport.broker.saml;

import java.net.URI;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.parsers.ParserConfigurationException;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import jakarta.xml.soap.SOAPException;
import jakarta.xml.soap.SOAPMessage;

import org.passport.broker.provider.AbstractIdentityProvider;
import org.passport.broker.provider.AuthenticationRequest;
import org.passport.broker.provider.BrokeredIdentityContext;
import org.passport.broker.provider.IdentityBrokerException;
import org.passport.broker.provider.IdentityProviderDataMarshaller;
import org.passport.broker.provider.IdentityProviderMapper;
import org.passport.common.util.PemUtils;
import org.passport.crypto.Algorithm;
import org.passport.crypto.KeyUse;
import org.passport.crypto.KeyWrapper;
import org.passport.dom.saml.v2.assertion.AssertionType;
import org.passport.dom.saml.v2.assertion.AuthnStatementType;
import org.passport.dom.saml.v2.assertion.NameIDType;
import org.passport.dom.saml.v2.assertion.SubjectType;
import org.passport.dom.saml.v2.metadata.AttributeConsumingServiceType;
import org.passport.dom.saml.v2.metadata.EndpointType;
import org.passport.dom.saml.v2.metadata.EntityDescriptorType;
import org.passport.dom.saml.v2.metadata.KeyDescriptorType;
import org.passport.dom.saml.v2.metadata.KeyTypes;
import org.passport.dom.saml.v2.metadata.LocalizedNameType;
import org.passport.dom.saml.v2.protocol.ArtifactResolveType;
import org.passport.dom.saml.v2.protocol.AuthnRequestType;
import org.passport.dom.saml.v2.protocol.LogoutRequestType;
import org.passport.dom.saml.v2.protocol.ResponseType;
import org.passport.events.EventBuilder;
import org.passport.http.simple.SimpleHttp;
import org.passport.keys.PublicKeyStorageProvider;
import org.passport.keys.PublicKeyStorageUtils;
import org.passport.models.FederatedIdentityModel;
import org.passport.models.IdentityProviderMapperModel;
import org.passport.models.KeyManager;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.UserSessionModel;
import org.passport.protocol.LoginProtocol;
import org.passport.protocol.oidc.OIDCLoginProtocol;
import org.passport.protocol.saml.JaxrsSAML2BindingBuilder;
import org.passport.protocol.saml.SAMLEncryptionAlgorithms;
import org.passport.protocol.saml.SamlMetadataPublicKeyLoader;
import org.passport.protocol.saml.SamlProtocol;
import org.passport.protocol.saml.SamlService;
import org.passport.protocol.saml.SamlSessionUtils;
import org.passport.protocol.saml.mappers.SamlMetadataDescriptorUpdater;
import org.passport.protocol.saml.preprocessor.SamlAuthenticationPreprocessor;
import org.passport.protocol.saml.profile.util.Soap;
import org.passport.saml.SAML2ArtifactResolveRequestBuilder;
import org.passport.saml.SAML2AuthnRequestBuilder;
import org.passport.saml.SAML2LogoutRequestBuilder;
import org.passport.saml.SAML2NameIDPolicyBuilder;
import org.passport.saml.SAML2RequestedAuthnContextBuilder;
import org.passport.saml.SPMetadataDescriptor;
import org.passport.saml.SamlProtocolExtensionsAwareBuilder.NodeGenerator;
import org.passport.saml.SignatureAlgorithm;
import org.passport.saml.common.constants.GeneralConstants;
import org.passport.saml.common.constants.JBossSAMLURIConstants;
import org.passport.saml.common.exceptions.ConfigurationException;
import org.passport.saml.common.exceptions.ParsingException;
import org.passport.saml.common.exceptions.ProcessingException;
import org.passport.saml.processing.api.saml.v2.request.SAML2Request;
import org.passport.saml.processing.api.saml.v2.response.SAML2Response;
import org.passport.saml.processing.core.saml.v2.common.SAMLDocumentHolder;
import org.passport.saml.processing.core.saml.v2.util.SAMLMetadataUtil;
import org.passport.saml.processing.core.util.PassportKeySamlExtensionGenerator;
import org.passport.saml.validators.DestinationValidator;
import org.passport.sessions.AuthenticationSessionModel;
import org.passport.util.Booleans;
import org.passport.util.JsonSerialization;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Pedro Igor
 */
public class SAMLIdentityProvider extends AbstractIdentityProvider<SAMLIdentityProviderConfig> {
    protected static final Logger logger = Logger.getLogger(SAMLIdentityProvider.class);

    private final DestinationValidator destinationValidator;
    public SAMLIdentityProvider(PassportSession session, SAMLIdentityProviderConfig config, DestinationValidator destinationValidator) {
        super(session, config);
        this.destinationValidator = destinationValidator;
    }

    @Override
    public Object callback(RealmModel realm, AuthenticationCallback callback, EventBuilder event) {
        return new SAMLEndpoint(session, this, getConfig(), callback, destinationValidator);
    }

    @Override
    public Response performLogin(AuthenticationRequest request) {
        try {
            UriInfo uriInfo = request.getUriInfo();
            RealmModel realm = request.getRealm();
            String issuerURL = getEntityId(uriInfo, realm);
            String destinationUrl = getConfig().getSingleSignOnServiceUrl();
            String nameIDPolicyFormat = getConfig().getNameIDPolicyFormat();

            if (nameIDPolicyFormat == null) {
                nameIDPolicyFormat =  JBossSAMLURIConstants.NAMEID_FORMAT_PERSISTENT.get();
            }

            String protocolBinding = JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.get();

            String assertionConsumerServiceUrl = request.getRedirectUri();

            if (getConfig().isArtifactBindingResponse()) {
                protocolBinding = JBossSAMLURIConstants.SAML_HTTP_ARTIFACT_BINDING.get();
            } else if (getConfig().isPostBindingResponse()) {
                protocolBinding = JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.get();
            }

            SAML2RequestedAuthnContextBuilder requestedAuthnContext =
                new SAML2RequestedAuthnContextBuilder()
                    .setComparison(getConfig().getAuthnContextComparisonType());

            for (String authnContextClassRef : getAuthnContextClassRefUris())
                requestedAuthnContext.addAuthnContextClassRef(authnContextClassRef);

            for (String authnContextDeclRef : getAuthnContextDeclRefUris())
                requestedAuthnContext.addAuthnContextDeclRef(authnContextDeclRef);

            Integer attributeConsumingServiceIndex = getConfig().getAttributeConsumingServiceIndex();

            String loginHint = Booleans.isTrue(getConfig().isLoginHint()) ? request.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM) : null;
            Boolean allowCreate = null;
            if (getConfig().getConfig().get(SAMLIdentityProviderConfig.ALLOW_CREATE) == null || getConfig().isAllowCreate())
                allowCreate = Boolean.TRUE;
            LoginProtocol protocol = session.getProvider(LoginProtocol.class, request.getAuthenticationSession().getProtocol());
            Boolean forceAuthn = getConfig().isForceAuthn();
            if (protocol.requireReauthentication(null, request.getAuthenticationSession()))
                forceAuthn = Boolean.TRUE;
            SAML2AuthnRequestBuilder authnRequestBuilder = new SAML2AuthnRequestBuilder()
                    .assertionConsumerUrl(assertionConsumerServiceUrl)
                    .destination(destinationUrl)
                    .issuer(issuerURL)
                    .forceAuthn(forceAuthn)
                    .protocolBinding(protocolBinding)
                    .nameIdPolicy(SAML2NameIDPolicyBuilder
                        .format(nameIDPolicyFormat)
                        .setAllowCreate(allowCreate))
                    .attributeConsumingServiceIndex(attributeConsumingServiceIndex)
                    .requestedAuthnContext(requestedAuthnContext)
                    .subject(loginHint);

            JaxrsSAML2BindingBuilder binding = new JaxrsSAML2BindingBuilder(session)
                    .relayState(request.getState().getEncoded());
            boolean postBinding = getConfig().isPostBindingAuthnRequest();

            if (getConfig().isWantAuthnRequestsSigned()) {
                KeyManager.ActiveRsaKey keys = session.keys().getActiveRsaKey(realm);

                String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keys.getKid(), keys.getCertificate());
                binding.signWith(keyName, keys.getPrivateKey(), keys.getPublicKey(), keys.getCertificate())
                        .signatureAlgorithm(getSignatureAlgorithm())
                        .signDocument();
                if (! postBinding && getConfig().isAddExtensionsElementWithKeyInfo()) {    // Only include extension if REDIRECT binding and signing whole SAML protocol message
                    authnRequestBuilder.addExtension(new PassportKeySamlExtensionGenerator(keyName));
                }
            }

            AuthnRequestType authnRequest = authnRequestBuilder.createAuthnRequest();
            for(Iterator<SamlAuthenticationPreprocessor> it = SamlSessionUtils.getSamlAuthenticationPreprocessorIterator(session); it.hasNext(); ) {
                authnRequest = it.next().beforeSendingLoginRequest(authnRequest, request.getAuthenticationSession());
            }

            if (authnRequest.getDestination() != null) {
                destinationUrl = authnRequest.getDestination().toString();
            }

            // Save the current RequestID in the Auth Session as we need to verify it against the ID returned from the IdP
            request.getAuthenticationSession().setClientNote(SamlProtocol.SAML_REQUEST_ID_BROKER, authnRequest.getID());

            if (postBinding) {
                return binding.postBinding(SAML2Request.convert(authnRequest)).request(destinationUrl);
            } else {
                return binding.redirectBinding(SAML2Request.convert(authnRequest)).request(destinationUrl);
            }
        } catch (Exception e) {
            throw new IdentityBrokerException("Could not create authentication request.", e);
        }
    }

    private String getEntityId(UriInfo uriInfo, RealmModel realm) {
        String configEntityId = getConfig().getEntityId();

        if (configEntityId == null || configEntityId.isEmpty())
            return UriBuilder.fromUri(uriInfo.getBaseUri()).path("realms").path(realm.getName()).build().toString();
        else
            return configEntityId;
    }

    private List<String> getAuthnContextClassRefUris() {
        String authnContextClassRefs = getConfig().getAuthnContextClassRefs();
        if (authnContextClassRefs == null || authnContextClassRefs.isEmpty())
            return new LinkedList<String>();

        try {
            return Arrays.asList(JsonSerialization.readValue(authnContextClassRefs, String[].class));
        } catch (Exception e) {
            logger.warn("Could not json-deserialize AuthContextClassRefs config entry: " + authnContextClassRefs, e);
            return new LinkedList<String>();
        }
    }

    private List<String> getAuthnContextDeclRefUris() {
        String authnContextDeclRefs = getConfig().getAuthnContextDeclRefs();
        if (authnContextDeclRefs == null || authnContextDeclRefs.isEmpty())
            return new LinkedList<String>();

        try {
            return Arrays.asList(JsonSerialization.readValue(authnContextDeclRefs, String[].class));
        } catch (Exception e) {
            logger.warn("Could not json-deserialize AuthContextDeclRefs config entry: " + authnContextDeclRefs, e);
            return new LinkedList<String>();
        }
    }

    @Override
    public void authenticationFinished(AuthenticationSessionModel authSession, BrokeredIdentityContext context)  {
        ResponseType responseType = (ResponseType)context.getContextData().get(SAMLEndpoint.SAML_LOGIN_RESPONSE);
        AssertionType assertion = (AssertionType)context.getContextData().get(SAMLEndpoint.SAML_ASSERTION);
        SubjectType subject = assertion.getSubject();
        SubjectType.STSubType subType = subject.getSubType();
        if (subType != null) {
            NameIDType subjectNameID = (NameIDType) subType.getBaseID();
            authSession.setUserSessionNote(SAMLEndpoint.SAML_FEDERATED_SUBJECT_NAMEID, subjectNameID.serializeAsString());
        }
        AuthnStatementType authn =  (AuthnStatementType)context.getContextData().get(SAMLEndpoint.SAML_AUTHN_STATEMENT);
        if (authn != null && authn.getSessionIndex() != null) {
            authSession.setUserSessionNote(SAMLEndpoint.SAML_FEDERATED_SESSION_INDEX, authn.getSessionIndex());

        }
    }

    @Override
    public Response retrieveToken(PassportSession session, FederatedIdentityModel identity) {
        return Response.ok(identity.getToken()).type(MediaType.TEXT_PLAIN_TYPE).build();
    }

    @Override
    public void backchannelLogout(PassportSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {
        String singleLogoutServiceUrl = getConfig().getSingleLogoutServiceUrl();
        if (singleLogoutServiceUrl == null || singleLogoutServiceUrl.trim().equals("") || !getConfig().isBackchannelSupported()) return;
        JaxrsSAML2BindingBuilder binding = buildLogoutBinding(session, userSession, realm);
        try {
            LogoutRequestType logoutRequest = buildLogoutRequest(userSession, uriInfo, realm, singleLogoutServiceUrl);
            if (logoutRequest.getDestination() != null) {
                singleLogoutServiceUrl = logoutRequest.getDestination().toString();
            }
            int status = SimpleHttp.create(session).doPost(singleLogoutServiceUrl)
                    .param(GeneralConstants.SAML_REQUEST_KEY, binding.postBinding(SAML2Request.convert(logoutRequest)).encoded())
                    .param(GeneralConstants.RELAY_STATE, userSession.getId()).asStatus();
            boolean success = status >=200 && status < 400;
            if (!success) {
                logger.warn("Failed saml backchannel broker logout to: " + singleLogoutServiceUrl);
            }
        } catch (Exception e) {
            logger.warn("Failed saml backchannel broker logout to: " + singleLogoutServiceUrl, e);
        }

    }

    @Override
    public Response passportInitiatedBrowserLogout(PassportSession session, UserSessionModel userSession, UriInfo uriInfo, RealmModel realm) {
        String singleLogoutServiceUrl = getConfig().getSingleLogoutServiceUrl();
        if (singleLogoutServiceUrl == null || singleLogoutServiceUrl.trim().equals("")) return null;

        if (getConfig().isBackchannelSupported()) {
            backchannelLogout(session, userSession, uriInfo, realm);
            return null;
       } else {
            try {
                LogoutRequestType logoutRequest = buildLogoutRequest(userSession, uriInfo, realm, singleLogoutServiceUrl);
                if (logoutRequest.getDestination() != null) {
                    singleLogoutServiceUrl = logoutRequest.getDestination().toString();
                }
                JaxrsSAML2BindingBuilder binding = buildLogoutBinding(session, userSession, realm);
                if (getConfig().isPostBindingLogout()) {
                    return binding.postBinding(SAML2Request.convert(logoutRequest)).request(singleLogoutServiceUrl);
                } else {
                    return binding.redirectBinding(SAML2Request.convert(logoutRequest)).request(singleLogoutServiceUrl);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected LogoutRequestType buildLogoutRequest(UserSessionModel userSession, UriInfo uriInfo, RealmModel realm, String singleLogoutServiceUrl, NodeGenerator... extensions) throws ConfigurationException {
        SAML2LogoutRequestBuilder logoutBuilder = new SAML2LogoutRequestBuilder()
                .assertionExpiration(realm.getAccessCodeLifespan())
                .issuer(getEntityId(uriInfo, realm))
                .sessionIndex(userSession.getNote(SAMLEndpoint.SAML_FEDERATED_SESSION_INDEX))
                .nameId(NameIDType.deserializeFromString(userSession.getNote(SAMLEndpoint.SAML_FEDERATED_SUBJECT_NAMEID)))
                .destination(singleLogoutServiceUrl);
        LogoutRequestType logoutRequest = logoutBuilder.createLogoutRequest();
        for (NodeGenerator extension : extensions) {
            logoutBuilder.addExtension(extension);
        }
        for (Iterator<SamlAuthenticationPreprocessor> it = SamlSessionUtils.getSamlAuthenticationPreprocessorIterator(session); it.hasNext();) {
            logoutRequest = it.next().beforeSendingLogoutRequest(logoutRequest, userSession, null);
        }
        return logoutRequest;
    }

    private JaxrsSAML2BindingBuilder buildLogoutBinding(PassportSession session, UserSessionModel userSession, RealmModel realm) {
        JaxrsSAML2BindingBuilder binding = new JaxrsSAML2BindingBuilder(session)
                .relayState(userSession.getId());
        if (getConfig().isWantAuthnRequestsSigned()) {
            KeyManager.ActiveRsaKey keys = session.keys().getActiveRsaKey(realm);
            String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keys.getKid(), keys.getCertificate());
            binding.signWith(keyName, keys.getPrivateKey(), keys.getPublicKey(), keys.getCertificate())
                    .signatureAlgorithm(getSignatureAlgorithm())
                    .signDocument();
        }
        return binding;
    }

    @Override
    public Response export(UriInfo uriInfo, RealmModel realm, String format) {
        try
        {
            URI endpoint = uriInfo.getBaseUriBuilder()
                    .path("realms").path(realm.getName())
                    .path("broker")
                    .path(getConfig().getAlias())
                    .path("endpoint")
                    .build();

            List<EndpointType> assertionConsumerServices = getConfig().isPostBindingAuthnRequest()
                    ? List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint),
                            new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint),
                            new EndpointType(JBossSAMLURIConstants.SAML_HTTP_ARTIFACT_BINDING.getUri(), endpoint))
                    : List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint),
                            new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint),
                            new EndpointType(JBossSAMLURIConstants.SAML_HTTP_ARTIFACT_BINDING.getUri(), endpoint));

            List<EndpointType> singleLogoutServices = getConfig().isPostBindingLogout()
                    ? List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint),
                            new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint))
                    : List.of(new EndpointType(JBossSAMLURIConstants.SAML_HTTP_REDIRECT_BINDING.getUri(), endpoint),
                            new EndpointType(JBossSAMLURIConstants.SAML_HTTP_POST_BINDING.getUri(), endpoint));

            boolean wantAuthnRequestsSigned = getConfig().isWantAuthnRequestsSigned();
            boolean wantAssertionsSigned = getConfig().isWantAssertionsSigned();
            boolean wantAssertionsEncrypted = getConfig().isWantAssertionsEncrypted();
            String entityId = getEntityId(uriInfo, realm);
            String nameIDPolicyFormat = getConfig().getNameIDPolicyFormat();

            // We export all keys for algorithm RS256, both active and passive so IDP is able to verify signature even
            //  if a key rotation happens in the meantime
            List<KeyDescriptorType> signingKeys = session.keys().getKeysStream(realm, KeyUse.SIG, Algorithm.RS256)
                    .filter(key -> key.getCertificate() != null)
                    .sorted(SamlService::compareKeys)
                    .map(key -> {
                        try {
                            return SPMetadataDescriptor.buildKeyInfoElement(key.getKid(), PemUtils.encodeCertificate(key.getCertificate()));
                        } catch (ParserConfigurationException e) {
                            logger.warn("Failed to export SAML SP Metadata!", e);
                            throw new RuntimeException(e);
                        }
                    })
                    .map(key -> SPMetadataDescriptor.buildKeyDescriptorType(key, KeyTypes.SIGNING, null))
                    .collect(Collectors.toList());

            // We export only active ENC keys so IDP uses different key as soon as possible if a key rotation happens
            String encAlg = getConfig().getEncryptionAlgorithm();
            List<KeyDescriptorType> encryptionKeys = session.keys().getKeysStream(realm)
                    .filter(key -> key.getStatus().isActive() && KeyUse.ENC.equals(key.getUse())
                            && (encAlg == null || Objects.equals(encAlg, key.getAlgorithmOrDefault()))
                            && SAMLEncryptionAlgorithms.forPassportIdentifier(key.getAlgorithm()) != null
                            && key.getCertificate() != null)
                    .sorted(SamlService::compareKeys)
                    .map(key -> {
                        Element keyInfo;
                        try {
                            keyInfo = SPMetadataDescriptor.buildKeyInfoElement(key.getKid(), PemUtils.encodeCertificate(key.getCertificate()));
                        } catch (ParserConfigurationException e) {
                            logger.warn("Failed to export SAML SP Metadata!", e);
                            throw new RuntimeException(e);
                        }

                        return SPMetadataDescriptor.buildKeyDescriptorType(keyInfo, KeyTypes.ENCRYPTION, SAMLEncryptionAlgorithms.forPassportIdentifier(key.getAlgorithm()).getXmlEncIdentifiers());
                    })
                    .collect(Collectors.toList());

            EntityDescriptorType entityDescriptor = SPMetadataDescriptor.buildSPDescriptor(
                assertionConsumerServices, singleLogoutServices,
                wantAuthnRequestsSigned, wantAssertionsSigned, wantAssertionsEncrypted,
                entityId, nameIDPolicyFormat, signingKeys, encryptionKeys, getConfig().getDescriptorCacheSeconds());

            // Create the AttributeConsumingService if at least one attribute importer mapper exists
            List<Entry<IdentityProviderMapperModel, SamlMetadataDescriptorUpdater>> metadataAttrProviders = new ArrayList<>();
            session.identityProviders().getMappersByAliasStream(getConfig().getAlias())
                .forEach(mapper -> {
                    IdentityProviderMapper target = (IdentityProviderMapper) session.getPassportSessionFactory().getProviderFactory(IdentityProviderMapper.class, mapper.getIdentityProviderMapper());
                    if (target instanceof SamlMetadataDescriptorUpdater samlMetadataDescriptorUpdater)
                        metadataAttrProviders.add(new java.util.AbstractMap.SimpleEntry<>(mapper, samlMetadataDescriptorUpdater));
                });

            if (!metadataAttrProviders.isEmpty()) {
                int attributeConsumingServiceIndex = getConfig().getAttributeConsumingServiceIndex() != null ? getConfig().getAttributeConsumingServiceIndex() : 1;
                String attributeConsumingServiceName = getConfig().getAttributeConsumingServiceName();
                //default value for attributeConsumingServiceName
                if (attributeConsumingServiceName == null)
                    attributeConsumingServiceName = realm.getDisplayName() != null ? realm.getDisplayName() : realm.getName() ;
                AttributeConsumingServiceType attributeConsumingService = new AttributeConsumingServiceType(attributeConsumingServiceIndex);
                attributeConsumingService.setIsDefault(true);

                String currentLocale = realm.getDefaultLocale() == null ? "en" : realm.getDefaultLocale();
                LocalizedNameType attributeConsumingServiceNameElement = new LocalizedNameType(currentLocale);
                attributeConsumingServiceNameElement.setValue(attributeConsumingServiceName);
                attributeConsumingService.addServiceName(attributeConsumingServiceNameElement);

                // Look for the SP descriptor and add the attribute consuming service
                for (EntityDescriptorType.EDTChoiceType choiceType : entityDescriptor.getChoiceType()) {
                    List<EntityDescriptorType.EDTDescriptorChoiceType> descriptors = choiceType.getDescriptors();
                    for (EntityDescriptorType.EDTDescriptorChoiceType descriptor : descriptors) {
                        descriptor.getSpDescriptor().addAttributeConsumerService(attributeConsumingService);
                    }
                }

                // Add the attribute mappers
                metadataAttrProviders.forEach(mapper -> {
                    SamlMetadataDescriptorUpdater metadataAttrProvider = mapper.getValue();
                    metadataAttrProvider.updateMetadata(mapper.getKey(), entityDescriptor);
                });
            }

            String descriptor;

            // Metadata signing
            if (getConfig().isSignSpMetadata()) {
                KeyWrapper keyWrapper = session.keys().getActiveKey(realm, KeyUse.SIG, Algorithm.RS256);
                X509Certificate certificate = keyWrapper.getCertificate();
                String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keyWrapper.getKid(), certificate);
                KeyPair keyPair = new KeyPair(certificate.getPublicKey(), (PrivateKey) keyWrapper.getPrivateKey());;

                descriptor = SAMLMetadataUtil.signEntityDescriptorType(entityDescriptor, getSignatureAlgorithm(), keyName, certificate, keyPair);
            } else {
                descriptor = SAMLMetadataUtil.writeEntityDescriptorType(entityDescriptor);
            }

            return Response.ok(descriptor, MediaType.APPLICATION_XML_TYPE).build();
        } catch (Exception e) {
            logger.warn("Failed to export SAML SP Metadata!", e);
            throw new RuntimeException(e);
        }
    }

    public SignatureAlgorithm getSignatureAlgorithm() {
        String alg = getConfig().getSignatureAlgorithm();
        if (alg != null) {
            SignatureAlgorithm algorithm = SignatureAlgorithm.valueOf(alg);
            if (algorithm != null) return algorithm;
        }
        return SignatureAlgorithm.RSA_SHA256;
    }

    @Override
    public IdentityProviderDataMarshaller getMarshaller() {
        return new SAMLDataMarshaller();
    }

    @Override
    public boolean reloadKeys() {
        if (getConfig().isEnabled() && getConfig().isUseMetadataDescriptorUrl()) {
            String modelKey = PublicKeyStorageUtils.getIdpModelCacheKey(session.getContext().getRealm().getId(), getConfig().getInternalId());
            PublicKeyStorageProvider keyStorage = session.getProvider(PublicKeyStorageProvider.class);
            return keyStorage.reloadKeys(modelKey, new SamlMetadataPublicKeyLoader(session, getConfig().getMetadataDescriptorUrl()));
        }
        return false;
    }

    @Override
    public boolean supportsLongStateParameter() {
        // SAML RelayState parameter has limits of 80 bytes per SAML specification
        return false;
    }

    public SAMLDocumentHolder resolveArtifact(PassportSession session, UriInfo uriInfo, RealmModel realm, String relayState, String samlArt) {
        //get the URL of the artifact resolution service provided by the Identity Provider
        String artifactResolutionServiceUrl = getConfig().getArtifactResolutionServiceUrl();
        if (artifactResolutionServiceUrl == null || artifactResolutionServiceUrl.trim().isEmpty()) {
            throw new RuntimeException("Artifact Resolution Service URL is not configured for the Identity Provider.");
        }
        try {
            // create the SAML Request object to resolve an artifact
            ArtifactResolveType artifactResolveRequest = buildArtifactResolveRequest(uriInfo, realm, artifactResolutionServiceUrl, samlArt);
            if (artifactResolveRequest.getDestination() != null) {
                artifactResolutionServiceUrl = artifactResolveRequest.getDestination().toString();
            }

            // convert the SAML Request object to a SAML Document (DOM)
            Document artifactResolveRequestAsDoc = SAML2Request.convert(artifactResolveRequest);

            // convert the SAML Document (DOM) to a SOAP Document (DOM)
            Document soapRequestAsDoc = buildArtifactResolveBinding(session, relayState, realm)
                    .soapBinding(artifactResolveRequestAsDoc).getDocument();

            // execute the SOAP request
            SOAPMessage soapResponse = Soap.createMessage()
                    .addMimeHeader("SOAPAction", "http://www.oasis-open.org/committees/security") // MAY in SOAP binding spec
                    .addToBody(soapRequestAsDoc)
                    .call(artifactResolutionServiceUrl, session);

            // extract the SAML Response (DOM) from the SOAP response
            Document artifactResolveResponseAsDoc = Soap.extractSoapMessage(soapResponse);

            // convert the SAML Response (DOM) to a SAML Response object and return it
            return SAML2Response.getSAML2ObjectFromDocument(artifactResolveResponseAsDoc);
        } catch (SOAPException | ConfigurationException | ProcessingException | ParsingException e) {
            logger.warn("Unable to resolve a SAML artifact to: " + artifactResolutionServiceUrl, e);
            throw new RuntimeException("Unable to resolve a SAML artifact to: " + artifactResolutionServiceUrl, e);
        }
    }

    protected ArtifactResolveType buildArtifactResolveRequest(UriInfo uriInfo, RealmModel realm, String artifactServiceUrl, String artifact, NodeGenerator... extensions) throws ConfigurationException {
        SAML2ArtifactResolveRequestBuilder artifactResolveRequestBuilder = new SAML2ArtifactResolveRequestBuilder()
                .issuer(getEntityId(uriInfo, realm))
                .destination(artifactServiceUrl)
                .artifact(artifact);
        ArtifactResolveType artifactResolveRequest = artifactResolveRequestBuilder.createArtifactResolveRequest();
        for (NodeGenerator extension : extensions) {
            artifactResolveRequestBuilder.addExtension(extension);
        }
        return artifactResolveRequest;
    }

    private JaxrsSAML2BindingBuilder buildArtifactResolveBinding(PassportSession session, String relayState, RealmModel realm) {
        JaxrsSAML2BindingBuilder binding = new JaxrsSAML2BindingBuilder(session).relayState(relayState);
        if (getConfig().isWantAuthnRequestsSigned()) {
            KeyManager.ActiveRsaKey keys = session.keys().getActiveRsaKey(realm);
            String keyName = getConfig().getXmlSigKeyInfoKeyNameTransformer().getKeyName(keys.getKid(), keys.getCertificate());
            binding.signWith(keyName, keys.getPrivateKey(), keys.getPublicKey(), keys.getCertificate())
                    .signatureAlgorithm(getSignatureAlgorithm())
                    .signDocument();
        }
        return binding;
    }
}
