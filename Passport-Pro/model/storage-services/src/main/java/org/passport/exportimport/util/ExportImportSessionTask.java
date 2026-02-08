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

package org.passport.exportimport.util;

import java.io.IOException;

import org.passport.connections.jpa.support.EntityManagers;
import org.passport.exportimport.ExportImportConfig;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.models.utils.PassportModelUtils;
import org.passport.utils.PassportSessionUtil;

/**
 * Just to wrap {@link IOException}
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public abstract class ExportImportSessionTask {

    public enum Mode {
        BATCHED, // turn on batched optimizations - good for read-only and bulk inserts, and flush / clear when done
        NORMAL
    }

    public void runTask(PassportSessionFactory factory) {
        runTask(factory, Mode.NORMAL);
    }

    public void runTask(PassportSessionFactory factory, Mode mode) {
        boolean useExistingSession = ExportImportConfig.isSingleTransaction();
        PassportSession existing = PassportSessionUtil.getPassportSession();
        if (useExistingSession && existing != null && existing.getTransactionManager().isActive()) {
            run(mode, existing);
        } else {
            PassportModelUtils.runJobInTransaction(factory, session -> this.run(mode, session));
        }
    }

    private void run(Mode mode, PassportSession session) {
        Runnable task = () -> {
            try {
                runExportImportTask(session);
            } catch (IOException ioe) {
                throw new RuntimeException("Error during export/import: " + ioe.getMessage(), ioe);
            }
        };
        if (mode == Mode.BATCHED) {
            EntityManagers.runInBatch(session, task, true);
        } else {
            task.run();
        }
    }

    protected abstract void runExportImportTask(PassportSession session) throws IOException;
}
