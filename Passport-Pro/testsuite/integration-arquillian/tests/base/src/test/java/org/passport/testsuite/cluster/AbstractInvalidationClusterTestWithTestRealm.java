package org.passport.testsuite.cluster;

import org.passport.representations.idm.RealmRepresentation;
import org.passport.testsuite.arquillian.ContainerInfo;

import org.junit.Before;

/**
 *
 * @author tkyjovsk
 */
public abstract class AbstractInvalidationClusterTestWithTestRealm<T, TR> extends AbstractInvalidationClusterTest<T, TR> {

    protected String testRealmName = null;
    
    @Before
    public void createTestRealm() {
        createTestRealm(frontendNode());
    }
    
    protected void createTestRealm(ContainerInfo node) {
        RealmRepresentation r = createTestRealmRepresentation();
        getAdminClientFor(node).realms().create(r);
        testRealmName = r.getRealm();
    }
    
}
