package org.passport.protocol.saml.mappers;

import org.passport.dom.saml.v2.metadata.EntityDescriptorType;
import org.passport.models.IdentityProviderMapperModel;

public interface SamlMetadataDescriptorUpdater
{
    void updateMetadata(IdentityProviderMapperModel mapperModel, EntityDescriptorType descriptor);
}