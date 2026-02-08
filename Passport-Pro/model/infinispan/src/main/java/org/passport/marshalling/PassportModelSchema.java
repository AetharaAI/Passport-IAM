/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates
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

package org.passport.marshalling;

import java.util.Objects;
import java.util.Optional;

import org.passport.cluster.infinispan.LockEntry;
import org.passport.cluster.infinispan.LockEntryPredicate;
import org.passport.cluster.infinispan.WrapperClusterEvent;
import org.passport.component.ComponentModel;
import org.passport.jgroups.certificates.ReloadCertificateFunction;
import org.passport.keys.infinispan.PublicKeyStorageInvalidationEvent;
import org.passport.models.UserSessionModel;
import org.passport.models.cache.infinispan.ClearCacheEvent;
import org.passport.models.cache.infinispan.authorization.events.PermissionTicketRemovedEvent;
import org.passport.models.cache.infinispan.authorization.events.PermissionTicketUpdatedEvent;
import org.passport.models.cache.infinispan.authorization.events.PolicyRemovedEvent;
import org.passport.models.cache.infinispan.authorization.events.PolicyUpdatedEvent;
import org.passport.models.cache.infinispan.authorization.events.ResourceRemovedEvent;
import org.passport.models.cache.infinispan.authorization.events.ResourceServerRemovedEvent;
import org.passport.models.cache.infinispan.authorization.events.ResourceServerUpdatedEvent;
import org.passport.models.cache.infinispan.authorization.events.ResourceUpdatedEvent;
import org.passport.models.cache.infinispan.authorization.events.ScopeRemovedEvent;
import org.passport.models.cache.infinispan.authorization.events.ScopeUpdatedEvent;
import org.passport.models.cache.infinispan.authorization.stream.InResourcePredicate;
import org.passport.models.cache.infinispan.authorization.stream.InResourceServerPredicate;
import org.passport.models.cache.infinispan.authorization.stream.InScopePredicate;
import org.passport.models.cache.infinispan.events.AuthenticationSessionAuthNoteUpdateEvent;
import org.passport.models.cache.infinispan.events.CacheKeyInvalidatedEvent;
import org.passport.models.cache.infinispan.events.ClientAddedEvent;
import org.passport.models.cache.infinispan.events.ClientRemovedEvent;
import org.passport.models.cache.infinispan.events.ClientScopeAddedEvent;
import org.passport.models.cache.infinispan.events.ClientScopeRemovedEvent;
import org.passport.models.cache.infinispan.events.ClientUpdatedEvent;
import org.passport.models.cache.infinispan.events.GroupAddedEvent;
import org.passport.models.cache.infinispan.events.GroupMovedEvent;
import org.passport.models.cache.infinispan.events.GroupRemovedEvent;
import org.passport.models.cache.infinispan.events.GroupUpdatedEvent;
import org.passport.models.cache.infinispan.events.RealmRemovedEvent;
import org.passport.models.cache.infinispan.events.RealmUpdatedEvent;
import org.passport.models.cache.infinispan.events.RoleAddedEvent;
import org.passport.models.cache.infinispan.events.RoleRemovedEvent;
import org.passport.models.cache.infinispan.events.RoleUpdatedEvent;
import org.passport.models.cache.infinispan.events.UserCacheRealmInvalidationEvent;
import org.passport.models.cache.infinispan.events.UserConsentsUpdatedEvent;
import org.passport.models.cache.infinispan.events.UserFederationLinkRemovedEvent;
import org.passport.models.cache.infinispan.events.UserFederationLinkUpdatedEvent;
import org.passport.models.cache.infinispan.events.UserFullInvalidationEvent;
import org.passport.models.cache.infinispan.events.UserUpdatedEvent;
import org.passport.models.cache.infinispan.stream.GroupListPredicate;
import org.passport.models.cache.infinispan.stream.HasRolePredicate;
import org.passport.models.cache.infinispan.stream.InClientPredicate;
import org.passport.models.cache.infinispan.stream.InGroupPredicate;
import org.passport.models.cache.infinispan.stream.InIdentityProviderPredicate;
import org.passport.models.cache.infinispan.stream.InRealmPredicate;
import org.passport.models.sessions.infinispan.changes.ReplaceFunction;
import org.passport.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.passport.models.sessions.infinispan.changes.sessions.SessionData;
import org.passport.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.passport.models.sessions.infinispan.entities.AuthenticatedClientSessionStore;
import org.passport.models.sessions.infinispan.entities.AuthenticationSessionEntity;
import org.passport.models.sessions.infinispan.entities.ClientSessionKey;
import org.passport.models.sessions.infinispan.entities.EmbeddedClientSessionKey;
import org.passport.models.sessions.infinispan.entities.LoginFailureEntity;
import org.passport.models.sessions.infinispan.entities.LoginFailureKey;
import org.passport.models.sessions.infinispan.entities.RemoteAuthenticatedClientSessionEntity;
import org.passport.models.sessions.infinispan.entities.RemoteUserSessionEntity;
import org.passport.models.sessions.infinispan.entities.RootAuthenticationSessionEntity;
import org.passport.models.sessions.infinispan.entities.SingleUseObjectValueEntity;
import org.passport.models.sessions.infinispan.entities.UserSessionEntity;
import org.passport.models.sessions.infinispan.events.RealmRemovedSessionEvent;
import org.passport.models.sessions.infinispan.events.RemoveAllUserLoginFailuresEvent;
import org.passport.models.sessions.infinispan.events.RemoveUserSessionsEvent;
import org.passport.models.sessions.infinispan.stream.AuthClientSessionSetMapper;
import org.passport.models.sessions.infinispan.stream.ClientSessionFilterByUser;
import org.passport.models.sessions.infinispan.stream.CollectionToStreamMapper;
import org.passport.models.sessions.infinispan.stream.GroupAndCountCollectorSupplier;
import org.passport.models.sessions.infinispan.stream.LoginFailuresLifespanUpdate;
import org.passport.models.sessions.infinispan.stream.MapEntryToKeyMapper;
import org.passport.models.sessions.infinispan.stream.RemoveKeyConsumer;
import org.passport.models.sessions.infinispan.stream.SessionPredicate;
import org.passport.models.sessions.infinispan.stream.SessionUnwrapMapper;
import org.passport.models.sessions.infinispan.stream.SessionWrapperPredicate;
import org.passport.models.sessions.infinispan.stream.UserSessionPredicate;
import org.passport.models.sessions.infinispan.stream.ValueIdentityBiFunction;
import org.passport.sessions.CommonClientSessionModel;
import org.passport.storage.UserStorageProviderClusterEvent;
import org.passport.storage.UserStorageProviderModel;

import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;
import org.infinispan.protostream.annotations.ProtoSyntax;
import org.infinispan.protostream.config.Configuration;
import org.infinispan.protostream.descriptors.Descriptor;
import org.infinispan.protostream.descriptors.FileDescriptor;
import org.infinispan.protostream.impl.parser.ProtostreamProtoParser;
import org.infinispan.protostream.types.java.CommonTypes;

@ProtoSchema(
        syntax = ProtoSyntax.PROTO3,
        schemaPackageName = Marshalling.PROTO_SCHEMA_PACKAGE,
        schemaFilePath = "proto/generated",
        allowNullFields = true,
        orderedMarshallers = true,

        // common-types for UUID
        dependsOn = CommonTypes.class,

        includeClasses = {
                // Model
                UserSessionModel.State.class,
                CommonClientSessionModel.ExecutionStatus.class,
                ComponentModel.MultiMapEntry.class,
                UserStorageProviderModel.class,
                UserStorageProviderClusterEvent.class,

                // clustering.infinispan package
                LockEntry.class,
                LockEntryPredicate.class,
                WrapperClusterEvent.class,
                WrapperClusterEvent.SiteFilter.class,

                // keys.infinispan package
                PublicKeyStorageInvalidationEvent.class,

                // models.cache.infinispan
                ClearCacheEvent.class,

                //models.cache.infinispan.authorization.events package
                PermissionTicketRemovedEvent.class,
                PermissionTicketUpdatedEvent.class,
                PolicyUpdatedEvent.class,
                PolicyRemovedEvent.class,
                ResourceUpdatedEvent.class,
                ResourceRemovedEvent.class,
                ResourceServerUpdatedEvent.class,
                ResourceServerRemovedEvent.class,
                ScopeUpdatedEvent.class,
                ScopeRemovedEvent.class,

                // models.sessions.infinispan.changes package
                SessionEntityWrapper.class,

                // models.sessions.infinispan.changes.sessions package
                SessionData.class,

                // models.cache.infinispan.authorization.stream package
                InResourcePredicate.class,
                InResourceServerPredicate.class,
                InScopePredicate.class,

                // models.sessions.infinispan.events package
                RealmRemovedSessionEvent.class,
                RemoveAllUserLoginFailuresEvent.class,
                RemoveUserSessionsEvent.class,

                // models.sessions.infinispan.stream package
                SessionPredicate.class,
                SessionWrapperPredicate.class,
                UserSessionPredicate.class,

                // models.cache.infinispan.stream package
                GroupListPredicate.class,
                HasRolePredicate.class,
                InClientPredicate.class,
                InGroupPredicate.class,
                InIdentityProviderPredicate.class,
                InRealmPredicate.class,

                // models.cache.infinispan.events package
                AuthenticationSessionAuthNoteUpdateEvent.class,
                CacheKeyInvalidatedEvent.class,
                ClientAddedEvent.class,
                ClientUpdatedEvent.class,
                ClientRemovedEvent.class,
                ClientScopeAddedEvent.class,
                ClientScopeRemovedEvent.class,
                GroupAddedEvent.class,
                GroupMovedEvent.class,
                GroupRemovedEvent.class,
                GroupUpdatedEvent.class,
                RealmUpdatedEvent.class,
                RealmRemovedEvent.class,
                RoleAddedEvent.class,
                RoleUpdatedEvent.class,
                RoleRemovedEvent.class,
                UserCacheRealmInvalidationEvent.class,
                UserConsentsUpdatedEvent.class,
                UserFederationLinkRemovedEvent.class,
                UserFederationLinkUpdatedEvent.class,
                UserFullInvalidationEvent.class,
                UserUpdatedEvent.class,

                // sessions.infinispan.entities package
                AuthenticatedClientSessionStore.class,
                AuthenticatedClientSessionEntity.class,
                AuthenticationSessionEntity.class,
                ClientSessionKey.class,
                EmbeddedClientSessionKey.class,
                LoginFailureEntity.class,
                LoginFailureKey.class,
                RemoteAuthenticatedClientSessionEntity.class,
                RemoteUserSessionEntity.class,
                RootAuthenticationSessionEntity.class,
                SingleUseObjectValueEntity.class,
                UserSessionEntity.class,
                ReplaceFunction.class,

                // sessions.infinispan.stream
                AuthClientSessionSetMapper.class,
                CollectionToStreamMapper.class,
                GroupAndCountCollectorSupplier.class,
                MapEntryToKeyMapper.class,
                SessionUnwrapMapper.class,
                ClientSessionFilterByUser.class,
                RemoveKeyConsumer.class,
                ValueIdentityBiFunction.class,
                LoginFailuresLifespanUpdate.class,

                // infinispan.module.certificates
                ReloadCertificateFunction.class,
        }
)
public interface PassportModelSchema extends GeneratedSchema {

    PassportModelSchema INSTANCE = new PassportModelSchemaImpl();

    /**
     * Parses a Google Protocol Buffers schema file.
     */
    static FileDescriptor parseProtoSchema(String fileContent) {
        var files = FileDescriptorSource.fromString("a", fileContent);
        var builder = Configuration.builder();
        PassportIndexSchemaUtil.configureAnnotationProcessor(builder);
        var parser = new ProtostreamProtoParser(builder.build());
        return parser.parse(files).get("a");
    }

    /**
     * Finds an entity in a Google Protocol Buffers schema file
     */
    static Optional<Descriptor> findEntity(FileDescriptor fileDescriptor, String entity) {
        return fileDescriptor.getMessageTypes().stream()
                .filter(descriptor -> Objects.equals(entity, descriptor.getFullName()))
                .findFirst();
    }
}
