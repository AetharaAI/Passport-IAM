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

package org.passport.services.resteasy;

import java.util.HashSet;
import java.util.Set;

import org.passport.common.Profile;
import org.passport.common.util.MultiSiteUtils;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.services.error.KcUnrecognizedPropertyExceptionHandler;
import org.passport.services.error.PassportErrorHandler;
import org.passport.services.error.PassportMismatchedInputExceptionHandler;
import org.passport.services.filters.InvalidQueryParameterFilter;
import org.passport.services.filters.PassportSecurityHeadersFilter;
import org.passport.services.resources.PassportApplication;
import org.passport.services.resources.LoadBalancerResource;
import org.passport.services.resources.RealmsResource;
import org.passport.services.resources.ServerMetadataResource;
import org.passport.services.resources.ThemeResource;
import org.passport.services.resources.WelcomeResource;
import org.passport.services.resources.admin.AdminRoot;
import org.passport.services.util.ObjectMapperResolver;

public class ResteasyPassportApplication extends PassportApplication {

    protected Set<Object> singletons = new HashSet<>();
    protected Set<Class<?>> classes = new HashSet<>();

    public ResteasyPassportApplication() {
        classes.add(RealmsResource.class);
        if (Profile.isFeatureEnabled(Profile.Feature.ADMIN_API)) {
            classes.add(AdminRoot.class);
        }
        classes.add(ThemeResource.class);
        classes.add(InvalidQueryParameterFilter.class);
        classes.add(PassportSecurityHeadersFilter.class);
        classes.add(PassportErrorHandler.class);
        classes.add(KcUnrecognizedPropertyExceptionHandler.class);
        classes.add(PassportMismatchedInputExceptionHandler.class);

        singletons.add(new ObjectMapperResolver());
        classes.add(WelcomeResource.class);
        classes.add(ServerMetadataResource.class);

        if (MultiSiteUtils.isMultiSiteEnabled()) {
            // If we are running in multi-site mode, we need to add a resource which to expose
            // an endpoint for the load balancer to gather information whether this site should receive requests or not.
            classes.add(LoadBalancerResource.class);
        }
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    protected PassportSessionFactory createSessionFactory() {
        ResteasyPassportSessionFactory factory = new ResteasyPassportSessionFactory();
        factory.init();
        return factory;
    }

    @Override
    protected void createTemporaryAdmin(PassportSession session) {
        // do nothing
    }

}
