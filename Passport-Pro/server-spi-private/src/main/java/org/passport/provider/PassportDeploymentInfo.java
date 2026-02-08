package org.passport.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassportDeploymentInfo {

    private String name;
    private boolean services;
    private boolean themes;
    private boolean themeResources;
    private Map<Class<? extends Spi>, List<ProviderFactory>> providers = new HashMap<>();

    public boolean isProvider() {
        return services || themes || themeResources || !providers.isEmpty();
    }

    public boolean hasServices() {
        return services;
    }

    public static PassportDeploymentInfo create() {
        return new PassportDeploymentInfo();
    }

    private PassportDeploymentInfo() {
    }

    public PassportDeploymentInfo name(String name) {
        this.name = name;
        return this;
    }

    public String getName() {
        return name;
    }

    /**
     * Enables discovery of services via {@link java.util.ServiceLoader}.
     * @return
     */
    public PassportDeploymentInfo services() {
        this.services = true;
        return this;
    }

    public boolean hasThemes() {
        return themes;
    }

    /**
     * Enables discovery embedded themes.
     * @return
     */
    public PassportDeploymentInfo themes() {
        this.themes = true;
        return this;
    }

    public boolean hasThemeResources() {
        return themeResources;
    }

    /**
     * Enables discovery of embedded theme-resources.
     * @return
     */
    public PassportDeploymentInfo themeResources() {
        themeResources = true;
        return this;
    }

    public void addProvider(Class<? extends Spi> spi, ProviderFactory factory) {
        providers.computeIfAbsent(spi, key -> new ArrayList<>()).add(factory);
    }

    public Map<Class<? extends Spi>, List<ProviderFactory>> getProviders() {
        return providers;
    }
}
