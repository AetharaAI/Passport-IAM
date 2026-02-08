package org.passport.testsuite.federation.storage;

import org.passport.representations.idm.ComponentRepresentation;
import org.passport.storage.UserStorageProvider.EditMode;

/**
 *
 * @author hmlnarik
 */
public final class UserStorageDirtyDeletionUnsyncedImportTest extends AbstractUserStorageDirtyDeletionTest {

    @Override
    protected ComponentRepresentation getFederationProvider() {
        return getFederationProvider(EditMode.UNSYNCED, true);
    }

}
