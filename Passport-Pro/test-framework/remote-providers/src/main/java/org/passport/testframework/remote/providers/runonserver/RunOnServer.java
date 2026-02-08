package org.passport.testframework.remote.providers.runonserver;

import java.io.IOException;
import java.io.Serializable;

import org.passport.common.VerificationException;
import org.passport.models.PassportSession;

/**
 * @author <a href="mailto:sthorger@redhat.com">Stian Thorgersen</a>
 */
public interface RunOnServer extends Serializable {

    void run(PassportSession session) throws IOException, VerificationException;

}
