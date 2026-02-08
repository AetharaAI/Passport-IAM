package org.passport.scripting;

import org.passport.provider.Provider;
import org.passport.provider.ProviderFactory;
import org.passport.provider.Spi;

/**
 * @author <a href="mailto:thomas.darimont@gmail.com">Thomas Darimont</a>
 */
public class ScriptingSpi implements Spi {

    @Override
    public boolean isInternal() {
        return true;
    }

    @Override
    public String getName() {
        return "scripting";
    }

    @Override
    public Class<? extends Provider> getProviderClass() {
        return ScriptingProvider.class;
    }

    @Override
    public Class<? extends ProviderFactory> getProviderFactoryClass() {
        return ScriptingProviderFactory.class;
    }
}
