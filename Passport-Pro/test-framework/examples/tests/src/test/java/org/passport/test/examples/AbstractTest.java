package org.passport.test.examples;

import org.passport.testframework.annotations.InjectRealm;
import org.passport.testframework.realm.ManagedRealm;

public abstract class AbstractTest {

    @InjectRealm
    ManagedRealm realm;

}
