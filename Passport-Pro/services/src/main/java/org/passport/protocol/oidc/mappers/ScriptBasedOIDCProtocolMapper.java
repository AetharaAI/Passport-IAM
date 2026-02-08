/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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

package org.passport.protocol.oidc.mappers;

import java.util.List;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.models.ClientSessionContext;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperContainerModel;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.RealmModel;
import org.passport.models.ScriptModel;
import org.passport.models.UserModel;
import org.passport.models.UserSessionModel;
import org.passport.protocol.ProtocolMapperConfigException;
import org.passport.protocol.ProtocolMapperUtils;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.provider.ProviderConfigProperty;
import org.passport.provider.ProviderConfigurationBuilder;
import org.passport.representations.AccessTokenResponse;
import org.passport.representations.IDToken;
import org.passport.scripting.EvaluatableScriptAdapter;
import org.passport.scripting.ScriptCompilationException;
import org.passport.scripting.ScriptingProvider;

import org.jboss.logging.Logger;

/**
 * OIDC {@link org.passport.protocol.ProtocolMapper} that uses a provided JavaScript fragment to compute the token claim value.
 *
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ScriptBasedOIDCProtocolMapper extends AbstractOIDCProtocolMapper implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper,
        OIDCAccessTokenResponseMapper, TokenIntrospectionTokenMapper, EnvironmentDependentProviderFactory {

  public static final String PROVIDER_ID = "oidc-script-based-protocol-mapper";

  private static final Logger LOGGER = Logger.getLogger(ScriptBasedOIDCProtocolMapper.class);

  public static final String SCRIPT = "script";

  private static final List<ProviderConfigProperty> configProperties;

  static {

    configProperties = ProviderConfigurationBuilder.create()
      .property()
      .name(SCRIPT)
      .type(ProviderConfigProperty.SCRIPT_TYPE)
      .label("Script")
      .helpText(
        "Script to compute the claim value. \n" + //
          " Available variables: \n" + //
          " 'user' - the current user.\n" + //
          " 'realm' - the current realm.\n" + //
          " 'token' - the current token.\n" + //
          " 'userSession' - the current userSession.\n" + //
          " 'passportSession' - the current passportSession.\n" //
      )
      .defaultValue("/**\n" + //
        " * Available variables: \n" + //
        " * user - the current user\n" + //
        " * realm - the current realm\n" + //
        " * token - the current token\n" + //
        " * userSession - the current userSession\n" + //
        " * passportSession - the current passportSession\n" + //
        " */\n\n\n//insert your code here..." //
      )
      .add()
      .property()
      .name(ProtocolMapperUtils.MULTIVALUED)
      .label(ProtocolMapperUtils.MULTIVALUED_LABEL)
      .helpText(ProtocolMapperUtils.MULTIVALUED_HELP_TEXT)
      .type(ProviderConfigProperty.BOOLEAN_TYPE)
      .defaultValue(false)
      .add()
      .build();

    OIDCAttributeMapperHelper.addAttributeConfig(configProperties, UserPropertyMapper.class);
  }

  public List<ProviderConfigProperty> getConfigProperties() {
    return configProperties;
  }

  @Override
  public String getId() {
    return PROVIDER_ID;
  }

  @Override
  public String getDisplayType() {
    return "Script Mapper";
  }

  @Override
  public String getDisplayCategory() {
    return TOKEN_MAPPER_CATEGORY;
  }

  @Override
  public String getHelpText() {
    return "Evaluates a JavaScript function to produce a token claim based on context information.";
  }

  @Override
  public boolean isSupported(Config.Scope config) {
    return Profile.isFeatureEnabled(Profile.Feature.SCRIPTS);
  }

  @Override
  public int getPriority() {
    return ProtocolMapperUtils.PRIORITY_SCRIPT_MAPPER;
  }

  @Override
  protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession, PassportSession passportSession, ClientSessionContext clientSessionCtx) {
    Object claimValue = evaluateScript(token, mappingModel, userSession, passportSession);
    OIDCAttributeMapperHelper.mapClaim(token, mappingModel, claimValue);
  }

  @Override
  protected void setClaim(AccessTokenResponse accessTokenResponse, ProtocolMapperModel mappingModel, UserSessionModel userSession,
          PassportSession passportSession, ClientSessionContext clientSessionCtx) {
    Object claimValue = evaluateScript(accessTokenResponse, mappingModel, userSession, passportSession);
    OIDCAttributeMapperHelper.mapClaim(accessTokenResponse, mappingModel, claimValue);
  }

  private Object evaluateScript(Object tokenBinding, ProtocolMapperModel mappingModel, UserSessionModel userSession, PassportSession passportSession) {
    UserModel user = userSession.getUser();
    String scriptSource = getScriptCode(mappingModel);
    RealmModel realm = userSession.getRealm();

    ScriptingProvider scripting = passportSession.getProvider(ScriptingProvider.class);
    ScriptModel scriptModel = scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, "token-mapper-script_" + mappingModel.getName(), scriptSource, null);

    EvaluatableScriptAdapter script = scripting.prepareEvaluatableScript(scriptModel);

    Object claimValue;
    try {
      claimValue = script.eval((bindings) -> {
        bindings.put("user", user);
        bindings.put("realm", realm);
        if (tokenBinding instanceof IDToken) {
          bindings.put("token", tokenBinding);
        } else if (tokenBinding instanceof AccessTokenResponse) {
          bindings.put("tokenResponse", tokenBinding);
        }
        bindings.put("userSession", userSession);
        bindings.put("passportSession", passportSession);
      });
    } catch (Exception ex) {
      LOGGER.error("Error during execution of ProtocolMapper script", ex);
      claimValue = null;
    }

    return claimValue;
  }

  @Override
  public void validateConfig(PassportSession session, RealmModel realm, ProtocolMapperContainerModel client, ProtocolMapperModel mapperModel) throws ProtocolMapperConfigException {

    String scriptCode = getScriptCode(mapperModel);
    if (scriptCode == null) {
      return;
    }

    ScriptingProvider scripting = session.getProvider(ScriptingProvider.class);
    ScriptModel scriptModel = scripting.createScript(realm.getId(), ScriptModel.TEXT_JAVASCRIPT, mapperModel.getName() + "-script", scriptCode, "");

    try {
      scripting.prepareEvaluatableScript(scriptModel);
    } catch (ScriptCompilationException  ex) {
      throw new ProtocolMapperConfigException("error", "{0}", ex.getMessage());
    }
  }

  protected String getScriptCode(ProtocolMapperModel mapperModel) {
    return mapperModel.getConfig().get(SCRIPT);
  }

  public static ProtocolMapperModel create(String name,
                                           String userAttribute,
                                           String tokenClaimName, String claimType,
                                           boolean accessToken, boolean idToken, boolean introspectionEndpoint, String script, boolean multiValued) {
    ProtocolMapperModel mapper = OIDCAttributeMapperHelper.createClaimMapper(name, userAttribute,
      tokenClaimName, claimType,
      accessToken, idToken,  introspectionEndpoint,
      script);

    mapper.getConfig().put(ProtocolMapperUtils.MULTIVALUED, String.valueOf(multiValued));

    return mapper; 
  }
}
