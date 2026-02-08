package org.passport.models.mapper;

import org.passport.models.ClientModel;
import org.passport.provider.Provider;
import org.passport.representations.admin.v2.BaseClientRepresentation;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public interface ClientModelMapper extends Provider, RepModelMapper<BaseClientRepresentation, ClientModel> {
}
