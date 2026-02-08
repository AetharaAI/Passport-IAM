package org.passport.theme.freemarker;

import org.passport.provider.Provider;
import org.passport.theme.FreeMarkerException;
import org.passport.theme.Theme;

public interface FreeMarkerProvider extends Provider {

    public String processTemplate(Object data, String templateName, Theme theme) throws FreeMarkerException;

}
