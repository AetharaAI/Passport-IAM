package org.passport.models.mapper;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.passport.models.ClientModel;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;
import org.passport.representations.admin.v2.BaseClientRepresentation;

/**
 * @author Vaclav Muzikar <vmuzikar@redhat.com>
 */
public abstract class BaseClientModelMapper<T extends BaseClientRepresentation> implements ClientModelMapper {
    protected final PassportSession session;

    public BaseClientModelMapper(PassportSession session) {
        this.session = session;
    }

    @Override
    public BaseClientRepresentation fromModel(ClientModel model) {
        // We don't want reps to depend on any unnecessary fields deps, hence no generated builder.

        T rep = createClientRepresentation();

        rep.setEnabled(model.isEnabled());
        rep.setClientId(model.getClientId());
        rep.setDescription(model.getDescription());
        rep.setDisplayName(model.getName());
        rep.setAppUrl(model.getBaseUrl());
        rep.setRedirectUris(new HashSet<>(model.getRedirectUris()));
        rep.setRoles(model.getRolesStream().map(RoleModel::getName).collect(Collectors.toSet()));

        fromModelSpecific(model, rep);

        return rep;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ClientModel toModel(BaseClientRepresentation rep, ClientModel existingModel) {
        if (existingModel == null) {
            existingModel = createClientModel(rep);
        }

        existingModel.setEnabled(Boolean.TRUE.equals(rep.getEnabled()));
        existingModel.setClientId(rep.getClientId());
        existingModel.setDescription(rep.getDescription());
        existingModel.setName(rep.getDisplayName());
        existingModel.setBaseUrl(rep.getAppUrl());
        existingModel.setRedirectUris(new HashSet<>(rep.getRedirectUris()));
        // Roles are not handled here

        toModelSpecific((T) rep, existingModel);

        return existingModel;
    }

    protected ClientModel createClientModel(BaseClientRepresentation rep) {
        RealmModel realm = session.getContext().getRealm();

        // dummy add/remove to obtain a detached model
        var model = realm.addClient(rep.getClientId());
        realm.removeClient(model.getId());
        return model;
    }

    protected abstract T createClientRepresentation();

    protected abstract void fromModelSpecific(ClientModel model, T rep);

    protected abstract void toModelSpecific(T rep, ClientModel model);

    @Override
    public void close() {
    }
}
