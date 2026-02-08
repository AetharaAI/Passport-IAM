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

package org.passport.federation.kerberos;

import java.util.List;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.common.constants.KerberosConstants;
import org.passport.component.ComponentModel;
import org.passport.component.ComponentValidationException;
import org.passport.federation.kerberos.impl.KerberosServerSubjectAuthenticator;
import org.passport.federation.kerberos.impl.KerberosUsernamePasswordAuthenticator;
import org.passport.federation.kerberos.impl.SPNEGOAuthenticator;
import org.passport.models.AuthenticationExecutionModel;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.LDAPConstants;
import org.passport.models.RealmModel;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;
import org.passport.representations.idm.CredentialRepresentation;
import org.passport.storage.UserStorageProvider;
import org.passport.storage.UserStorageProviderFactory;
import org.passport.storage.UserStorageProviderModel;
import org.passport.utils.CredentialHelper;

import org.jboss.logging.Logger;

/**
 * Factory for standalone Kerberos federation provider. Standalone means that it's not backed by LDAP. For Kerberos backed by LDAP (like MS AD or ApacheDS environment)
 * you should rather use LDAP Federation Provider.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class KerberosFederationProviderFactory implements UserStorageProviderFactory<KerberosFederationProvider>, EnvironmentDependentProviderFactory {

    private static final Logger logger = Logger.getLogger(KerberosFederationProviderFactory.class);
    public static final String PROVIDER_NAME = "kerberos";

    @Override
    public KerberosFederationProvider create(PassportSession session, ComponentModel model) {
        return new KerberosFederationProvider(session, new UserStorageProviderModel(model), this);
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.KERBEROS);
    }

    protected static final List<ProviderConfigProperty> configProperties;

    static {
        configProperties = getConfigProps();
    }

    private static List<ProviderConfigProperty> getConfigProps() {
        return ProviderConfigurationBuilder.create()
                .property().name(KerberosConstants.KERBEROS_REALM)
                .label("kerberos-realm")
                .helpText("kerberos-realm.tooltip")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.SERVER_PRINCIPAL)
                .label("server-principal")
                .helpText("server-principal.tooltip")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.KEYTAB)
                .label("keytab")
                .helpText("keytab.tooltip")
                .type(ProviderConfigProperty.STRING_TYPE)
                .add()
                .property().name(KerberosConstants.DEBUG)
                .label("debug")
                .helpText("debug.tooltip")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(KerberosConstants.ALLOW_PASSWORD_AUTHENTICATION)
                .label("allow-password-authentication")
                .helpText("allow-password-authentication.tooltip")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .property().name(LDAPConstants.EDIT_MODE)
                .label("edit-mode")
                .helpText("edit-mode.tooltip")
                .type(ProviderConfigProperty.LIST_TYPE)
                .options(UserStorageProvider.EditMode.READ_ONLY.toString(), UserStorageProvider.EditMode.UNSYNCED.toString())
                .add()
                .property().name(KerberosConstants.UPDATE_PROFILE_FIRST_LOGIN)
                .label("update-profile-first-login")
                .helpText("update-profile-first-login.tooltip")
                .type(ProviderConfigProperty.BOOLEAN_TYPE)
                .defaultValue("false")
                .add()
                .build();
    }

     @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configProperties;
    }


    @Override
    public void init(Config.Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    protected SPNEGOAuthenticator createSPNEGOAuthenticator(String spnegoToken, CommonKerberosConfig kerberosConfig) {
        KerberosServerSubjectAuthenticator kerberosAuth = createKerberosSubjectAuthenticator(kerberosConfig);
        return new SPNEGOAuthenticator(kerberosConfig, kerberosAuth, spnegoToken);
    }

    protected KerberosServerSubjectAuthenticator createKerberosSubjectAuthenticator(CommonKerberosConfig kerberosConfig) {
        return new KerberosServerSubjectAuthenticator(kerberosConfig);
    }

    protected KerberosUsernamePasswordAuthenticator createKerberosUsernamePasswordAuthenticator(CommonKerberosConfig kerberosConfig) {
        return new KerberosUsernamePasswordAuthenticator(kerberosConfig);
    }

    @Override
    public void onCreate(PassportSession session, RealmModel realm, ComponentModel model) {
        CredentialHelper.setOrReplaceAuthenticationRequirement(session, realm, CredentialRepresentation.KERBEROS,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE, AuthenticationExecutionModel.Requirement.DISABLED);
    }

    @Override
    public void onUpdate(PassportSession session, RealmModel realm, ComponentModel oldModel, ComponentModel newModel) {
        CredentialHelper.setOrReplaceAuthenticationRequirement(session, realm, CredentialRepresentation.KERBEROS,
                AuthenticationExecutionModel.Requirement.ALTERNATIVE, AuthenticationExecutionModel.Requirement.DISABLED);
    }

    @Override
    public void preRemove(PassportSession session, RealmModel realm, ComponentModel model) {
        CredentialHelper.setOrReplaceAuthenticationRequirement(session, realm, CredentialRepresentation.KERBEROS,
                AuthenticationExecutionModel.Requirement.DISABLED, null);
    }

    @Override
    public void validateConfiguration(PassportSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        // Trim whitespace from string configuration values
        trimConfigValue(config, KerberosConstants.SERVER_PRINCIPAL);
        trimConfigValue(config, KerberosConstants.KERBEROS_REALM);
        trimConfigValue(config, KerberosConstants.KEYTAB);
    }

    private void trimConfigValue(ComponentModel config, String configKey) {
        String value = config.getConfig().getFirst(configKey);
        if (value != null) {
            String trimmedValue = value.trim();
            if (!value.equals(trimmedValue)) {
                // Update the config with trimmed value
                config.getConfig().putSingle(configKey, trimmedValue);
            }
        }
    }
}
