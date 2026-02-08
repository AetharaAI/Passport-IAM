package org.passport.protocol.docker.mapper;

import java.util.Collections;
import java.util.List;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.protocol.ProtocolMapper;
import org.passport.protocol.docker.DockerAuthV2Protocol;
import org.passport.provider.ProviderConfigProperty;

public abstract class DockerAuthV2ProtocolMapper implements ProtocolMapper {

    public static final String DOCKER_AUTH_V2_CATEGORY = "Docker Auth Mapper";

    @Override
    public String getProtocol() {
        return DockerAuthV2Protocol.LOGIN_PROTOCOL;
    }

    @Override
    public String getDisplayCategory() {
        return DOCKER_AUTH_V2_CATEGORY;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return Collections.emptyList();
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public final ProtocolMapper create(final PassportSession session) {
        throw new UnsupportedOperationException("The create method is not supported by this mapper");
    }

    @Override
    public void init(final Config.Scope config) {
        // no-op
    }

    @Override
    public void postInit(final PassportSessionFactory factory) {
        // no-op
    }
}
