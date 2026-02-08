package org.passport.protocol.docker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.passport.Config;
import org.passport.common.Profile;
import org.passport.events.EventBuilder;
import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.ProtocolMapperModel;
import org.passport.models.RealmModel;
import org.passport.protocol.AbstractLoginProtocolFactory;
import org.passport.protocol.LoginProtocol;
import org.passport.protocol.docker.mapper.AllowAllDockerProtocolMapper;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.representations.idm.ClientRepresentation;

public class DockerAuthV2ProtocolFactory extends AbstractLoginProtocolFactory implements EnvironmentDependentProviderFactory {

    static Map<String, ProtocolMapperModel> builtins = new HashMap<>();
    static List<ProtocolMapperModel> defaultBuiltins = new ArrayList<>();

    static {
        final ProtocolMapperModel addAllRequestedScopeMapper = new ProtocolMapperModel();
        addAllRequestedScopeMapper.setName(AllowAllDockerProtocolMapper.PROVIDER_ID);
        addAllRequestedScopeMapper.setProtocolMapper(AllowAllDockerProtocolMapper.PROVIDER_ID);
        addAllRequestedScopeMapper.setProtocol(DockerAuthV2Protocol.LOGIN_PROTOCOL);
        addAllRequestedScopeMapper.setConfig(Collections.emptyMap());
        builtins.put(AllowAllDockerProtocolMapper.PROVIDER_ID, addAllRequestedScopeMapper);
        defaultBuiltins.add(addAllRequestedScopeMapper);
    }

    @Override
    protected void createDefaultClientScopesImpl(RealmModel newRealm) {
        // no-op
    }

    @Override
    protected void addDefaults(final ClientModel client) {
        defaultBuiltins.forEach(builtinMapper -> client.addProtocolMapper(builtinMapper));
    }

    @Override
    public Map<String, ProtocolMapperModel> getBuiltinMappers() {
        return builtins;
    }

    @Override
    public Object createProtocolEndpoint(final PassportSession session, final EventBuilder event) {
        return new DockerV2LoginProtocolService(session, event);
    }

    @Override
    public void setupClientDefaults(final ClientRepresentation rep, final ClientModel newClient) {
        // no-op
    }


    @Override
    public LoginProtocol create(final PassportSession session) {
        return new DockerAuthV2Protocol().setSession(session);
    }

    @Override
    public String getId() {
        return DockerAuthV2Protocol.LOGIN_PROTOCOL;
    }

    @Override
    public boolean isSupported(Config.Scope config) {
        return Profile.isFeatureEnabled(Profile.Feature.DOCKER);
    }

    @Override
    public int order() {
        return -100;
    }
}
