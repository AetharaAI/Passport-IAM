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

package org.passport.models.sessions.infinispan.remote.transaction;

import org.passport.models.sessions.infinispan.changes.remote.remover.ConditionalRemover;
import org.passport.models.sessions.infinispan.changes.remote.remover.EmptyConditionalRemover;
import org.passport.models.sessions.infinispan.entities.SingleUseObjectValueEntity;

import org.infinispan.client.hotrod.RemoteCache;

/**
 * Syntactic sugar for
 * {@code RemoteInfinispanPassportTransaction<String, SingleUseObjectValueEntity, ConditionalRemover<String,
 * SingleUseObjectValueEntity>>}
 */
public class SingleUseObjectTransaction extends RemoteInfinispanPassportTransaction<String, SingleUseObjectValueEntity, ConditionalRemover<String, SingleUseObjectValueEntity>> {

    public SingleUseObjectTransaction(RemoteCache<String, SingleUseObjectValueEntity> cache) {
        super(cache, EmptyConditionalRemover.instance());
    }
}
