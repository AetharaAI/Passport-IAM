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

package org.passport.testsuite.domainextension.spi.impl;

import org.passport.Config.Scope;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.testsuite.domainextension.spi.ExampleService;
import org.passport.testsuite.domainextension.spi.ExampleServiceProviderFactory;

public class ExampleServiceProviderFactoryImpl implements ExampleServiceProviderFactory {

    @Override
    public ExampleService create(PassportSession session) {
        return new ExampleServiceImpl(session);
    }

    @Override
    public void init(Scope config) {

    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "exampleServiceImpl";
    }

}
