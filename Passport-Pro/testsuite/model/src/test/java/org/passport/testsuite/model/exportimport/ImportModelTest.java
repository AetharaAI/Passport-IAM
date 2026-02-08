/*
 * Copyright 2023 Red Hat, Inc. and/or its affiliates
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

package org.passport.testsuite.model.exportimport;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.passport.exportimport.ExportImportConfig;
import org.passport.exportimport.ExportImportManager;
import org.passport.exportimport.ExportProvider;
import org.passport.exportimport.ImportProvider;
import org.passport.exportimport.dir.DirExportProviderFactory;
import org.passport.exportimport.dir.DirImportProviderFactory;
import org.passport.exportimport.singlefile.SingleFileImportProviderFactory;
import org.passport.models.PassportSession;
import org.passport.models.RealmModel;
import org.passport.services.managers.ApplianceBootstrap;
import org.passport.testsuite.model.PassportModelTest;
import org.passport.testsuite.model.RequireProvider;

import org.junit.Assert;
import org.junit.Test;

@RequireProvider(value = ImportProvider.class)
public class ImportModelTest extends PassportModelTest {

    public static final String SPI_NAME = "import";

    @Override
    public void createEnvironment(PassportSession s) {
        // Master realm is needed for importing a realm
        if (s.realms().getRealmByName("master") == null) {
            new ApplianceBootstrap(s).createMasterRealm();
        }
        // clean-up test realm which might be left-over from a previous run
        RealmModel test = s.realms().getRealmByName("test");
        if (test != null) {
            s.realms().removeRealm(test.getId());
        }
    }

    @Override
    public void cleanEnvironment(PassportSession s) {
        RealmModel master = s.realms().getRealmByName("master");
        if (master != null) {
            s.realms().removeRealm(master.getId());
        }
        RealmModel test = s.realms().getRealmByName("test");
        if (test != null) {
            s.realms().removeRealm(test.getId());
        }
    }

    @Test
    @RequireProvider(value = ExportProvider.class, only = SingleFileImportProviderFactory.PROVIDER_ID)
    public void testImportSingleFile() {
        try {
            Path singleFileExport = Paths.get("src/test/resources/exportimport/singleFile/testrealm.json");

            CONFIG.spi(SPI_NAME)
                    .config("importer", new SingleFileImportProviderFactory().getId());
            CONFIG.spi(SPI_NAME)
                    .provider(SingleFileImportProviderFactory.PROVIDER_ID)
                    .config(SingleFileImportProviderFactory.FILE, singleFileExport.toAbsolutePath().toString());

            inComittedTransaction(session -> {
                ExportImportConfig.setAction(ExportImportConfig.ACTION_IMPORT);
                ExportImportManager exportImportManager = new ExportImportManager(session);
                exportImportManager.runImport();
            });

            inComittedTransaction(session -> {
                Assert.assertNotNull(session.realms().getRealmByName("test"));
            });

        } finally {
            CONFIG.spi(SPI_NAME)
                    .config("importer", null);
            CONFIG.spi(SPI_NAME)
                    .provider(SingleFileImportProviderFactory.PROVIDER_ID)
                    .config(SingleFileImportProviderFactory.FILE, null);
        }
    }

    @Test
    @RequireProvider(value = ExportProvider.class, only = DirImportProviderFactory.PROVIDER_ID)
    public void testImportDirectory() {
        try {
            Path importFolder = Paths.get("src/test/resources/exportimport/dir");
            CONFIG.spi(SPI_NAME)
                    .config("importer", new DirImportProviderFactory().getId());
            CONFIG.spi(SPI_NAME)
                    .provider(DirImportProviderFactory.PROVIDER_ID)
                    .config(DirImportProviderFactory.DIR, importFolder.toAbsolutePath().toString());

            inComittedTransaction(session -> {
                ExportImportConfig.setAction(ExportImportConfig.ACTION_IMPORT);
                ExportImportManager exportImportManager = new ExportImportManager(session);
                exportImportManager.runImport();
            });

            inComittedTransaction(session -> {
                Assert.assertNotNull(session.realms().getRealmByName("test"));
            });

        } finally {
            CONFIG.spi(SPI_NAME)
                    .config("importer", null);
            CONFIG.spi(SPI_NAME)
                    .provider(DirImportProviderFactory.PROVIDER_ID)
                    .config(DirExportProviderFactory.DIR, null);
        }
    }

}
