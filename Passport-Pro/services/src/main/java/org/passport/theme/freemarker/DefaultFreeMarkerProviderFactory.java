package org.passport.theme.freemarker;

import java.util.concurrent.ConcurrentHashMap;

import org.passport.Config;
import org.passport.models.PassportSession;
import org.passport.models.PassportSessionFactory;
import org.passport.theme.PassportSanitizerMethod;

import freemarker.template.Template;

public class DefaultFreeMarkerProviderFactory implements FreeMarkerProviderFactory {

    private volatile DefaultFreeMarkerProvider provider;
    private ConcurrentHashMap<String, Template> cache;
    private PassportSanitizerMethod kcSanitizeMethod;

    @Override
    public DefaultFreeMarkerProvider create(PassportSession session) {
        if (provider == null) {
            synchronized (this) {
                if (provider == null) {
                    if (Config.scope("theme").getBoolean("cacheTemplates", true)) {
                        cache = new ConcurrentHashMap<>();
                    }
                    kcSanitizeMethod = new PassportSanitizerMethod();
                    provider = new DefaultFreeMarkerProvider(cache, kcSanitizeMethod);
                }
            }
        }
        return provider;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(PassportSessionFactory factory) {
    }

    @Override
    public void close() {
    }

    @Override
    public String getId() {
        return "default";
    }

}
