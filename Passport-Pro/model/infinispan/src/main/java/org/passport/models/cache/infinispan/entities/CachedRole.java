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

package org.passport.models.cache.infinispan.entities;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.passport.common.util.MultivaluedHashMap;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.models.RoleModel;
import org.passport.models.cache.infinispan.DefaultLazyLoader;
import org.passport.models.cache.infinispan.LazyLoader;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class CachedRole extends AbstractRevisioned implements InRealm {

    final protected String name;
    final protected String realm;
    final protected String description;
    protected boolean composite;
    final protected LazyLoader<RoleModel, Set<String>> composites;
    /**
     * Use this so the cache invalidation can retrieve any previously cached role mappings to determine if this
     * items should be evicted.
     */
    private Set<String> cachedComposites = new HashSet<>();
    private final LazyLoader<RoleModel, MultivaluedHashMap<String, String>> attributes;

    public CachedRole(Long revision, RoleModel model, RealmModel realm) {
        super(revision, model.getId());
        composite = model.isComposite();
        description = model.getDescription();
        name = model.getName();
        this.realm = realm.getId();
        if (composite) {
            composites = new DefaultLazyLoader<>(roleModel -> roleModel.getCompositesStream().map(RoleModel::getId).collect(Collectors.toSet()), HashSet::new);
        } else {
            composites = new DefaultLazyLoader<>(roleModel -> new HashSet<>(), HashSet::new);
        }
        attributes = new DefaultLazyLoader<>(roleModel -> new MultivaluedHashMap<>(roleModel.getAttributes()), MultivaluedHashMap::new);
    }

    public String getName() {
        return name;
    }

    public String getRealm() {
        return realm;
    }

    public String getDescription() {
        return description;
    }

    public boolean isComposite() {
        return composite;
    }

    public Set<String> getComposites(PassportSession session, Supplier<RoleModel> roleModel) {
        cachedComposites = composites.get(session, roleModel);
        composite = !cachedComposites.isEmpty();
        return cachedComposites;
    }

    /**
     * Use this so the cache invalidation can retrieve any previously cached role mappings to determine if this
     * items should be evicted. Will return an empty list if it hasn't been cached yet (and then no invalidation is necessary)
     */
    public Set<String> getCachedComposites() {
        return cachedComposites;
    }

    public MultivaluedHashMap<String, String> getAttributes(PassportSession session, Supplier<RoleModel> roleModel) {
        return attributes.get(session, roleModel);
    }
}
