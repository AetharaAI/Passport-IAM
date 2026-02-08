package org.passport.testsuite.arquillian.undertow;

import org.passport.testsuite.arquillian.undertow.lb.SimpleUndertowLoadBalancerContainer;

import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.core.spi.LoadableExtension;

/**
 *
 * @author tkyjovsk
 */
public class PassportOnUndertowArquillianExtension implements LoadableExtension {

    @Override
    public void register(ExtensionBuilder builder) {
        builder.service(DeployableContainer.class, PassportOnUndertow.class);
        builder.service(DeployableContainer.class, SimpleUndertowLoadBalancerContainer.class);
    }

}
