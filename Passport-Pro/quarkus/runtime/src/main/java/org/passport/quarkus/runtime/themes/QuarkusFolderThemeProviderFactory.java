package org.passport.quarkus.runtime.themes;

import java.io.File;
import java.util.Optional;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.quarkus.runtime.Environment;
import org.passport.theme.FolderThemeProvider;
import org.passport.theme.ThemeProvider;
import org.passport.theme.ThemeProviderFactory;

public class QuarkusFolderThemeProviderFactory implements ThemeProviderFactory {

    private static final String CONFIG_DIR_KEY = "dir";
    private FolderThemeProvider themeProvider;

    @Override
    public ThemeProvider create(PassportSession sessions) {
        return themeProvider;
    }

    @Override
    public void init(Config.Scope config) {
        String configDir = config.get(CONFIG_DIR_KEY);
        File rootDir = getThemeRootDirWithFallback(configDir);
        themeProvider = new FolderThemeProvider(rootDir);
    }

    @Override
    public void postInit(PassportSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return "folder";
    }

    /**
     * Determines if the theme root directory we get
     * from {@link Config} exists.
     * If not, uses the default theme directory as a fallback.
     *
     * @param rootDirFromConfig string value from {@link Config}
     * @return Directory to use as theme root directory in {@link File} format, either from config or from default. Null if none is available.
     * @throws RuntimeException when filesystem path is not accessible
     */
    private File getThemeRootDirWithFallback(String rootDirFromConfig) {
        return Optional.ofNullable(rootDirFromConfig).or(Environment::getDefaultThemeRootDir).map(File::new)
                .filter(File::exists).orElse(null);
    }
}
