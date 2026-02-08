package org.passport.testsuite.theme;

import org.passport.Config;
import org.passport.platform.Platform;
import org.passport.provider.EnvironmentDependentProviderFactory;
import org.passport.theme.ClasspathThemeResourceProviderFactory;

public class TestThemeResourceProvider extends ClasspathThemeResourceProviderFactory implements EnvironmentDependentProviderFactory {

    public TestThemeResourceProvider() {
        super("test-resources", TestThemeResourceProvider.class.getClassLoader());
    }

    /**
     * Quarkus detects theme resources automatically, so this provider should only be enabled on Undertow
     *
     * @return true if platform is Undertow
     */
    @Override
    public boolean isSupported(Config.Scope config) {
        return Platform.getPlatform().name().equals("Undertow");
    }
}
